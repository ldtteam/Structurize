package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Structures;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Class handling the colony styles messages.
 */
public class StructurizeStylesMessage implements IMessage
{
    private final Map<String, String> md5Map;

    /**
     * Empty constructor used when registering the message.
     */
    public StructurizeStylesMessage()
    {
        this.md5Map = Structures.getMD5s();
    }

    public StructurizeStylesMessage(final PacketBuffer buf)
    {
        this.md5Map = new HashMap<>();

        final int count = buf.readInt();
        for (int i = 0; i < count; i++)
        {
            final String filename = buf.readUtf(32767);
            final String md5 = buf.readUtf(32767);
            md5Map.put(filename, md5);
        }
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(md5Map.size());
        for (final Map.Entry<String, String> entry : md5Map.entrySet())
        {
            buf.writeUtf(entry.getKey());
            buf.writeUtf(entry.getValue());
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
        Structures.init();
        Structures.setMD5s(md5Map);
    }
}
