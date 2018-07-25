package com.structurize.coremod.network.messages;

import com.structurize.api.util.BlockPosUtil;
import com.structurize.api.util.Shape;
import com.structurize.coremod.management.Manager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message to request a shape from the server.
 */
public class GetShapeMessage extends AbstractMessage<GetShapeMessage, IMessage>
{
    /**
     * The block to use for the schem.
     */
    private ItemStack block;

    /**
     * The shape to get.
     */
    private Shape shape;

    /**
     * Position to scan from.
     */
    private BlockPos pos;

    /**
     * The length of the shape.
     */
    private int length;

    /**
     * The width of the shape.
     */
    private int width;

    /**
     * The height of the shape.
     */
    private int height;

    /**
     * Empty constructor used when registering the message.
     */
    public GetShapeMessage()
    {
        super();
    }

    /**
     * Get shape message creation.
     * @param pos the pos to center it around.
     * @param length the length.
     * @param width the width.
     * @param height the height.
     * @param block the block to use.
     */
    public GetShapeMessage(@NotNull final BlockPos pos, final int length, final int width, final int height, final Shape shape, final ItemStack block)
    {
        super();
        this.pos = pos;
        this.length = length;
        this.width = width;
        this.height = height;
        this.shape = shape;
        this.block = block;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        pos = BlockPosUtil.readFromByteBuf(buf);
        length = buf.readInt();
        width = buf.readInt();
        height = buf.readInt();
        shape = Shape.values()[buf.readInt()];
        block = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        BlockPosUtil.writeToByteBuf(buf, pos);
        buf.writeInt(length);
        buf.writeInt(width);
        buf.writeInt(height);
        buf.writeInt(shape.ordinal());
        ByteBufUtils.writeItemStack(buf, block);
    }

    @Override
    public void messageOnServerThread(final GetShapeMessage message, final EntityPlayerMP player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            return;
        }

        Manager.getStructureFromFormula(player.getServerWorld(), message.width, message.length, message.height, message.shape, message.block, player);
    }
}
