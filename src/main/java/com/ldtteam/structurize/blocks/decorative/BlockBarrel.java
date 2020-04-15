package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import com.ldtteam.structurize.blocks.cactus.BlockCactusPlank;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ResourceLocation;

import static com.ldtteam.structurize.blocks.decorative.BlockTimberFrame.FACING;
import static net.minecraft.block.FourWayBlock.NORTH;
import static net.minecraft.block.FourWayBlock.WATERLOGGED;

public class BlockBarrel extends AbstractBlockStructurize<BlockBarrel>
{
    public BlockBarrel()
    {
        super(Block.Properties.from(Blocks.OAK_PLANKS).hardnessAndResistance(3f, 1f));
        this.setRegistryName("blockbarreldeco_onside");
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }
}
