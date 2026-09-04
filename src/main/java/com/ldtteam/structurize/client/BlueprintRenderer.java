package com.ldtteam.structurize.client;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtils;
import com.ldtteam.structurize.client.fakelevel.BlueprintBlockAccess;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.tag.ModTags;
import com.ldtteam.structurize.util.BlockInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.model.data.ModelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Prepares and submits blueprint preview geometry through Minecraft's current level render pipeline.
 */
public class BlueprintRenderer implements AutoCloseable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BlueprintRenderer.class);
    public static final float TRANSPARENCY_THRESHOLD = 0.99F;
    private static boolean hasWarnedExceptions = false;

    private final BlueprintBlockAccess blockAccess;
    private final List<Entity> entities = new ArrayList<>();
    private final List<BlockEntity> tileEntities = new ArrayList<>();
    private final List<MovingBlockRenderState> blockStates = new ArrayList<>();
    private final List<FluidInstance> fluidInstances = new ArrayList<>();
    private long lastGameTime;
    private Set<Object> crashingObjects = Collections.newSetFromMap(new IdentityHashMap<>());

    public static BlueprintRenderer buildRendererForBlueprint(final Blueprint blueprint)
    {
        return new BlueprintRenderer(new BlueprintBlockAccess(blueprint));
    }

    private BlueprintRenderer(final BlueprintBlockAccess blockAccess)
    {
        this.blockAccess = blockAccess;
    }

    public void updateBlueprint(final BlueprintPreviewData previewData)
    {
        if (blockAccess.getLevelSource() != previewData.getBlueprint()
            && blockAccess.getLevelSource().hashCode() == previewData.getBlueprint().hashCode())
        {
            blockAccess.setLevelSource(previewData.getBlueprint());
        }
    }

    private void init(final BlueprintPreviewData previewData, final Map<Object, Exception> suppressedExceptions)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        final Blueprint blueprint = previewData.getBlueprint();
        // Blueprint tile entities can contribute model data that is required by
        // their block models (for example, Domum Ornamentum and MineColonies
        // blocks). Keep that data keyed by the blueprint-local position just as
        // the legacy baked-model renderer did. Passing ModelData.EMPTY for all
        // blocks makes those models collect no render parts, which leaves only
        // the placement outline visible in the preview.
        final Map<BlockPos, ModelData> teModelData = new HashMap<>();

        clearCachedState();

        final Map<BlockPos, BlockEntity> tileEntitiesMap =
            BlueprintUtils.instantiateTileEntities(blueprint, blockAccess, teModelData);
        entities.addAll(BlueprintUtils.instantiateEntities(blueprint, blockAccess));

        blockAccess.setBlockEntities(tileEntitiesMap);
        blockAccess.setEntities(entities);
        blockAccess.setSolidSubstitutionOverride(previewData.getSolidSubstitutionOverride());
        blockAccess.setRenderBlocksNiceOverride(previewData.getRenderBlocksNice());

        for (final BlockInfo blockInfo : blueprint.getBlockInfoAsList())
        {
            final BlockPos blockPos = blockInfo.getPos();
            BlockState state = blockInfo.getState();

            try
            {
                if (previewData.getRenderBlocksNice() && state.getBlock() == ModBlocks.blockTagSubstitution.get())
                {
                    if (tileEntitiesMap.remove(blockPos) instanceof final BlockEntityTagSubstitution tagTE)
                    {
                        final BlockEntityTagSubstitution.ReplacementBlock replacement = tagTE.getReplacement();
                        state = replacement.getBlockState();

                        Optional.ofNullable(replacement.createBlockEntity(blockPos)).ifPresent(newBe -> {
                            newBe.setLevel(blockAccess);
                            teModelData.put(blockPos, newBe.getModelData());
                            tileEntitiesMap.put(blockPos, newBe);
                        });
                    }
                    else
                    {
                        state = Blocks.AIR.defaultBlockState();
                    }
                }
                else
                {
                    state = blockAccess.prepareBlockStateForRendering(state, blockPos);
                }

                if (state.isAir())
                {
                    continue;
                }

                final FluidState fluidState = state.getFluidState();
                if (!fluidState.isEmpty())
                {
                    fluidInstances.add(new FluidInstance(blockPos, state, fluidState));
                }

                if (state.getRenderShape() != RenderShape.INVISIBLE)
                {
                    blockStates.add(createMovingBlockState(
                        minecraft,
                        blockPos,
                        state,
                        teModelData.getOrDefault(blockPos, ModelData.EMPTY)));
                }
            }
            catch (final ReportedException exception)
            {
                suppressedExceptions.put(blockInfo, exception);
            }
        }

        blockAccess.setSolidSubstitutionOverride(null);
        blockAccess.setRenderBlocksNiceOverride(Structurize.getConfig().getClient().renderPlaceholdersNice.get());
        tileEntities.addAll(tileEntitiesMap.values());
    }

    private MovingBlockRenderState createMovingBlockState(
        final Minecraft minecraft,
        final BlockPos pos,
        final BlockState state,
        final ModelData modelData)
    {
        final MovingBlockRenderState renderState = new MovingBlockRenderState();
        renderState.randomSeedPos = pos;
        renderState.blockPos = pos;
        renderState.blockState = state;
        renderState.modelData = modelData;
        renderState.cardinalLighting = minecraft.level.cardinalLighting();
        renderState.lightEngine = blockAccess.getLightEngine();

        final ClientLevel realLevel = minecraft.level;
        if (realLevel != null)
        {
            renderState.biome = realLevel.getBiome(blockAccess.getWorldPos().offset(pos));
        }
        return renderState;
    }

    public void draw(final BlueprintPreviewData previewData, final BlockPos pos, final SubmitCustomGeometryEvent ctx)
    {
        if (crashingObjects == null)
        {
            return;
        }

        try
        {
            reportSuppressedExceptions(previewData, drawUnsafe(previewData, pos, ctx));
        }
        catch (final Exception exception)
        {
            final CrashReport crashReport = CrashReport.forThrowable(exception, "Rendering blueprint");
            final CrashReportCategory category = crashReport.addCategory("Blueprint:");
            previewData.getBlueprint().describeSelfInCrashReport(category);
            LOGGER.error(crashReport.getDetails());

            crashingObjects = null;
            final var player = Minecraft.getInstance().player;
            if (player != null)
            {
                player.sendSystemMessage(Component.translatable(
                    "structurize.preview_renderer.cannot_render", previewData.getBlueprint().getName()));
            }
        }
    }

    private void reportSuppressedExceptions(
        final BlueprintPreviewData previewData,
        final Map<Object, Exception> suppressedExceptions)
    {
        if (suppressedExceptions.isEmpty())
        {
            return;
        }

        if (!hasWarnedExceptions)
        {
            hasWarnedExceptions = true;
            final var player = Minecraft.getInstance().player;
            if (player != null)
            {
                player.sendSystemMessage(Component.translatable("structurize.preview_renderer.exception"));
            }
        }

        boolean crashReported = false;
        boolean isEmpty = true;
        for (final Map.Entry<Object, Exception> entry : suppressedExceptions.entrySet())
        {
            if (!crashingObjects.add(entry.getKey()))
            {
                continue;
            }
            isEmpty = false;

            if (entry.getValue() instanceof final ReportedException reportedException)
            {
                previewData.getBlueprint()
                    .describeSelfInCrashReport(reportedException.getReport().addCategory("Rendering blueprint"));
                LOGGER.error(reportedException.getReport().getDetails());
                crashReported = true;
            }
            else
            {
                LOGGER.error("", entry.getValue());
            }
        }

        if (!crashReported && !isEmpty)
        {
            final CrashReport crashReport = CrashReport.forThrowable(new Exception(), "Summary");
            previewData.getBlueprint().describeSelfInCrashReport(crashReport.addCategory("Rendering blueprint"));
            LOGGER.error(crashReport.getDetails());
        }
    }

    public Map<Object, Exception> drawUnsafe(
        final BlueprintPreviewData previewData,
        final BlockPos pos,
        final SubmitCustomGeometryEvent ctx)
    {
        updateBlueprint(previewData);
        final BlockPos anchorPos = pos.subtract(previewData.getBlueprint().getPrimaryBlockOffset());
        blockAccess.setWorldPos(anchorPos);

        if (blockStates.isEmpty() && fluidInstances.isEmpty() && entities.isEmpty() && tileEntities.isEmpty())
        {
            init(previewData, new IdentityHashMap<>());
        }

        final Map<Object, Exception> suppressedExceptions = new IdentityHashMap<>();
        final Minecraft minecraft = Minecraft.getInstance();
        final long gameTime = minecraft.level.getGameTime();
        final float partialTicks = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        final Vec3 viewPosition = minecraft.gameRenderer.mainCamera().position();
        final Vec3 realRoot = Vec3.atLowerCornerOf(anchorPos).subtract(viewPosition);

        final PoseStack poseStack = ctx.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(realRoot.x(), realRoot.y(), realRoot.z());

        // This collector is owned by LevelRenderer and is consumed by the
        // current frame's feature dispatcher. A private SubmitNodeStorage is
        // never rendered and makes the preview silently disappear.
        final SubmitNodeCollector collector = ctx.getSubmitNodeCollector();
        submitBlocks(minecraft, collector, poseStack, previewData);
        submitFluids(minecraft, collector, poseStack);
        submitEntities(minecraft, collector, poseStack, gameTime, partialTicks, suppressedExceptions);
        submitBlockEntities(minecraft, collector, poseStack, anchorPos, gameTime, partialTicks);

        poseStack.popPose();
        lastGameTime = gameTime;
        return suppressedExceptions;
    }

    private void submitBlocks(
        final Minecraft minecraft,
        final SubmitNodeCollector collector,
        final PoseStack poseStack,
        final BlueprintPreviewData previewData)
    {
        final float previewAlpha = previewAlpha(previewData);
        final boolean blendPreview = previewAlpha >= 0.0F && previewAlpha < TRANSPARENCY_THRESHOLD;
        final ModelBlockRenderer translucentRenderer = blendPreview
            ? new ModelBlockRenderer(minecraft.options.ambientOcclusion().get(), false, minecraft.getBlockColors())
            : null;

        for (final MovingBlockRenderState state : blockStates)
        {
            // MovingBlockFeatureRenderer tessellates a block at the origin of
            // the submitted pose. The render state keeps the local position for
            // lighting/model-data lookups, but it is not used as a translation.
            // Apply the blueprint-local offset here or every block collapses at
            // the anchor (and is effectively hidden by the terrain).
            final BlockPos blockPos = state.blockPos;
            poseStack.pushPose();
            // Retain the small legacy offset to avoid z-fighting with the
            // terrain when a preview is placed directly on existing blocks.
            poseStack.translate(blockPos.getX() + 0.01F, blockPos.getY() + 0.01F, blockPos.getZ() + 0.01F);
            if (!blendPreview)
            {
                collector.submitMovingBlock(poseStack, state, 0);
            }
            else
            {
                final BlockStateModel model = minecraft.getModelManager().getBlockStateModelSet().get(state.blockState);
                collector.submitCustomGeometry(
                    poseStack,
                    RenderTypes.translucentMovingBlock(),
                    (pose, buffer) -> translucentRenderer.tesselateBlock(
                        (x, y, z, quad, instance) -> {
                            instance.multiplyColor(ARGB.color(previewAlpha, -1));
                            final PoseStack.Pose translatedPose = pose.copy();
                            translatedPose.translate(x, y, z);
                            buffer.putBakedQuad(translatedPose, quad, instance);
                        },
                        0.0F,
                        0.0F,
                        0.0F,
                        state,
                        state.blockPos,
                        state.blockState,
                        model,
                        state.blockState.getSeed(state.randomSeedPos)));
            }
            poseStack.popPose();
        }
    }

    private float previewAlpha(final BlueprintPreviewData previewData)
    {
        final float override = previewData.getOverridePreviewTransparency();
        if (override >= 0.0F)
        {
            return override;
        }
        return Structurize.getConfig().getClient().rendererTransparency.get().floatValue();
    }

    private void submitFluids(
        final Minecraft minecraft,
        final SubmitNodeCollector collector,
        final PoseStack poseStack)
    {
        if (fluidInstances.isEmpty())
        {
            return;
        }

        final FluidRenderer fluidRenderer = new FluidRenderer(minecraft.getModelManager().getFluidStateModelSet());
        for (final FluidInstance instance : fluidInstances)
        {
            final ChunkSectionLayer sectionLayer = minecraft.getModelManager().getFluidStateModelSet()
                .get(instance.fluidState()).layer();
            final RenderType renderType = movingRenderType(sectionLayer);

            final BlockAndTintGetter fluidLevel = new BlueprintBlockTintGetter();
            // FluidRenderer emits section-local coordinates (the same contract
            // used by the old chunk-buffer wrapper). Translate the pose by the
            // section origin so fluids keep their blueprint-local position.
            final BlockPos fluidPos = instance.pos();
            final int sectionX = fluidPos.getX() - (fluidPos.getX() & 15);
            final int sectionY = fluidPos.getY() - (fluidPos.getY() & 15);
            final int sectionZ = fluidPos.getZ() - (fluidPos.getZ() & 15);
            poseStack.pushPose();
            poseStack.translate(sectionX, sectionY, sectionZ);
            collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> fluidRenderer.tesselate(
                fluidLevel,
                fluidPos,
                layer -> layer == sectionLayer ? new PoseVertexConsumer(pose, buffer) : null,
                instance.state(),
                instance.fluidState()));
            poseStack.popPose();
        }
    }

    private RenderType movingRenderType(final ChunkSectionLayer layer)
    {
        return switch (layer)
        {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        };
    }

    private void submitEntities(
        final Minecraft minecraft,
        final SubmitNodeCollector collector,
        final PoseStack poseStack,
        final long gameTime,
        final float partialTicks,
        final Map<Object, Exception> suppressedExceptions)
    {
        final EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        for (final Entity entity : entities)
        {
            if (gameTime != lastGameTime && entity.getType().builtInRegistryHolder().is(ModTags.PREVIEW_TICKING_ENTITIES))
            {
                try
                {
                    entity.tick();
                }
                catch (final Exception exception)
                {
                    suppressedExceptions.put(entity, exception);
                }
            }

            try
            {
                final EntityRenderState state = dispatcher.extractEntity(entity, partialTicks);
                dispatcher.submit(state, cameraState(minecraft), entity.getX(), entity.getY(), entity.getZ(), poseStack, collector);
            }
            catch (final ClassCastException | ReportedException exception)
            {
                suppressedExceptions.put(entity, exception);
            }
        }
    }

    private void submitBlockEntities(
        final Minecraft minecraft,
        final SubmitNodeCollector collector,
        final PoseStack poseStack,
        final BlockPos anchorPos,
        final long gameTime,
        final float partialTicks)
    {
        final BlockEntityRenderDispatcher dispatcher = minecraft.getBlockEntityRenderDispatcher();
        dispatcher.prepare(Vec3.ZERO);
        for (final BlockEntity tileEntity : tileEntities)
        {
            tickPreviewBlockEntity(minecraft, anchorPos, tileEntity, gameTime);
            final BlockEntityRenderState state = dispatcher.tryExtractRenderState(tileEntity, partialTicks, null, false);
            if (state == null)
            {
                continue;
            }

            final BlockPos tePos = tileEntity.getBlockPos();
            poseStack.pushPose();
            poseStack.translate(tePos.getX(), tePos.getY(), tePos.getZ());
            dispatcher.submit(state, poseStack, collector, cameraState(minecraft));
            poseStack.popPose();
        }
    }

    private CameraRenderState cameraState(final Minecraft minecraft)
    {
        final Camera camera = minecraft.gameRenderer.mainCamera();
        final CameraRenderState state = new CameraRenderState();
        state.initialized = true;
        state.pos = camera.position();
        state.blockPos = camera.blockPosition();
        state.xRot = camera.xRot();
        state.yRot = camera.yRot();
        state.orientation.set(camera.rotation());
        return state;
    }

    private void tickPreviewBlockEntity(
        final Minecraft minecraft,
        final BlockPos anchorPos,
        final BlockEntity tileEntity,
        final long gameTime)
    {
        if (gameTime == lastGameTime)
        {
            return;
        }

        final BlockPos tePos = tileEntity.getBlockPos();
        final BlockState blockState = blockAccess.getBlockState(tePos);
        if (tileEntity instanceof final SpawnerBlockEntity spawner)
        {
            SpawnerBlockEntity.clientTick(minecraft.level, anchorPos.offset(tePos), blockState, spawner);
        }
        else if (tileEntity instanceof final EnchantingTableBlockEntity enchantingTable)
        {
            EnchantingTableBlockEntity.bookAnimationTick(minecraft.level, anchorPos.offset(tePos), blockState, enchantingTable);
        }
        else if (tileEntity instanceof final CampfireBlockEntity campfire
            && blockState.getBlock() instanceof CampfireBlock
            && blockState.getValue(CampfireBlock.LIT))
        {
            CampfireBlockEntity.particleTick(minecraft.level, anchorPos.offset(tePos), blockState, campfire);
        }
        else if (tileEntity instanceof final SkullBlockEntity skull
            && blockState.getBlock() instanceof SkullBlock
            && (blockState.is(Blocks.DRAGON_HEAD) || blockState.is(Blocks.DRAGON_WALL_HEAD)))
        {
            SkullBlockEntity.animation(blockAccess, tePos, blockState, skull);
        }
        else if (tileEntity instanceof final BeaconBlockEntity beacon)
        {
            BeaconBlockEntity.tick(blockAccess, tePos, blockState, beacon);
        }
    }

    private void clearCachedState()
    {
        entities.clear();
        tileEntities.clear();
        blockStates.clear();
        fluidInstances.clear();
    }

    @Override
    public void close()
    {
        clearCachedState();
    }

    private record FluidInstance(BlockPos pos, BlockState state, FluidState fluidState)
    {
    }

    private record PoseVertexConsumer(PoseStack.Pose pose, VertexConsumer delegate) implements VertexConsumer
    {
        @Override
        public VertexConsumer addVertex(final float x, final float y, final float z)
        {
            delegate.addVertex(pose.pose(), x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(final int red, final int green, final int blue, final int alpha)
        {
            delegate.setColor(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer setColor(final int color)
        {
            delegate.setColor(color);
            return this;
        }

        @Override
        public VertexConsumer setUv(final float u, final float v)
        {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(final int u, final int v)
        {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(final int u, final int v)
        {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(final float x, final float y, final float z)
        {
            delegate.setNormal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(final float width)
        {
            delegate.setLineWidth(width);
            return this;
        }
    }

    private final class BlueprintBlockTintGetter implements BlockAndTintGetter
    {
        @Override
        public net.minecraft.world.level.CardinalLighting cardinalLighting()
        {
            return Minecraft.getInstance().level.cardinalLighting();
        }

        @Override
        public net.minecraft.world.level.lighting.LevelLightEngine getLightEngine()
        {
            return blockAccess.getLightEngine();
        }

        @Override
        public int getBlockTint(final BlockPos pos, final ColorResolver color)
        {
            final ClientLevel level = Minecraft.getInstance().level;
            return level == null ? -1 : color.getColor(
                level.getBiome(blockAccess.getWorldPos().offset(pos)).value(), pos.getX(), pos.getZ());
        }

        @Override
        public BlockEntity getBlockEntity(final BlockPos pos)
        {
            return blockAccess.getBlockEntity(pos);
        }

        @Override
        public BlockState getBlockState(final BlockPos pos)
        {
            return blockAccess.getBlockState(pos);
        }

        @Override
        public FluidState getFluidState(final BlockPos pos)
        {
            return blockAccess.getFluidState(pos);
        }

        @Override
        public int getHeight()
        {
            return blockAccess.getHeight();
        }

        @Override
        public int getMinY()
        {
            return blockAccess.getMinY();
        }

        @Override
        public ModelData getModelData(final BlockPos pos)
        {
            return ModelData.EMPTY;
        }
    }
}
