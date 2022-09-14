package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;

/**
 * Handles ShowScanMessages.
 */
public class ShowScanMessage implements IMessage
{
    private final BlockPos from;
    private final BlockPos to;
    @Nullable private final BlockPos anchor;

    /**
     * Tell the client to update its scan region display.
     *
     * @param from The first corner.
     * @param to The second corner.
     * @param anchor The anchor, if any.
     */
    public ShowScanMessage(@NotNull final BlockPos from,
                           @NotNull final BlockPos to,
                           @Nullable final BlockPos anchor)
    {
        this.from = from;
        this.to = to;
        this.anchor = anchor;
    }

    /**
     * Deserialize.
     */
    public ShowScanMessage(final FriendlyByteBuf buf)
    {
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
    }

    /**
     * Serialize.
     */
    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
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
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        RenderingCache.queue("scan", new BoxPreviewData(this.from, this.to, Optional.ofNullable(this.anchor)));
    }
}
