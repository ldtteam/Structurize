package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.storage.rendering.ServerPreviewDistributor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Sync player settings to server.
 */
public class SyncSettingsToServer implements IMessage
{
    private final boolean displayShared;

    /**
     * Buffer reading message constructor.
     */
    public SyncSettingsToServer(final FriendlyByteBuf buf)
    {
        this.displayShared = buf.readBoolean();
    }

    /**
     * Send setting data from the client.
     */
    public SyncSettingsToServer()
    {
        this.displayShared = Structurize.getConfig().getClient().displayShared.get();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBoolean(displayShared);
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
        ServerPreviewDistributor.register(ctxIn.getSender(), displayShared);
    }
}
