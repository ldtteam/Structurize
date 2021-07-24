package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.util.constant.Constants;
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
