package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.util.ScanToolData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Send the scan tool update message to the client.
 */
public class UpdateScanToolMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "update_scan_tool", UpdateScanToolMessage::new);

    /**
     * Data.
     */
    private final CompoundTag tag;

    /**
     * Empty public constructor.
     */
    public UpdateScanToolMessage(final FriendlyByteBuf buf)
    {
        super(buf, TYPE);
        this.tag = buf.readNbt();
    }

    /**
     * Update the scan tool.
     * @param data the new data
     */
    public UpdateScanToolMessage(@NotNull ScanToolData data)
    {
        super(TYPE);
        this.tag = data.getInternalTag().copy();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeNbt(this.tag);
    }

    @Override
    public void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        final ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ItemScanTool tool)
        {
            stack.setTag(this.tag);
            tool.loadSlot(new ScanToolData(stack.getOrCreateTag()), stack);
        }
    }
}
