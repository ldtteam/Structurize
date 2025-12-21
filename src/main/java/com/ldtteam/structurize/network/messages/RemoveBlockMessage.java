package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.client.gui.util.ItemPositionsStorage;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.operations.RemoveBlockOperation;
import com.ldtteam.structurize.operations.RemoveFilteredOperation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Message to remove a block from the world.
 */
public class RemoveBlockMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "remove_block", RemoveBlockMessage::new);

    /**
     * The list of items to remove and their positions
     */
    private List<ItemPositionsStorage> toRemove = new ArrayList<>();

    /**
     * Empty constructor used when registering the message.
     */
    protected RemoveBlockMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
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
        super(TYPE);
        toRemove = List.of(itemPositionsStorage);
    }

    /**
     * Create a message to remove a block from the world.
     */
    public RemoveBlockMessage(final List<ItemPositionsStorage> itemPositionsStorageList)
    {
        super(TYPE);
        toRemove = itemPositionsStorageList;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(toRemove.size());
        for (final ItemPositionsStorage positionsStorage : toRemove)
        {
            positionsStorage.serialize(buf);
        }
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        if (!player.isCreative())
        {
            return;
        }

        if (toRemove.size() > 1)
        {
            Manager.addToQueue(new RemoveFilteredOperation(player, toRemove));
            return;
        }

        if (!toRemove.isEmpty())
        {
            Manager.addToQueue(new RemoveBlockOperation(player, toRemove.get(0)));
        }
    }
}
