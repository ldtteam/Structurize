package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.util.ScanToolData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Send the scan message for a player to the server.
 */
public class ScanOnServerMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "scan_on_server", ScanOnServerMessage::new);

    /**
     * Scan data.
     */
    private final ScanToolData.Slot slot;

    /**
     * Empty public constructor.
     */
    protected ScanOnServerMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.slot = ScanToolData.Slot.STREAM_CODEC.decode(buf);
    }

    public ScanOnServerMessage(final ScanToolData.Slot slot)
    {
        super(TYPE);
        this.slot = slot;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        ScanToolData.Slot.STREAM_CODEC.encode(buf, slot);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        ItemScanTool.saveStructure(player.getCommandSenderWorld(), player, this.slot);
    }
}
