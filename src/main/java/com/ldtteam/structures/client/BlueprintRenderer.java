package com.ldtteam.structures.client;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.FluidRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * The renderer for blueprint.
 * Holds all information required to render a blueprint.
 */
public class BlueprintRenderer
{
    private final BlueprintBlockAccess blockAccess;

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

    private BlueprintRenderer(
      final BlueprintBlockAccess blockAccess)
    {
        this.blockAccess = blockAccess;
    }

    /**
     * Sets up the renders VBO
     *
     * @param pos the pos to render it at.
     */
    public void draw(final BlockPos pos, final MatrixStack matrixStack, final float partialTicks)
    {
        final Vec3d viewPosition = Minecraft.getInstance().getRenderManager().info.getProjectedView();
        final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        final BlockPos primaryBlockOffset = BlueprintUtils.getPrimaryBlockOffset(blockAccess.getBlueprint());
        int x = pos.getX() - primaryBlockOffset.getX();
        int y = pos.getY() - primaryBlockOffset.getY();
        int z = pos.getZ() - primaryBlockOffset.getZ();

        final Random random = new Random();

        final IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        for (BlockInfo blockInfo : blockAccess.getBlueprint().getBlockInfoAsList())
        {
            matrixStack.push();
            matrixStack.translate(x-viewPosition.getX(), y-viewPosition.getY(), z-viewPosition.getZ());
            final Matrix4f model = matrixStack.getLast().getPositionMatrix();
            BlockState state = blockInfo.getState();

            final BlockPos blockPos = blockInfo.getPos();
            matrixStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());

            blockrendererdispatcher.renderModel(state,
              blockPos,
              blockAccess,
              matrixStack,
              buffer.getBuffer(RenderTypeLookup.getRenderType(state)),
              true,
              random);
            matrixStack.translate(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());
            matrixStack.pop();

            matrixStack.push();
            final IFluidState fluidState = state.getFluidState();
            if (!fluidState.isEmpty())
            {
                FluidRenderer.render(model, blockAccess, blockPos, buffer.getBuffer(RenderTypeLookup.getRenderType(fluidState)), fluidState);
            }
            matrixStack.pop();
        }

        buffer.finish();

        BlueprintUtils.instantiateEntities(blockAccess.getBlueprint(), blockAccess).forEach(entity -> {
            double cleanX = MathHelper.lerp(partialTicks, entity.lastTickPosX, entity.getPosX());
            double cleanY = MathHelper.lerp(partialTicks, entity.lastTickPosY, entity.getPosY());
            double cleanZ = MathHelper.lerp(partialTicks, entity.lastTickPosZ, entity.getPosZ());
            float rot = MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw);
            Minecraft.getInstance().getRenderManager().renderEntityStatic(entity, cleanX+x-viewPosition.getX(), cleanY+y-viewPosition.getY() ,cleanZ+z-viewPosition.getZ(), rot, 0, matrixStack, buffer, 200);
        });
    }
}
