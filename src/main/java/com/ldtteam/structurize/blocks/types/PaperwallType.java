package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.blocks.decorative.BlockPaperwall;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Types that the {@link BlockPaperwall} supports
 */
public enum PaperwallType implements IStringSerializable
{
    OAK(0, "oak", MaterialColor.WOOD),
    SPRUCE(1, "spruce", MaterialColor.OBSIDIAN),
    BIRCH(2, "birch", MaterialColor.SAND),
    JUNGLE(3, "jungle", MaterialColor.DIRT);

    private static final PaperwallType[] META_LOOKUP = new PaperwallType[values().length];
    static
    {
        for (final PaperwallType enumtype : values())
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

    PaperwallType(final int metaIn, final String nameIn, final MaterialColor MaterialColorIn)
    {
        this(metaIn, nameIn, nameIn, MaterialColorIn);
    }

    PaperwallType(final int metaIn, final String nameIn, final String unlocalizedNameIn, final MaterialColor materialColor)
    {
        this.meta = metaIn;
        this.name = nameIn;
        this.unlocalizedName = unlocalizedNameIn;
        this.materialColor = materialColor;
    }

    public static PaperwallType byMetadata(final int meta)
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
