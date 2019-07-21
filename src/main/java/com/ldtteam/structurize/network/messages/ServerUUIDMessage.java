package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.network.PacketUtils;
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
    private UUID serverUUID;

    /**
     * Empty constructor used when registering the message.
     */
    public ServerUUIDMessage()
    {
        super();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        serverUUID = PacketUtils.readUUID(buf);
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        PacketUtils.writeUUID(buf, Manager.getServerUUID());
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
