package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.ChangeStorage;
import com.ldtteam.structurize.util.TickedWorldOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Message to remove an entity from the world.
 */
public class RemoveEntityMessage implements IMessage
{
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
    public RemoveEntityMessage(final FriendlyByteBuf buf)
    {
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
        this.from = pos1;
        this.to = pos2;
        this.entityName = entityName;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeUtf(entityName);
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
        if (!ctxIn.getSender().isCreative())
        {
            return;
        }

        final Level world = ctxIn.getSender().level();
        final ChangeStorage storage = new ChangeStorage(Component.translatable("com.ldtteam.structurize." + TickedWorldOperation.OperationType.REMOVE_ENTITY.toString().toLowerCase(Locale.US), entityName), ctxIn.getSender().getUUID());
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
