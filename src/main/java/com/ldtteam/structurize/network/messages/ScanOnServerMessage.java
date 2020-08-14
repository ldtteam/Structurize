package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.items.ItemScanTool;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Send the scan message for a player to the server.
 */
public class ScanOnServerMessage implements IMessage
{
    /**
     * Position to scan from.
     */
    private final BlockPos from;

    /**
     * Position to scan to.
     */
    private final BlockPos to;

    /**
     * The Anchor Pos.
     */
    private final Optional<BlockPos> anchorPos;

    /**
     * Name of the file.
     */
    private final String name;

    /**
     * Whether to scan entities
     */
    private final boolean saveEntities;

    /**
     * Empty public constructor.
     */
    public ScanOnServerMessage(final PacketBuffer buf)
    {
        this.name = buf.readString(32767);
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
        this.saveEntities = buf.readBoolean();
        this.anchorPos = buf.readBoolean() ? Optional.of(buf.readBlockPos()) : Optional.empty();
    }

    public ScanOnServerMessage(@NotNull final BlockPos from, @NotNull final BlockPos to, @NotNull final String name, final boolean saveEntities, final Optional<BlockPos> anchorPos)
    {
        this.from = from;
        this.to = to;
        this.name = name;
        this.saveEntities = saveEntities;
        this.anchorPos = anchorPos;
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeString(name);
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeBoolean(saveEntities);
        buf.writeBoolean(anchorPos.isPresent());
        anchorPos.ifPresent(buf::writeBlockPos);
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
        ItemScanTool.saveStructure(ctxIn.getSender().getEntityWorld(), from, to, ctxIn.getSender(), name, saveEntities, this.anchorPos);
    }
}
