package com.ldtteam.structures.helpers;

import com.ldtteam.structurize.api.util.Shape;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.network.messages.LSStructureDisplayerMessage;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.netty.buffer.ByteBuf;

/**
 * Class used to store.
 */
public final class Settings
{
    /**
     * Single instance of this class.
     */
    public static final Settings                 instance = new Settings();
    private final       BlockPos.MutableBlockPos offset   = new BlockPos.MutableBlockPos();

    /**
     * The position of the structure.
     */
    private BlockPos  pos            = null;
    private boolean   isMirrored     = false;
    @Nullable
    private Structure structure      = null;
    private int       rotation       = 0;
    private String    structureName  = null;

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
     * Possible free to place structure.
     */
    private WindowBuildTool.FreeMode freeMode = null;

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
     * @param name     the name of the schematic.
     * @param freeMode the mode.
     */
    public void setupStaticMode(final String name, final WindowBuildTool.FreeMode freeMode)
    {
        this.staticSchematicMode = true;
        this.staticSchematicName = name;
        this.freeMode = freeMode;
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
        if (structure != null && structure.isBluePrintMissing())
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
        offset.setPos(0, 0, 0);
        rotation = 0;
        isMirrored = false;
        staticSchematicMode = false;
        staticSchematicName = null;
        freeMode = null;
        hollow = false;
        structureName = null;
        pos = null;
        box = null;
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
     * Makes the building mirror.
     */
    public void mirror()
    {
        if (structure == null)
        {
            return;
        }
        isMirrored = !isMirrored;
        structure.getSettings().setMirror(getMirror());
        structure.setPlacementSettings(structure.getSettings());
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
     * Getter of the mode in static mode.
     *
     * @return the FreeMode (enum).
     */
    public WindowBuildTool.FreeMode getFreeMode()
    {
        return freeMode;
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
            this.stack = new Tuple<>(s, this.stack.getSecond());
        }
        else
        {
            this.stack = new Tuple<>(this.stack.getFirst(), s);
        }
    }

    /**
     * Get the current block.
     * @param main if main block or fill block.
     * @return the shape.
     */
    public ItemStack getBlock(final boolean main)
    {
        return main ? this.stack.getFirst() : this.stack.getSecond();
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
     */
    public void fromBytes(ByteBuf buf)
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
            freeMode = WindowBuildTool.FreeMode.values()[buf.readInt()];
        }
        else
        {
            freeMode = null;
        }

        // block pos

        if (buf.readBoolean())
        {
            offset.setPos(buf.readInt(), buf.readInt(), buf.readInt());
        }
        else
        {
            offset.setPos(0, 0, 0);
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
            box = new Tuple<BlockPos, BlockPos>(new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()), new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()));
        }
        else
        {
            box = null;
        }

        // strings

        if (buf.readBoolean())
        {
            structureName = ByteBufUtils.readUTF8String(buf);
        }
        else
        {
            structureName = null;
        }
        
        if (buf.readBoolean())
        {
            staticSchematicName = ByteBufUtils.readUTF8String(buf);
        }
        else
        {
            staticSchematicName = null;
        }

        // itemstack

        if (buf.readBoolean())
        {
            stack = new Tuple<>(ByteBufUtils.readItemStack(buf), ByteBufUtils.readItemStack(buf));
        }
        else
        {
            stack = new Tuple<>(new ItemStack(Blocks.GOLD_BLOCK), new ItemStack(Blocks.GOLD_BLOCK));
        }
    }

    /**
     * Serializable from {@link LSStructureDisplayerMessage}
     */
    public void toBytes(ByteBuf buf)
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

        buf.writeBoolean(freeMode != null);
        if (freeMode != null)
        {
            buf.writeInt(freeMode.ordinal());
        }

        // block pos

        buf.writeBoolean(offset != null);
        if (offset != null)
        {
            buf.writeInt(offset.getX());
            buf.writeInt(offset.getY());
            buf.writeInt(offset.getZ());
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
            buf.writeInt(box.getFirst().getX());
            buf.writeInt(box.getFirst().getY());
            buf.writeInt(box.getFirst().getZ());
            buf.writeInt(box.getSecond().getX());
            buf.writeInt(box.getSecond().getY());
            buf.writeInt(box.getSecond().getZ());
        }

        // strings

        buf.writeBoolean(structureName != null);
        if (structureName != null)
        {
            ByteBufUtils.writeUTF8String(buf, structureName);
        }

        buf.writeBoolean(staticSchematicName != null);
        if (staticSchematicName != null)
        {
            ByteBufUtils.writeUTF8String(buf, staticSchematicName);
        }

        // itemstacks

        buf.writeBoolean(stack != null);
        if (stack != null)
        {
            ByteBufUtils.writeItemStack(buf, stack.getFirst());
            ByteBufUtils.writeItemStack(buf, stack.getSecond());
        }
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
}
