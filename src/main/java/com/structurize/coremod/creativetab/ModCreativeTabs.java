package com.structurize.coremod.creativetab;

import com.structurize.api.util.constant.Constants;
import com.structurize.coremod.items.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

/**
 * Class used to handle the creativeTab of structurize.
 */
public final class ModCreativeTabs
{
    public static final CreativeTabs STRUCTURIZE = new CreativeTabs(Constants.MOD_ID)
    {

        @Override
        public ItemStack getTabIconItem()
        {
            return new ItemStack(ModItems.buildTool);
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
    private ModCreativeTabs()
    {
        /*
         * Intentionally left empty.
         */
    }
}
