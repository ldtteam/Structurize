package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.api.util.BlockPosUtil;
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
 * Message to remove a block from the world.
 */
public class RemoveBlockMessage extends AbstractMessage<RemoveBlockMessage, IMessage>
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
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        from = BlockPosUtil.readFromByteBuf(buf);
        to = BlockPosUtil.readFromByteBuf(buf);
        block = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        BlockPosUtil.writeToByteBuf(buf, from);
        BlockPosUtil.writeToByteBuf(buf, to);
        ByteBufUtils.writeItemStack(buf, block);
    }

    @Override
    public void messageOnServerThread(final RemoveBlockMessage message, final EntityPlayerMP player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            return;
        }
        Manager.addToQueue(new ScanToolOperation(ScanToolOperation.OperationType.REMOVE_BLOCK, message.from, message.to, player, message.block, ItemStack.EMPTY));
    }
}
