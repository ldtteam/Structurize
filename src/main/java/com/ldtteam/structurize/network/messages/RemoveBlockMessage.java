package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.operations.RemoveBlockOperation;
import com.ldtteam.structurize.operations.RemoveFilteredOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
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
     * Position to scan from.
     */
    private final BlockPos from;

    /**
     * Position to scan to.
     */
    private final BlockPos to;

    /**
     * The blocks to remove from the world.
     */
    private final List<ItemStack> blocks;

    /**
     * Empty constructor used when registering the message.
     */
    public RemoveBlockMessage(final FriendlyByteBuf buf)
    {
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
        this.blocks = new ArrayList<>();
        final int blockCount = buf.readInt();
        for (int i = 0; i < blockCount; i++)
        {
            this.blocks.add(buf.readItem());
        }
    }

    /**
     * Create a message to remove a block from the world.
     *
     * @param pos1  start coordinate.
     * @param pos2  end coordinate.
     * @param stack the block to remove.
     */
    public RemoveBlockMessage(final BlockPos pos1, final BlockPos pos2, final ItemStack stack)
    {
        this(pos1, pos2, List.of(stack));
    }

    /**
     * Create a message to remove a block from the world.
     *
     * @param pos1   start coordinate.
     * @param pos2   end coordinate.
     * @param stacks the blocks to remove.
     */
    public RemoveBlockMessage(final BlockPos pos1, final BlockPos pos2, final List<ItemStack> stacks)
    {
        this.from = pos1;
        this.to = pos2;
        this.blocks = stacks;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeInt(blocks.size());
        for (final ItemStack block : blocks)
        {
            buf.writeItem(block);
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

        if (blocks.size() > 1)
        {
            Manager.addToQueue(new RemoveFilteredOperation(ctxIn.getSender(), from, to, blocks));
            return;
        }

        if (!blocks.isEmpty())
        {
            Manager.addToQueue(new RemoveBlockOperation(ctxIn.getSender(), from, to, blocks.get(0)));
        }
    }
}
