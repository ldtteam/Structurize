package com.ldtteam.structures.client;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.event.ClientEventSubscriber;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.FluidRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The renderer for blueprint.
 * Holds all information required to render a blueprint.
 */
public class BlueprintRenderer implements AutoCloseable
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Supplier<Map<RenderType, VertexBuffer>> blockVertexBuffersFactory = () -> RenderType.chunkBufferLayers()
        .stream()
        .collect(Collectors.toMap((p_228934_0_) -> {
            return p_228934_0_;
        }, (p_228933_0_) -> {
            return new VertexBuffer(DefaultVertexFormats.BLOCK);
        }));

    private final BlueprintBlockAccess blockAccess;
    private List<Entity> entities;
    private List<TileEntity> tileEntities;
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

        final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRenderer();
        final Random random = new Random();
        final MatrixStack matrixStack = new MatrixStack();
        final List<BlockInfo> blocks = blockAccess.getBlueprint().getBlockInfoAsList();
        final Map<RenderType, VertexBuffer> newVertexBuffers = blockVertexBuffersFactory.get();

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
                        state = Minecraft.getInstance().level != null
                                ? BlockUtils.getFluidForDimension( Minecraft.getInstance().level)
                                : Blocks.WATER.defaultBlockState();
                    }

                    final BlockPos blockPos = blockInfo.getPos();
                    final FluidState fluidState = state.getFluidState();

                    matrixStack.pushPose();
                    matrixStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                    if (state.getRenderShape() != BlockRenderType.INVISIBLE && RenderTypeLookup.canRenderInLayer(state, renderType))
                    {
                        blockRendererDispatcher
                            .renderModel(state, blockPos, blockAccess, matrixStack, buffer, true, random, EmptyModelData.INSTANCE);
                    }

                    if (!fluidState.isEmpty() && RenderTypeLookup.canRenderInLayer(fluidState, renderType))
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
    public void draw(final BlockPos pos, final MatrixStack matrixStack, final float partialTicks)
    {
        Minecraft.getInstance().getProfiler().push("struct_render_init");
        if (Settings.instance.shouldRefresh())
        {
            init();
        }

        Minecraft.getInstance().getProfiler().popPush("struct_render_blocks");
        final Minecraft mc = Minecraft.getInstance();
        final Vector3d viewPosition = mc.gameRenderer.getMainCamera().getPosition();
        final BlockPos primaryBlockOffset = blockAccess.getBlueprint().getPrimaryBlockOffset();
        final int x = pos.getX() - primaryBlockOffset.getX();
        final int y = pos.getY() - primaryBlockOffset.getY();
        final int z = pos.getZ() - primaryBlockOffset.getZ();

        // missing clipping helper? frustum?
        // missing chunk system and render distance!

        matrixStack.pushPose();
        matrixStack.translate(x - viewPosition.x(), y - viewPosition.y(), z - viewPosition.z());
        final Matrix4f rawPosMatrix = matrixStack.last().pose();

        // Render blocks

        Minecraft.getInstance().getProfiler().popPush("struct_render_blocks_finish");
        renderBlockLayer(RenderType.solid(), rawPosMatrix);
        // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        mc.getModelManager().getAtlas(AtlasTexture.LOCATION_BLOCKS).setBlurMipmap(false, mc.options.mipmapLevels > 0);
        renderBlockLayer(RenderType.cutoutMipped(), rawPosMatrix);
        mc.getModelManager().getAtlas(AtlasTexture.LOCATION_BLOCKS).restoreLastBlurMipmap();
        renderBlockLayer(RenderType.cutout(), rawPosMatrix);

        OptifineCompat.getInstance().endTerrainBeginEntities();

        Minecraft.getInstance().getProfiler().popPush("struct_render_entities");
        final IRenderTypeBuffer.Impl renderBufferSource = ClientEventSubscriber.renderBuffers.bufferSource();

        // Entities

        // if clipping etc., see WorldRenderer for what's missing
        entities.forEach(entity -> {
            if (entity instanceof ItemFrameEntity && ((ItemFrameEntity) entity).getItem().getItem() instanceof CompassItem)
            {
                final ItemFrameEntity copy = EntityType.ITEM_FRAME.create(blockAccess);
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
                    MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot),
                    0,
                    matrixStack,
                    renderBufferSource,
                    200);
        });

        Minecraft.getInstance().getProfiler().popPush("struct_render_entities_finish");
        renderBufferSource.endBatch(RenderType.entitySolid(AtlasTexture.LOCATION_BLOCKS));
        renderBufferSource.endBatch(RenderType.entityCutout(AtlasTexture.LOCATION_BLOCKS));
        renderBufferSource.endBatch(RenderType.entityCutoutNoCull(AtlasTexture.LOCATION_BLOCKS));
        renderBufferSource.endBatch(RenderType.entitySmoothCutout(AtlasTexture.LOCATION_BLOCKS));

        OptifineCompat.getInstance().endEntitiesBeginBlockEntities();

        // Block entities

        Minecraft.getInstance().getProfiler().popPush("struct_render_blockentities");
        final ActiveRenderInfo oldActiveRenderInfo = TileEntityRendererDispatcher.instance.camera;
        final World oldWorld = TileEntityRendererDispatcher.instance.level;
        TileEntityRendererDispatcher.instance.camera = new ActiveRenderInfo();
        TileEntityRendererDispatcher.instance.camera.setPosition(viewPosition.subtract(x, y, z));
        TileEntityRendererDispatcher.instance.level = blockAccess;
        tileEntities.forEach(tileEntity -> {
            final BlockPos tePos = tileEntity.getBlockPos();
            matrixStack.pushPose();
            matrixStack.translate(tePos.getX(), tePos.getY(), tePos.getZ());

            OptifineCompat.getInstance().preRenderBlockEntity(tileEntity);

            TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, matrixStack, renderBufferSource);
            matrixStack.popPose();
        });
        TileEntityRendererDispatcher.instance.camera = oldActiveRenderInfo;
        TileEntityRendererDispatcher.instance.level = oldWorld;

        Minecraft.getInstance().getProfiler().popPush("struct_render_blockentities_finish");
        renderBufferSource.endBatch(RenderType.solid());
        renderBufferSource.endBatch(Atlases.solidBlockSheet());
        renderBufferSource.endBatch(Atlases.cutoutBlockSheet());
        renderBufferSource.endBatch(Atlases.bedSheet());
        renderBufferSource.endBatch(Atlases.shulkerBoxSheet());
        renderBufferSource.endBatch(Atlases.signSheet());
        renderBufferSource.endBatch(Atlases.chestSheet());
        ClientEventSubscriber.renderBuffers.outlineBufferSource().endOutlineBatch(); // not used now
        renderBufferSource.endBatch(Atlases.translucentCullBlockSheet());
        renderBufferSource.endBatch(Atlases.bannerSheet());
        renderBufferSource.endBatch(Atlases.shieldSheet());
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

        OptifineCompat.getInstance().endBlockEntitiesPreWaterBeginWater();

        Minecraft.getInstance().getProfiler().popPush("struct_render_blocks_finish2");
        renderBlockLayer(RenderType.translucent(), rawPosMatrix);
        renderBlockLayer(RenderType.tripwire(), rawPosMatrix);

        OptifineCompat.getInstance().endWater();

        matrixStack.popPose();
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

    private void renderBlockLayer(final RenderType layerRenderType, final Matrix4f rawPosMatrix)
    {
        final VertexBuffer buffer = vertexBuffers.get(layerRenderType);

        layerRenderType.setupRenderState();
        OptifineCompat.getInstance().preLayerDraw(layerRenderType);

        buffer.bind();
        DefaultVertexFormats.BLOCK.setupBufferState(0);
        OptifineCompat.getInstance().setupArrayPointers();
        buffer.draw(rawPosMatrix, layerRenderType.mode());

        VertexBuffer.unbind();
        RenderSystem.clearCurrentColor();
        DefaultVertexFormats.BLOCK.clearBufferState();

        OptifineCompat.getInstance().postLayerDraw(layerRenderType);
        layerRenderType.clearRenderState();
    }
}
