package com.ldtteam.structurize.client.gui.util;

import com.ldtteam.structurize.api.util.ItemStorage;
import com.ldtteam.structurize.api.util.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data storage for holding all positions for a given itemstorage.
 */
public class ItemPositionsStorage
{
    public final ItemStorage    itemStorage;
    public final List<BlockPos> positions = new ArrayList<>();

    public ItemPositionsStorage(final ItemStorage itemStorage)
    {
        this.itemStorage = itemStorage;
    }

    public ItemPositionsStorage(final FriendlyByteBuf buf)
    {
        itemStorage = new ItemStorage(buf);
        final int count = buf.readVarInt();

        for (int i = 0; i < count; i++)
        {
            positions.add(new BlockPos(buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));
        }
    }

    public void addPos(final BlockPos pos)
    {
        positions.add(pos);
    }

    public void addItemAndPos(final ItemStorage toAdd, final BlockPos pos)
    {
        if (!toAdd.equals(itemStorage))
        {
            Log.getLogger().warn("Tried to add a different item to the item storage!", new Exception());
            return;
        }

        positions.add(pos);
        itemStorage.setAmount(positions.size());
    }

    public void removePos(final BlockPos pos)
    {
        positions.remove(pos);
    }

    public boolean isEmpty()
    {
        return positions.isEmpty();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof final ItemPositionsStorage that))
        {
            return false;
        }
        return Objects.equals(itemStorage, that.itemStorage);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(itemStorage);
    }

    /**
     * Serialize to buffer
     *
     * @param buf
     */
    public void serialize(final FriendlyByteBuf buf)
    {
        itemStorage.serialize(buf);
        buf.writeVarInt(positions.size());
        for (final BlockPos pos : positions)
        {
            buf.writeVarInt(pos.getX());
            buf.writeVarInt(pos.getY());
            buf.writeVarInt(pos.getZ());
        }
    }
}
