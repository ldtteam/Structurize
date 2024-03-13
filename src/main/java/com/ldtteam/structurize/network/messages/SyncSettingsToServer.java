package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.storage.rendering.ServerPreviewDistributor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Sync player settings to server.
 */
public class SyncSettingsToServer extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "sync_settings_to_server", SyncSettingsToServer::new);

    private final boolean displayShared;

    /**
     * Buffer reading message constructor.
     */
    protected SyncSettingsToServer(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.displayShared = buf.readBoolean();
    }

    /**
     * Send setting data from the client.
     */
    public SyncSettingsToServer()
    {
        super(TYPE);
        this.displayShared = Structurize.getConfig().getClient().displayShared.get();
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBoolean(displayShared);
    }

    @Override
    protected void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        ServerPreviewDistributor.register(player, displayShared);
    }
}
