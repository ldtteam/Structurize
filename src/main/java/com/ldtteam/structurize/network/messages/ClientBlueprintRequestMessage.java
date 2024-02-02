package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.storage.ClientFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Request a blueprint from the client.
 */
public class ClientBlueprintRequestMessage implements IMessage
{
    /**
     * Structure placement info.
     */
    public final BuildToolPlacementMessage.HandlerType type;
    public final String   handlerId;
    public final String   structurePackId;
    public final String   blueprintPath;
    public final BlockPos pos;
    public final Rotation rotation;
    public final Mirror   mirror;


    /**
     * Buffer reading message constructor.
     */
    public ClientBlueprintRequestMessage(final FriendlyByteBuf buf)
    {
        this.type = BuildToolPlacementMessage.HandlerType.values()[buf.readInt()];
        this.handlerId = buf.readUtf(32767);

        this.structurePackId = buf.readUtf(32767);
        this.blueprintPath = buf.readUtf(32767);
        this.pos = buf.readBlockPos();
        this.rotation = Rotation.values()[buf.readInt()];
        this.mirror = Mirror.values()[buf.readInt()];
    }

    /**
     * Send requesting data from the client.
     *
     * @param msg the placement message requiring the client request.
     */
    public ClientBlueprintRequestMessage(final BuildToolPlacementMessage msg)
    {
        this.type = msg.type;
        this.handlerId = msg.handlerId;

        this.structurePackId = msg.structurePackId;
        this.blueprintPath = msg.blueprintPath;
        this.pos = msg.pos;
        this.rotation = msg.rotation;
        this.mirror = msg.mirror;
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
        ClientFutureProcessor.queueBlueprintData(new ClientFutureProcessor.BlueprintDataProcessingData(StructurePacks.getBlueprintDataFuture(structurePackId, blueprintPath), (blueprintData) -> {
            if (blueprintData != null)
            {
                Network.getNetwork().sendToServer(new BlueprintSyncMessage(this, blueprintData));
            }
        }));
    }
}
