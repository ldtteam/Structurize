package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.ItemStackUtils;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.client.gui.util.ItemPositionsStorage;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.operations.ReplaceBlockOperation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Message to replace a block from the world with another one.
 */
public class ReplaceBlockMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "replace_block", ReplaceBlockMessage::new);

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
    protected ReplaceBlockMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.blockTo = ItemStackUtils.deserializeFromBuffer(buf);
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
        super(TYPE);
        this.toReplace = toReplace;
        this.blockTo = blockTo;
        this.pct = pct;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        ItemStackUtils.serializeToBuffer(blockTo, buf);
        buf.writeInt(pct);
        toReplace.serialize(buf);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        if (!player.isCreative())
        {
            return;
        }

        Manager.addToQueue(new ReplaceBlockOperation(player, toReplace, blockTo, pct));
    }
}
