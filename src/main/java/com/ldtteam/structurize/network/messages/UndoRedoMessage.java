package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message class which handles undoing a change to the world.
 */
public class UndoRedoMessage implements IMessage
{
    private final int     id;
    private final boolean undo;

    /**
     * Empty public constructor.
     */
    public UndoRedoMessage(final int id, final boolean undo)
    {
        this.undo = undo;
        this.id = id;
    }

    public UndoRedoMessage(final FriendlyByteBuf buf)
    {
        this.id = buf.readInt();
        this.undo = buf.readBoolean();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(id);
        buf.writeBoolean(undo);
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

        if (undo)
        {
            Manager.undo(ctxIn.getSender(), id);
        }
        else
        {
            Manager.redo(ctxIn.getSender(), id);
        }
    }
}
