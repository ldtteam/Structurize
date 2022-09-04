package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Sync blueprint preview data to the client.
 */
public class SyncPreviewCacheToClient implements IMessage
{
    /**
     * The preview data.
     */
    private final BlueprintPreviewData previewData;

    /**
     * The UUID of the origin player.
     */
    private final String playerUUID;

    /**
     * Buffer reading message constructor.
     */
    public SyncPreviewCacheToClient(final FriendlyByteBuf buf)
    {
        this.previewData = new BlueprintPreviewData(buf);
        this.playerUUID = buf.readUtf(32767);
    }

    /**
     * Send preview data from the server.
     */
    public SyncPreviewCacheToClient(final BlueprintPreviewData previewData, final String playerUUID)
    {
        this.previewData = previewData;
        this.playerUUID = playerUUID;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        this.previewData.writeToBuf(buf);
        buf.writeUtf(this.playerUUID);
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
        if (previewData.isEmpty())
        {
            if (RenderingCache.hasBlueprint(this.playerUUID))
            {
                RenderingCache.removeBlueprint(this.playerUUID);
            }
        }
        else
        {
            RenderingCache.queue(this.playerUUID, this.previewData);
        }
    }
}
