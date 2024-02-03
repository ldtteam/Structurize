package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.Manager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.UUID;

/**
 * Class handling the Server UUID Message.
 */
public class ServerUUIDMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "server_uuid", ServerUUIDMessage::new);

    private final UUID serverUUID;

    /**
     * Empty constructor used when registering the message.
     */
    public ServerUUIDMessage()
    {
        super(TYPE);
        this.serverUUID = Manager.getServerUUID();
    }

    public ServerUUIDMessage(final FriendlyByteBuf buf)
    {
        super(buf, TYPE);
        this.serverUUID = buf.readUUID();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUUID(Manager.getServerUUID());
    }

    @Override
    public void onExecute(final PlayPayloadContext context, final Player player)
    {
        Manager.setServerUUID(serverUUID);
    }
}
