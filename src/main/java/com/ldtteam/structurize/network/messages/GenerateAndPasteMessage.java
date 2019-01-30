package com.ldtteam.structurize.network.messages;

import com.structurize.api.util.BlockPosUtil;
import com.structurize.api.util.Shape;
import com.ldtteam.structurize.management.Manager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message to request a shape from the server.
 */
public class GenerateAndPasteMessage extends AbstractMessage<GenerateAndPasteMessage, IMessage>
{
    /**
     * If hollow or not.
     */
    private boolean hollow;

    /**
     * The block to use for the schem.
     */
    private ItemStack block;

    /**
     * The fill block to use for the schem.
     */
    private ItemStack block2;

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
     * The frequency of a wave for example.
     */
    private int frequency;

    /**
     * The rotation.
     */
    private int rotation;

    /**
     * The mirror.
     */
    private boolean mirror;

    /**
     * Empty constructor used when registering the message.
     */
    public GenerateAndPasteMessage()
    {
        super();
    }

    /**
     * Creates a shape on serverside and sends it back.
     *
     * @param pos       the start pos.
     * @param length    the length.
     * @param width     the width.
     * @param height    the height.
     * @param frequency the frequency.
     * @param shape     the shape.
     * @param block     the block to set.
     * @param hollow    if hollow or not.
     */
    public GenerateAndPasteMessage(
      @NotNull final BlockPos pos,
      final int length,
      final int width,
      final int height,
      final int frequency,
      final Shape shape,
      final ItemStack block,
      final ItemStack block2,
      final boolean hollow, final int rotation, final Mirror mirror)
    {
        super();
        this.pos = pos;
        this.length = length;
        this.width = width;
        this.height = height;
        this.frequency = frequency;
        this.shape = shape;
        this.block = block;
        this.block2 = block2;
        this.hollow = hollow;
        this.mirror = mirror != Mirror.NONE;
        this.rotation = rotation;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        pos = BlockPosUtil.readFromByteBuf(buf);
        length = buf.readInt();
        width = buf.readInt();
        height = buf.readInt();
        frequency = buf.readInt();
        shape = Shape.values()[buf.readInt()];
        block = ByteBufUtils.readItemStack(buf);
        block2 = ByteBufUtils.readItemStack(buf);
        hollow = buf.readBoolean();
        rotation = buf.readInt();
        mirror = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        BlockPosUtil.writeToByteBuf(buf, pos);
        buf.writeInt(length);
        buf.writeInt(width);
        buf.writeInt(height);
        buf.writeInt(frequency);
        buf.writeInt(shape.ordinal());
        ByteBufUtils.writeItemStack(buf, block);
        ByteBufUtils.writeItemStack(buf, block2);
        buf.writeBoolean(hollow);
        buf.writeInt(rotation);
        buf.writeBoolean(mirror);
    }

    @Override
    public void messageOnServerThread(final GenerateAndPasteMessage message, final EntityPlayerMP player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            return;
        }

        Manager.pasteStructure(player.getServerWorld(),
          message.pos,
          message.width,
          message.length,
          message.height,
          message.frequency,
          message.shape,
          message.block,
          message.block2,
          message.hollow,
          player,
          message.mirror ? Mirror.FRONT_BACK : Mirror.NONE,
          message.rotation);
    }
}
