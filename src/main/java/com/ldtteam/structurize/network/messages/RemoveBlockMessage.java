package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.TickedWorldOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

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
     * The block to remove from the world.
     */
    private final ItemStack block;

    /**
     * Empty constructor used when registering the message.
     */
    protected RemoveBlockMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
        this.block = buf.readItem();
    }

    /**
     * Create a message to remove a block from the world.
     * @param pos1 start coordinate.
     * @param pos2 end coordinate.
     * @param stack the block to remove.
     */
    public RemoveBlockMessage(final BlockPos pos1, final BlockPos pos2, final ItemStack stack)
    {
        super(TYPE);
        this.from = pos1;
        this.to = pos2;
        this.block = stack;
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeItem(block);
    }

    @Override
    protected void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        if (!player.isCreative())
        {
            return;
        }
        Manager.addToQueue(new TickedWorldOperation(TickedWorldOperation.OperationType.REMOVE_BLOCK, from, to, player, block, ItemStack.EMPTY, 100));
    }
}
