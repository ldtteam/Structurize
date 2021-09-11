package com.ldtteam.structurize.blocks.schematic;

import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.ldtteam.structurize.tileentities.TileEntityTagSubstitution;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

/**
 * This block is a substitution block (it disappears on normal build) but stores blueprint
 * data (mostly tags) during scan.
 */
public class BlockTagSubstitution extends BlockSubstitution implements IAnchorBlock
{
    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TileEntityTagSubstitution();
    }
}
