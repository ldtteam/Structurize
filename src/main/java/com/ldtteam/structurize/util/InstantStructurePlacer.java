package com.ldtteam.structurize.util;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.api.configuration.Configurations;
import com.ldtteam.structurize.api.util.ChangeStorage;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.placementhandlers.IPlacementHandler;
import com.ldtteam.structurize.placementhandlers.PlacementHandlers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Interface for using the structure codebase.
 */
public class InstantStructurePlacer
{
    /**
     * The structure of this placer.
     */
    protected final Structure structure;

    /**
     * If complete or not.
     */
    private boolean complete = false;

    /**
     * Create a new instnat structure placer.
     *
     * @param structure the structure to place.
     */
    public InstantStructurePlacer(final Structure structure)
    {
        this.structure = structure;
    }

    /**
     * Remove a structure from the world.
     *
     * @param pos coordinates
     */
    public void removeStructure(@NotNull final BlockPos pos)
    {
        structure.setLocalPosition(pos);
        for (int j = 0; j < this.structure.getHeight(); j++)
        {
            for (int k = 0; k < this.structure.getLength(); k++)
            {
                for (int i = 0; i < this.structure.getWidth(); i++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(i, j, k);
                    final BlockPos worldPos = pos.add(localPos);
                    if (!structure.getWorld().isAirBlock(worldPos))
                    {
                        structure.getWorld().setBlockToAir(worldPos);
                    }
                }
            }
        }
    }

    /**
     * Setup the structure placement and add to buffer.
     *
     * @param pos      the world anchor.
     * @param complete if complete or not.
     * @param player   the issuing player.
     */
    public void setupStructurePlacement(@NotNull final BlockPos pos, final boolean complete, final ServerPlayerEntity player)
    {
        structure.setLocalPosition(pos);
        this.complete = complete;
        structure.setPosition(pos);
        Manager.addToQueue(new ScanToolOperation(this, player));
    }

    /**
     * Place a structure into the world.
     *
     * @param world the placing player.
     * @param storage the change storage.
     * @param inputPos the start pos.
     * @return the last pos.
     */
    public BlockPos placeStructure(final World world, final ChangeStorage storage, final BlockPos inputPos)
    {
        structure.setLocalPosition(inputPos);
        @NotNull final List<BlockPos> delayedBlocks = new ArrayList<>();
        final BlockPos endPos = new BlockPos(this.structure.getWidth(), this.structure.getHeight(), this.structure.getLength());
        BlockPos currentPos = inputPos;
        int count = 0;

        for (int y = currentPos.getY(); y < endPos.getY(); y++)
        {
            for (int x = currentPos.getX(); x < endPos.getX(); x++)
            {
                for (int z = currentPos.getZ(); z < endPos.getZ(); z++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(x, y, z);
                    final IBlockState localState = this.structure.getBlockState(localPos);
                    if (localState == null)
                    {
                        continue;
                    }
                    final Block localBlock = localState.getBlock();

                    final BlockPos worldPos = structure.getPosition().add(localPos);

                    if ((localBlock == ModBlocks.blockSubstitution && !this.complete) || localBlock instanceof IAnchorBlock)
                    {
                        continue;
                    }
                    count++;

                    storage.addPositionStorage(worldPos, world);

                    if (localState.getMaterial().isSolid())
                    {
                        this.handleBlockPlacement(world, worldPos, localState, this.complete, this.structure.getTileEntityData(localPos));
                    }
                    else
                    {
                        delayedBlocks.add(localPos);
                    }

                    if (count >= Configurations.gameplay.maxOperationsPerTick)
                    {
                        this.handleDelayedBlocks(delayedBlocks, storage, world);
                        return new BlockPos(x, y, z);
                    }
                }
                currentPos = new BlockPos(x, y, 0);
            }
            currentPos = new BlockPos(0, y, 0);
        }
        this.handleDelayedBlocks(delayedBlocks, storage, world);

        for (final CompoundNBT compound : this.structure.getEntityData())
        {
            if (compound != null)
            {
                try
                {
                    final BlockPos pos = this.structure.getPosition();
                    final Entity entity = EntityList.createEntityFromNBT(compound, world);
                    entity.setUniqueId(UUID.randomUUID());
                    final Vec3d worldPos = entity.getPositionVector().add(pos.getX(), pos.getY(), pos.getZ());

                    if (entity instanceof EntityHanging)
                    {
                        entity.posX = worldPos.x;
                        entity.posY = worldPos.y;
                        entity.posZ = worldPos.z;

                        final BlockPos hanging = ((EntityHanging) entity).getHangingPosition();
                        entity.setPosition(hanging.getX() + pos.getX(), hanging.getY() + pos.getY(), hanging.getZ() + pos.getZ());
                    }
                    else
                    {
                        entity.setPosition(worldPos.x, worldPos.y, worldPos.z);
                    }
                    world.spawnEntity(entity);
                    storage.addToBeKilledEntity(entity);
                }
                catch (final RuntimeException e)
                {
                    Log.getLogger().info("Couldn't restore entitiy", e);
                }
            }
        }

        return null;
    }

