package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.storage.ServerStructurePackLoader;
import com.ldtteam.structurize.storage.StructurePackMeta;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
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
    private final Map<String, Double> clientStructurePacks = new HashMap<>();

    /**
     * Public standard constructor.
     */
    public NotifyServerAboutStructurePacksMessage(final FriendlyByteBuf buf)
    {
        final int length = buf.readInt();
        for (int i = 0; i < length; i++)
        {
            this.clientStructurePacks.put(buf.readUtf(32767), buf.readDouble());
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
        for (final Map.Entry<String, Double> packInfo : this.clientStructurePacks.entrySet())
        {
            buf.writeUtf(packInfo.getKey());
            buf.writeDouble(packInfo.getValue());
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
