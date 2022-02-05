package com.ldtteam.structurize.placement;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.helpers.WallExtents;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.TickedWorldOperation;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Utility methods related to structure placement.
 */
public class StructurePlacementUtils
{
    /**
     * Unload a structure at a certain location.
     *
     * @param world    the world.
     * @param startPos the position.
     * @param name     the name.
     * @param settings the placement settings.
     */
    public static void unloadStructure(final Level world, final BlockPos startPos, final String name, final PlacementSettings settings)
    {
        final IStructureHandler structure = new CreativeStructureHandler(world, startPos, name, settings, false);
        structure.getBluePrint().rotateWithMirror(settings.getRotation(), settings.getMirror(), world);
        structure.setBlueprint(BlueprintUtil.createWall(structure.getBluePrint(), settings.getWallExtents()));

        final StructurePlacer placer = new StructurePlacer(structure);
        placer.executeStructureStep(world, null, new BlockPos(0, 0, 0), StructurePlacer.Operation.BLOCK_REMOVAL,
          () ->  placer.getIterator().increment((info, pos, handler) -> handler.getWorld().getBlockState(pos).getBlock() instanceof AirBlock), true);
    }

    /**
     * Load a structure into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj the world to load it in
     * @param name     the structures name
     * @param pos      coordinates
     * @param settings the placement settings.
     * @param fancyPlacement if fancy or complete.
     * @param player   the placing player.
     */
    public static void loadAndPlaceStructureWithRotation(
            final Level worldObj, final String name,
            final BlockPos pos, final PlacementSettings settings,
            final boolean fancyPlacement,
            final ServerPlayer player)
    {
        try
        {
            final IStructureHandler structure = new CreativeStructureHandler(worldObj, pos, name, settings, fancyPlacement);
            structure.getBluePrint().rotateWithMirror(settings.getRotation(), settings.getMirror(), worldObj);
            structure.setBlueprint(BlueprintUtil.createWall(structure.getBluePrint(), settings.getWallExtents()));

            final StructurePlacer instantPlacer = new StructurePlacer(structure);
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
     * @param settings the placement settings.
     * @param fancyPlacement if fancy or complete.
     * @param player   the placing player.
     */
    public static void loadAndPlaceStructureWithRotation(
      final Level worldObj, final Blueprint blueprint,
      final BlockPos pos, final PlacementSettings settings,
      final boolean fancyPlacement,
      final ServerPlayer player)
    {
        try
        {
            final IStructureHandler structure = new CreativeStructureHandler(worldObj, pos, blueprint, settings, fancyPlacement);
            if (fancyPlacement)
            {
                structure.fancyPlacement();
            }
            structure.getBluePrint().rotateWithMirror(settings.getRotation(), settings.getMirror(), worldObj);
            structure.setBlueprint(BlueprintUtil.createWall(structure.getBluePrint(), settings.getWallExtents()));

            final StructurePlacer instantPlacer = new StructurePlacer(structure);
            Manager.addToQueue(new TickedWorldOperation(instantPlacer, player));
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
    }

}
