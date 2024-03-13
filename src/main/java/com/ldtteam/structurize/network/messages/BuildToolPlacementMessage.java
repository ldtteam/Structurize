package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.storage.BlueprintPlacementHandling;
import com.ldtteam.structurize.api.RotationMirror;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the blueprint.
 * This also buffers the incoming messages, places one per tick and loads the file off-thread.
 */
public class BuildToolPlacementMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "buildtool_placement", BuildToolPlacementMessage::new);

    /**
     * Identify the Client side handler.
     */
    public final HandlerType type;
    public final String      handlerId;

    /**
     * Structure placement info.
     */
    public final String   structurePackId;
    public final String   blueprintPath;
    public final  BlockPos pos;
    public final RotationMirror rotationMirror;

    /**
     * Cached placement info.
     */
    public Level        world;
    public ServerPlayer player;
    public boolean      clientPack = false;

    /**
     * Type of button.
     */
    public enum HandlerType
    {
        Complete,
        Pretty,
        Survival
    }

    /**
     * Buffer reading message constructor.
     */
    protected BuildToolPlacementMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.type = HandlerType.values()[buf.readInt()];
        this.handlerId = buf.readUtf(32767);

        this.structurePackId = buf.readUtf(32767);
        this.blueprintPath = buf.readUtf(32767);
        this.pos = buf.readBlockPos();
        this.rotationMirror = RotationMirror.values()[buf.readInt()];
    }

    /**
     * Send placement data to the server.
     *
     * @param type            the type of placement.
     * @param handlerId       additional handler meta data.
     * @param structurePackId the id of the pack.
     * @param blueprintPath   the path of the structure in the pack.
     * @param pos             the position of the blueprint.
     * @param rotMir          the rotation and the mirror of the blueprint.
     */
    public BuildToolPlacementMessage(
      final HandlerType type,
      final String handlerId,
      final String structurePackId,
      final String blueprintPath,
      final BlockPos pos,
      final RotationMirror rotMir)
    {
        super(TYPE);
        this.type = type;
        this.handlerId = handlerId;

        this.structurePackId = structurePackId;
        this.blueprintPath = blueprintPath;
        this.pos = pos;
        this.rotationMirror = rotMir;
    }

    /**
     * Create a placement message from the sync message.
     * @param msg the sync message.
     */
    public BuildToolPlacementMessage(final BlueprintSyncMessage msg, final ServerPlayer player, final Level world)
    {
        super(TYPE);
        this.type = msg.type;
        this.handlerId = msg.handlerId;

        this.structurePackId = msg.structurePackId;
        this.blueprintPath = msg.blueprintPath;
        this.pos = msg.pos;
        this.rotationMirror = msg.rotationMirror;

        this.clientPack = true;
        this.player = player;
        this.world = world;
    }

    /**
     * Writes this packet to a {@link ByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.type.ordinal());
        buf.writeUtf(this.handlerId);

        buf.writeUtf(this.structurePackId);
        buf.writeUtf(this.blueprintPath);
        buf.writeBlockPos(this.pos);
        buf.writeInt(this.rotationMirror.ordinal());
    }

    @Override
    protected void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        world = player.level();
        this.player = player;
        BlueprintPlacementHandling.handlePlacement(this);
    }
}
