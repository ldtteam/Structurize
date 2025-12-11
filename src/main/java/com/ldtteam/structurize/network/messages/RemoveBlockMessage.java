package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.client.gui.util.ItemPositionsStorage;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.operations.RemoveBlockOperation;
import com.ldtteam.structurize.operations.RemoveFilteredOperation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Message to remove a block from the world.
 */
public class RemoveBlockMessage implements IMessage
{
    /**
     * The list of items to remove and their positions
     */
    private List<ItemPositionsStorage> toRemove = new ArrayList<>();

    /**
     * Empty constructor used when registering the message.
     */
    public RemoveBlockMessage(final FriendlyByteBuf buf)
    {
        final int count = buf.readInt();

        for (int i = 0; i < count; i++)
        {
            toRemove.add(new ItemPositionsStorage(buf));
        }
    }

    /**
     * Create a message to remove a block from the world.
     */
    public RemoveBlockMessage(final ItemPositionsStorage itemPositionsStorage)
    {
        toRemove = List.of(itemPositionsStorage);
    }

    /**
     * Create a message to remove a block from the world.
     */
    public RemoveBlockMessage(final List<ItemPositionsStorage> itemPositionsStorageList)
    {
        toRemove = itemPositionsStorageList;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(toRemove.size());
        for (final ItemPositionsStorage positionsStorage : toRemove)
        {
            positionsStorage.serialize(buf);
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (!ctxIn.getSender().isCreative())
        {
            return;
        }

        if (toRemove.size() > 1)
        {
            Manager.addToQueue(new RemoveFilteredOperation(ctxIn.getSender(), toRemove));
            return;
        }

        if (!toRemove.isEmpty())
        {
            Manager.addToQueue(new RemoveBlockOperation(ctxIn.getSender(), toRemove.get(0)));
        }
    }
}
