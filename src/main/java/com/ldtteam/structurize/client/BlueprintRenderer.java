package com.ldtteam.structurize.client;

import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtils;
import com.ldtteam.structurize.config.BlueprintRenderSettings;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.FluidRenderer;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import static com.ldtteam.structurize.api.util.constant.Constants.RENDER_PLACEHOLDERS;

/**
 * The renderer for blueprint.
 * Holds all information required to render a blueprint.
 */
public class BlueprintRenderer implements AutoCloseable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BlueprintRenderer.class);
    private static final Supplier<Map<RenderType, VertexBuffer>> blockVertexBuffersFactory = () -> RenderType.chunkBufferLayers()
        .stream()
        .collect(Collectors.toMap((type) -> type, (type) -> new VertexBuffer()));
    // TODO: remove when forge events
    private static final RenderBuffers renderBuffers = new RenderBuffers();

    private final BlueprintBlockAccess blockAccess;
    private List<Entity> entities;
    private List<BlockEntity> tileEntities;
    private Map<RenderType, VertexBuffer> vertexBuffers;
    private long lastGameTime;

    /**
     * Static factory utility method to handle the extraction of the values from the blueprint.
     *
     * @param blueprint The blueprint to create an instance for.
     * @return The renderer.
     */
    public static BlueprintRenderer buildRendererForBlueprint(final Blueprint blueprint)
    {
        final BlueprintBlockAccess blockAccess = new BlueprintBlockAccess(blueprint);
        return new BlueprintRenderer(blockAccess);
    }

    private BlueprintRenderer(final BlueprintBlockAccess blockAccess)
    {
        this.blockAccess = blockAccess;
    }

    /**
     * Updates blueprint reference if it has same hash.
     *
     * @param previewData blueprint and context from active structure
     */
    public void updateBlueprint(final BlueprintPreviewData previewData)
    {
        if (blockAccess.getBlueprint() != previewData.getBlueprint() && blockAccess.getBlueprint().hashCode() == previewData.getBlueprint().hashCode())
        {
            blockAccess.setBlueprint(previewData.getBlueprint());
        }
    }

    private void init(final BlockPos anchorPos)
    {
        final Blueprint blueprint = blockAccess.getBlueprint();
        final List<BlockInfo> blocks = blueprint.getBlockInfoAsList();
        final Map<BlockPos, ModelData> teModelData = new HashMap<>();

        final Minecraft mc = Minecraft.getInstance();
        final BlockRenderDispatcher blockRendererDispatcher = mc.getBlockRenderer();
        final RandomSource random = RandomSource.create();
        final Level serverLevel = mc.hasSingleplayerServer() ? mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).level : null;
        final BlockState defaultFluidState = BlockUtils.getFluidForDimension(serverLevel == null ? mc.level : serverLevel);

        clearVertexBuffers();
        entities = BlueprintUtils.instantiateEntities(blueprint, blockAccess);
        tileEntities = new ArrayList<>(BlueprintUtils.instantiateTileEntities(blueprint, blockAccess, teModelData));

        final PoseStack matrixStack = new PoseStack();
        final ChunkBufferBuilderPack newBuffers = new OurChunkBufferBuilderPack();
        final RenderType[] blockRenderTypes = RenderType.chunkBufferLayers().toArray(RenderType[]::new);

        for (final BlockInfo blockInfo : blocks)
        {
            try
            {
                final BlockPos blockPos = blockInfo.getPos();
                BlockState state = blockInfo.getState();
                if (!BlueprintRenderSettings.instance.renderSettings.get(RENDER_PLACEHOLDERS))
                {
                    if (state.getBlock() == ModBlocks.blockSubstitution.get())
                    {
                        state = Blocks.AIR.defaultBlockState();
                    }
                    if (state.getBlock() == ModBlocks.blockTagSubstitution.get())
                    {
                        final Optional<BlockEntity> tagTE = tileEntities.stream()
                                .filter(te -> te.getBlockPos().equals(blockPos) && te instanceof BlockEntityTagSubstitution)
                                .findFirst();
                        if (tagTE.isPresent())
                        {
                            final BlockEntityTagSubstitution.ReplacementBlock replacement = ((BlockEntityTagSubstitution) tagTE.get()).getReplacement();
                            state = replacement.getBlockState();
                            tileEntities.remove(tagTE.get());
                            Optional.ofNullable(replacement.createBlockEntity(blockPos)).ifPresent(e ->
                            {
                                e.setLevel(blockAccess);
                                teModelData.put(blockPos, e.getModelData());
                                tileEntities.add(e);
                            });
                        }
                        else
                        {
                            state = Blocks.AIR.defaultBlockState();
                        }
                    }
                    if (state.getBlock() == ModBlocks.blockFluidSubstitution.get())
                    {
                        state = defaultFluidState;
                    }
                    if (SharedConstants.IS_RUNNING_IN_IDE && serverLevel != null && state.getBlock() == ModBlocks.blockSolidSubstitution.get())
                    {
                        state = BlockUtils.getWorldgenBlock(serverLevel, anchorPos.offset(blockPos), blueprint.getRawBlockStateFunction().compose(b -> b.subtract(anchorPos)));
                        if (state == null)
                        {
                            state = blockInfo.getState();
                        }
                    }
                }

                final FluidState fluidState = state.getFluidState();

                matrixStack.pushPose();
                matrixStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                final BakedModel blockModel = blockRendererDispatcher.getBlockModel(state);
                final @NotNull ModelData modelData = blockModel.getModelData(blockAccess, blockPos, state, teModelData.getOrDefault(blockPos, ModelData.EMPTY))
;
                for (final RenderType renderType : blockModel.getRenderTypes(state, random, modelData))
                {
                    renderType.setupRenderState();
                    final BufferBuilder buffer = newBuffers.builder(renderType);
                    renderType.setupRenderState();
                    if (state.getRenderShape() != RenderShape.INVISIBLE)
                    {
                        // buffer builder is now vertex consumer
                        blockRendererDispatcher.renderBatched(state,
                            blockPos,
                            blockAccess,
                            matrixStack,
                            buffer,
                            true,
                            random,
                            modelData,
                            renderType);
                    }

                    if (!fluidState.isEmpty())
                    {
                        FluidRenderer.render(blockAccess, blockPos, buffer, fluidState);
                    }
                    renderType.clearRenderState();
                }

                matrixStack.popPose();
            }
            catch (final ReportedException e)
            {
                LOGGER.error("Error while trying to render structure part: " + e.getMessage(), e.getCause());
            }
        }

        vertexBuffers = blockVertexBuffersFactory.get();
        for (final RenderType renderType : blockRenderTypes)
        {
            final RenderedBuffer newBuffer = newBuffers.builder(renderType).endOrDiscardIfEmpty();
            if (newBuffer == null)
            {
                vertexBuffers.remove(renderType);
            }
            else
            {
                // TODO: OptifineCompat.getInstance().beforeBuilderUpload(null);
                final VertexBuffer vertexBuffer = vertexBuffers.get(renderType);
                vertexBuffer.bind();
                vertexBuffer.upload(newBuffer);
            }
        }
        newBuffers.clearAll();
        VertexBuffer.unbind();
    }

    /**
     * Draws structure into world.
     */
    public void draw(final BlueprintPreviewData previewData, final BlockPos pos, final PoseStack matrixStack, final float partialTicks)
    {
        final Minecraft mc = Minecraft.getInstance();
        final long gameTime = mc.level.getGameTime();

        mc.getProfiler().push("struct_render_init");
        
        // make sure instances are synced
        updateBlueprint(previewData);

        final BlockPos anchorPos = pos.subtract(blockAccess.getBlueprint().getPrimaryBlockOffset());
        blockAccess.setWorldPos(anchorPos);
        if (vertexBuffers == null || previewData.shouldRefresh())
        {
            return;
        }

        // init
        if (vertexBuffers == null)
        {
            init(anchorPos);
        }

        mc.getProfiler().popPush("struct_render_prepare");
        final Vec3 viewPosition = mc.gameRenderer.getMainCamera().getPosition();
        final Vec3 realRenderRootVecd = Vec3.atLowerCornerOf(anchorPos).subtract(viewPosition);
        final Vector3f realRenderRootVecf = new Vector3f(realRenderRootVecd);

        // missing clipping helper? frustum?
        // missing chunk system and render distance!
        // level animate tick! done?

        matrixStack.pushPose();
        // move back to camera, everything must go into offsets cuz fog
        matrixStack.translate(viewPosition.x(), viewPosition.y(), viewPosition.z());
        final Matrix4f mvMatrix = matrixStack.last().pose();

        // Render blocks
         
        FogRenderer.setupFog(mc.gameRenderer.getMainCamera(),
            FogRenderer.FogMode.FOG_TERRAIN,
            Math.max(mc.gameRenderer.getRenderDistance(), 32.0F),
            mc.level.effects().isFoggyAt(Mth.floor(viewPosition.x()), Mth.floor(viewPosition.y()))
                || mc.gui.getBossOverlay().shouldCreateWorldFog(),
            partialTicks);

        OptifineCompat.getInstance().setupFog();

        mc.getProfiler().popPush("struct_render_blocks");
        renderBlockLayer(RenderType.solid(), mvMatrix, realRenderRootVecf);
        // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        mc.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).setBlurMipmap(false, mc.options.mipmapLevels().get() > 0);
        renderBlockLayer(RenderType.cutoutMipped(), mvMatrix, realRenderRootVecf);
        mc.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).restoreLastBlurMipmap();
        renderBlockLayer(RenderType.cutout(), mvMatrix, realRenderRootVecf);

        OptifineCompat.getInstance().endTerrainBeginEntities();

        mc.getProfiler().popPush("struct_render_entities");
        final MultiBufferSource.BufferSource renderBufferSource = renderBuffers.bufferSource();

        // Entities

        // if clipping etc., see WorldRenderer for what's missing
        matrixStack.pushPose();
        matrixStack.translate(realRenderRootVecd.x(), realRenderRootVecd.y(), realRenderRootVecd.z());
        for (Entity entity : entities)
        {
            if (gameTime != lastGameTime)
            {
                if (entity instanceof EndCrystal crystal)
                {
                    // safeguarded INLINE crystal.tick()
                    crystal.time++;
                }
            }

            OptifineCompat.getInstance().preRenderEntity(entity);

            try
            {
                Minecraft.getInstance()
                  .getEntityRenderDispatcher()
                  .render(entity,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    entity.getYRot(),
                    partialTicks,
                    matrixStack,
                    renderBufferSource,
                    LightTexture.pack(15, 15));
            }
            catch (final ClassCastException ex)
            {
                // Oops
            }
        }
        matrixStack.popPose();

        mc.getProfiler().popPush("struct_render_entities_finish");
        renderBufferSource.endLastBatch();
        renderBufferSource.endBatch(RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS));
        renderBufferSource.endBatch(RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS));
        renderBufferSource.endBatch(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        renderBufferSource.endBatch(RenderType.entitySmoothCutout(InventoryMenu.BLOCK_ATLAS));

        OptifineCompat.getInstance().endEntitiesBeginBlockEntities();

        // Block entities

        mc.getProfiler().popPush("struct_render_blockentities");
        final Camera oldActiveRenderInfo = mc.getBlockEntityRenderDispatcher().camera;
        final Level oldWorld = mc.getBlockEntityRenderDispatcher().level;
        mc.getBlockEntityRenderDispatcher().camera = new Camera();
        mc.getBlockEntityRenderDispatcher().camera.setPosition(viewPosition.subtract(anchorPos.getX(), anchorPos.getY(), anchorPos.getZ()));
        mc.getBlockEntityRenderDispatcher().level = blockAccess;
        for(final BlockEntity tileEntity : tileEntities)
        {
            final BlockPos tePos = tileEntity.getBlockPos();
            final Vec3 realRenderTePos = realRenderRootVecd.add(tePos.getX(), tePos.getY(), tePos.getZ());

            if (gameTime != lastGameTime)
            {
                // hooks from EntityBlock#getTicker(Level, BlockState, BlockEntityType) for client side
                if (tileEntity instanceof SpawnerBlockEntity spawner)
                {
                    SpawnerBlockEntity.clientTick(mc.level, anchorPos.offset(tePos), blockAccess.getBlockState(tePos), spawner);
                }
                else if (tileEntity instanceof EnchantmentTableBlockEntity enchTable)
                {
                    EnchantmentTableBlockEntity
                        .bookAnimationTick(mc.level, anchorPos.offset(tePos), blockAccess.getBlockState(tePos), enchTable);
                }
                else if (tileEntity instanceof CampfireBlockEntity campfire)
                {
                    final BlockState bs = blockAccess.getBlockState(tePos);
                    if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT))
                    {
                        CampfireBlockEntity.particleTick(mc.level, anchorPos.offset(tePos), bs, campfire);
                    }
                }
                else if (tileEntity instanceof SkullBlockEntity skull)
                {
                    final BlockState bs = blockAccess.getBlockState(tePos);
                    if (bs.getBlock() instanceof SkullBlock &&  bs.is(Blocks.DRAGON_HEAD) || bs.is(Blocks.DRAGON_WALL_HEAD))
                    {
                        SkullBlockEntity.dragonHeadAnimation(mc.level, anchorPos.offset(tePos), bs, skull);
                    }
                }
                // TODO: investigate beams (beacon...)
            }

            matrixStack.pushPose();
            matrixStack.translate(realRenderTePos.x, realRenderTePos.y, realRenderTePos.z);

            OptifineCompat.getInstance().preRenderBlockEntity(tileEntity);

            mc.getBlockEntityRenderDispatcher().render(tileEntity, partialTicks, matrixStack, renderBufferSource);
            matrixStack.popPose();
        }
        mc.getBlockEntityRenderDispatcher().camera = oldActiveRenderInfo;
        mc.getBlockEntityRenderDispatcher().level = oldWorld;

        mc.getProfiler().popPush("struct_render_blockentities_finish");
        renderBufferSource.endBatch(RenderType.solid());
        renderBufferSource.endBatch(RenderType.endPortal());
        renderBufferSource.endBatch(RenderType.endGateway());
        renderBufferSource.endBatch(Sheets.solidBlockSheet());
        renderBufferSource.endBatch(Sheets.cutoutBlockSheet());
        renderBufferSource.endBatch(Sheets.bedSheet());
        renderBufferSource.endBatch(Sheets.shulkerBoxSheet());
        renderBufferSource.endBatch(Sheets.signSheet());
        renderBufferSource.endBatch(Sheets.chestSheet());
        if (OptifineCompat.getInstance().isOptifineEnabled())
        {
            renderBufferSource.endBatch(Sheets.bannerSheet());
        }
        renderBuffers.outlineBufferSource().endOutlineBatch(); // not used now
        OptifineCompat.getInstance().endBlockEntitiesBeginDebug(renderBuffers);

        renderBufferSource.endBatch(Sheets.translucentCullBlockSheet());
        renderBufferSource.endBatch(Sheets.bannerSheet());
        renderBufferSource.endBatch(Sheets.shieldSheet());
        renderBufferSource.endBatch(RenderType.armorGlint());
        renderBufferSource.endBatch(RenderType.armorEntityGlint());
        renderBufferSource.endBatch(RenderType.glint());
        renderBufferSource.endBatch(RenderType.glintDirect());
        renderBufferSource.endBatch(RenderType.glintTranslucent());
        renderBufferSource.endBatch(RenderType.entityGlint());
        renderBufferSource.endBatch(RenderType.entityGlintDirect());
        renderBufferSource.endBatch(RenderType.waterMask());
        renderBuffers.crumblingBufferSource().endBatch(); // not used now

        mc.getProfiler().popPush("struct_render_blocks2");
        OptifineCompat.getInstance().endDebugPreWaterBeginWater();
        renderBlockLayer(RenderType.translucent(), mvMatrix, realRenderRootVecf);
        OptifineCompat.getInstance().endWater();

        renderBufferSource.endBatch(RenderType.lines());
        renderBufferSource.endBatch();
        renderBlockLayer(RenderType.tripwire(), mvMatrix, realRenderRootVecf);

        OptifineCompat.getInstance().resetFog();
        FogRenderer.setupNoFog();
        matrixStack.popPose();

        RenderSystem.applyModelViewMatrix(); // ensure no polution
        lastGameTime = gameTime;
        mc.getProfiler().pop();
    }

    /**
     * Clears GL references and frees GL objects.
     */
    private void clearVertexBuffers()
    {
        if (vertexBuffers != null)
        {
            vertexBuffers.values().forEach(VertexBuffer::close);
            vertexBuffers = null;
        }
    }

    @Override
    public void close()
    {
        clearVertexBuffers();
    }

    private void renderBlockLayer(final RenderType layerRenderType, final Matrix4f mvMatrix, final Vector3f realRenderRootPos)
    {
        final VertexBuffer vertexBuffer = vertexBuffers.get(layerRenderType);
        if (vertexBuffer == null)
        {
            return;
        }

        layerRenderType.setupRenderState();

        final ShaderInstance shaderinstance = RenderSystem.getShader();
        BufferUploader.reset();

        for (int k = 0; k < 12; ++k)
        {
            shaderinstance.setSampler("Sampler" + k, RenderSystem.getShaderTexture(k));
        }

        if (shaderinstance.MODEL_VIEW_MATRIX != null)
        {
            shaderinstance.MODEL_VIEW_MATRIX.set(mvMatrix);
        }

        if (shaderinstance.PROJECTION_MATRIX != null)
        {
            shaderinstance.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
        }

        if (shaderinstance.COLOR_MODULATOR != null)
        {
            shaderinstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }

        if (shaderinstance.FOG_START != null)
        {
            shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
        }

        if (shaderinstance.FOG_END != null)
        {
            shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
        }

        if (shaderinstance.FOG_COLOR != null)
        {
            shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }

        if (shaderinstance.FOG_SHAPE != null)
        {
            shaderinstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }

        if (shaderinstance.TEXTURE_MATRIX != null)
        {
            shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }

        if (shaderinstance.GAME_TIME != null)
        {
            shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }

        RenderSystem.setupShaderLights(shaderinstance);
        shaderinstance.apply();
        OptifineCompat.getInstance().preLayerDraw(layerRenderType, mvMatrix);

        final Uniform uniform = shaderinstance.CHUNK_OFFSET;
        if (uniform != null && !OptifineCompat.getInstance().isShaderProgramActive())
        {
            uniform.set(realRenderRootPos);
            uniform.upload();
        }

        OptifineCompat.getInstance().setUniformChunkOffset(realRenderRootPos.x(), realRenderRootPos.y(), realRenderRootPos.z());

        vertexBuffer.bind();
        vertexBuffer.draw();

        if (uniform != null)
        {
            uniform.set(Vector3f.ZERO);
        }
        OptifineCompat.getInstance().setUniformChunkOffset(0.0f, 0.0f, 0.0f);

        shaderinstance.clear();

        VertexBuffer.unbind();
        layerRenderType.clearRenderState();
    }

    private static class OurChunkBufferBuilderPack extends ChunkBufferBuilderPack
    {
        @Override
        public BufferBuilder builder(RenderType renderType)
        {
            final BufferBuilder buffer = super.builder(renderType);
            if (!buffer.building())
            {
                buffer.begin(renderType.mode(), renderType.format());
            }
            return buffer;
        }
        
    }
}
