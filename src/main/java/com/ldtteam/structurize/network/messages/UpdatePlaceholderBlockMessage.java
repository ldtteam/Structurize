package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.tileentities.TileEntityPlaceholder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Message to remove a block from the world.
 */
public class UpdatePlaceholderBlockMessage implements IMessage
{
    /**
     * Position of the TE.
     */
    private final BlockPos pos;

    /**
     * The block to set.
     */
    private final ItemStack block;

    /**
     * Empty constructor used when registering the message.
     */
    public UpdatePlaceholderBlockMessage(final PacketBuffer buf)
    {
        this.pos = buf.readBlockPos();
        this.block = buf.readItemStack();
    }

    /**
     * Create a message to setup the placerholder.
     * @param pos coordinate.
     * @param stack the block to remove.
     */
    public UpdatePlaceholderBlockMessage(@NotNull final BlockPos pos, @NotNull final ItemStack stack, final List<String> tagStringList)
    {
        this.pos = pos;
        this.block = stack;
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeItemStack(block);
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
