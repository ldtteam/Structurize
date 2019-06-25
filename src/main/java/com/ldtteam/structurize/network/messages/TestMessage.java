package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.Instances;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class TestMessage implements IMessage
{
    private String str;

    public TestMessage()
    {

    }

    public TestMessage(final String str)
    {
        this.str = str;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeString(str);
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        str = buf.readString();
    }

    @Override
    public void onExecute(final Context ctxIn, final boolean isLogicalServer)
    {
        if (isLogicalServer)
        {
            Instances.getModLogger().info("Incoming message from client: " + str);
        }
        else
        {
            Instances.getModLogger().info("Incoming message from server: " + str);
        }
    }
}
