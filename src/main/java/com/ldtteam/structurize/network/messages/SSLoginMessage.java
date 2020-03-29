package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.client.gui.SSLoginGui;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Schematic server login message. Opens login gui.
 */
public class SSLoginMessage implements IMessage
{
    @Override
    public void toBytes(final PacketBuffer buf)
    {
        // noop
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        // noop
    }

    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final Context ctxIn, final boolean isLogicalServer)
    {
        new SSLoginGui().open();
    }
}
