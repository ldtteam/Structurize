package com.ldtteam.structurize.client;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.lib.BlueprintUtils;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.event.ClientEventSubscriber;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.FluidRenderer;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private final BlueprintBlockAccess blockAccess;
    private List<Entity> entities;
    private List<BlockEntity> tileEntities;
    private Map<RenderType, VertexBuffer> vertexBuffers;

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
        init();
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

    private void init()
    {
        clearVertexBuffers();
        entities = BlueprintUtils.instantiateEntities(blockAccess.getBlueprint(), blockAccess);
        tileEntities = BlueprintUtils.instantiateTileEntities(blockAccess.getBlueprint(), blockAccess);

        final BlockRenderDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRenderer();
        final Random random = new Random();
        final PoseStack matrixStack = new PoseStack();
        final List<BlockInfo> blocks = blockAccess.getBlueprint().getBlockInfoAsList();
        final Map<RenderType, VertexBuffer> newVertexBuffers = blockVertexBuffersFactory.get();
        final BlockState defaultFluidState = Minecraft.getInstance().level != null
            ? BlockUtils.getFluidForDimension(Minecraft.getInstance().level)
            : Blocks.WATER.defaultBlockState();

        for (final RenderType renderType : RenderType.chunkBufferLayers())
        {
            final BufferBuilder buffer = new BufferBuilder(renderType.bufferSize());
            buffer.begin(renderType.mode(), renderType.format());
            for (final BlockInfo blockInfo : blocks)
            {
                try
                {
                    BlockState state = blockInfo.getState();
                    if (state.getBlock() == ModBlocks.blockSubstitution.get())
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
                        // TODO: once the all mighty event forge pr is pulled - model data
                        blockRendererDispatcher
                            .renderBatched(state, blockPos, blockAccess, matrixStack, buffer, true, random, EmptyModelData.INSTANCE);
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
        Minecraft.getInstance().getProfiler().push("struct_render_init");
        if (Settings.instance.shouldRefresh())
        {
            init();
        }

        Minecraft.getInstance().getProfiler().popPush("struct_render_blocks");
        final Minecraft mc = Minecraft.getInstance();
        final Vec3 viewPosition = mc.gameRenderer.getMainCamera().getPosition();
        final BlockPos primaryBlockOffset = blockAccess.getBlueprint().getPrimaryBlockOffset();
        final int x = pos.getX() - primaryBlockOffset.getX();
        final int y = pos.getY() - primaryBlockOffset.getY();
        final int z = pos.getZ() - primaryBlockOffset.getZ();

        // missing clipping helper? frustum?
        // missing chunk system and render distance!

        matrixStack.pushPose();
        matrixStack.translate(x - viewPosition.x(), y - viewPosition.y(), z - viewPosition.z());

        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().mulPoseMatrix(matrixStack.last().pose());
        RenderSystem.applyModelViewMatrix();

        // Render blocks

        Minecraft.getInstance().getProfiler().popPush("struct_render_blocks_finish");
        renderBlockLayer(RenderType.solid());
        // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        mc.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).setBlurMipmap(false, mc.options.mipmapLevels > 0);
        renderBlockLayer(RenderType.cutoutMipped());
        mc.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).restoreLastBlurMipmap();
        renderBlockLayer(RenderType.cutout());

        OptifineCompat.getInstance().endTerrainBeginEntities();

        Minecraft.getInstance().getProfiler().popPush("struct_render_entities");
        final MultiBufferSource.BufferSource renderBufferSource = ClientEventSubscriber.renderBuffers.bufferSource();

        // Entities

        // if clipping etc., see WorldRenderer for what's missing
        entities.forEach(entity -> {
            if (entity instanceof ItemFrame && ((ItemFrame) entity).getItem().getItem() instanceof CompassItem)
            {
                final ItemFrame copy = EntityType.ITEM_FRAME.create(blockAccess);
                copy.restoreFrom(entity);
                copy.setItem(ItemStack.EMPTY);
                entity = copy;
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
        });

        Minecraft.getInstance().getProfiler().popPush("struct_render_entities_finish");
        renderBufferSource.endBatch(RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS));
        renderBufferSource.endBatch(RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS));
        renderBufferSource.endBatch(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        renderBufferSource.endBatch(RenderType.entitySmoothCutout(InventoryMenu.BLOCK_ATLAS));

        OptifineCompat.getInstance().endEntitiesBeginBlockEntities();

        // Block entities

        Minecraft.getInstance().getProfiler().popPush("struct_render_blockentities");
        final Camera oldActiveRenderInfo = Minecraft.getInstance().getBlockEntityRenderDispatcher().camera;
        final Level oldWorld = Minecraft.getInstance().getBlockEntityRenderDispatcher().level;
        Minecraft.getInstance().getBlockEntityRenderDispatcher().camera = new Camera();
        Minecraft.getInstance().getBlockEntityRenderDispatcher().camera.setPosition(viewPosition.subtract(x, y, z));
        Minecraft.getInstance().getBlockEntityRenderDispatcher().level = blockAccess;
        tileEntities.forEach(tileEntity -> {
            final BlockPos tePos = tileEntity.getBlockPos();
            matrixStack.pushPose();
            matrixStack.translate(tePos.getX(), tePos.getY(), tePos.getZ());

            OptifineCompat.getInstance().preRenderBlockEntity(tileEntity);

            Minecraft.getInstance().getBlockEntityRenderDispatcher().render(tileEntity, partialTicks, matrixStack, renderBufferSource);
            matrixStack.popPose();
        });
        Minecraft.getInstance().getBlockEntityRenderDispatcher().camera = oldActiveRenderInfo;
        Minecraft.getInstance().getBlockEntityRenderDispatcher().level = oldWorld;


        Minecraft.getInstance().getProfiler().popPush("struct_render_blockentities_finish");
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
        ClientEventSubscriber.renderBuffers.outlineBufferSource().endOutlineBatch(); // not used now
        OptifineCompat.getInstance().endBlockEntitiesBeginDebug();

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
        ClientEventSubscriber.renderBuffers.crumblingBufferSource().endBatch(); // not used now
        renderBufferSource.endBatch(RenderType.lines());
        renderBufferSource.endBatch();

        Minecraft.getInstance().getProfiler().popPush("struct_render_blocks_finish2");
        RenderSystem.applyModelViewMatrix();
        OptifineCompat.getInstance().endDebugPreWaterBeginWater();
        renderBlockLayer(RenderType.translucent());
        OptifineCompat.getInstance().endWater();
        renderBlockLayer(RenderType.tripwire());

        matrixStack.popPose();
        RenderSystem.getModelViewStack().popPose();
        Minecraft.getInstance().getProfiler().pop();
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

    private void renderBlockLayer(final RenderType layerRenderType)
    {
        layerRenderType.setupRenderState();

        VertexFormat vertexformat = layerRenderType.format();
        ShaderInstance shaderinstance = RenderSystem.getShader();
        BufferUploader.reset();

        for (int k = 0; k < 12; ++k)
        {
            shaderinstance.setSampler("Sampler" + k, RenderSystem.getShaderTexture(k));
        }

        if (shaderinstance.MODEL_VIEW_MATRIX != null)
        {
            shaderinstance.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
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
        Uniform uniform = shaderinstance.CHUNK_OFFSET;

        if (uniform != null)
        {
            uniform.set(0f, 0f, 0f);
            uniform.upload();
        }

        vertexBuffers.get(layerRenderType).drawChunkLayer();

        if (uniform != null)
        {
            uniform.set(Vector3f.ZERO);
        }

        shaderinstance.clear();
        vertexformat.clearBufferState();

        VertexBuffer.unbind();
        VertexBuffer.unbindVertexArray();
        layerRenderType.clearRenderState();
    }
}
