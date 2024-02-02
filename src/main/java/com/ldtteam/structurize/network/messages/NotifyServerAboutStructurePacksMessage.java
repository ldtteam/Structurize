package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.storage.ServerStructurePackLoader;
import com.ldtteam.structurize.storage.StructurePackMeta;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Notify the server about the structure packs on the client side.
 */
public class NotifyServerAboutStructurePacksMessage implements IMessage
{
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

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (isLogicalServer)
        {
            ServerStructurePackLoader.onClientSyncAttempt(this.clientStructurePacks, ctxIn.getSender());
        }
    }
}
