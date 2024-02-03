package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.storage.BlueprintPlacementHandling;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.apache.commons.io.FilenameUtils;

/**
 * Sends a blueprint from the client to the server.
 */
public class BlueprintSyncMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "blueprint_sync", BlueprintSyncMessage::new);

    /**
     * Structure placement info.
     */
    public final BuildToolPlacementMessage.HandlerType type;
    public final String handlerId;
    public       String structurePackId;
    public final String blueprintPath;
    public final BlockPos pos;
    public final Rotation rotation;
    public final Mirror   mirror;

    /**
     * Blueprint data future.
     */
    public byte[] blueprintData;

    /**
     * Buffer reading message constructor.
     */
    public BlueprintSyncMessage(final FriendlyByteBuf buf)
    {
        super(buf, TYPE);
        this.type = BuildToolPlacementMessage.HandlerType.values()[buf.readInt()];
        this.handlerId = buf.readUtf(32767);

        this.structurePackId = buf.readUtf(32767);
        this.blueprintPath = FilenameUtils.normalize(buf.readUtf(32767));
        this.pos = buf.readBlockPos();
        this.rotation = Rotation.values()[buf.readInt()];
        this.mirror = Mirror.values()[buf.readInt()];

        this.blueprintData = buf.readByteArray();
    }

    /**
     * Send requested data from the client.
     *
     * @param msg the request message to get most data from.
     * @param blueprintData the blueprint data.
     */
    public BlueprintSyncMessage(
      final ClientBlueprintRequestMessage msg,
      final byte[] blueprintData)
    {
        super(TYPE);
        this.type = msg.type;
        this.handlerId = msg.handlerId;

        this.structurePackId = msg.structurePackId;
        this.blueprintPath = msg.blueprintPath;
        this.pos = msg.pos;
        this.rotation = msg.rotation;
        this.mirror = msg.mirror;
        this.blueprintData = blueprintData;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.type.ordinal());
        buf.writeUtf(this.handlerId);

        buf.writeUtf(this.structurePackId);
        buf.writeUtf(this.blueprintPath);
        buf.writeBlockPos(this.pos);
        buf.writeInt(this.rotation.ordinal());
        buf.writeInt(this.mirror.ordinal());

        buf.writeByteArray(this.blueprintData);
    }

    @Override
    public void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        BlueprintPlacementHandling.handlePlacement(this, player);
    }
}
