package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Class handling the Server UUID Message.
 */
public class ServerUUIDMessage implements IMessage
{
    private final UUID serverUUID;

    /**
     * Empty constructor used when registering the message.
     */
    public ServerUUIDMessage()
    {
        this.serverUUID = Manager.getServerUUID();
    }

    public ServerUUIDMessage(final PacketBuffer buf)
    {
        this.serverUUID = buf.readUUID();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeUUID(Manager.getServerUUID());
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
        Manager.setServerUUID(serverUUID);
    }
}
