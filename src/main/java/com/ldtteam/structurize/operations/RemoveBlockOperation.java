package com.ldtteam.structurize.operations;

import com.ldtteam.structurize.client.gui.util.ItemPositionsStorage;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/**
 * Operation for removing one type of block.
 */
public class RemoveBlockOperation extends AreaOperation
{
    /**
     * What type of block to remove.
     */
    private final ItemPositionsStorage toRemove;

    /**
     * Default constructor.
     */
    public RemoveBlockOperation(final ServerPlayer player, final ItemPositionsStorage toRemove)
    {
        super(Component.translatable("com.ldtteam.structurize.remove_block", toRemove.itemStorage.getItemStack().getDisplayName()), player, toRemove.positions);
        this.toRemove = toRemove;
    }

    @Override
    protected void apply(final ServerLevel world, final BlockPos position)
    {
        if (BlockUtils.doBlocksMatch(toRemove.itemStorage.getItemStack(), world, position))
        {
            storage.addPreviousDataFor(position, world);
            world.removeBlock(position, false);
            if (!world.getFluidState(position).isEmpty())
            {
                world.setBlock(position, Blocks.AIR.defaultBlockState(), 3);
            }
            storage.addPostDataFor(position, world);
        }
    }
}
