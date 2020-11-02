package com.ldtteam.structures.client;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.event.ClientEventSubscriber;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.FluidRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;

/**
 * The renderer for blueprint.
 * Holds all information required to render a blueprint.
 */
public class BlueprintRenderer implements AutoCloseable
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Supplier<Map<RenderType, VertexBuffer>> blockVertexBuffersFactory = () -> RenderType.getBlockRenderTypes()
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

        final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        final Random random = new Random();
        final MatrixStack matrixStack = new MatrixStack();
        final List<BlockInfo> blocks = blockAccess.getBlueprint().getBlockInfoAsList();
        final Map<RenderType, VertexBuffer> newVertexBuffers = blockVertexBuffersFactory.get();

        for (final RenderType renderType : RenderType.getBlockRenderTypes())
        {
            final BufferBuilder buffer = new BufferBuilder(renderType.getBufferSize());
            buffer.begin(renderType.getDrawMode(), renderType.getVertexFormat());
            for (final BlockInfo blockInfo : blocks)
            {
                try
                {
                    BlockState state = blockInfo.getState();
                    if (state.getBlock() == ModBlocks.blockSubstitution)
                    {
                        state = Blocks.AIR.getDefaultState();
                    }

                    final BlockPos blockPos = blockInfo.getPos();
                    final FluidState fluidState = state.getFluidState();

                    matrixStack.push();
                    matrixStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                    if (state.getRenderType() != BlockRenderType.INVISIBLE && RenderTypeLookup.canRenderInLayer(state, renderType))
                    {
                        blockRendererDispatcher
                            .renderModel(state, blockPos, blockAccess, matrixStack, buffer, true, random, EmptyModelData.INSTANCE);
                    }

                    if (!fluidState.isEmpty() && RenderTypeLookup.canRenderInLayer(fluidState, renderType))
                    {
                        FluidRenderer.render(blockAccess, blockPos, buffer, fluidState);
                    }

                    matrixStack.pop();
                }
                catch (final ReportedException e)
                {
                    LOGGER.error("Error while trying to render structure part: " + e.getMessage(), e.getCause());
                }
            }
            buffer.finishDrawing();
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
        Minecraft.getInstance().getProfiler().startSection("struct_render_init");
        if (Settings.instance.shouldRefresh())
        {
            init();
        }

        Minecraft.getInstance().getProfiler().endStartSection("struct_render_blocks");
        final Minecraft mc = Minecraft.getInstance();
        final Vector3d viewPosition = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
        final BlockPos primaryBlockOffset = blockAccess.getBlueprint().getPrimaryBlockOffset();
        final int x = pos.getX() - primaryBlockOffset.getX();
        final int y = pos.getY() - primaryBlockOffset.getY();
        final int z = pos.getZ() - primaryBlockOffset.getZ();

        // missing clipping helper? frustum?
        // missing chunk system and render distance!

        matrixStack.push();
        matrixStack.translate(x - viewPosition.getX(), y - viewPosition.getY(), z - viewPosition.getZ());
        final Matrix4f rawPosMatrix = matrixStack.getLast().getMatrix();

        // Render blocks

        Minecraft.getInstance().getProfiler().endStartSection("struct_render_blocks_finish");
        renderBlockLayer(RenderType.getSolid(), rawPosMatrix);
        // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        mc.getModelManager().getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, mc.gameSettings.mipmapLevels > 0);
        renderBlockLayer(RenderType.getCutoutMipped(), rawPosMatrix);
        mc.getModelManager().getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        renderBlockLayer(RenderType.getCutout(), rawPosMatrix);

        OptifineCompat.getInstance().endTerrainBeginEntities();

        Minecraft.getInstance().getProfiler().endStartSection("struct_render_entities");
        final IRenderTypeBuffer.Impl renderBufferSource = ClientEventSubscriber.renderBuffers.getBufferSource();

        // Entities

        // if clipping etc., see WorldRenderer for what's missing
        entities.forEach(entity -> {
            if (entity instanceof ItemFrameEntity && ((ItemFrameEntity) entity).getDisplayedItem().getItem() instanceof CompassItem)
            {
                final ItemFrameEntity copy = EntityType.ITEM_FRAME.create(blockAccess);
                copy.copyDataFromOld(entity);
                copy.setDisplayedItem(ItemStack.EMPTY);
                entity = copy;
            }

            OptifineCompat.getInstance().preRenderEntity(entity);

            Minecraft.getInstance()
                .getRenderManager()
                .renderEntityStatic(entity,
                    entity.getPosX(),
                    entity.getPosY(),
                    entity.getPosZ(),
                    MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw),
                    0,
                    matrixStack,
                    renderBufferSource,
                    200);
        });

        Minecraft.getInstance().getProfiler().endStartSection("struct_render_entities_finish");
        renderBufferSource.finish(RenderType.getEntitySolid(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
        renderBufferSource.finish(RenderType.getEntityCutout(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
        renderBufferSource.finish(RenderType.getEntityCutoutNoCull(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
        renderBufferSource.finish(RenderType.getEntitySmoothCutout(AtlasTexture.LOCATION_BLOCKS_TEXTURE));

        OptifineCompat.getInstance().endEntitiesBeginBlockEntities();

        // Block entities

        Minecraft.getInstance().getProfiler().endStartSection("struct_render_blockentities");
        final ActiveRenderInfo oldActiveRenderInfo = TileEntityRendererDispatcher.instance.renderInfo;
        final World oldWorld = TileEntityRendererDispatcher.instance.world;
        TileEntityRendererDispatcher.instance.renderInfo = new ActiveRenderInfo();
        TileEntityRendererDispatcher.instance.renderInfo.setPosition(viewPosition.subtract(x, y, z));
        TileEntityRendererDispatcher.instance.world = blockAccess;
        tileEntities.forEach(tileEntity -> {
            final BlockPos tePos = tileEntity.getPos();
            matrixStack.push();
            matrixStack.translate(tePos.getX(), tePos.getY(), tePos.getZ());

            OptifineCompat.getInstance().preRenderBlockEntity(tileEntity);

            TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, partialTicks, matrixStack, renderBufferSource);
            matrixStack.pop();
        });
        TileEntityRendererDispatcher.instance.renderInfo = oldActiveRenderInfo;
        TileEntityRendererDispatcher.instance.world = oldWorld;

        Minecraft.getInstance().getProfiler().endStartSection("struct_render_blockentities_finish");
        renderBufferSource.finish(RenderType.getSolid());
        renderBufferSource.finish(Atlases.getSolidBlockType());
        renderBufferSource.finish(Atlases.getCutoutBlockType());
        renderBufferSource.finish(Atlases.getBedType());
        renderBufferSource.finish(Atlases.getShulkerBoxType());
        renderBufferSource.finish(Atlases.getSignType());
        renderBufferSource.finish(Atlases.getChestType());
        ClientEventSubscriber.renderBuffers.getOutlineBufferSource().finish(); // not used now
        renderBufferSource.finish(Atlases.getTranslucentCullBlockType());
        renderBufferSource.finish(Atlases.getBannerType());
        renderBufferSource.finish(Atlases.getShieldType());
        renderBufferSource.finish(RenderType.getArmorGlint());
        renderBufferSource.finish(RenderType.getArmorEntityGlint());
        renderBufferSource.finish(RenderType.getGlint());
        renderBufferSource.finish(RenderType.getEntityGlint());
        renderBufferSource.finish(RenderType.getWaterMask());
        ClientEventSubscriber.renderBuffers.getCrumblingBufferSource().finish(); // not used now
        renderBufferSource.finish(RenderType.getLines());
        renderBufferSource.finish();

        OptifineCompat.getInstance().endBlockEntitiesPreWaterBeginWater();

        Minecraft.getInstance().getProfiler().endStartSection("struct_render_blocks_finish2");
        renderBlockLayer(RenderType.getTripwire(), rawPosMatrix);
        renderBlockLayer(RenderType.getTranslucent(), rawPosMatrix);

        OptifineCompat.getInstance().endWater();

        matrixStack.pop();
        Minecraft.getInstance().getProfiler().endSection();
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

        buffer.bindBuffer();
        DefaultVertexFormats.BLOCK.setupBufferState(0);
        OptifineCompat.getInstance().setupArrayPointers();
        buffer.draw(rawPosMatrix, layerRenderType.getDrawMode());

        VertexBuffer.unbindBuffer();
        RenderSystem.clearCurrentColor();
        DefaultVertexFormats.BLOCK.clearBufferState();

        OptifineCompat.getInstance().postLayerDraw(layerRenderType);
        layerRenderType.clearRenderState();
    }
}
