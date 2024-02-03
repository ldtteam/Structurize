package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.client.gui.WindowUndoRedo;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.ChangeStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class OperationHistoryMessage extends AbstractPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forBothSides(Constants.MOD_ID, "operation_history", OperationHistoryMessage::new);

    /**
     * List of operations and their IDs
     */
    private List<Tuple<String, Integer>> operationIDs = new ArrayList<>();

    /**
     * Empty constructor used when registering the
     */
    public OperationHistoryMessage(final FriendlyByteBuf buf)
    {
        super(buf, TYPE);
        final int count = buf.readInt();
        operationIDs = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            operationIDs.add(new Tuple<>(buf.readUtf(), buf.readInt()));
        }
    }

    public OperationHistoryMessage()
    {
        super(TYPE);
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(operationIDs.size());
        for (final Tuple<String, Integer> operation : operationIDs)
        {
            buf.writeUtf(operation.getA());
            buf.writeInt(operation.getB());
        }
    }

    @Override
    protected void onClientExecute(final PlayPayloadContext context, final Player player)
    {
        WindowUndoRedo.lastOperations = operationIDs;
    }

    @Override
    protected void onServerExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        final List<ChangeStorage> operations = Manager.getChangeStoragesForPlayer(player.getUUID());
        operationIDs = new ArrayList<>();
        for (final ChangeStorage storage : operations)
        {
            operationIDs.add(new Tuple<>(storage.getOperation().getString(), storage.getID()));
        }

        this.sendToPlayer(player);        
    }
}
