package com.ldtteam.structurize.network.messages;

import com.ldtteam.blockout.BOScreen;
import com.ldtteam.structurize.client.gui.WindowScan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Used to notify client of finished replacement operation
 */
public class ReplaceBlockClientUpdateMessage implements IMessage
{
    public ReplaceBlockClientUpdateMessage()
    {
    }

    public ReplaceBlockClientUpdateMessage(final PacketBuffer buf)
    {
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
    }

    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final Context ctxIn, final boolean isLogicalServer)
    {
        if (!isLogicalServer)
        {
            final Screen screen = Minecraft.getInstance().currentScreen;
            if (screen instanceof BOScreen && ((BOScreen) screen).getWindow() instanceof WindowScan)
            {
                // delay one tick with enqueue to ensure all block updates are processed first
                Minecraft.getInstance().enqueue(() -> ((WindowScan) ((BOScreen) screen).getWindow()).updateResources());
            }
        }
    }
}
