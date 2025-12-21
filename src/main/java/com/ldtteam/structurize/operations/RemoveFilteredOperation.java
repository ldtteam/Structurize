package com.ldtteam.structurize.operations;

import com.ldtteam.structurize.client.gui.util.ItemPositionsStorage;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Operation for removing multiple types of blocks.
 */
public class RemoveFilteredOperation extends AreaOperation
{
    /**
     * What type of blocks to remove.
     */
    private final List<ItemPositionsStorage> toRemove;

    /**
     * Default constructor.
     */
    public RemoveFilteredOperation(final ServerPlayer player, final List<ItemPositionsStorage> toRemove)
    {
        super(Component.translatable("com.ldtteam.structurize.remove_blocks"), player, consolidatePositions(toRemove));
        this.toRemove = toRemove;
    }

    /**
     * Combines all positions into a list
     * @param toRemove
     * @return
     */
    private static List<BlockPos> consolidatePositions(final List<ItemPositionsStorage> toRemove)
    {
        Set<BlockPos> positions = new HashSet<>();
        for (final ItemPositionsStorage itemPositionsStorage : toRemove)
        {
            positions.addAll(itemPositionsStorage.positions);
        }
        return new ArrayList<>(positions);
    }

    @Override
    protected void apply(final ServerLevel world, final BlockPos position)
    {
        for (final ItemPositionsStorage itemPositionsStorage : toRemove)
        {
            if (BlockUtils.doBlocksMatch(itemPositionsStorage.itemStorage.getItemStack(), world, position))
            {
                storage.addPreviousDataFor(position, world);
                world.removeBlock(position, false);
                storage.addPostDataFor(position, world);
            }
        }
    }
}
