package com.ldtteam.structurize.util;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.PositionStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    private final List<CompoundNBT> entities = new ArrayList<>();

    /**
     * List of entities to kill in range.
     */
    private final List<Entity> entitiesToKill = new ArrayList<>();

    /**
     * The responsible player.
     */
    private final PlayerEntity player;

    /**
     * Initiate an empty changeStorage to manually fill it.
     * @param player the player owner of it.
     */
    public ChangeStorage(final PlayerEntity player)
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
    public ChangeStorage(final World world, final BlockPos from, final BlockPos to, final PlayerEntity player)
    {
        this.player = player;

        for (int x = Math.min(from.getX(), to.getX()); x <= Math.max(from.getX(), to.getX()); x++)
        {
            for (int y = Math.min(from.getY(), to.getY()); y <= Math.max(from.getY(), to.getY()); y++)
            {
                for (int z = Math.min(from.getZ(), to.getZ()); z <= Math.max(from.getZ(), to.getZ()); z++)
                {
                    final BlockPos place = new BlockPos(x, y, z);
                    blocks.put(place, new PositionStorage(world.getBlockState(place), world.getTileEntity(place)));
                }
            }
        }

        final List<Entity> tempEntities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(from, to));
        entities.addAll(tempEntities.stream().map(Entity::serializeNBT).collect(Collectors.toList()));
    }

    /**
     * Add a position storage to the list.
     * @param place the place.
     * @param world the world.
     */
    public void addPositionStorage(final BlockPos place, final World world)
    {
        if (!blocks.containsKey(place))
        {
            blocks.put(place, new PositionStorage(world.getBlockState(place), world.getTileEntity(place)));
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
    public boolean undo(final World world)
    {
        int count = 0;
        for (final Map.Entry<BlockPos, PositionStorage> entry : new ArrayList<>(blocks.entrySet()))
        {
            world.setBlockState(entry.getKey(), entry.getValue().getState());
            if (entry.getValue().getEntity() != null)
            {
                world.setTileEntity(entry.getKey(), entry.getValue().getEntity());
            }
            blocks.remove(entry.getKey());
            count++;

            if (count >= Structurize.getConfig().getCommon().maxOperationsPerTick.get())
            {
                return false;
            }
        }

        for (final CompoundNBT data : entities)
        {
            final Optional<EntityType<?>> type = EntityType.readEntityType(data);
            if (type.isPresent())
            {
                final Entity entity = type.get().create(world);
                if (entity != null)
                {
                    entity.deserializeNBT(data);
                    world.addEntity(entity);
                }
            }
        }
        entitiesToKill.forEach(Entity::remove);

        return true;
    }

    /**
     * Check if a certain player is owner of this change.
     * @param player the player to check.
     * @return true if so.
     */
    public boolean isOwner(final PlayerEntity player)
    {
        return this.player.getUniqueID().equals(player.getUniqueID());
    }
}
