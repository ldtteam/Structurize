package com.ldtteam.structurize.util;

import com.ldtteam.structures.helpers.BlueprintIterator;
import com.ldtteam.structures.helpers.CreativeStructureHandler;
import com.ldtteam.structures.helpers.IStructureHandler;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.placementhandlers.IPlacementHandler;
import com.ldtteam.structurize.placementhandlers.PlacementHandlers;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.IBucketPickupHandler;
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
     * The structure iterator.
     */
    protected final BlueprintIterator iterator;

    /**
     * The handler.
     */
    protected final IStructureHandler handler;

    /**
     * If complete or not.
     */
    private boolean complete = false;

    /**
     * Create a new instant structure placer.
     *
     * @param handler the structure handler.
     */
    public InstantStructurePlacer(final IStructureHandler handler)
    {
        this.iterator = new BlueprintIterator(handler);
        this.handler = handler;
    }

    /**
     * Remove a structure from the world.
     *
     * @param pos coordinates
     */
    public void removeStructure(@NotNull final BlockPos pos)
    {
        iterator.setProgressPos(new BlockPos(0, 0, 0));
        for (int j = 0; j < this.handler.getBluePrint().getSizeY(); j++)
        {
            for (int k = 0; k < this.handler.getBluePrint().getSizeZ(); k++)
            {
                for (int i = 0; i < this.handler.getBluePrint().getSizeZ(); i++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(i, j, k);
                    final BlockPos worldPos = pos.add(localPos);
                    if (!handler.getWorld().isAirBlock(worldPos))
                    {
                        handler.getWorld().removeBlock(worldPos, false);
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
            @NotNull final IStructureHandler structure = new CreativeStructureHandler(worldObj, pos, name, new PlacementSettings(mirror, rotation));
            structure.getBluePrint().rotateWithMirror(rotation, mirror, worldObj);

            @NotNull final InstantStructurePlacer instantPlacer = new InstantStructurePlacer(structure);
            instantPlacer.setupStructurePlacement(complete, player);
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
    }

    /**
     * Setup the structure placement and add to buffer.
     *
     * @param complete if complete or not.
     * @param player   the issuing player.
     */
    public void setupStructurePlacement(final boolean complete, final ServerPlayerEntity player)
    {
        this.complete = complete;
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
        return placeStructure(world, storage, inputPos, (structure, pos) -> structure.getBluePrint()
          .getBlockState(pos).getBlock() == ModBlocks.blockSubstitution || structure.getBluePrint().getBlockState(pos).getBlock() instanceof IAnchorBlock);
    }

    /**
     * Place a structure into the world.
     * 
     * @param world             the placing player.
     * @param storage           storage the change storage.
     * @param inputPos          the start pos.
     * @param skipIfNotComplete a function that determines whether to skip a block
     *                          if complete is false.
     * @return the last pos.
     */
    public BlockPos placeStructure(final World world, final ChangeStorage storage, final BlockPos inputPos, BiFunction<IStructureHandler, BlockPos, Boolean> skipIfNotComplete)
    {
        iterator.setProgressPos(inputPos);
        @NotNull final List<BlockPos> delayedBlocks = new ArrayList<>();
        final BlockPos endPos = new BlockPos(this.handler.getBluePrint().getSizeX(), this.handler.getBluePrint().getSizeY(), this.handler.getBluePrint().getSizeZ());
        BlockPos currentPos = new BlockPos(0, inputPos.getY(), 0);
        for (int y = currentPos.getY(); y < endPos.getY(); y++)
        {
            for (int x = currentPos.getX(); x < endPos.getX(); x++)
            {
                for (int z = currentPos.getZ(); z < endPos.getZ(); z++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(x, y, z);
                    final BlockState localState = handler.getBluePrint().getBlockState(localPos);
                    if (localState == null)
                    {
                        continue;
                    }

                    final BlockPos worldPos = handler.getPosition().add(localPos).subtract(handler.getBluePrint().getPrimaryBlockOffset());
                    final BlockState worldState = world.getBlockState(worldPos);
                    // checking whether the fluid is the same overall likely cause a bigger performance impact than just removing it
                    // and replacing it in the rare cases where it would have matched.
                    if (worldState.getBlock() instanceof IBucketPickupHandler || worldState.getBlock() instanceof FlowingFluidBlock
                      && localState.getBlock() != ModBlocks.blockSubstitution)
                    {
                        BlockUtils.removeFluid(world, worldPos);
                    }
                }
                currentPos = new BlockPos(x, y, 0);
            }
            currentPos = new BlockPos(0, y, 0);
        }

        currentPos = new BlockPos(inputPos.getX(), inputPos.getY(), inputPos.getZ());
        int count = 0;
        for (int y = currentPos.getY(); y < endPos.getY(); y++)
        {
            for (int x = currentPos.getX(); x < endPos.getX(); x++)
            {
                for (int z = currentPos.getZ(); z < endPos.getZ(); z++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(x, y, z);
                    final BlockState localState = handler.getBluePrint().getBlockState(localPos);
                    if (localState == null)
                    {
                        continue;
                    }

                    final BlockPos worldPos = handler.getPosition().add(localPos).subtract(handler.getBluePrint().getPrimaryBlockOffset());

                    if (!complete && skipIfNotComplete.apply(handler, localPos))
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
                        handleBlockPlacement(world, worldPos, localState, complete, handler.getBluePrint().getTileEntityData(worldPos, localPos));
                    }
                    else
                    {
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

        for (final CompoundNBT compound : this.handler.getBluePrint().getEntities())
        {
            if (compound != null)
            {
                try
                {
                    final BlockPos pos = this.handler.getPosition().subtract(handler.getBluePrint().getPrimaryBlockOffset());

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
            final BlockState localState = handler.getBluePrint().getBlockState(coords);
            final BlockPos newWorldPos = handler.getPosition().add(coords).subtract(handler.getBluePrint().getPrimaryBlockOffset());
            if (storage != null)
            {
                storage.addPositionStorage(coords, world);
            }
            final BlockInfo info = this.handler.getBluePrint().getBlockInfoAsMap().get(coords);
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
                handlers.handle(world, pos, localState, tileEntityData, complete, handler.getPosition(), handler.getSettings());
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
        iterator.setProgressPos(new BlockPos(0, 0, 0));
        for (int j = 0; j < this.handler.getBluePrint().getSizeY(); j++)
        {
            for (int k = 0; k < this.handler.getBluePrint().getSizeZ(); k++)
            {
                for (int i = 0; i < this.handler.getBluePrint().getSizeX(); i++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(i, j, k);

                    final BlockPos worldPos = pos.add(localPos);

                    if (worldPos.getY() <= pos.getY() && !handler.getWorld().getBlockState(worldPos.down()).getMaterial().isSolid())
                    {
                        return false;
                    }

                    final BlockState worldState = handler.getWorld().getBlockState(worldPos);
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
