package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.ScanToolOperation;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to remove a block from the world.
 */
public class RemoveBlockMessage implements IMessage
{
    /**
     * Position to scan from.
     */
    private BlockPos from;

    /**
     * Position to scan to.
     */
    private BlockPos to;

    /**
     * The block to remove from the world.
     */
    private ItemStack block;

    /**
     * Empty constructor used when registering the message.
     */
    public RemoveBlockMessage()
    {
        super();
    }

    /**
     * Create a message to remove a block from the world.
     * @param pos1 start coordinate.
     * @param pos2 end coordinate.
     * @param stack the block to remove.
     */
    public RemoveBlockMessage(@NotNull final BlockPos pos1, @NotNull final BlockPos pos2, @NotNull final ItemStack stack)
    {
        super();
        this.from = pos1;
        this.to = pos2;
        this.block = stack;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        from = buf.readBlockPos();
        to = buf.readBlockPos();
        block = buf.readItemStack();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeItemStack(block);
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
        Manager.addToQueue(new ScanToolOperation(ScanToolOperation.OperationType.REMOVE_BLOCK, from, to, ctxIn.getSender(), block, ItemStack.EMPTY));
    }
}
