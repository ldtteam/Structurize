package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.operations.RemoveBlockOperation;
import com.ldtteam.structurize.operations.RemoveFilteredOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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
    protected RemoveBlockMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
        this.blocks = new ArrayList<>();
        final int blockCount = buf.readInt();
        for (int i = 0; i < blockCount; i++)
        {
            this.blocks.add(ItemStack.STREAM_CODEC.decode(buf));
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
        super(TYPE);
        this.from = pos1;
        this.to = pos2;
        this.blocks = stacks;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeInt(blocks.size());
        for (final ItemStack block : blocks)
        {
            ItemStack.STREAM_CODEC.encode(buf, block);
        }
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        if (!player.isCreative())
        {
            return;
        }

        if (blocks.size() > 1)
        {
            Manager.addToQueue(new RemoveFilteredOperation(player, from, to, blocks));
            return;
        }

        if (!blocks.isEmpty())
        {
            Manager.addToQueue(new RemoveBlockOperation(player, from, to, blocks.get(0)));
        }
    }
}
