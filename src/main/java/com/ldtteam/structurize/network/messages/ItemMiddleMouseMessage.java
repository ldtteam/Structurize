package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.api.util.IMiddleClickableItem;
import com.ldtteam.structurize.api.util.IScrollableItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Notify server that client clicked or scrolled the middle mouse on a special item
 */
public class ItemMiddleMouseMessage implements IMessage
{
    @Nullable private final BlockPos pos;
    private final double delta;

    /**
     * Construct message for a middle mouse click event.
     * @param pos the block position clicked, or null if in air
     */
    public ItemMiddleMouseMessage(@Nullable final BlockPos pos)
    {
        this.pos = pos;
        this.delta = 0;
    }

    /**
     * Construct message for a middle mouse shift-scroll event.
     * @param delta the scroll delta; negative is upwards
     */
    public ItemMiddleMouseMessage(final double delta)
    {
        this.pos = null;
        this.delta = delta;
    }

    /**
     * Construct from network.
     * @param buf buffer
     */
    public ItemMiddleMouseMessage(@NotNull final FriendlyByteBuf buf)
    {
        this.pos = buf.readBoolean() ? buf.readBlockPos() : null;
        this.delta = buf.readDouble();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        if (this.pos == null)
        {
            buf.writeBoolean(false);
        }
        else
        {
            buf.writeBoolean(true);
            buf.writeBlockPos(this.pos);
        }
        buf.writeDouble(this.delta);
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

        if (this.delta == 0)
        {
            if (current.getItem() instanceof IMiddleClickableItem clickableItem)
            {
                clickableItem.onMiddleClick(player, current, this.pos);
            }
        }
        else
        {
            if (current.getItem() instanceof IScrollableItem scrollableItem)
            {
                scrollableItem.onMouseScroll(player, current, this.delta);
            }
        }
    }
}
