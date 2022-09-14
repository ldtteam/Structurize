package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.items.ItemCommandTool;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Updates the command in a command block auto-scanner.
 */
public class UpdateScanCommandBlockMessage implements IMessage
{
    private final BlockPos pos;
    private final BlockPos from;
    private final BlockPos to;
    @Nullable private final BlockPos anchor;
    private final String fileName;
    private final boolean force;

    /**
     * Construct message.
     * @param pos The position of the command block to be updated.
     * @param from The first corner of the scan.
     * @param to The second corner of the scan.
     * @param anchor The anchor position (or null).
     * @param fileName The scan filename.
     * @param force Force overwriting the command even if it seems dodgy.
     */
    public UpdateScanCommandBlockMessage(@NotNull final BlockPos pos,
                                         @NotNull final BlockPos from,
                                         @NotNull final BlockPos to,
                                         @Nullable final BlockPos anchor,
                                         @NotNull final String fileName,
                                         final boolean force)
    {
        this.pos = pos;
        this.from = from;
        this.to = to;
        this.anchor = anchor;
        this.fileName = fileName;
        this.force = force;
    }

    /**
     * Deserialize.
     */
    public UpdateScanCommandBlockMessage(@NotNull final FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
        if (buf.readBoolean())
        {
            this.anchor = buf.readBlockPos();
        }
        else
        {
            this.anchor = null;
        }
        this.fileName = buf.readUtf();
        this.force = buf.readBoolean();
    }

    /**
     * Serialize.
     */
    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.pos);
        buf.writeBlockPos(this.from);
        buf.writeBlockPos(this.to);
        if (this.anchor == null)
        {
            buf.writeBoolean(false);
        }
        else
        {
            buf.writeBoolean(true);
            buf.writeBlockPos(this.anchor);
        }
        buf.writeUtf(this.fileName);
        buf.writeBoolean(this.force);
    }

    @Override
    public @Nullable LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(@NotNull final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        ItemCommandTool.pasteCommandBlock(ctxIn.getSender(), this.pos,
                this.from, this.to, this.anchor, this.fileName, this.force);
    }
}
