package com.ldtteam.structurize.client;

import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.phys.Vec3;

public class TagSubstitutionRenderer implements BlockEntityRenderer<BlockEntityTagSubstitution, TagSubstitutionRenderer.State>
{
    private static TagSubstitutionRenderer instance;

    public static TagSubstitutionRenderer getInstance()
    {
        return instance;
    }

    private final BlockEntityRendererProvider.Context context;

    public TagSubstitutionRenderer(@NotNull final BlockEntityRendererProvider.Context context)
    {
        instance = this;
        this.context = context;
    }

    @Override
    public State createRenderState()
    {
        return new State();
    }

    @Override
    public void extractRenderState(@NotNull final BlockEntityTagSubstitution entity,
        @NotNull final State state,
        final float partialTick,
        @NotNull final Vec3 cameraPosition,
        final ModelFeatureRenderer.CrumblingOverlay breakProgress)
    {
        BlockEntityRenderState.extractBase(entity, state, breakProgress);
        state.partialTick = partialTick;
        state.replacement = entity.getReplacement();
        state.tilePos = entity.getTilePos();
    }

    @Override
    public void submit(@NotNull final State state,
        @NotNull final PoseStack poseStack,
        @NotNull final SubmitNodeCollector collector,
        @NotNull final CameraRenderState camera)
    {
        if (state.replacement == null || state.tilePos == null || state.replacement.isEmpty())
        {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(0.98F, 0.98F, 0.98F);
        poseStack.translate(0.01F, 0.01F, 0.01F);

        final BlockEntity replacementEntity = state.replacement.getBlockEntity(state.tilePos);
        if (replacementEntity == null)
        {
            submitBlockModel(
                state.replacement.getBlockState(),
                state.lightCoords,
                net.minecraft.client.renderer.rendertype.RenderTypes.translucentMovingBlock(),
                poseStack,
                collector);
        }
        else
        {
            final BlockEntityRenderState nestedState = context.blockEntityRenderDispatcher()
                .tryExtractRenderState(replacementEntity, state.partialTick, null, false);
            if (nestedState != null)
            {
                context.blockEntityRenderDispatcher().submit(nestedState, poseStack, collector, camera);
            }
            else
            {
                submitBlockModel(
                    state.replacement.getBlockState(),
                    state.lightCoords,
                    net.minecraft.client.renderer.rendertype.RenderTypes.translucentMovingBlock(),
                    poseStack,
                    collector);
            }
        }

        poseStack.popPose();
    }

    private static void submitBlockModel(final BlockState blockState,
        final int packedLight,
        @NotNull final RenderType renderType,
        @NotNull final PoseStack poseStack,
        @NotNull final SubmitNodeCollector collector)
    {
        if (blockState.getRenderShape() != RenderShape.MODEL)
        {
            return;
        }

        final BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
        final List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, blockState, RandomSource.create(42L), parts);
        collector.submitBlockModel(poseStack, renderType, parts, new int[0], packedLight, 0, 0);
    }

    public static class State extends BlockEntityRenderState
    {
        private float partialTick;
        private BlockEntityTagSubstitution.ReplacementBlock replacement;
        private BlockPos tilePos;
    }
}
