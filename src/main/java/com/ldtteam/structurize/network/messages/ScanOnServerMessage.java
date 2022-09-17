package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.ScanToolData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Send the scan message for a player to the server.
 */
public class ScanOnServerMessage implements IMessage
{
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
    public ScanOnServerMessage(final FriendlyByteBuf buf)
    {
        final String name = buf.readUtf(32767);
        final BlockPos from = buf.readBlockPos();
        final BlockPos to = buf.readBlockPos();
        final Optional<BlockPos> anchorPos = buf.readBoolean() ? Optional.of(buf.readBlockPos()) : Optional.empty();

        this.slot = new ScanToolData.Slot(name, new BoxPreviewData(from, to, anchorPos));
        this.saveEntities = buf.readBoolean();
    }

    public ScanOnServerMessage(final ScanToolData.Slot slot, final boolean saveEntities)
    {
        this.slot = slot;
        this.saveEntities = saveEntities;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUtf(slot.getName());
        buf.writeBlockPos(slot.getBox().getPos1());
        buf.writeBlockPos(slot.getBox().getPos2());
        buf.writeBoolean(slot.getBox().getAnchor().isPresent());
        slot.getBox().getAnchor().ifPresent(buf::writeBlockPos);

        buf.writeBoolean(saveEntities);
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
        ItemScanTool.saveStructure(ctxIn.getSender().getCommandSenderWorld(), ctxIn.getSender(), this.slot, saveEntities);
    }
}
