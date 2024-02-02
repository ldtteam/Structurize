package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Sync blueprint preview data to the client.
 */
public class SyncPreviewCacheToClient implements IMessage
{
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
    public SyncPreviewCacheToClient(final FriendlyByteBuf buf)
    {
        this.previewData = new BlueprintPreviewData(buf, false);
        this.playerUUID = buf.readUUID();
    }

    /**
     * Send preview data from the server.
     */
    public SyncPreviewCacheToClient(final BlueprintPreviewData previewData, final UUID playerUUID)
    {
        this.previewData = previewData;
        this.playerUUID = playerUUID;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        this.previewData.writeToBuf(buf);
        buf.writeUUID(this.playerUUID);
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
