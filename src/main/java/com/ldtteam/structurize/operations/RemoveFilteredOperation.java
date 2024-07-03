package com.ldtteam.structurize.operations;

import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Operation for removing multiple types of blocks.
 */
public class RemoveFilteredOperation extends AreaOperation
{
    /**
     * What type of blocks to remove.
     */
    private final List<ItemStack> blocks;

    /**
     * Default constructor.
     *
     * @param startPos the start pos to iterate from.
     * @param endPos   the end pos to iterate to.
     * @param blocks   what type of blocks to remove.
     */
    public RemoveFilteredOperation(final Player player, final BlockPos startPos, final BlockPos endPos, final List<ItemStack> blocks)
    {
        super(Component.translatable("com.ldtteam.structurize.remove_blocks"), player, startPos, endPos);
        this.blocks = blocks;
    }

    @Override
    protected void apply(final ServerLevel world, final BlockPos position)
    {
        for (final ItemStack block : blocks)
        {
            if (BlockUtils.doBlocksMatch(block, world, position))
            {
                storage.addPreviousDataFor(position, world);
                world.removeBlock(position, false);
                storage.addPostDataFor(position, world);
            }
        }
    }
}
