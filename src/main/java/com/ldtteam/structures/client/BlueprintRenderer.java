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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.ReportedException;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import com.mojang.math.Matrix4f;
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

import com.mojang.blaze3d.vertex.BufferBuilder;
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
        .collect(Collectors.toMap((type) -> type, (type) -> new VertexBuffer(DefaultVertexFormat.BLOCK)));

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

                    if (state.getRenderShape() != RenderShape.INVISIBLE && ItemBlockRenderTypes.canRenderInLayer(state, renderType))
                    {
                        blockRendererDispatcher
                            .renderModel(state, blockPos, blockAccess, matrixStack, buffer, true, random, EmptyModelData.INSTANCE);
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
        final Matrix4f rawPosMatrix = matrixStack.last().pose();

        // Render blocks

        Minecraft.getInstance().getProfiler().popPush("struct_render_blocks_finish");
        renderBlockLayer(RenderType.solid(), rawPosMatrix);
        // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, mc.options.mipmapLevels > 0);
        renderBlockLayer(RenderType.cutoutMipped(), rawPosMatrix);
        mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
        renderBlockLayer(RenderType.cutout(), rawPosMatrix);

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
                    Mth.lerp(partialTicks, entity.yRotO, entity.yRot),
                    0,
                    matrixStack,
                    renderBufferSource,
                    200);
        });

        Minecraft.getInstance().getProfiler().popPush("struct_render_entities_finish");
        renderBufferSource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        renderBufferSource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        renderBufferSource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        renderBufferSource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));

        OptifineCompat.getInstance().endEntitiesBeginBlockEntities();

        // Block entities

        Minecraft.getInstance().getProfiler().popPush("struct_render_blockentities");
        final Camera oldActiveRenderInfo = BlockEntityRenderDispatcher.instance.camera;
        final Level oldWorld = BlockEntityRenderDispatcher.instance.level;
        BlockEntityRenderDispatcher.instance.camera = new Camera();
        BlockEntityRenderDispatcher.instance.camera.setPosition(viewPosition.subtract(x, y, z));
        BlockEntityRenderDispatcher.instance.level = blockAccess;
        tileEntities.forEach(tileEntity -> {
            final BlockPos tePos = tileEntity.getBlockPos();
            matrixStack.pushPose();
            matrixStack.translate(tePos.getX(), tePos.getY(), tePos.getZ());

            OptifineCompat.getInstance().preRenderBlockEntity(tileEntity);

            BlockEntityRenderDispatcher.instance.render(tileEntity, partialTicks, matrixStack, renderBufferSource);
            matrixStack.popPose();
        });
        BlockEntityRenderDispatcher.instance.camera = oldActiveRenderInfo;
        BlockEntityRenderDispatcher.instance.level = oldWorld;


        Minecraft.getInstance().getProfiler().popPush("struct_render_blockentities_finish");
        renderBufferSource.endBatch(RenderType.solid());
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
        OptifineCompat.getInstance().endDebugPreWaterBeginWater();
        renderBlockLayer(RenderType.translucent(), rawPosMatrix);
        OptifineCompat.getInstance().endWater();
        renderBlockLayer(RenderType.tripwire(), rawPosMatrix);

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
        DefaultVertexFormat.BLOCK.setupBufferState(0);
        OptifineCompat.getInstance().setupArrayPointers();
        buffer.draw(rawPosMatrix, layerRenderType.mode());

        VertexBuffer.unbind();
        RenderSystem.clearCurrentColor();
        DefaultVertexFormat.BLOCK.clearBufferState();

        OptifineCompat.getInstance().postLayerDraw(layerRenderType);
        layerRenderType.clearRenderState();
    }
}
