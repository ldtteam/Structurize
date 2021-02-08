package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.blocks.IBlockList;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockPaperWall;
import com.ldtteam.structurize.items.ModItemGroups;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.RegistryObject;
import org.jetbrains.annotations.NotNull;

/**
 * Types that the {@link BlockPaperWall} supports
 */
public enum PaperWallType implements IBlockList<BlockPaperWall>, IStringSerializable
{
    OAK("oak"),
    SPRUCE("spruce"),
    BIRCH("birch"),
    JUNGLE("jungle"),
    ACACIA("acacia"),
    DARK_OAK("dark_oak"),
    CACTUS("cactus");

    private final String name;
    private final RegistryObject<BlockPaperWall> block;

    PaperWallType(final String nameIn)
    {
        this.name = nameIn;

        this.block = ModBlocks.register(
          getName() + "_blockpaperwall",
          () -> new BlockPaperWall(getName()),
          ModItemGroups.CONSTRUCTION
        );
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
    public RegistryObject<BlockPaperWall> getBlock()
    {
        return block;
    }
}
