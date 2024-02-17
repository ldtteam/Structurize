package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
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
    private final List<Tuple<String, Integer>> operationIDs;

    /**
     * Empty constructor used when registering the
     */
    protected OperationHistoryMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        operationIDs = buf.readList(b -> new Tuple<>(b.readUtf(), b.readInt()));
    }

    public OperationHistoryMessage()
    {
        super(TYPE);
        operationIDs = new ArrayList<>();
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeCollection(operationIDs, (b, operation) -> {
            b.writeUtf(operation.getA());
            b.writeInt(operation.getB());
        });
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
        for (final ChangeStorage storage : operations)
        {
            operationIDs.add(new Tuple<>(storage.getOperation().getString(), storage.getID()));
        }

        this.sendToPlayer(player);        
    }
}
