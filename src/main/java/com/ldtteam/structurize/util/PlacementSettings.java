package com.ldtteam.structurize.util;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.helpers.WallExtents;
import net.minecraft.network.FriendlyByteBuf;
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

    /**
     * Create a new placement settings object with legacy settings.
     * @param mirror true to mirror.
     * @param rotation number of times to rotate clockwise.
     * @param wall the wall extents.
     */
    public PlacementSettings(final boolean mirror, final int rotation, final WallExtents wall)
    {
        this(mirror ? Mirror.FRONT_BACK : Mirror.NONE, BlockPosUtil.getRotationFromRotations(rotation), wall);
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

    public static PlacementSettings read(final FriendlyByteBuf buf)
    {
        final Mirror mirror = buf.readEnum(Mirror.class);
        final Rotation rotation = buf.readEnum(Rotation.class);
        final WallExtents wall = WallExtents.read(buf);

        return new PlacementSettings(mirror, rotation, wall);
    }

    public void write(final FriendlyByteBuf buf)
    {
        buf.writeEnum(this.mirror);
        buf.writeEnum(this.rotation);
        wall.write(buf);
    }

    // the nbt read/write variants are deliberately not implemented here, to make
    // you think about upgrade/migration a bit more ... and also because there's a
    // backwards compatibility issue with Minecolonies work order persistence
}
