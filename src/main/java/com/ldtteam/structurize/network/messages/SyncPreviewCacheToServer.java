package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.storage.rendering.ServerPreviewDistributor;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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
    protected SyncPreviewCacheToServer(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
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
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        this.previewData.writeToBuf(buf);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        ServerPreviewDistributor.distribute(this.previewData.prepareBlueprint(player.level().registryAccess()), player);
    }
}
