package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.types.BrickType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Class used to handle the creativeTab of structurize.
 */
public final class ModItemGroups
{
    public static final CreativeModeTab STRUCTURIZE = new CreativeModeTab(Constants.MOD_ID)
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(ModItems.buildTool.get());
        }

        @Override
        public boolean hasSearchBar()
        {
            return true;
        }
    };

    public static final CreativeModeTab CONSTRUCTION = new CreativeModeTab(Constants.MOD_ID + ".construction")
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(BrickType.BROWN.getMainRegisteredBlock().get());
        }

        @Override
        public boolean hasSearchBar()
        {
            return true;
        }
    };

    public static final CreativeModeTab SHINGLES = new CreativeModeTab(Constants.MOD_ID + ".shingles")
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(ModBlocks.getShingles().stream().findFirst().orElse(null));
        }

        @Override
        public boolean hasSearchBar()
        {
            return true;
        }
    };

    public static final CreativeModeTab TIMBER_FRAMES = new CreativeModeTab(Constants.MOD_ID + ".timber_frames")
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(ModBlocks.getTimberFrames().stream().findFirst().orElse(null));
        }

        @Override
        public boolean hasSearchBar()
        {
            return true;
        }
    };

    /**
     * Private constructor to hide the implicit one.
     */
    private ModItemGroups()
    {
        /*
         * Intentionally left empty.
         */
    }
}
