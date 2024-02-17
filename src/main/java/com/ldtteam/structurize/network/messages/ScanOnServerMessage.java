package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.ScanToolData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Optional;

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
     * Whether to scan entities
     */
    private final boolean saveEntities;

    /**
     * Empty public constructor.
     */
    protected ScanOnServerMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        final String name = buf.readUtf(32767);
        final BlockPos from = buf.readBlockPos();
        final BlockPos to = buf.readBlockPos();
        final Optional<BlockPos> anchorPos = buf.readBoolean() ? Optional.of(buf.readBlockPos()) : Optional.empty();

        this.slot = new ScanToolData.Slot(name, new BoxPreviewData(from, to, anchorPos));
        this.saveEntities = buf.readBoolean();
    }

    public ScanOnServerMessage(final ScanToolData.Slot slot, final boolean saveEntities)
    {
        super(TYPE);
        this.slot = slot;
        this.saveEntities = saveEntities;
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUtf(slot.getName());
        buf.writeBlockPos(slot.getBox().getPos1());
        buf.writeBlockPos(slot.getBox().getPos2());
        buf.writeBoolean(slot.getBox().getAnchor().isPresent());
        slot.getBox().getAnchor().ifPresent(buf::writeBlockPos);

        buf.writeBoolean(saveEntities);
    }

    @Override
    protected void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        ItemScanTool.saveStructure(player.getCommandSenderWorld(), player, this.slot, saveEntities);
    }
}
