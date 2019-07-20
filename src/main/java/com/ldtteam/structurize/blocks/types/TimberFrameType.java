package com.ldtteam.structurize.blocks.types;


import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

//Creates types for TimberFrame with different variants of wood and texture

public enum TimberFrameType implements IStringSerializable
{
    PLAIN(0, "plain", MaterialColor.WOOD),
    DOUBLECROSSED(1, "doublecrossed", MaterialColor.ADOBE),
    FRAMED(2, "framed", MaterialColor.AIR),
    SIDEFRAMED(3, "sideframed", MaterialColor.BLACK),
    GATEFRAMED(4, "gateframed", MaterialColor.BLUE),
    ONECROSSEDLR(5, "onecrossedlr", MaterialColor.SNOW),
    ONECROSSEDRL(6, "onecrossedrl", MaterialColor.BROWN),
    DOWNGATED(7, "downgated", MaterialColor.CLAY),
    HORIZONTALPLAIN(8, "horizontalplain", MaterialColor.ICE),
    HORIZONTALNOCAP(9, "horizontalnocap", MaterialColor.CYAN);
    private static final TimberFrameType[] META_LOOKUP = new TimberFrameType[values().length];
    static
    {
        for (final TimberFrameType enumtype : values())
        {
            META_LOOKUP[enumtype.getMetadata()] = enumtype;
        }
    }
    private final int           meta;
    private final String        name;
    private final String        unlocalizedName;
    private final MaterialColor materialColor;

    TimberFrameType(final int metaIn, final String nameIn, final MaterialColor MaterialColorIn)
    {
        this(metaIn, nameIn, nameIn, MaterialColorIn);
    }
    TimberFrameType(final int metaIn, final String nameIn, final String unlocalizedNameIn, final MaterialColor materialColorIn)
    {
        this.meta = metaIn;
        this.name = nameIn;
        this.unlocalizedName = unlocalizedNameIn;
        this.materialColor = materialColorIn;
    }
    public static TimberFrameType byMetadata(final int meta)
    {
        int tempMeta = meta;
        if (tempMeta < 0 || tempMeta >= META_LOOKUP.length)
        {
            tempMeta = 0;
        }

        return META_LOOKUP[tempMeta];
    }
    public int getMetadata()
    {
        return this.meta;
    }

    /**
     * The color which represents this entry on a map.
     * @return the MaterialColor object.
     */
    public MaterialColor getMaterialColor()
    {
        return this.materialColor;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    @NotNull
    public String getName()
    {
        return this.name;
    }

    public String getUnlocalizedName()
    {
        return this.unlocalizedName;
    }

}
