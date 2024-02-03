package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.storage.ClientFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.RotationMirror;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Request a blueprint from the client.
 */
public class ClientBlueprintRequestMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "blueprint_request", ClientBlueprintRequestMessage::new);

    /**
     * Structure placement info.
     */
    public final BuildToolPlacementMessage.HandlerType type;
    public final String   handlerId;
    public final String   structurePackId;
    public final String   blueprintPath;
    public final BlockPos pos;
    public final RotationMirror rotationMirror;


    /**
     * Buffer reading message constructor.
     */
    public ClientBlueprintRequestMessage(final FriendlyByteBuf buf)
    {
        super(buf, TYPE);
        this.type = BuildToolPlacementMessage.HandlerType.values()[buf.readInt()];
        this.handlerId = buf.readUtf(32767);

        this.structurePackId = buf.readUtf(32767);
        this.blueprintPath = buf.readUtf(32767);
        this.pos = buf.readBlockPos();
        this.rotationMirror = RotationMirror.values()[buf.readInt()];
    }

    /**
     * Send requesting data from the client.
     *
     * @param msg the placement message requiring the client request.
     */
    public ClientBlueprintRequestMessage(final BuildToolPlacementMessage msg)
    {
        super(TYPE);
        this.type = msg.type;
        this.handlerId = msg.handlerId;

        this.structurePackId = msg.structurePackId;
        this.blueprintPath = msg.blueprintPath;
        this.pos = msg.pos;
        this.rotationMirror = msg.rotationMirror;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.type.ordinal());
        buf.writeUtf(this.handlerId);

        buf.writeUtf(this.structurePackId);
        buf.writeUtf(this.blueprintPath);
        buf.writeBlockPos(this.pos);
        buf.writeInt(this.rotationMirror.ordinal());
    }

    @Override
    public void onExecute(final PlayPayloadContext context, final Player player)
    {
        ClientFutureProcessor.queueBlueprintData(new ClientFutureProcessor.BlueprintDataProcessingData(StructurePacks.getBlueprintDataFuture(structurePackId, blueprintPath), (blueprintData) -> {
            if (blueprintData != null)
            {
                new BlueprintSyncMessage(this, blueprintData).sendToServer();
            }
        }));
    }
}
