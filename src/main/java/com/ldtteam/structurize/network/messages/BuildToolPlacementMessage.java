package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.storage.BlueprintPlacementHandling;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the blueprint.
 * This also buffers the incoming messages, places one per tick and loads the file off-thread.
 */
public class BuildToolPlacementMessage implements IMessage
{
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
    public final Rotation rotation;
    public final Mirror   mirror;

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
    public BuildToolPlacementMessage(final FriendlyByteBuf buf)
    {
        this.type = HandlerType.values()[buf.readInt()];
        this.handlerId = buf.readUtf(32767);

        this.structurePackId = buf.readUtf(32767);
        this.blueprintPath = buf.readUtf(32767);
        this.pos = buf.readBlockPos();
        this.rotation = Rotation.values()[buf.readInt()];
        this.mirror = Mirror.values()[buf.readInt()];
    }

    /**
     * Send placement data to the server.
     *
     * @param type            the type of placement.
     * @param handlerId       additional handler meta data.
     * @param structurePackId the id of the pack.
     * @param blueprintPath   the path of the structure in the pack.
     * @param pos             the position of the blueprint.
     * @param rotation        the rotation of the blueprint.
     * @param mirror          the mirror of the blueprint.
     */
    public BuildToolPlacementMessage(
      final HandlerType type,
      final String handlerId,
      final String structurePackId,
      final String blueprintPath,
      final BlockPos pos,
      final Rotation rotation,
      final Mirror mirror)
    {
        this.type = type;
        this.handlerId = handlerId;

        this.structurePackId = structurePackId;
        this.blueprintPath = blueprintPath;
        this.pos = pos;
        this.rotation = rotation;
        this.mirror = mirror;
    }

    /**
     * Create a placement message from the sync message.
     * @param msg the sync message.
     */
    public BuildToolPlacementMessage(final BlueprintSyncMessage msg, final ServerPlayer player, final Level world)
    {
        this.type = msg.type;
        this.handlerId = msg.handlerId;

        this.structurePackId = msg.structurePackId;
        this.blueprintPath = msg.blueprintPath;
        this.pos = msg.pos;
        this.rotation = msg.rotation;
        this.mirror = msg.mirror;

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
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.type.ordinal());
        buf.writeUtf(this.handlerId);

        buf.writeUtf(this.structurePackId);
        buf.writeUtf(this.blueprintPath);
        buf.writeBlockPos(this.pos);
        buf.writeInt(this.rotation.ordinal());
        buf.writeInt(this.mirror.ordinal());
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
        world = ctxIn.getSender().level;
        player = ctxIn.getSender();
        BlueprintPlacementHandling.handlePlacement(this);
    }
}
