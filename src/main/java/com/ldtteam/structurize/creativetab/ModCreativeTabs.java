package com.ldtteam.structurize.creativetab;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

/**
 * Class used to handle the creativeTab of structurize.
 */
public final class ModCreativeTabs
{
    public static final ItemGroup STRUCTURIZE = new ItemGroup(Constants.MOD_ID)
    {
        @Override
        public ItemStack createIcon()
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
