package com.ldtteam.structurize.network.messages.index;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.index.PackManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent from the client to the server to remove a pack from the index.
 *
 * <p>Only removes the index entry — no schematic files on disk are deleted.
 */
public class DeletePackMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "delete_pack", DeletePackMessage::new);

    private final String packId;

    public DeletePackMessage(final String packId)
    {
        super(TYPE);
        this.packId = packId;
    }

    protected DeletePackMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
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

        PackManager.deletePack(packId);
    }
}