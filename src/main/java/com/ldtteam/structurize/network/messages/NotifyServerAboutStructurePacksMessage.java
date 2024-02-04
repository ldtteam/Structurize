package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.storage.ServerStructurePackLoader;
import com.ldtteam.structurize.storage.StructurePackMeta;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Notify the server about the structure packs on the client side.
 */
public class NotifyServerAboutStructurePacksMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "notify_server_about_structure_packs", NotifyServerAboutStructurePacksMessage::new);

    /**
     * List of client structure packs.
     * Contains String Name, and Integer version.
     */
    final Map<String, Integer> clientStructurePacks = new HashMap<>();

    /**
     * Public standard constructor.
     */
    public NotifyServerAboutStructurePacksMessage(final FriendlyByteBuf buf)
    {
        super(buf, TYPE);
        final int length = buf.readInt();
        for (int i = 0; i < length; i++)
        {
            this.clientStructurePacks.put(buf.readUtf(32767), buf.readInt());
        }
    }

    /**
     * Notify the server about the client structurepacks.
     * @param clientStructurePacks the list of packs.
     */
    public NotifyServerAboutStructurePacksMessage(final Collection<StructurePackMeta> clientStructurePacks)
    {
        super(TYPE);
        for (final StructurePackMeta pack : clientStructurePacks)
        {
            if (!pack.isImmutable())
            {
                this.clientStructurePacks.put(pack.getName(), pack.getVersion());
            }
        }
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.clientStructurePacks.size());
        for (final Map.Entry<String, Integer> packInfo : this.clientStructurePacks.entrySet())
        {
            buf.writeUtf(packInfo.getKey());
            buf.writeInt(packInfo.getValue());
        }
    }

    @Override
    public void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        ServerStructurePackLoader.onClientSyncAttempt(this.clientStructurePacks, player);
    }
}
