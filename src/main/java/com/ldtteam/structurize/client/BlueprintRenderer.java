package com.ldtteam.structurize.client;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtils;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.FluidRenderer;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.ReportedException;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;

/**
 * The renderer for blueprint.
 * Holds all information required to render a blueprint.
 */
public class BlueprintRenderer implements AutoCloseable
{
    private static final Logger LOGGER = LogManager.getLogger();
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
     * @param blueprint blueprint from active structure
     */
    public void updateBlueprint(final Blueprint blueprint)
    {
        if (blockAccess.getBlueprint() != blueprint && blockAccess.getBlueprint().hashCode() == blueprint.hashCode())
        {
            blockAccess.setBlueprint(blueprint);
            Settings.instance.scheduleRefresh();
        }
    }

    @SuppressWarnings("resource")
    private void init()
    {
        final Map<BlockPos, IModelData> teModelData = new HashMap<>();

        clearVertexBuffers();
        entities = BlueprintUtils.instantiateEntities(blockAccess.getBlueprint(), blockAccess);
        tileEntities = BlueprintUtils.instantiateTileEntities(blockAccess.getBlueprint(), blockAccess, teModelData);

        final BlockRenderDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRenderer();
        final Random random = new Random();
        final PoseStack matrixStack = new PoseStack();
        final List<BlockInfo> blocks = blockAccess.getBlueprint().getBlockInfoAsList();
        final Map<RenderType, VertexBuffer> newVertexBuffers = blockVertexBuffersFactory.get();
        final BlockState defaultFluidState = BlockUtils.getFluidForDimension(Minecraft.getInstance().level);

        for (final RenderType renderType : RenderType.chunkBufferLayers())
        {
            final BufferBuilder buffer = new BufferBuilder(renderType.bufferSize());
            buffer.begin(renderType.mode(), renderType.format());
            for (final BlockInfo blockInfo : blocks)
            {
                try
                {
                    BlockState state = blockInfo.getState();
                    if ((state.getBlock() == ModBlocks.blockSubstitution.get() && Settings.instance.renderLightPlaceholders()) ||
                          state.getBlock() == ModBlocks.blockTagSubstitution.get())
                    {
                        state = Blocks.AIR.defaultBlockState();
                    }
                    if (state.getBlock() == ModBlocks.blockFluidSubstitution.get())
                    {
                        state = defaultFluidState;
                    }

                    final BlockPos blockPos = blockInfo.getPos();
                    final FluidState fluidState = state.getFluidState();

                    matrixStack.pushPose();
                    matrixStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                    if (state.getRenderShape() != RenderShape.INVISIBLE && ItemBlockRenderTypes.canRenderInLayer(state, renderType))
                    {
                        blockRendererDispatcher.renderBatched(state,
                            blockPos,
                            blockAccess,
                            matrixStack,
                            buffer,
                            true,
                            random,
                            teModelData.getOrDefault(blockPos, EmptyModelData.INSTANCE));
                    }

                    if (!fluidState.isEmpty() && ItemBlockRenderTypes.canRenderInLayer(fluidState, renderType))
                    {
                        FluidRenderer.render(blockAccess, blockPos, buffer, fluidState);
                    }

                    matrixStack.popPose();
                }
                catch (final ReportedException e)
                {
                    LOGGER.error("Error while trying to render structure part: " + e.getMessage(), e.getCause());
                }
            }
            buffer.end();
            OptifineCompat.getInstance().beforeBuilderUpload(buffer);
            newVertexBuffers.get(renderType).upload(buffer);
        }
        vertexBuffers = newVertexBuffers;
    }

    /**
     * Draws structure into world.
     */
    public void draw(final BlockPos pos, final PoseStack matrixStack, final float partialTicks)
    {
        final Minecraft mc = Minecraft.getInstance();
        final long gameTime = mc.level.getGameTime();
    
        mc.getProfiler().push("struct_render_init");
        final BlockPos anchorPos = pos.subtract(blockAccess.getBlueprint().getPrimaryBlockOffset());
        blockAccess.setWorldPos(anchorPos);
        if (Settings.instance.shouldRefresh() || vertexBuffers == null)
        {
            init();
        }

        mc.getProfiler().popPush("struct_render_prepare");
        final Vec3 viewPosition = mc.gameRenderer.getMainCamera().getPosition();
        final Vec3 realRenderRootVecd = Vec3.atLowerCornerOf(anchorPos).subtract(viewPosition);
        final Vector3f realRenderRootVecf = new Vector3f(realRenderRootVecd);

        // missing clipping helper? frustum?
        // missing chunk system and render distance!
        // TODO: level animate tick!

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
        mc.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).setBlurMipmap(false, mc.options.mipmapLevels > 0);
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
            if (entity instanceof ItemFrame && ((ItemFrame) entity).getItem().getItem() instanceof CompassItem)
            {
                final ItemFrame copy = EntityType.ITEM_FRAME.create(blockAccess);
                copy.restoreFrom(entity);
                copy.setItem(ItemStack.EMPTY);
                entity = copy;
            }

            if (gameTime != lastGameTime)
            {
                if (entity instanceof EndCrystal crystal)
                {
                    // safeguarded INLINE crystal.tick()
                    crystal.time++;
                }
            }

            OptifineCompat.getInstance().preRenderEntity(entity);

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
                    if (bs.getValue(CampfireBlock.LIT))
                    {
                        CampfireBlockEntity.particleTick(mc.level, anchorPos.offset(tePos), bs, campfire);
                    }
                }
                else if (tileEntity instanceof SkullBlockEntity skull)
                {
                    final BlockState bs = blockAccess.getBlockState(tePos);
                    if (bs.is(Blocks.DRAGON_HEAD) || bs.is(Blocks.DRAGON_WALL_HEAD))
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
            vertexBuffers.values().forEach(buffer -> buffer.close());
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
        layerRenderType.setupRenderState();

        final VertexFormat vertexformat = layerRenderType.format();
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

        vertexBuffers.get(layerRenderType).drawChunkLayer();

        if (uniform != null)
        {
            uniform.set(Vector3f.ZERO);
        }
        OptifineCompat.getInstance().setUniformChunkOffset(0.0f, 0.0f, 0.0f);

        shaderinstance.clear();
        vertexformat.clearBufferState();

        VertexBuffer.unbind();
        VertexBuffer.unbindVertexArray();
        layerRenderType.clearRenderState();
    }
}
