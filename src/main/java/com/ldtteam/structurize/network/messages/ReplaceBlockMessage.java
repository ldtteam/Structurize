package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.client.gui.util.ItemPositionsStorage;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.operations.ReplaceBlockOperation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message to replace a block from the world with another one.
 */
public class ReplaceBlockMessage implements IMessage
{
    /**
     *  The block to replace with its positions
     */
    private final ItemPositionsStorage toReplace;

    /**
     * The block to remove from the world.
     */
    private final ItemStack blockTo;

    /**
     * Pct of blocks to replace.
     */
    private final int pct;

    /**
     * Empty constructor used when registering the message.
     */
    public ReplaceBlockMessage(final FriendlyByteBuf buf)
    {
        this.blockTo = buf.readItem();
        this.pct = buf.readInt();
        toReplace = new ItemPositionsStorage(buf);
    }

    /**
     * Create a message to replace a block from the world.
     *
     * @param blockTo   the block to replace it with.
     */
    public ReplaceBlockMessage(final ItemPositionsStorage toReplace, final ItemStack blockTo, final int pct)
    {
        this.toReplace = toReplace;
        this.blockTo = blockTo;
        this.pct = pct;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeItem(blockTo);
        buf.writeInt(pct);
        toReplace.serialize(buf);
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

        Manager.addToQueue(new ReplaceBlockOperation(ctxIn.getSender(), toReplace, blockTo, pct));
    }
}
