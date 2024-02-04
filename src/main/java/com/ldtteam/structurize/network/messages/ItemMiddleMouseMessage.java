package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.IScrollableItem;
import com.ldtteam.structurize.api.ISpecialBlockPickItem;
import com.ldtteam.structurize.api.constants.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Notify server that client clicked or scrolled the middle mouse on a special item
 */
public class ItemMiddleMouseMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "item_middle_mouse", ItemMiddleMouseMessage::new);

    @Nullable private final BlockPos pos;
    private final double deltaX;
    private final double deltaY;
    private final boolean ctrlKey;

    /**
     * Construct message for a middle mouse click event.
     * @param pos the block position clicked, or null if in air
     * @param ctrlKey ctrl key is held
     */
    public ItemMiddleMouseMessage(@Nullable final BlockPos pos, final boolean ctrlKey)
    {
        super(TYPE);
        this.pos = pos;
        this.deltaX = 0;
        this.deltaY = 0;
        this.ctrlKey = ctrlKey;
    }

    /**
     * Construct message for a middle mouse shift-scroll event.
     * @param deltaX the scroll delta; negative is upwards
     * @param deltaY the scroll delta; negative is upwards
     * @param ctrlKey ctrl key is held
     */
    public ItemMiddleMouseMessage(final double deltaX, final double deltaY, final boolean ctrlKey)
    {
        super(TYPE);
        this.pos = null;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.ctrlKey = ctrlKey;
    }

    /**
     * Construct from network.
     * @param buf buffer
     */
    public ItemMiddleMouseMessage(@NotNull final FriendlyByteBuf buf)
    {
        super(buf, TYPE);
        this.pos = buf.readBoolean() ? buf.readBlockPos() : null;
        this.deltaX = buf.readDouble();
        this.deltaY = buf.readDouble();
        this.ctrlKey = buf.readBoolean();
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
        buf.writeDouble(this.deltaX);
        buf.writeDouble(this.deltaY);
        buf.writeBoolean(this.ctrlKey);
    }

    @Override
    public void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        final ItemStack current = player.getInventory().getSelected();

        if (this.deltaX == 0 && this.deltaY == 0)
        {
            if (current.getItem() instanceof ISpecialBlockPickItem clickableItem)
            {
                clickableItem.onBlockPick(player, current, this.pos, this.ctrlKey);
            }
        }
        else
        {
            if (current.getItem() instanceof IScrollableItem scrollableItem)
            {
                scrollableItem.onMouseScroll(player, current, this.deltaX, this.deltaY, this.ctrlKey);
            }
        }
    }
}
