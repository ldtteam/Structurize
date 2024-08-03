package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.util.ScanToolData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Send the scan tool update message to the client.
 */
public class UpdateScanToolMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "update_scan_tool", UpdateScanToolMessage::new);

    private final ScanToolData data;

    /**
     * Empty public constructor.
     */
    protected UpdateScanToolMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);

        this.data = ScanToolData.STREAM_CODEC.decode(buf);
    }

    /**
     * Update the scan tool.
     * @param data the new data
     */
    public UpdateScanToolMessage(final ScanToolData data)
    {
        super(TYPE);

        this.data = data;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        ScanToolData.STREAM_CODEC.encode(buf, this.data);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        final ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ItemScanTool tool)
        {
            // normally you should sanity check client data more, but there's nothing particularly abuse-prone here
            tool.loadSlot(ScanToolData.update(stack, old -> this.data), stack);
        }
    }
}