    /**
     * Handle the delayed blocks (non solid blocks)
     *
     * @param delayedBlocks the delayed block list.
     * @param storage       the changeStorage.
     * @param world         the world.
     */
    private void handleDelayedBlocks(final List<BlockPos> delayedBlocks, final ChangeStorage storage, final World world)
    {
        for (@NotNull final BlockPos coords : delayedBlocks)
        {
            final IBlockState localState = this.structure.getBlockState(coords);
            final BlockPos newWorldPos = structure.getPosition().add(coords);
            storage.addPositionStorage(coords, world);
            final BlockInfo info = this.structure.getBlockInfo(coords);
            this.handleBlockPlacement(world, newWorldPos, localState, this.complete, info == null ? null : info.getTileEntityData());
        }
    }

    /**
     * This method handles the block placement.
     * When we extract this into another mod, we have to override the method.
     *
     * @param world          the world.
     * @param pos            the world position.
     * @param localState     the local state.
     * @param complete       if complete with it.
     * @param tileEntityData the tileEntity.
     */
    public void handleBlockPlacement(final World world, final BlockPos pos, final IBlockState localState, final boolean complete, final CompoundNBT tileEntityData)
    {
        for (final IPlacementHandler handlers : PlacementHandlers.handlers)
        {
            if (handlers.canHandle(world, pos, localState))
            {
                handlers.handle(world, pos, localState, tileEntityData, complete, structure.getPosition(), structure.getSettings());
                return;
            }
        }
    }

    /**
     * Check if there is enough free space to place a structure in the world.
     *
     * @param pos coordinates
     * @return true if there is free space.
     */
    public boolean checkForFreeSpace(@NotNull final BlockPos pos)
    {
        structure.setLocalPosition(pos);
        for (int j = 0; j < this.structure.getHeight(); j++)
        {
            for (int k = 0; k < this.structure.getLength(); k++)
            {
                for (int i = 0; i < this.structure.getWidth(); i++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(i, j, k);

                    final BlockPos worldPos = pos.add(localPos);

                    if (worldPos.getY() <= pos.getY() && !structure.getWorld().getBlockState(worldPos.down()).getMaterial().isSolid())
                    {
                        return false;
                    }

                    final IBlockState worldState = structure.getWorld().getBlockState(worldPos);
                    if (worldState.getBlock() == Blocks.BEDROCK)
                    {
                        return false;
                    }

                    if (worldPos.getY() > pos.getY() && worldState.getBlock() != Blocks.AIR)
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Set the placement to complete.
     *
     * @param complete if placing the complete structure with placeholders.
     */
    public void setComplete(final boolean complete)
    {
        this.complete = complete;
    }
}
