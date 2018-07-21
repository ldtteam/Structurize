package com.copypaste.coremod.creativetab;

import com.copypaste.api.util.constant.Constants;
import com.copypaste.coremod.items.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

/**
 * Class used to handle the creativeTab of copypaste.
 */
public final class ModCreativeTabs
{
    public static final CreativeTabs COPY_PASTE = new CreativeTabs(Constants.MOD_ID)
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
