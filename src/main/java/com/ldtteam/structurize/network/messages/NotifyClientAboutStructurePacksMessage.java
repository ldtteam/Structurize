package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.storage.ClientStructurePackLoader;
import com.ldtteam.structurize.storage.StructurePackMeta;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Notify the client about the structure packs on the server side.
 */
public class NotifyClientAboutStructurePacksMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "notify_client_about_structure_packs", NotifyClientAboutStructurePacksMessage::new);

    /**
     * List of server structure packs.
     * Contains String Name, and Integer version.
     */
    private final Map<String, Double> serverStructurePacks = new HashMap<>();

    /**
     * Public standard constructor.
     */
    protected NotifyClientAboutStructurePacksMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        final int length = buf.readInt();
        for (int i = 0; i < length; i++)
        {
            this.serverStructurePacks.put(buf.readUtf(32767), buf.readDouble());
        }
    }

    /**
     * Notify the client about the server structurepacks.
     * @param clientStructurePacks the list of packs.
     */
    public NotifyClientAboutStructurePacksMessage(final Map<String, StructurePackMeta> clientStructurePacks)
    {
        super(TYPE);
        for (final StructurePackMeta pack : clientStructurePacks.values())
        {
            if (!pack.isImmutable())
            {
                this.serverStructurePacks.put(pack.getName(), pack.getVersion());
            }
        }
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.serverStructurePacks.size());
        for (final Map.Entry<String, Double> packInfo : this.serverStructurePacks.entrySet())
        {
            buf.writeUtf(packInfo.getKey());
            buf.writeDouble(packInfo.getValue());
        }
    }

    @Override
    protected void onExecute(final PlayPayloadContext context, final Player player)
    {
        ClientStructurePackLoader.onServerSyncAttempt(this.serverStructurePacks);
    }
}
