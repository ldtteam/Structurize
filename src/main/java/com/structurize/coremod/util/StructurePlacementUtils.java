package com.structurize.coremod.util;

import com.structurize.api.util.Log;
import com.structurize.structures.helpers.Structure;
import com.structurize.structures.helpers.StructureProxy;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
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
     * @param first    the name.
     * @param rotation the rotation.
     * @param mirror   the mirror.
     */
    public static void unloadStructure(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final String first, final int rotation, @NotNull final Mirror mirror)
    {
        @NotNull final StructureWrapper structureWrapper = new StructureWrapper(world, first);
        structureWrapper.position = pos;
        structureWrapper.rotate(rotation, world, pos, mirror);
        structureWrapper.removeStructure(pos.subtract(structureWrapper.getOffset()));
    }

    /**
     * Load a structure into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj  the world to load it in
     * @param pos       coordinates
     * @param rotations number of times rotated
     * @param mirror    the mirror used.
     * @param player    the placing player.
     */
    public static void loadAndPlaceShapeWithRotation(
      final WorldServer worldObj,
      final Template template,
      @NotNull final BlockPos pos, final int rotations, @NotNull final Mirror mirror, final EntityPlayerMP player)
    {
        try
        {
            final Structure structure = new Structure(worldObj);
            StructureProxy proxy = new StructureProxy(structure);
            structure.setBluePrint(template);
            structure.setPlacementSettings(new PlacementSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE));
            @NotNull final StructureWrapper structureWrapper = new StructureWrapper(worldObj, proxy, "shape" + player.getName() + ".nbt");
            structureWrapper.position = pos;
            structureWrapper.rotate(rotations, worldObj, pos, mirror);
            structureWrapper.setupStructurePlacement(pos.subtract(structureWrapper.getOffset()), false, player);
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
     * @param worldObj  the world to load it in
     * @param name      the structures name
     * @param pos       coordinates
     * @param rotations number of times rotated
     * @param mirror    the mirror used.
     * @param complete  paste it complete (with structure blocks) or without
     * @param player    the placing player.
     */
    public static void loadAndPlaceStructureWithRotation(
      final World worldObj, @NotNull final String name,
      @NotNull final BlockPos pos, final int rotations, @NotNull final Mirror mirror,
      final boolean complete, final EntityPlayerMP player)
    {
        try
        {
            @NotNull final StructureWrapper structureWrapper = new StructureWrapper(worldObj, name);
            structureWrapper.position = pos;
            structureWrapper.rotate(rotations, worldObj, pos, mirror);
            structureWrapper.setupStructurePlacement(pos.subtract(structureWrapper.getOffset()), complete, player);
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
    }
}
