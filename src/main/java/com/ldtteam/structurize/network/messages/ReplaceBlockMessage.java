package com.ldtteam.structurize.network.messages;

import com.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.util.ScanToolOperation;
import com.ldtteam.structurize.management.Manager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;


/**
 * Message to replace a block from the world with another one.
 */
public class ReplaceBlockMessage extends AbstractMessage<ReplaceBlockMessage, IMessage>
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
    private ItemStack blockFrom;

    /**
     * The block to remove from the world.
     */
    private ItemStack blockTo;

    /**
     * Empty constructor used when registering the message.
     */
    public ReplaceBlockMessage()
    {
        super();
    }

    /**
     * Create a message to replace a block from the world.
     * @param pos1 start coordinate.
     * @param pos2 end coordinate.
     * @param blockFrom the block to replace.
     * @param blockTo the block to replace it with.
     */
    public ReplaceBlockMessage(@NotNull final BlockPos pos1, @NotNull final BlockPos pos2, @NotNull final ItemStack blockFrom, @NotNull final ItemStack blockTo)
    {
        super();
        this.from = pos1;
        this.to = pos2;
        this.blockFrom = blockFrom;
        this.blockTo = blockTo;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        from = BlockPosUtil.readFromByteBuf(buf);
        to = BlockPosUtil.readFromByteBuf(buf);
        blockTo = ByteBufUtils.readItemStack(buf);
        blockFrom = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        BlockPosUtil.writeToByteBuf(buf, from);
        BlockPosUtil.writeToByteBuf(buf, to);
        ByteBufUtils.writeItemStack(buf, blockTo);
        ByteBufUtils.writeItemStack(buf, blockFrom);
    }

    @Override
    public void messageOnServerThread(final ReplaceBlockMessage message, final EntityPlayerMP player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            return;
        }

        Manager.addToQueue(new ScanToolOperation(ScanToolOperation.OperationType.REPLACE_BLOCK, message.from, message.to, player, message.blockFrom, message.blockTo));
    }
}
