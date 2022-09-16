package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Tells the client to update their scan render box.
 */
public class ShowScanMessage implements IMessage
{
    private final BoxPreviewData box;

    /**
     * Construct from box
     * @param box the box to sync
     */
    public ShowScanMessage(@NotNull final BoxPreviewData box)
    {
        this.box = box;
    }

    /**
     * Construct from network
     * @param buf the buffer
     */
    public ShowScanMessage(@NotNull final FriendlyByteBuf buf)
    {
        final BlockPos from = buf.readBlockPos();
        final BlockPos to = buf.readBlockPos();
        final BlockPos anchor = buf.readBoolean() ? buf.readBlockPos() : null;

        this.box = new BoxPreviewData(from, to, Optional.ofNullable(anchor));
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.box.getPos1());
        buf.writeBlockPos(this.box.getPos2());
        if (this.box.getAnchor().isPresent())
        {
            buf.writeBoolean(true);
            buf.writeBlockPos(this.box.getAnchor().get());
        }
        else
        {
            buf.writeBoolean(false);
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(@NotNull final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        RenderingCache.queue("scan", this.box);
    }
}
