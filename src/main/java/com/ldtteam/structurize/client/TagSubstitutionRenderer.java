package com.ldtteam.structurize.client;

import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.items.ItemTagSubstitution;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

/**
 * Tag anchor renderer; renders replacement block model inside of anchor "overlay" model.
 */
public class TagSubstitutionRenderer extends BlockEntityWithoutLevelRenderer implements BlockEntityRenderer<BlockEntityTagSubstitution>
{
    private static TagSubstitutionRenderer INSTANCE;

    public static TagSubstitutionRenderer getInstance()
    {
        return INSTANCE;
    }


    private final BlockEntityRendererProvider.Context context;

    public TagSubstitutionRenderer(@NotNull final BlockEntityRendererProvider.Context context)
    {
        super(context.getBlockEntityRenderDispatcher(), context.getModelSet());

        INSTANCE = this;
        this.context = context;
    }

    @Override
    public void render(@NotNull final BlockEntityTagSubstitution entity,
                       final float partialTick,
                       @NotNull final PoseStack poseStack,
                       @NotNull final MultiBufferSource buffers,
                       final int packedLight,
                       final int packedOverlay)
    {
        final RenderType renderType = ForgeRenderTypes.ITEM_LAYERED_TRANSLUCENT.get();

        render(entity.getReplacement(), entity.getTilePos(), partialTick, poseStack, buffers, packedLight, packedOverlay, renderType);
    }

    @Override
    public void renderByItem(@NotNull final ItemStack stack,
                             @NotNull final ItemDisplayContext transformType,
                             @NotNull final PoseStack poseStack,
                             @NotNull final MultiBufferSource buffers,
                             final int packedLight,
                             final int packedOverlay)
    {
        final RenderType renderType = ForgeRenderTypes.ITEM_LAYERED_TRANSLUCENT.get();

        if (stack.getItem() instanceof ItemTagSubstitution anchor)
        {
            this.context.getBlockRenderDispatcher().renderSingleBlock(anchor.getBlock().defaultBlockState(),
                    poseStack, buffers, packedLight, packedOverlay, ModelData.EMPTY, renderType);

            render(anchor.getAbsorbedBlock(stack), BlockPos.ZERO, 0, poseStack, buffers, packedLight, packedOverlay, renderType);
        }
    }

    private void render(@NotNull final BlockEntityTagSubstitution.ReplacementBlock replacement,
                        @NotNull final BlockPos pos,
                        final float partialTick,
                        @NotNull PoseStack poseStack,
                        @NotNull MultiBufferSource buffers,
                        final int packedLight,
                        final int packedOverlay,
                        @NotNull final RenderType renderType)
    {
        if (!replacement.isEmpty())
        {
            poseStack.pushPose();
            poseStack.scale(0.98f,0.98f,0.98f);
            poseStack.translate(0.01f, 0.01f, 0.01f);

            final BlockRenderDispatcher dispatcher = this.context.getBlockRenderDispatcher();

            final BlockEntity replacementEntity = replacement.getBlockEntity(pos);
            if (replacementEntity != null)
            {
                // seems a little silly to create a blueprint, but the entityDispatcher won't render without a level...
                final Blueprint blueprint = replacement.createBlueprint();
                final BlueprintBlockAccess blockAccess = new BlueprintBlockAccess(blueprint);
                replacementEntity.setLevel(blockAccess);

                final BlockEntityRenderDispatcher entityDispatcher = this.context.getBlockEntityRenderDispatcher();
                if (replacement.getBlockState().getRenderShape() == RenderShape.MODEL)
                {
                    dispatcher.renderSingleBlock(replacement.getBlockState(), poseStack, buffers, packedLight, packedOverlay, replacementEntity.getModelData(), renderType);
                }
                entityDispatcher.render(replacementEntity, partialTick, poseStack, buffers);
            }
            else
            {
                dispatcher.renderSingleBlock(replacement.getBlockState(), poseStack, buffers, packedLight, packedOverlay, ModelData.EMPTY, renderType);
            }

            poseStack.popPose();
        }
    }
}
