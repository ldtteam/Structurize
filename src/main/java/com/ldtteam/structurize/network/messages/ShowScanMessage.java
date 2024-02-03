package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Tells the client to update their scan render box.
 */
public class ShowScanMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "show_scan", ShowScanMessage::new);

    private final BoxPreviewData box;

    /**
     * Construct from box
     * @param box the box to sync
     */
    public ShowScanMessage(@NotNull final BoxPreviewData box)
    {
        super(TYPE);
        this.box = box;
    }

    /**
     * Construct from network
     * @param buf the buffer
     */
    public ShowScanMessage(@NotNull final FriendlyByteBuf buf)
    {
        super(buf, TYPE);
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

    @Override
    public void onExecute(final PlayPayloadContext context, final Player player)
    {
        RenderingCache.queue("scan", this.box);
    }
}
