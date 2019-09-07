package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.FIRST_POS_STRING;
import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.SECOND_POS_STRING;

/**
 * Send the scan tool update message to the client.
 */
public class UpdateScanToolMessage implements IMessage
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
     * Empty public constructor.
     */
    public UpdateScanToolMessage()
    {
        super();
    }

    /**
     * Update the scan tool.
     * @param from the start pos.
     * @param to the end pos.
     */
    public UpdateScanToolMessage(@NotNull final BlockPos from, @NotNull final BlockPos to)
    {
        super();
        final ItemStack stack = Minecraft.getInstance().player.getHeldItemMainhand();
        if (stack.getItem() == ModItems.scanTool)
        {
            final CompoundNBT compound = stack.getOrCreateTag();
            compound.put(FIRST_POS_STRING, NBTUtil.writeBlockPos(from));
            compound.put(SECOND_POS_STRING, NBTUtil.writeBlockPos(to));
        }
        this.from = from;
        this.to = to;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        from = buf.readBlockPos();
        to = buf.readBlockPos();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
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
        final ItemStack stack = ctxIn.getSender().getHeldItemMainhand();
        if (stack.getItem() == ModItems.scanTool)
        {
            final CompoundNBT compound = stack.getOrCreateTag();
            compound.put(FIRST_POS_STRING, NBTUtil.writeBlockPos(from));
            compound.put(SECOND_POS_STRING, NBTUtil.writeBlockPos(to));
        }
    }
}
