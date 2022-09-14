package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Send the scan tool update message to the client.
 */
public class UpdateScanToolMessage implements IMessage
{
    /**
     * Structure name.
     */
    private final String name;

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
        final String name = buf.readUtf();
        this.name = name.isEmpty() ? null : name;
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
    }

    /**
     * Update the scan tool.
     * @param from the start pos.
     * @param to the end pos.
     */
    @SuppressWarnings("resource")
    public UpdateScanToolMessage(@Nullable final String name, final BlockPos from, final BlockPos to)
    {
        final ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        if (stack.getItem() == ModItems.scanTool.get())
        {
            ItemScanTool.setStructureName(stack, name);
            ItemScanTool.setBounds(stack, from, to);
        }
        this.name = name;
        this.from = from;
        this.to = to;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUtf(name == null ? "" : name);
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
            ItemScanTool.setStructureName(stack, name);
            ItemScanTool.setBounds(stack, from, to);
        }
    }
}
