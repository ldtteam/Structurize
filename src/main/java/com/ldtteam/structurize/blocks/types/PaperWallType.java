package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.blocks.IBlockList;
import com.ldtteam.structurize.blocks.decorative.BlockPaperWall;
import org.jetbrains.annotations.NotNull;

/**
 * Types that the {@link BlockPaperWall} supports
 */
public enum PaperWallType implements IBlockList<BlockPaperWall>
{
    OAK("oak"),
    SPRUCE("spruce"),
    BIRCH("birch"),
    JUNGLE("jungle"),
    ACACIA("acacia"),
    DARK_OAK("dark_oak"),
    CACTUS("cactus");

    private final String name;

    PaperWallType(final String nameIn)
    {
        this.name = nameIn;
    }

    @NotNull
    @Override
    public String getString()
    {
        return this.name;
    }

    @NotNull
    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public BlockPaperWall construct()
    {
        return new BlockPaperWall(getName());
    }
}
