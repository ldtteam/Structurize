package com.ldtteam.structurize.network.messages.index;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.network.messages.SyncPackManagerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent from the client to the server to request re-validation of an existing pack.
 * The server runs {@link com.ldtteam.structurize.index.PackManager#validatePack} and broadcasts
 * the updated validation state to all players via a {@link SyncPackManagerMessage}.
 */
public class ValidatePackMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "validate_pack", ValidatePackMessage::new);

    private final String packId;

    public ValidatePackMessage(final String packId)
    {
        super(TYPE);
        this.packId = packId;
    }

    protected ValidatePackMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.packId = buf.readUtf();
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeUtf(packId);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        if (!Structurize.getConfig().getServer().isSchematicBuildServer.get())
        {
            return;
        }

        PackManager.validatePack(packId, player.serverLevel());
    }
}
