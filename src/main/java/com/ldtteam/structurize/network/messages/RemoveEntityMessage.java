package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.operations.RemoveEntityOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Message to remove an entity from the world.
 */
public class RemoveEntityMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "remove_entity", RemoveEntityMessage::new);

    /**
     * Position to scan from.
     */
    private final BlockPos from;

    /**
     * Position to scan to.
     */
    private final BlockPos to;

    /**
     * The entity to remove from the world.
     */
    private final ResourceLocation entityName;

    /**
     * Empty constructor used when registering the message.
     */
    protected RemoveEntityMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
        this.entityName = buf.readResourceLocation();
    }

    /**
     * Create a message to remove an entity from the world.
     *
     * @param pos1       start coordinate.
     * @param pos2       end coordinate.
     * @param entityName the entity to remove.
     */
    public RemoveEntityMessage(final BlockPos pos1, final BlockPos pos2, final ResourceLocation entityName)
    {
        super(TYPE);
        this.from = pos1;
        this.to = pos2;
        this.entityName = entityName;
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeResourceLocation(entityName);
    }

    @Override
    protected void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        if (!player.isCreative())
        {
            return;
        }

        final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityName);
        if (type != null)
        {
            Manager.addToQueue(new RemoveEntityOperation(player, from, to, type));
        }
    }
}
