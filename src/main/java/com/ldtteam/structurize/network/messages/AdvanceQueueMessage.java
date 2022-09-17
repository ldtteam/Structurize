package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.TickedWorldOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message to advance the queue manually.
 */
public class AdvanceQueueMessage implements IMessage
{
    /**
     * Empty constructor used when registering the message.
     */
    public AdvanceQueueMessage(final FriendlyByteBuf buf)
    {
        super();
    }

    /**
     * Create a message to advance the queue.

     */
    public AdvanceQueueMessage()
    {
        super();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {

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
        TickedWorldOperation.next = true;
    }
}
