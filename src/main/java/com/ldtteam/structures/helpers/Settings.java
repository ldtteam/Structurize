package com.ldtteam.structures.helpers;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.Shape;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Class used to store.
 */
public final class Settings implements INBTSerializable<CompoundTag>
{
    /**
     * Single instance of this class.
     */
    public static final Settings instance = new Settings();

    /**
     * The position of the structure.
     */
    private BlockPos  pos           = null;
    private boolean   isMirrored    = false;
    @Nullable
    private Blueprint blueprint     = null;
    private int       rotation      = 0;
    private String    structureName = null;
    private Optional<BlockPos> anchorPos = Optional.empty();

    /**
     * The style index to use currently.
     */
    private String style = "";

    /**
     * Shape variables.
     */
    private int     width  = 1;
    private int     height = 1;
    private int     length = 1;
    private int     frequency = 1;
    private String  equation = "";

    private boolean hollow = false;

    /**
     * The default shape to use.
     */
    private Shape shape = Shape.CUBE;

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
    private String staticSchematicName = null;

    /**
     * The stack used to present blocks.
     */
    private Tuple<ItemStack, ItemStack> stack = new Tuple<>(new ItemStack(Blocks.GOLD_BLOCK), new ItemStack(Blocks.GOLD_BLOCK));

    /**
     * If data has to be refreshed.
     */
    private boolean shouldRefresh = false;

    /**
     * If received info.
     */
    private boolean receivedInfo;

    /**
     * If we are in structurize rendering phase.
     */
    private boolean isStructurizePass = false;

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
     * Called when structurize renders it's things
     */
    public void startStructurizePass()
    {
        isStructurizePass = true;
    }

    /**
     * Called when structurize finishes rendering of it's things
     */
    public void endStructurizePass()
    {
        isStructurizePass = false;
    }

    /**
     * @return true if in structurize rendering phase (events), false if other mods are rendering using structurize api
     */
    public boolean isStructurizePass()
    {
        return isStructurizePass;
    }

    public boolean shouldRefresh()
    {
        if (!isStructurizePass())
        {
            return false;
        }

        final boolean ret = shouldRefresh;
        shouldRefresh = false;
        return ret;
    }

