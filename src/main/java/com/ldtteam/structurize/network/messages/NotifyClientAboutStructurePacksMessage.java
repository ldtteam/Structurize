package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.storage.ClientStructurePackLoader;
import com.ldtteam.structurize.storage.StructurePackMeta;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Notify the client about the structure packs on the server side.
 */
public class NotifyClientAboutStructurePacksMessage implements IMessage
{
    /**
     * List of server structure packs.
     * Contains String Name, and Integer version.
     */
    final Map<String, Integer> serverStructurePacks = new HashMap<>();

    /**
     * Public standard constructor.
     */
    public NotifyClientAboutStructurePacksMessage(final FriendlyByteBuf buf)
    {
        final int length = buf.readInt();
        for (int i = 0; i < length; i++)
        {
            this.serverStructurePacks.put(buf.readUtf(32767), buf.readInt());
        }
    }

    /**
     * Notify the client about the server structurepacks.
     * @param clientStructurePacks the list of packs.
     */
    public NotifyClientAboutStructurePacksMessage(final Map<String, StructurePackMeta> clientStructurePacks)
    {
        for (final StructurePackMeta pack : clientStructurePacks.values())
        {
            if (!pack.isImmutable())
            {
                this.serverStructurePacks.put(pack.getName(), pack.getVersion());
            }
        }
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.serverStructurePacks.size());
        for (final Map.Entry<String, Integer> packInfo : this.serverStructurePacks.entrySet())
        {
            buf.writeUtf(packInfo.getKey());
            buf.writeInt(packInfo.getValue());
        }
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
        if (!isLogicalServer)
        {
            ClientStructurePackLoader.onServerSyncAttempt(this.serverStructurePacks);
        }
    }
}
