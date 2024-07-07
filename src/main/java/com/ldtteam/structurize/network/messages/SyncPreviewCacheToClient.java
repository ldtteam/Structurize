package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Sync blueprint preview data to the client.
 */
public class SyncPreviewCacheToClient extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "sync_preview_cache_to_client", SyncPreviewCacheToClient::new);

    public static final String SHARED_PREFIX = "shared:";

    /**
     * The preview data.
     */
    private final BlueprintPreviewData previewData;

    /**
     * The UUID of the origin player.
     */
    private final UUID playerUUID;

    /**
     * Buffer reading message constructor.
     */
    protected SyncPreviewCacheToClient(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.previewData = new BlueprintPreviewData(buf, false);
        this.playerUUID = buf.readUUID();
    }

    /**
     * Send preview data from the server.
     */
    public SyncPreviewCacheToClient(final BlueprintPreviewData previewData, final UUID playerUUID)
    {
        super(TYPE);
        this.previewData = previewData;
        this.playerUUID = playerUUID;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        this.previewData.writeToBuf(buf);
        buf.writeUUID(this.playerUUID);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final Player player)
    {
        final String uuid = SHARED_PREFIX + playerUUID.toString();
        if (previewData.isEmpty())
        {
            if (RenderingCache.hasBlueprint(uuid))
            {
                RenderingCache.removeBlueprint(uuid);
            }
        }
        else
        {
            RenderingCache.queue(uuid, this.previewData);
        }
    }
}
