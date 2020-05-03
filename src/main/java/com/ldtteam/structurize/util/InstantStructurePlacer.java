package com.ldtteam.structurize.util;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.placementhandlers.IPlacementHandler;
import com.ldtteam.structurize.placementhandlers.PlacementHandlers;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

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
                        structure.getWorld().removeBlock(worldPos, false);
                    }
                }
            }
        }
    }

    /**
     * Load a structure into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj  the world to load it in
     * @param name      the structures name
     * @param pos       coordinates
     * @param rotation  the rotation.
     * @param mirror    the mirror used.
     * @param complete  paste it complete (with structure blocks) or without
     * @param player    the placing player.
     */
    public static void loadAndPlaceStructureWithRotation(
      final World worldObj, @NotNull final String name,
      @NotNull final BlockPos pos, final Rotation rotation,
      @NotNull final Mirror mirror, final boolean complete,
      final ServerPlayerEntity player)
    {
        try
        {
            @NotNull final Structure structure = new Structure(worldObj, name, new PlacementSettings(mirror, rotation));
            structure.setPosition(pos);
            structure.rotate(rotation, worldObj, pos, mirror);
            @NotNull final InstantStructurePlacer structureWrapper = new InstantStructurePlacer(structure);
            structureWrapper.setupStructurePlacement(pos.subtract(structure.getOffset()), complete, player);
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
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
     * @param world    the placing player.
     * @param storage  the change storage.
     * @param inputPos the start pos.
     * @return the last pos.
     */
    public BlockPos placeStructure(final World world, final ChangeStorage storage, final BlockPos inputPos)
    {
        return placeStructure(world, storage, inputPos, (structure, pos) -> structure.getBlockState(pos).getBlock() == ModBlocks.blockSubstitution
                                                                           || structure.getBlockState(pos).getBlock() instanceof IAnchorBlock);
    }

    /**
     * 
     * @param world             the placing player.
     * @param storage           storage the change storage.
     * @param inputPos          the start pos.
     * @param skipIfNotComplete a function that determines whether to skip a block
     *                          if complete is false.
     * @return the last pos.
     */
    public BlockPos placeStructure(final World world, final ChangeStorage storage, final BlockPos inputPos, BiFunction<Structure, BlockPos, Boolean> skipIfNotComplete)
    {
        structure.setLocalPosition(inputPos);
        @NotNull final List<BlockPos> delayedBlocks = new ArrayList<>();
        final BlockPos endPos = new BlockPos(this.structure.getWidth(), this.structure.getHeight(), this.structure.getLength());
        BlockPos currentPos = new BlockPos(inputPos.getX(), inputPos.equals(BlockPos.ZERO) ? endPos.getY() - 1 : inputPos.getY(), inputPos.getZ());
        int count = 0;

        for (int y = currentPos.getY(); y >= 0; y--)
        {
            for (int x = currentPos.getX(); x < endPos.getX(); x++)
            {
                for (int z = currentPos.getZ(); z < endPos.getZ(); z++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(x, y, z);
                    final BlockState localState = structure.getBlockState(localPos);
                    if (localState == null)
                    {
                        continue;
                    }

                    final BlockPos worldPos = structure.getPosition().add(localPos);

                    if (!complete && skipIfNotComplete.apply(structure, localPos))
                    {
                        continue;
                    }
                    count++;

                    if (storage != null)
                    {
                        storage.addPositionStorage(worldPos, world);
                    }

                    if (localState.getMaterial().isSolid())
                    {
                        handleBlockPlacement(world, worldPos, localState, complete, structure.getTileEntityData(localPos));
                    }
                    else
                    {
                        BlockUtils.removeFluid(world, worldPos);
                        delayedBlocks.add(localPos);
                    }

                    if (count >= Structurize.getConfig().getCommon().maxOperationsPerTick.get())
                    {
                        handleDelayedBlocks(delayedBlocks, storage, world);
                        return new BlockPos(x, y, z);
                    }
                }
                currentPos = new BlockPos(x, y, 0);
            }
            currentPos = new BlockPos(0, y, 0);
        }
        handleDelayedBlocks(delayedBlocks, storage, world);

        for (final CompoundNBT compound : this.structure.getEntityData())
        {
            if (compound != null)
            {
                try
                {
                    final BlockPos pos = this.structure.getPosition();

                    final Optional<EntityType<?>> type = EntityType.readEntityType(compound);
                    if (type.isPresent())
                    {
                        final Entity entity = type.get().create(world);
                        if (entity != null)
                        {
                            entity.deserializeNBT(compound);

                            entity.setUniqueId(UUID.randomUUID());
                            final Vec3d worldPos = entity.getPositionVector().add(pos.getX(), pos.getY(), pos.getZ());
                            entity.setPosition(worldPos.x, worldPos.y, worldPos.z);

                            world.addEntity(entity);
                            if (storage != null)
                            {
                                storage.addToBeKilledEntity(entity);
                            }
                        }
                    }
                }
                catch (final RuntimeException e)
                {
                    Log.getLogger().info("Couldn't restore entity", e);
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
            final BlockState localState = structure.getBlockState(coords);
            final BlockPos newWorldPos = structure.getPosition().add(coords);
            if (storage != null)
            {
                storage.addPositionStorage(coords, world);
            }
            final BlockInfo info = this.structure.getBlockInfo(coords);
            handleBlockPlacement(world, newWorldPos, localState, this.complete, info == null ? null : info.getTileEntityData());
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
    public void handleBlockPlacement(
        final World world,
        final BlockPos pos,
        final BlockState localState,
        final boolean complete,
        final CompoundNBT tileEntityData)
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

                    final BlockState worldState = structure.getWorld().getBlockState(worldPos);
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

    /**
     * Checks whether this placement is complete.
     * 
     * @return whether this placement is complete.
     */
    public boolean getComplete()
    {
        return complete;
    }
}
