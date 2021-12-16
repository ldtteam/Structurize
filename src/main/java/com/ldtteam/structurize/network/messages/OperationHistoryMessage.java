package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.client.gui.WindowUndoRedo;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.ChangeStorage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OperationHistoryMessage implements IMessage
{
    /**
     * List of operations and their IDs
     */
    private List<Tuple<String, Integer>> operationIDs = new ArrayList<>();

    /**
     * Empty constructor used when registering the
     */
    public OperationHistoryMessage(final PacketBuffer buf)
    {
        final int count = buf.readInt();
        operationIDs = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            operationIDs.add(new Tuple<>(buf.readUtf(), buf.readInt()));
        }
    }

    public OperationHistoryMessage()
    {

    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(operationIDs.size());
        for (final Tuple<String, Integer> operation : operationIDs)
        {
            buf.writeUtf(operation.getA());
            buf.writeInt(operation.getB());
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return null;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (isLogicalServer)
        {
            if (ctxIn.getSender() == null)
            {
                return;
            }

            final List<ChangeStorage> operations = Manager.getChangeStoragesForPlayer(ctxIn.getSender().getUUID());
            operationIDs = new ArrayList<>();
            for (final ChangeStorage storage : operations)
            {
                operationIDs.add(new Tuple<>(storage.getOperation(), storage.getID()));
            }

            Network.getNetwork().sendToPlayer(this, ctxIn.getSender());
        }
        else
        {
            WindowUndoRedo.lastOperations = operationIDs;
        }
    }
}
