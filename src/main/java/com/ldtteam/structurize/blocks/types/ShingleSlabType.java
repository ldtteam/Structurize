package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.blocks.decorative.BlockShingleSlab;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Types that the {@link BlockShingleSlab} supports
 */
public enum ShingleSlabType implements IStringSerializable
{
    TOP(0, "top", MaterialColor.WOOD),
    ONE_WAY(1, "oneway", MaterialColor.OBSIDIAN),
    TWO_WAY(2, "twoway", MaterialColor.SAND),
    THREE_WAY(3, "threeway", MaterialColor.GOLD),
    FOUR_WAY(4, "fourway", MaterialColor.BROWN),
    CURVED(5, "curved", MaterialColor.DIRT);

    private static final ShingleSlabType[] META_LOOKUP = new ShingleSlabType[values().length];
    static
    {
        for (final ShingleSlabType enumtype : values())
        {
            META_LOOKUP[enumtype.getMetadata()] = enumtype;
        }
    }
    private final int      meta;
    private final String   name;
    private final String   unlocalizedName;
    /**
     * The color that represents this entry on a map.
     */
    private final MaterialColor materialColor;

    ShingleSlabType(final int metaIn, final String nameIn, final MaterialColor MaterialColorIn)
    {
        this(metaIn, nameIn, nameIn, MaterialColorIn);
    }

    ShingleSlabType(final int metaIn, final String nameIn, final String unlocalizedNameIn, final MaterialColor materialColor)
    {
        this.meta = metaIn;
        this.name = nameIn;
        this.unlocalizedName = unlocalizedNameIn;
        this.materialColor = materialColor;
    }

    public static ShingleSlabType byMetadata(final int meta)
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
