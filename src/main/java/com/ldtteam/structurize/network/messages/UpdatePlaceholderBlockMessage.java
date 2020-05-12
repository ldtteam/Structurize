package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.tileentities.TileEntityPlaceholder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Message to remove a block from the world.
 */
public class UpdatePlaceholderBlockMessage implements IMessage
{
    /**
     * Position of the TE.
     */
    private BlockPos pos;

    /**
     * The block to set.
     */
    private ItemStack block;

    /**
     * The list of tags
     */
    private List<String> tagStringList = new ArrayList<>();

    /**
     * Empty constructor used when registering the message.
     */
    public UpdatePlaceholderBlockMessage()
    {
        super();
    }

    /**
     * Create a message to setup the placerholder.
     * @param pos coordinate.
     * @param stack the block to remove.
     */
    public UpdatePlaceholderBlockMessage(@NotNull final BlockPos pos, @NotNull final ItemStack stack, final List<String> tagStringList)
    {
        super();
        this.pos = pos;
        this.block = stack;
        this.tagStringList = tagStringList;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        pos = buf.readBlockPos();
        block = buf.readItemStack();
        int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            tagStringList.add(buf.readString());
        }
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeItemStack(block);
        buf.writeInt(tagStringList.size());
        for (final String tag : tagStringList)
        {
            buf.writeString(tag);
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

        final TileEntityPlaceholder te = (TileEntityPlaceholder) ctxIn.getSender().world.getTileEntity(pos);
        if (te != null)
        {
            te.setStack(block);
        }
    }
}
