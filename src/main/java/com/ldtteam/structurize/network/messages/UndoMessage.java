package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message class which handles undoing a change to the world.
 */
public class UndoMessage implements IMessage
{
    /**
     * Empty public constructor.
     */
    public UndoMessage()
    {
    }

    public UndoMessage(final FriendlyByteBuf buf)
    {
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        /*
         * Nothing needed.
         */
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

        Manager.undo(ctxIn.getSender());
    }
}
