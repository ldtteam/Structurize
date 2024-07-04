package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Messages for adding or removing a tag
 */
public class SetTagInTool extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "set_tag_in_tool", SetTagInTool::new);

    /**
     * The tag to use
     */
    private final String tag;

    /**
     * The tags blockpos
     */
    private final int slot;

    /**
     * Empty constructor used when registering the
     */
    protected SetTagInTool(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.tag = buf.readUtf(32767);
        this.slot = buf.readInt();
    }

    public SetTagInTool(final String tag, final int slot)
    {
        super(TYPE);
        this.slot = slot;
        this.tag = tag;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeUtf(tag);
        buf.writeInt(slot);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        if (!player.isCreative())
        {
            player.displayClientMessage(Component.translatable("structurize.gui.tagtool.creative_only"), false);
            return;
        }

        final ItemStack stack = player.getInventory().getItem(slot);
        if (stack.getItem() == ModItems.tagTool.get())
        {
            stack.getOrCreateTag().putString(ItemTagTool.TAG_CURRENT_TAG, tag);
        }
    }
}
