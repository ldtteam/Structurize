package com.ldtteam.structurize.util;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.helpers.CreativeStructureHandler;
import com.ldtteam.structures.helpers.IStructureHandler;
import com.ldtteam.structurize.api.util.Log;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
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
     * @param pos      the position.
     * @param name    the name.
     * @param rotation the rotation.
     * @param mirror   the mirror.
     */
    public static void unloadStructure(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final String name, final Rotation rotation, @NotNull final Mirror mirror)
    {
        @NotNull final IStructureHandler structure = new CreativeStructureHandler(world, pos, name, new PlacementSettings(mirror, rotation));
        structure.getBluePrint().rotateWithMirror(rotation, mirror, world);

        @NotNull final InstantStructurePlacer instantPlacer = new InstantStructurePlacer(structure);
        instantPlacer.removeStructure(pos.subtract(structure.getBluePrint().getPrimaryBlockOffset()));
    }

    /**
     * Load a structure into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj the world to load it in
     * @param blueprint the blueprint to load and place.
     * @param pos      coordinates
     * @param rotation the rotation.
     * @param mirror   the mirror used.
     * @param player   the placing player.
     */
    public static void loadAndPlaceShapeWithRotation(
      final ServerWorld worldObj,
      final Blueprint blueprint,
      @NotNull final BlockPos pos,
      final Rotation rotation,
      @NotNull final Mirror mirror,
      final ServerPlayerEntity player)
    {
        try
        {
            @NotNull final IStructureHandler structure = new CreativeStructureHandler(worldObj, pos, blueprint, new PlacementSettings(mirror, rotation));
            structure.getBluePrint().rotateWithMirror(rotation, mirror, worldObj);

            @NotNull final InstantStructurePlacer structureWrapper = new InstantStructurePlacer(structure);
            structureWrapper.setupStructurePlacement(false, player);
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
    }
}
