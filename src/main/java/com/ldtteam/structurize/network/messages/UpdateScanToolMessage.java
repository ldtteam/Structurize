package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
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
    private final BlockPos from;

    /**
     * Position to scan to.
     */
    private final BlockPos to;

    /**
     * Empty public constructor.
     */
    public UpdateScanToolMessage(final FriendlyByteBuf buf)
    {
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
    }

    /**
     * Update the scan tool.
     * @param from the start pos.
     * @param to the end pos.
     */
    @SuppressWarnings("resource")
    public UpdateScanToolMessage(final BlockPos from, final BlockPos to)
    {
        final ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        if (stack.getItem() == ModItems.scanTool.get())
        {
            final CompoundTag compound = stack.getOrCreateTag();
            compound.put(FIRST_POS_STRING, NbtUtils.writeBlockPos(from));
            compound.put(SECOND_POS_STRING, NbtUtils.writeBlockPos(to));
        }
        this.from = from;
        this.to = to;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
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
        final ItemStack stack = ctxIn.getSender().getMainHandItem();
        if (stack.getItem() == ModItems.scanTool.get())
        {
            final CompoundTag compound = stack.getOrCreateTag();
            compound.put(FIRST_POS_STRING, NbtUtils.writeBlockPos(from));
            compound.put(SECOND_POS_STRING, NbtUtils.writeBlockPos(to));
        }
    }
}
