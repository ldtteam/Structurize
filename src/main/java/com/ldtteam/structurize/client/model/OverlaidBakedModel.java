package com.ldtteam.structurize.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * This exists because it seems to be the only way to override {@link #isCustomRenderer}...
 */
public class OverlaidBakedModel extends BakedModelWrapper<BakedModel>
{
    public OverlaidBakedModel(@NotNull final BakedModel overlay)
    {
        super(overlay);
    }

    @Override
    public boolean isCustomRenderer()
    {
        return true;
    }

    @NotNull
    @Override
    public BakedModel applyTransform(@NotNull final ItemDisplayContext transformType,
                                     @NotNull final PoseStack poseStack,
                                     final boolean applyLeftHandTransform)
    {
        return new OverlaidBakedModel(originalModel.applyTransform(transformType, poseStack, applyLeftHandTransform));
    }
}
