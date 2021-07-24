package com.ldtteam.structurize.util;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.PositionStorage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Change storage to store changes to an area to be able to undo them.
 */
public class ChangeStorage
{
    /**
     * List of blocks with position.
     */
    private final Map<BlockPos, PositionStorage> blocks = new HashMap<>();

    /**
     * List of entities in range.
     */
    private final List<CompoundTag> entities = new ArrayList<>();

    /**
     * List of entities to kill in range.
     */
    private final List<Entity> entitiesToKill = new ArrayList<>();

    /**
     * The responsible player.
     */
    private final Player player;

    /**
     * Initiate an empty changeStorage to manually fill it.
     * @param player the player owner of it.
     */
    public ChangeStorage(final Player player)
    {
        this.player = player;
    }

    /**
     * Inititate the change storage with the world to calc the positions.
     * @param world the world.
     * @param from the first position.
     * @param to the second position.
     * @param player the player assigned to it.
     */
    public ChangeStorage(final Level world, final BlockPos from, final BlockPos to, final Player player)
    {
        this.player = player;

        for (int x = Math.min(from.getX(), to.getX()); x <= Math.max(from.getX(), to.getX()); x++)
        {
            for (int y = Math.min(from.getY(), to.getY()); y <= Math.max(from.getY(), to.getY()); y++)
            {
                for (int z = Math.min(from.getZ(), to.getZ()); z <= Math.max(from.getZ(), to.getZ()); z++)
                {
                    final BlockPos place = new BlockPos(x, y, z);
                    blocks.put(place, new PositionStorage(world.getBlockState(place), world.getBlockEntity(place)));
                }
            }
        }

        final List<Entity> tempEntities = world.getEntitiesOfClass(Entity.class, new AABB(from, to));
        entities.addAll(tempEntities.stream().map(Entity::serializeNBT).collect(Collectors.toList()));
    }

    /**
     * Add a position storage to the list.
     * @param place the place.
     * @param world the world.
     */
    public void addPositionStorage(final BlockPos place, final Level world)
    {
        if (!blocks.containsKey(place))
        {
            blocks.put(place, new PositionStorage(world.getBlockState(place), world.getBlockEntity(place)));
        }
    }

    /**
     * Add entities to list to be readded.
     * @param list the list of entities.
     */
    public void addEntities(final List<Entity> list)
    {
        entities.addAll(list.stream().map(Entity::serializeNBT).collect(Collectors.toList()));
    }

    /**
     * Add a entity to be killed to the list.
     * @param entity the place.
     */
    public void addToBeKilledEntity(final Entity entity)
    {
        entitiesToKill.add(entity);
    }

    /**
     * Reload the previous state of the positions.
     * @param world the world to manipulate.
     * @return true if successful.
     */
    public boolean undo(final Level world)
    {
        int count = 0;
        for (final Map.Entry<BlockPos, PositionStorage> entry : new ArrayList<>(blocks.entrySet()))
        {
            world.setBlockAndUpdate(entry.getKey(), entry.getValue().getState());
            if (entry.getValue().getEntity() != null)
            {
                world.setBlockEntity(entry.getValue().getEntity());
            }
            blocks.remove(entry.getKey());
            count++;

            if (count >= Structurize.getConfig().getServer().maxOperationsPerTick.get())
            {
                return false;
            }
        }

        for (final CompoundTag data : entities)
        {
            final Optional<EntityType<?>> type = EntityType.by(data);
            if (type.isPresent())
            {
                final Entity entity = type.get().create(world);
                if (entity != null)
                {
                    entity.deserializeNBT(data);
                    world.addFreshEntity(entity);
                }
            }
        }
        entitiesToKill.forEach(entity -> entity.remove(false));

        return true;
    }

    /**
     * Check if a certain player is owner of this change.
     * @param player the player to check.
     * @return true if so.
     */
    public boolean isOwner(final Player player)
    {
        return this.player.getUUID().equals(player.getUUID());
    }
}
