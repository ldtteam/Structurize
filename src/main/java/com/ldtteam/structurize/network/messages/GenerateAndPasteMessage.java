package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.api.util.Shape;
import com.ldtteam.structurize.management.Manager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message to request a shape from the server.
 */
public class GenerateAndPasteMessage implements IMessage
{
    /**
     * If hollow or not.
     */
    private final boolean hollow;

    /**
     * The block to use for the schem.
     */
    private final ItemStack block;

    /**
     * The fill block to use for the schem.
     */
    private final ItemStack block2;

    /**
     * The shape to get.
     */
    private final Shape shape;

    /**
     * Position to scan from.
     */
    private final BlockPos pos;

    /**
     * The length of the shape.
     */
    private final int length;

    /**
     * The width of the shape.
     */
    private final int width;

    /**
     * The height of the shape.
     */
    private final int height;

    /**
     * The frequency of a wave for example.
     */
    private final int frequency;

    /**
     * The equation of the random shape.
     */
    private final String equation;

    /**
     * The rotation.
     */
    private final int rotation;

    /**
     * The mirror.
     */
    private final boolean mirror;

    /**
     * Empty constructor used when registering the message.
     */
    public GenerateAndPasteMessage(final FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
        this.length = buf.readInt();
        this.width = buf.readInt();
        this.height = buf.readInt();
        this.frequency = buf.readInt();
        this.shape = Shape.values()[buf.readInt()];
        this.block = buf.readItem();
        this.block2 = buf.readItem();
        this.hollow = buf.readBoolean();
        this.rotation = buf.readInt();
        this.mirror = buf.readBoolean();
        this.equation = buf.readUtf(32767);
    }

    /**
     * Creates a shape on serverside and sends it back.
     *
     * @param pos       the start pos.
     * @param length    the length.
     * @param width     the width.
     * @param height    the height.
     * @param equation  the equation.
     * @param frequency the frequency.
     * @param shape     the shape.
     * @param block     the block to set.
     * @param block2    the fill block.
     * @param hollow    if hollow or not.
     * @param rotation  the rotation of it.
     * @param mirror    its mirror.
     */
    public GenerateAndPasteMessage(
      final BlockPos pos,
      final int length,
      final int width,
      final int height,
      final int frequency,
      final String equation,
      final Shape shape,
      final ItemStack block,
      final ItemStack block2,
      final boolean hollow,
      final Rotation rotation,
      final Mirror mirror)
    {
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
        this.rotation = rotation.ordinal();
        this.equation = equation;
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {

        buf.writeBlockPos(pos);
        buf.writeInt(length);
        buf.writeInt(width);
        buf.writeInt(height);
        buf.writeInt(frequency);
        buf.writeInt(shape.ordinal());
        buf.writeItem(block);
        buf.writeItem(block2);
        buf.writeBoolean(hollow);
        buf.writeInt(rotation);
        buf.writeBoolean(mirror);
        buf.writeUtf(equation);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (!ctxIn.getSender().isCreative())
        {
            return;
        }

        Manager.pasteStructure(ctxIn.getSender().getLevel(),
          pos,
          width,
          length,
          height,
          frequency,
          equation,
          shape,
          block,
          block2,
          hollow,
          ctxIn.getSender(),
          mirror ? Mirror.FRONT_BACK : Mirror.NONE,
          Rotation.values()[rotation]);
    }
}
