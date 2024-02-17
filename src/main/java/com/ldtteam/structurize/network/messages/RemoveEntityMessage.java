package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.ChangeStorage;
import com.ldtteam.structurize.util.TickedWorldOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.List;
import java.util.Locale;

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
    private final String entityName;

    /**
     * Empty constructor used when registering the message.
     */
    protected RemoveEntityMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
        this.entityName = buf.readUtf(32767);
    }

    /**
     * Create a message to remove an entity from the world.
     * @param pos1 start coordinate.
     * @param pos2 end coordinate.
     * @param entityName the entity to remove.
     */
    public RemoveEntityMessage(final BlockPos pos1, final BlockPos pos2, final String entityName)
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
        buf.writeUtf(entityName);
    }

    @Override
    protected void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        if (!player.isCreative())
        {
            return;
        }

        final Level world = player.level();
        final ChangeStorage storage = new ChangeStorage(Component.translatable("com.ldtteam.structurize." + TickedWorldOperation.OperationType.REMOVE_ENTITY.toString().toLowerCase(Locale.US), entityName), player.getUUID());
        for(int x = Math.min(from.getX(), to.getX()); x <= Math.max(from.getX(), to.getX()); x++)
        {
            for (int y = Math.min(from.getY(), to.getY()); y <= Math.max(from.getY(), to.getY()); y++)
            {
                for (int z = Math.min(from.getZ(), to.getZ()); z <= Math.max(from.getZ(), to.getZ()); z++)
                {
                    final BlockPos here = new BlockPos(x, y, z);
                    final List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(here));
                    storage.addEntities(list);

                    for(final Entity entity: list)
                    {
                        if (entity.getName().getString().equals(entityName))
                        {
                            entity.remove(Entity.RemovalReason.DISCARDED);
                        }
                    }
                }
            }
        }
        Manager.addToUndoRedoCache(storage);
    }
}
