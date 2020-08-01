package com.ldtteam.structures.helpers;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.Shape;
import com.ldtteam.structurize.network.messages.LSStructureDisplayerMessage;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Class used to store.
 */
public final class Settings
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

    public void startStructurizePass()
    {
        isStructurizePass = true;
    }

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
        this.pos = this.pos.add(pos);
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
            this.blueprint.rotateWithMirror(BlockPosUtil.getRotationFromRotations(rotation), isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, Minecraft.getInstance().world);
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
            blueprint.rotateWithMirror(offset == 1 || offset == -3 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90, Mirror.NONE, Minecraft.getInstance().world);
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
        blueprint.rotateWithMirror(Rotation.NONE, this.rotation % 2 == 0 ? Mirror.FRONT_BACK : Mirror.LEFT_RIGHT, Minecraft.getInstance().world);
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

    /**
     * Serializable from {@link LSStructureDisplayerMessage}
     * @param buf the packet buffer to read it from.
     */
    public void fromBytes(final PacketBuffer buf)
    {
        isMirrored = buf.readBoolean();
        staticSchematicMode = buf.readBoolean();
        hollow = buf.readBoolean();

        rotation = buf.readInt();
        width = buf.readInt();
        height = buf.readInt();
        length = buf.readInt();
        frequency = buf.readInt();

        // enums

        if (buf.readBoolean())
        {
            shape = Shape.values()[buf.readInt()];
        }
        else
        {
            shape = Shape.CUBE;
        }

        if (buf.readBoolean())
        {
            pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        }
        else
        {
            pos = null;
        }

        if (buf.readBoolean())
        {
            box = new Tuple<>(new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()), new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()));
        }
        else
        {
            box = null;
        }

        if (buf.readBoolean())
        {
            structureName = buf.readString(32767);
        }
        else
        {
            structureName = null;
        }

        if (buf.readBoolean())
        {
            staticSchematicName = buf.readString(32767);
        }
        else
        {
            staticSchematicName = null;
        }

        // itemstack

        if (buf.readBoolean())
        {
            stack = new Tuple<>(buf.readItemStack(), buf.readItemStack());
        }
        else
        {
            stack = new Tuple<>(new ItemStack(Blocks.GOLD_BLOCK), new ItemStack(Blocks.GOLD_BLOCK));
        }

        if (buf.readBoolean())
        {
            equation = buf.readString(32767);
        }

        if (buf.readBoolean())
        {
            anchorPos = Optional.of(buf.readBlockPos());
        }
    }

    /**
     * Serializable from {@link LSStructureDisplayerMessage}
     * @param buf the packet buffer to write it in.
     */
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeBoolean(isMirrored);
        buf.writeBoolean(staticSchematicMode);
        buf.writeBoolean(hollow);

        buf.writeInt(rotation);
        buf.writeInt(width);
        buf.writeInt(height);
        buf.writeInt(length);
        buf.writeInt(frequency);

        // enums

        buf.writeBoolean(shape != null);
        if (shape != null)
        {
            buf.writeInt(shape.ordinal());
        }

        buf.writeBoolean(pos != null);
        if (pos != null)
        {
            buf.writeInt(pos.getX());
            buf.writeInt(pos.getY());
            buf.writeInt(pos.getZ());
        }

        buf.writeBoolean(box != null);
        if (box != null)
        {
            buf.writeInt(box.getA().getX());
            buf.writeInt(box.getA().getY());
            buf.writeInt(box.getA().getZ());
            buf.writeInt(box.getB().getX());
            buf.writeInt(box.getB().getY());
            buf.writeInt(box.getB().getZ());
        }

        // strings

        buf.writeBoolean(structureName != null);
        if (structureName != null)
        {
            buf.writeString(structureName);
        }

        buf.writeBoolean(staticSchematicName != null);
        if (staticSchematicName != null)
        {
            buf.writeString(staticSchematicName);
        }

        // itemstacks

        buf.writeBoolean(stack != null);
        if (stack != null)
        {
            buf.writeItemStack(stack.getA());
            buf.writeItemStack(stack.getB());
        }

        buf.writeBoolean(!equation.isEmpty());
        if (!equation.isEmpty())
        {
            buf.writeString(equation);
        }

        buf.writeBoolean(anchorPos.isPresent());
        anchorPos.ifPresent(buf::writeBlockPos);
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
