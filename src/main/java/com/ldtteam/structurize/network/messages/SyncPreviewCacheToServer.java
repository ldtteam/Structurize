package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.storage.rendering.ServerPreviewDistributor;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Sync blueprint preview data to the server.
 */
public class SyncPreviewCacheToServer implements IMessage
{
    /**
     * The preview data.
     */
    private final BlueprintPreviewData previewData;

    /**
     * Buffer reading message constructor.
     */
    public SyncPreviewCacheToServer(final FriendlyByteBuf buf)
    {
        this.previewData = new BlueprintPreviewData(buf);
    }

    /**
     * Send preview data from the client.
     */
    public SyncPreviewCacheToServer(final BlueprintPreviewData previewData)
    {
        this.previewData = previewData;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        this.previewData.writeToBuf(buf);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        ServerPreviewDistributor.distribute(this.previewData, ctxIn.getSender());
    }
}
