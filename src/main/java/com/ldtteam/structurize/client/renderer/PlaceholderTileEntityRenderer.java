package com.ldtteam.structurize.client.renderer;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.PlaceholderBlock;
import com.ldtteam.structurize.tileentities.TileEntityPlaceholder;
import com.ldtteam.structurize.util.BlockUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Class to render the scarecrow.
 */
@OnlyIn(Dist.CLIENT)
public class PlaceholderTileEntityRenderer extends TileEntityRenderer<TileEntityPlaceholder>
{
    /**
     * The state to render.
     */
    public BlockState state = ModBlocks.placeholderBlock.getDefaultState();

    /**
     * The itemstack to render.
     */
    public ItemStack stack = new ItemStack(state.getBlock(), 1);

    /**
     * Random obj.
     */
    private final Random random = new Random();

    /**
     * The public constructor for the renderer.
     */
    public PlaceholderTileEntityRenderer(final TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public void render(final TileEntityPlaceholder te, final float partialTicks, final MatrixStack matrixStack, @NotNull final IRenderTypeBuffer iRenderTypeBuffer, final int lightA, final int lightB)
    {
        //Store the transformation
        matrixStack.push();

        //In the case of worldLags tileEntities may sometimes disappear.
        if (te.getWorld().getBlockState(te.getPos()).getBlock() instanceof PlaceholderBlock)
        {
            final Direction facing = te.getWorld().getBlockState(te.getPos()).get(PlaceholderBlock.HORIZONTAL_FACING);
            if (this.state.hasProperty(PlaceholderBlock.HORIZONTAL_FACING))
            {
                if (this.state.get(PlaceholderBlock.HORIZONTAL_FACING) != facing)
                {
                    this.state = this.state.with(PlaceholderBlock.HORIZONTAL_FACING, facing);
                }

            }
            matrixStack.translate(0.5, 0.5, 0.5);
            matrixStack.translate(-0.5, -0.5, -0.5);

            if (!stack.isItemEqual(te.getStack()))
            {
                stack = te.getStack();
                this.state = BlockUtils.getBlockStateFromStack(stack, Blocks.GOLD_BLOCK.getDefaultState());
                if (this.state.hasProperty(PlaceholderBlock.HORIZONTAL_FACING))
                {
                    this.state = this.state.with(PlaceholderBlock.HORIZONTAL_FACING, facing);
                }
            }
        }

        if (state.getBlock() != ModBlocks.placeholderBlock)
        {
            final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

            for (final RenderType layer : RenderType.getBlockRenderTypes())
            {
                if (RenderTypeLookup.canRenderInLayer(state, layer))
                {
                    blockRendererDispatcher.renderModel(state,
                      te.getPos(),
                      te.getWorld(),
                      matrixStack,
                      iRenderTypeBuffer.getBuffer(layer),
                      false,
                      random,
                      EmptyModelData.INSTANCE);
                }
            }
        }
        matrixStack.pop();
    }
}