    public void scheduleRefresh()
    {
        shouldRefresh = true;
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
     * @param box, the box which should be drawn (two positions).
     */
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
     * set the frequency.
     *
     * @param frequency the height
     */
    public void setFrequency(final int frequency)
    {
        this.frequency = frequency;
    }

    /**
     * get the width.
     *
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
     * get the frequency.
     *
     * @return the height
     */
    public int getFrequency()
    {
        return this.frequency;
    }

    /**
     * Set location to render current schematic.
     *
     * @param pos location to render.
     */
    public void moveTo(final BlockPos pos)
    {
        if (this.blueprint == null)
        {
            return;
        }
        this.pos = this.pos.offset(pos);
    }

    /**
     * @return The schematic we are currently rendering.
     */
    @Nullable
    public Blueprint getActiveStructure()
    {
        return this.blueprint;
    }

    /**
     * Set a structure to render.
     *
     * @param blueprint structure to render.
     */
    public void setActiveSchematic(final Blueprint blueprint)
    {
        if (blueprint == null)
        {
            reset();
        }
        else
        {
            this.blueprint = blueprint;
            this.blueprint.rotateWithMirror(BlockPosUtil.getRotationFromRotations(rotation), isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, Minecraft.getInstance().level);
        }
    }

    /**
     * Reset the schematic rendering.
     */
    public void reset()
    {
        blueprint = null;
        rotation = 0;
        isMirrored = false;
        staticSchematicMode = false;
        staticSchematicName = null;
        hollow = false;
        structureName = null;
        pos = null;
        box = null;
        equation = "";
    }

    /**
     * Reset the schematic rendering.
     */
    public void softReset()
    {
        blueprint = null;
        staticSchematicMode = false;
        staticSchematicName = null;
        hollow = false;
        pos = null;
        box = null;
        equation = "";
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
        int offset = rotation - this.rotation;

        this.rotation = rotation;
        if (blueprint != null)
        {
            blueprint.rotateWithMirror(offset == 1 || offset == -3 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90, Mirror.NONE, Minecraft.getInstance().level);
        }
        scheduleRefresh();
    }

    /**
     * Makes the building mirror.
     */
    public void mirror()
    {
        if (blueprint == null)
        {
            return;
        }

        isMirrored = !isMirrored;
        blueprint.rotateWithMirror(Rotation.NONE, this.rotation % 2 == 0 ? Mirror.FRONT_BACK : Mirror.LEFT_RIGHT, Minecraft.getInstance().level);
        scheduleRefresh();
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

    /**
     * Sets the current shape.
     *
     * @param s the name of the shape.
     */
    public void setShape(final String s)
    {
        shape = Shape.valueOf(s);
    }

    /**
     * Get the current shape.
     *
     * @return the shape.
     */
    public Shape getShape()
    {
        return shape;
    }

    /**
     * Sets the current block.
     *
     * @param s the itemStack.
     * @param mainBlock the main block.
     */
    public void setBlock(final ItemStack s, final boolean mainBlock)
    {
        if (mainBlock)
        {
            this.stack = new Tuple<>(s, this.stack.getB());
        }
        else
        {
            this.stack = new Tuple<>(this.stack.getA(), s);
        }
    }

    /**
     * Get the current block.
     * @param main if main block or fill block.
     * @return the shape.
     */
    public ItemStack getBlock(final boolean main)
    {
        return main ? this.stack.getA() : this.stack.getB();
    }

    /**
     * Check if the shape should be hollow.
     * @return true if so.
     */
    public boolean isHollow()
    {
        return hollow;
    }

    /**
     * Set the structure to be hollow or full.
     * @param hollow true if hollow.
     */
    public void setHollow(final boolean hollow)
    {
        this.hollow = hollow;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        isMirrored = nbt.getBoolean("mirror");
        staticSchematicMode = nbt.getBoolean("static");
        hollow = nbt.getBoolean("hollow");

        rotation = nbt.getInt("rot");
        width = nbt.getInt("w");
        height = nbt.getInt("h");
        length = nbt.getInt("len");
        frequency = nbt.getInt("freq");

        // enums

        if (nbt.contains("shape"))
        {
            shape = Shape.values()[nbt.getInt("shape")];
        }
        else
        {
            shape = Shape.CUBE;
        }

        if (nbt.contains("pos"))
        {
            pos = NbtUtils.readBlockPos(nbt.getCompound("pos"));
        }
        else
        {
            pos = null;
        }

        if (nbt.contains("box"))
        {
            box = new Tuple<>(NbtUtils.readBlockPos(nbt.getCompound("box")), NbtUtils.readBlockPos(nbt.getCompound("box2")));
        }
        else
        {
            box = null;
        }

        if (nbt.contains("struct_name"))
        {
            structureName = nbt.getString("struct_name");
        }
        else
        {
            structureName = null;
        }

        if (nbt.contains("static_name"))
        {
            staticSchematicName = nbt.getString("static_name");
        }
        else
        {
            staticSchematicName = null;
        }

        // itemstack

        if (nbt.contains("stack"))
        {
            stack = new Tuple<>(ItemStack.of(nbt.getCompound("stack")), ItemStack.of(nbt.getCompound("stack2")));
        }
        else
        {
            stack = new Tuple<>(new ItemStack(Blocks.GOLD_BLOCK), new ItemStack(Blocks.GOLD_BLOCK));
        }

        if (nbt.contains("equa"))
        {
            equation = nbt.getString("equa");
        }

        if (nbt.contains("anch_pos"))
        {
            anchorPos = Optional.of(NbtUtils.readBlockPos(nbt.getCompound("anch_pos")));
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag nbt = new CompoundTag();

        nbt.putBoolean("mirror", isMirrored);
        nbt.putBoolean("static", staticSchematicMode);
        nbt.putBoolean("hollow", hollow);

        nbt.putInt("rot", rotation);
        nbt.putInt("w", width);
        nbt.putInt("h", height);
        nbt.putInt("len", length);
        nbt.putInt("freq", frequency);

        // enums

        if (shape != null)
        {
            nbt.putInt("shape", shape.ordinal());
        }

        if (pos != null)
        {
            nbt.put("pos", NbtUtils.writeBlockPos(pos));
        }

        if (box != null)
        {
            nbt.put("box", NbtUtils.writeBlockPos(box.getA()));
            nbt.put("box2", NbtUtils.writeBlockPos(box.getB()));
        }

        // strings

        if (structureName != null)
        {
            nbt.putString("struct_name", structureName);
        }

        if (staticSchematicName != null)
        {
            nbt.putString("static_name", staticSchematicName);
        }

        // itemstacks

        if (stack != null)
        {
            nbt.put("stack", stack.getA().serializeNBT());
            nbt.put("stack2", stack.getB().serializeNBT());
        }

        if (!equation.isEmpty())
        {
            nbt.putString("equa", equation);
        }

        anchorPos.ifPresent(anch_pos -> nbt.put("anch_pos", NbtUtils.writeBlockPos(anch_pos)));

        return nbt;
    }

    /**
     * Sets the equation of the random shape.
     * @param localEquation the equation to set.
     */
    public void setEquation(final String localEquation)
    {
        this.equation = localEquation;
    }

    /**
     * Getter for the equation of the random shape.
     * @return the String.
     */
    public String getEquation()
    {
        return this.equation;
    }

    /**
     * Get the current style to use.
     * @return the current style.
     */
    public String getStyle()
    {
        return style;
    }

    /**
     * Set the current style to use.
     * @param style the to set.
     */
    public void setStyle(final String style)
    {
        this.style = style;
    }

    /**
     * Get the currently Anchor Position
     * @return Optional anchor position
     */
    public Optional<BlockPos> getAnchorPos()
    {
        return anchorPos;
    }

    /**
     * Set the currently Anchor Position
     * @param anchorPos Optional anchor position
     */
    public void setAnchorPos(final Optional<BlockPos> anchorPos)
    {
        this.anchorPos = anchorPos;
    }

    /**
     * Check if the player got the info message already.
     * @return true if so.
     */
    public boolean hasReceivedInfo()
    {
        return this.receivedInfo;
    }

    /**
     * Set that the player received the info already this session.
     */
    public void setReceivedInfo()
    {
        this.receivedInfo = true;
    }
}
