package com.ldtteam.structurize.util;

import com.ldtteam.structurize.helpers.WallExtents;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/**
 * Placement settings for the blueprints.
 */
public class PlacementSettings
{
    /**
     * The currently used mirror of the settings.
     */
    public Mirror mirror = Mirror.NONE;

    /**
     * The currently used rotation of the settings.
     */
    public Rotation rotation = Rotation.NONE;

    /**
     * The currently used wall extents.
     */
    public WallExtents wall = new WallExtents();

    /**
     * Create a new empty placement settings object.
     */
    public PlacementSettings()
    {
        /*
         * Nothing to do here.
         */
    }

    /**
     * Create a new placement settings object with specific setting.
     * @param mirror the mirror.
     * @param rotation the rotation.
     * @param wall the wall extents.
     */
    public PlacementSettings(final Mirror mirror, final Rotation rotation, final WallExtents wall)
    {
        this.mirror = mirror;
        this.rotation = rotation;
        this.wall = wall;
    }

    public Mirror getMirror()
    {
        return mirror;
    }

    public void setMirror(final Mirror mirror)
    {
        this.mirror = mirror;
    }

    public Rotation getRotation()
    {
        return rotation;
    }

    public void setRotation(final Rotation rotation)
    {
        this.rotation = rotation;
    }

    public WallExtents getWallExtents()
    {
        return wall;
    }

    public void setWallExtents(final WallExtents wall)
    {
        this.wall = wall;
    }
}
