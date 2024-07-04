package com.ldtteam.structurize.operations;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.util.ChangeStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

/**
 * Operation for removing one type of block.
 */
public class RemoveEntityOperation extends BaseOperation
{
    /**
     * The start position to iterate from.
     */
    private final BlockPos startPos;

    /**
     * The end position to iterate to.
     */
    private final BlockPos endPos;

    /**
     * What type of entity to remove.
     */
    private final EntityType<?> entityType;

    /**
     * Default constructor.
     *
     * @param startPos   the start pos to iterate from.
     * @param endPos     the end pos to iterate to.
     * @param entityType what type of entity to remove.
     */
    public RemoveEntityOperation(final Player player, final BlockPos startPos, final BlockPos endPos, final EntityType<?> entityType)
    {
        super(new ChangeStorage(Component.translatable("com.ldtteam.structurize.remove_entity", entityType.getDescription()),
          player != null ? player.getUUID() : UUID.randomUUID()));
        this.startPos = startPos;
        this.endPos = endPos;
        this.entityType = entityType;
    }

    @Override
    public boolean apply(final ServerLevel world)
    {
        final List<Entity> list = world.getEntitiesOfClass(Entity.class, AABB.encapsulatingFullBlocks(startPos, endPos));
        storage.addEntities(list, world.registryAccess());

        int count = 0;
        for (final Entity entity : list)
        {
            if (entity.getType().equals(entityType))
            {
                entity.remove(Entity.RemovalReason.DISCARDED);

                count++;
                if (count >= Structurize.getConfig().getServer().maxOperationsPerTick.get())
                {
                    return false;
                }
            }
        }

        return true;
    }
}
