package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

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
    protected ShowScanMessage(@NotNull final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.box = BoxPreviewData.STREAM_CODEC.decode(buf);
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        BoxPreviewData.STREAM_CODEC.encode(buf, box);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final Player player)
    {
        RenderingCache.queue("scan", this.box);
    }
}
