package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.items.ItemScanTool;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScanToolTeleportMessage implements IMessage
{
    public ScanToolTeleportMessage()
    {
    }

    public ScanToolTeleportMessage(@NotNull final FriendlyByteBuf buf)
    {
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
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
        if (stack.getItem() instanceof ItemScanTool tool)
        {
            tool.onTeleport(ctxIn.getSender(), stack);
        }
    }
}
