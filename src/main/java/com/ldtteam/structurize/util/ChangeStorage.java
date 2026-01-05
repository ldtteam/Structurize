package com.ldtteam.structurize.util;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.Manager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Change storage to store changes to an area to be able to undo them.
 */
public class ChangeStorage
{
    /**
     * Simple int ID creator
     */
    private static int storageIDs = 0;

    /**
     * This storages unique ID
     */
    private final int id;

    /**
     * List of blocks with position.
     */
    private final Map<BlockPos, BlockChangeData> blocks = new HashMap<>();

    /**
     * List of entities in range.
     */
    private final List<CompoundTag> removedEntities = new ArrayList<>();

    /**
     * List of entities to kill in range.
     */
    private final List<Entity> addedEntities = new ArrayList<>();

    /**
     * The operation which was done
     */
    private final Component operation;

    /**
     * Current operation iteration
     */
    private Iterator<Map.Entry<BlockPos, BlockChangeData>> iterator = null;

    private final UUID player;

    /**
     * Initiate an empty changeStorage to manually fill it.
     *
     * @param player the player owner of it.
     */
    public ChangeStorage(final Component operation, final UUID player)
    {
        this.player = player;
        this.id = storageIDs++;
        this.operation = operation;
    }

    /**
     * Add a position storage to the list.
     *
     * @param place the place.
     * @param world the world.
     */
    public void addPreviousDataFor(final BlockPos place, final Level world)
    {
        blocks.computeIfAbsent(place, p -> new BlockChangeData()).withPreState(world.getBlockState(place)).withPreTE(world.getBlockEntity(place));
    }

    /**
     * Add a position storage to the list.
     *
     * @param place the place.
     * @param world the world.
     */
    public void addPostDataFor(final BlockPos place, final Level world)
    {
        blocks.computeIfAbsent(place, p -> new BlockChangeData()).withPostState(world.getBlockState(place)).withPostTE(world.getBlockEntity(place));
    }

    /**
     * Add entities to list to be readded.
     *
     * @param list the list of entities.
     */
    public void addEntities(final List<Entity> list)
    {
        removedEntities.addAll(list.stream().map(Entity::serializeNBT).collect(Collectors.toList()));
    }

    /**
     * Add a entity to be killed to the list.
     *
     * @param entity the place.
     */
    public void addToBeKilledEntity(final Entity entity)
    {
        addedEntities.add(entity);
    }

    /**
     * Reload the previous state of the positions.
     *
     * @param world       the world to manipulate.
     * @param undoStorage
     * @return true if successful.
     */
    public boolean undo(final Level world, @Nullable final ChangeStorage undoStorage)
    {
        if (iterator == null)
        {
            iterator = blocks.entrySet().iterator();
        }

        int count = 0;
        while (iterator.hasNext())
        {
            final Map.Entry<BlockPos, BlockChangeData> entry = iterator.next();
            // Only revert block changes which this operation caused
            if (world.getBlockState(entry.getKey()).getBlock() != entry.getValue().getPostState().getBlock()
                || entry.getValue().getPostTE() != world.getBlockEntity(entry.getKey()))
            {
                continue;
            }

            if (undoStorage != null)
            {
                undoStorage.addPreviousDataFor(entry.getKey(), world);
            }
            world.setBlock(entry.getKey(), Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            world.setBlock(entry.getKey(), entry.getValue().getPreState(), Constants.UPDATE_FLAG);

            if (entry.getValue().getPreTE() != null)
            {
                world.setBlockEntity(entry.getValue().getPreTE());
            }
            world.markAndNotifyBlock(entry.getKey(), world.getChunkAt(entry.getKey()), entry.getValue().getPreState(), entry.getValue().getPreState(), 2, 512);

            if (undoStorage != null)
            {
                undoStorage.addPostDataFor(entry.getKey(), world);
            }

            count++;

            if (count >= Structurize.getConfig().getServer().maxOperationsPerTick.get())
            {
                return false;
            }
        }

        for (final CompoundTag data : removedEntities)
        {
            final Optional<EntityType<?>> type = EntityType.by(data);
            if (type.isPresent())
            {
                final Entity entity = type.get().create(world);
                if (entity != null)
                {
                    entity.deserializeNBT(data);
                    world.addFreshEntity(entity);
                    if (undoStorage != null)
                    {
                        undoStorage.addedEntities.add(entity);
                    }
                }
            }
        }
        addedEntities.forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));

        if (undoStorage != null)
        {
            Manager.addToUndoRedoCache(undoStorage);
        }
        return true;
    }

    /**
     * Reload the previous state of the positions.
     *
     * @param world the world to manipulate.
     * @return true if successful.
     */
    public boolean redo(final Level world)
    {
        int count = 0;

        if (iterator == null)
        {
            iterator = blocks.entrySet().iterator();
        }

        while (iterator.hasNext())
        {
            final Map.Entry<BlockPos, BlockChangeData> entry = iterator.next();
            if (world.getBlockState(entry.getKey()).getBlock() != entry.getValue().getPreState().getBlock())
            {
                continue;
            }

            world.setBlock(entry.getKey(), Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            world.setBlock(entry.getKey(), entry.getValue().getPostState(), Constants.UPDATE_FLAG);
            if (entry.getValue().getPostTE() != null)
            {
                world.setBlockEntity(entry.getValue().getPostTE());
            }
            world.markAndNotifyBlock(entry.getKey(), world.getChunkAt(entry.getKey()), entry.getValue().getPostState(), entry.getValue().getPostState(), 2, 512);

            count++;

            if (count >= Structurize.getConfig().getServer().maxOperationsPerTick.get())
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the operation of this changestorage
     *
     * @return
     */
    public Component getOperation()
    {
        return operation;
    }

    /**
     * Resets the iteration
     */
    public void resetUnRedo()
    {
        iterator = null;
    }

    /**
     * Check whether the current operation on this is done
     *
     * @return
     */
    public boolean isDone()
    {
        return iterator == null || !iterator.hasNext();
    }

    /**
     * Get this change storages unique ID
     *
     * @return
     */
    public int getID()
    {
        return id;
    }

    /**
     * Get the players ID
     *
     * @return
     */
    public UUID getPlayerID()
    {
        return player;
    }
}
