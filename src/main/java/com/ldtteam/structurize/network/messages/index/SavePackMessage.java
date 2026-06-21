package com.ldtteam.structurize.network.messages.index;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.network.messages.SaveScanMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent from the client to the server to save (validate and scan) all schematics within a pack.
 *
 * <p>The server creates a blueprint for each schematic from its stored bounding box, validates it
 * against the pack type's requirements, and — if no blocking errors are found — sends the
 * resulting blueprint back to the requesting player via {@link SaveScanMessage}. The stored
 * validation state is updated for each schematic regardless of whether the scan proceeds.
 */
public class SavePackMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "save_pack", SavePackMessage::new);

    private final String packId;

    public SavePackMessage(final String packId)
    {
        super(TYPE);
        this.packId = packId;
    }

    protected SavePackMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
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

        PackManager.savePack(packId, player.serverLevel(), player);
    }
}
