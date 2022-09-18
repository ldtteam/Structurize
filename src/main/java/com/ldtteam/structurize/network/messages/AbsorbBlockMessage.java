package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.items.ItemTagSubstitution;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbsorbBlockMessage implements IMessage
{
    private final BlockPos pos;
    private final ItemStack stack;

    public AbsorbBlockMessage(@NotNull final BlockPos pos, @NotNull final ItemStack stack)
    {
        this.pos = pos;
        this.stack = stack;
    }

    public AbsorbBlockMessage(@NotNull final FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
        this.stack = buf.readItem();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.pos);
        buf.writeItemStack(this.stack, false);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(@NotNull final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayer player = ctxIn.getSender();
        final ItemStack current = player.getInventory().getSelected();

        if (current.getItem() instanceof ItemTagSubstitution anchor)
        {
            anchor.onAbsorbBlock(player, current, this.pos, this.stack);
        }
    }
}
