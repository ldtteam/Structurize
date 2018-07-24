package com.structurize.structures.helpers;

import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used to store.
 */
public final class Settings
{
    /**
     * Single instance of this class.
     */
    public static final Settings                 instance       = new Settings();
    private final       BlockPos.MutableBlockPos offset         = new BlockPos.MutableBlockPos();
    /**
     * The position of the structure.
     */
    private             BlockPos                 pos            = null;
    private             boolean                  isMirrored     = false;
    @Nullable
    private             Structure                structure      = null;
    private             int                      rotation       = 0;
    private             String                   structureName  = null;
    private             boolean                  isPendingReset = false;
    private             int                      width          = 1;
    private             int                      height         = 1;
    private             int                      length         = 1;

    /**
     * Possible box.
     */
    private Tuple<BlockPos, BlockPos> box = null;

    /**
     * Check if the tool is in the static schematic mode.
     */
    private boolean staticSchematicMode = false;

    /**
     * Name of the static schematic if existent.
     */
    private String staticSchematicName = "";

    /**
     * Private constructor to hide implicit one.
     */
    private Settings()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Set up the static mode.
     *
     * @param name the name of the schematic.
     */
    public void setupStaticMode(final String name)
    {
        this.staticSchematicMode = true;
        this.staticSchematicName = name;
    }

    /**
     * set the position.
     *
     * @return the position
     */
    public BlockPos getPosition()
    {
        return pos;
    }

    /**
     * Get a possibly existing box.
     *
     * @return a blockpos tuple.
     */
    @Nullable
    public Tuple<BlockPos, BlockPos> getBox()
    {
        return box;
    }

    /**
     * Set a possible box.
     */
    @NotNull
    public void setBox(final Tuple<BlockPos, BlockPos> box)
    {
        this.box = box;
    }

    /**
     * set the position.
     *
     * @param position to render
     */
    public void setPosition(final BlockPos position)
    {
        pos = position;
    }

    /**
     * set the width.
     *
     * @param width the width
     */
    public void setWidth(final int width)
    {
        this.width = width;
    }

    /**
     * set the length.
     *
     * @param length the length
     */
    public void setLength(final int length)
    {
        this.length = length;
    }

    /**
     * set the height.
     *
     * @param height the height
     */
    public void setHeight(final int height)
    {
        this.height = height;
    }

    /**
     * get the width.
     * @return the width
     */
    public int getWidth()
    {
        return this.width;
    }

    /**
     * get the length.
     *
     * @return the length
     */
    public int getLength()
    {
        return this.length;
    }

    /**
     * get the height.
     *
     * @return the height
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Set location to render current schematic.
     *
     * @param pos location to render.
     */
    public void moveTo(final BlockPos pos)
    {
        if (this.structure == null)
        {
            return;
        }
        this.pos = this.pos.add(pos);
    }

    /**
     * @return The schematic we are currently rendering.
     */
    @Nullable
    public Structure getActiveStructure()
    {
        if (structure != null && structure.isTemplateMissing())
        {
            this.structure = null;
        }

        return this.structure;
    }

    /**
     * Set a structure to render.
     *
     * @param structure structure to render.
     */
    public void setActiveSchematic(final Structure structure)
    {
        if (structure == null)
        {
            reset();
        }
        else
        {
            this.structure = structure;
        }
    }

    /**
     * Reset the schematic rendering.
     */
    public void reset()
    {
        structure = null;
        isPendingReset = false;
        offset.setPos(0, 0, 0);
        rotation = 0;
        isMirrored = false;
        staticSchematicMode = false;
        staticSchematicName = "";
    }

    /**
     * Saves the schematic info when the client closes the build tool window.
     *
     * @param structureName name of the structure.
     * @param rotation      The number of times the building is rotated.
     */
    public void setSchematicInfo(final String structureName, final int rotation)
    {
        this.structureName = structureName;
        this.rotation = rotation;
    }

    /**
     * @return the structure name currently used.
     */
    public String getStructureName()
    {
        return structureName;
    }

    public void setStructureName(final String structureName)
    {
        this.structureName = structureName;
    }

    /**
     * @return The number of times the schematic is rotated.
     */
    public int getRotation()
    {
        return rotation;
    }

    /**
     * Sets the rotation.
     *
     * @param rotation the rotation to set.
     */
    public void setRotation(final int rotation)
    {
        this.rotation = rotation;
    }

    /**
     * Call reset next tick.
     */
    public void markDirty()
    {
        isPendingReset = true;
    }

    /**
     * @return true if Settings should be reset.
     */
    public boolean isDirty()
    {
        return isPendingReset;
    }

    /**
     * Makes the building mirror.
     */
    public void mirror()
    {
        if (structure == null)
        {
            return;
        }
        isMirrored = !isMirrored;

        structure.setPlacementSettings(structure.getSettings().setMirror(getMirror()));
    }

    /**
     * Get the mirror.
     *
     * @return the mirror object.
     */
    public Mirror getMirror()
    {
        if (isMirrored)
        {
            return Mirror.FRONT_BACK;
        }
        else
        {
            return Mirror.NONE;
        }
    }

    /**
     * Check if static mode.
     *
     * @return true if so.
     */
    public boolean isStaticSchematicMode()
    {
        return staticSchematicMode;
    }

    /**
     * Get the schematic name of the static mode.
     *
     * @return the string.
     */
    public String getStaticSchematicName()
    {
        return staticSchematicName;
    }
}
