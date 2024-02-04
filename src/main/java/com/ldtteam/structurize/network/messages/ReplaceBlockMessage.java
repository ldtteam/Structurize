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
 * Message to replace a block from the world with another one.
 */
public class ReplaceBlockMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "replace_block", ReplaceBlockMessage::new);

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
    private final ItemStack blockFrom;

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
        super(buf, TYPE);
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
        this.blockTo = buf.readItem();
        this.blockFrom = buf.readItem();
        this.pct = buf.readInt();
    }

    /**
     * Create a message to replace a block from the world.
     * @param pos1 start coordinate.
     * @param pos2 end coordinate.
     * @param blockFrom the block to replace.
     * @param blockTo the block to replace it with.
     */
    public ReplaceBlockMessage(final BlockPos pos1, final BlockPos pos2, final ItemStack blockFrom, final ItemStack blockTo, final int pct)
    {
        super(TYPE);
        this.from = pos1;
        this.to = pos2;
        this.blockFrom = blockFrom;
        this.blockTo = blockTo;
        this.pct = pct;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeItem(blockTo);
        buf.writeItem(blockFrom);
        buf.writeInt(pct);
    }

    @Override
    public void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        if (!player.isCreative())
        {
            return;
        }

        Manager.addToQueue(new TickedWorldOperation(TickedWorldOperation.OperationType.REPLACE_BLOCK, from, to, player, blockFrom, blockTo, pct));
    }
}
