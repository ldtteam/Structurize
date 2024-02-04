package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.storage.rendering.ServerPreviewDistributor;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Sync blueprint preview data to the server.
 */
public class SyncPreviewCacheToServer extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "sync_preview_cache_to_server", SyncPreviewCacheToServer::new);

    /**
     * The preview data.
     */
    private final BlueprintPreviewData previewData;

    /**
     * Buffer reading message constructor.
     */
    public SyncPreviewCacheToServer(final FriendlyByteBuf buf)
    {
        super(buf, TYPE);
        this.previewData = new BlueprintPreviewData(buf);
    }

    /**
     * Send preview data from the client.
     */
    public SyncPreviewCacheToServer(final BlueprintPreviewData previewData)
    {
        super(TYPE);
        this.previewData = previewData;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        this.previewData.writeToBuf(buf);
    }

    @Override
    public void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        ServerPreviewDistributor.distribute(this.previewData, player);
    }
}
