package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.ChangeStorage;
import com.ldtteam.structurize.util.TickedWorldOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
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
    private final ResourceLocation entityName;

    /**
     * Empty constructor used when registering the message.
     */
    public RemoveEntityMessage(final FriendlyByteBuf buf)
    {
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
        this.from = pos1;
        this.to = pos2;
        this.entityName = entityName;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeResourceLocation(entityName);
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

        final EntityType type = BuiltInRegistries.ENTITY_TYPE.get(entityName);
        final Level world = ctxIn.getSender().level();
        final ChangeStorage storage =
          new ChangeStorage(Component.translatable("com.ldtteam.structurize." + TickedWorldOperation.OperationType.REMOVE_ENTITY.toString().toLowerCase(Locale.US), entityName),
            ctxIn.getSender().getUUID());

        final List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(from, to));
        storage.addEntities(list);

        for (final Entity entity : list)
        {
            if (entity.getType() == type)
            {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
        Manager.addToUndoRedoCache(storage);
    }
}
