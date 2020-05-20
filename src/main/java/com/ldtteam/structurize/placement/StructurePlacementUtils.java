package com.ldtteam.structurize.placement;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.TickedWorldOperation;
import net.minecraft.block.AirBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Utility methods related to structure placement.
 */
public class StructurePlacementUtils
{
    /**
     * Unload a structure at a certain location.
     *
     * @param world    the world.
     * @param startPos      the position.
     * @param name    the name.
     * @param rotation the rotation.
     * @param mirror   the mirror.
     */
    public static void unloadStructure(@NotNull final World world, @NotNull final BlockPos startPos, @NotNull final String name, final Rotation rotation, @NotNull final Mirror mirror)
    {
        @NotNull final IStructureHandler structure = new CreativeStructureHandler(world, startPos, name, new PlacementSettings(mirror, rotation), false);
        structure.getBluePrint().rotateWithMirror(rotation, mirror, world);

        @NotNull final StructurePlacer placer = new StructurePlacer(structure);
        placer.executeStructureStep(world, null, new BlockPos(0, 0, 0), StructurePlacer.Operation.BLOCK_REMOVAL,
          () ->  placer.getIterator().increment((info, pos, theWorld) -> theWorld.getBlockState(pos).getBlock() instanceof AirBlock), true);
    }

    /**
     * Load a structure into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj the world to load it in
     * @param name     the structures name
     * @param pos      coordinates
     * @param rotation the rotation.
     * @param mirror   the mirror used.
     * @param fancyPlacement if fancy or complete.
     * @param player   the placing player.
     */
    public static void loadAndPlaceStructureWithRotation(
      final World worldObj, @NotNull final String name,
      @NotNull final BlockPos pos, final Rotation rotation,
      @NotNull final Mirror mirror,
      final boolean fancyPlacement,
      final ServerPlayerEntity player)
    {
        try
        {
            @NotNull final IStructureHandler structure = new CreativeStructureHandler(worldObj, pos, name, new PlacementSettings(mirror, rotation), fancyPlacement);
            structure.getBluePrint().rotateWithMirror(rotation, mirror, worldObj);

            @NotNull final StructurePlacer instantPlacer = new StructurePlacer(structure);
            Manager.addToQueue(new TickedWorldOperation(instantPlacer, player));
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
    }

    /**
     * Load a structure into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj the world to load it in
     * @param blueprint the structures blueprint
     * @param pos      coordinates
     * @param rotation the rotation.
     * @param mirror   the mirror used.
     * @param fancyPlacement if fancy or complete.
     * @param player   the placing player.
     */
    public static void loadAndPlaceStructureWithRotation(
      final World worldObj, @NotNull final Blueprint blueprint,
      @NotNull final BlockPos pos, final Rotation rotation,
      @NotNull final Mirror mirror,
      final boolean fancyPlacement,
      final ServerPlayerEntity player)
    {
        try
        {
            @NotNull final IStructureHandler structure = new CreativeStructureHandler(worldObj, pos, blueprint, new PlacementSettings(mirror, rotation), fancyPlacement);
            if (fancyPlacement)
            {
                structure.fancyPlacement();
            }
            structure.getBluePrint().rotateWithMirror(rotation, mirror, worldObj);

            @NotNull final StructurePlacer instantPlacer = new StructurePlacer(structure);
            Manager.addToQueue(new TickedWorldOperation(instantPlacer, player));
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
    }

}
