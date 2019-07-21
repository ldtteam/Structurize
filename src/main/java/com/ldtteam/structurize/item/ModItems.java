package com.ldtteam.structurize.item;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.creativetab.ModCreativeTabs;
import com.ldtteam.structurize.util.constants.GeneralConstants;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Utils for mod items init
 */
public class ModItems
{
    private static final ModItemGroup CREATIVE_TAB = new ModItemGroup();
    private static final Item.Properties properties = new Item.Properties().group(ModCreativeTabs.STRUCTURIZE);

    public static final BuildTool BUILD_TOOL = new BuildTool(CREATIVE_TAB);
    public static final ScanTool SCAN_TOOL = new ScanTool(CREATIVE_TAB);
    public static final ShapeTool SHAPE_TOOL = new ShapeTool(CREATIVE_TAB);
    public static final Caliper CALIPER = new Caliper(CREATIVE_TAB);
    public static Item CACTUS_DOOR;

    static
    {
        CREATIVE_TAB.setIcon(BUILD_TOOL);
    }

    /**
     * Private constructor to hide implicit public one.
     */
    private ModItems()
    {
        /*
         * Intentionally left empty
         */
    }

    /**
     * Register mod items.
     *
     * @param registry forge item registry
     */
    public static void registerItems(final IForgeRegistry<Item> registry)
    {
        CACTUS_DOOR = new TallBlockItem(ModBlocks.blockCactusDoor, properties).setRegistryName(ModBlocks.blockCactusDoor.getRegistryName());

        registry.registerAll(BUILD_TOOL, SCAN_TOOL, SHAPE_TOOL, CALIPER, CACTUS_DOOR);
    }

    /**
     * Creates blockitem from given block.
     *
     * @param block already registered block
     * @return new BlockItem
     */
    private static BlockItem newBI(final Block block)
    {
        return newBI(block, CREATIVE_TAB);
    }

    /**
     * Creates blockitem from given block and item group.
     *
     * @param block     already registered block
     * @param itemGroup creative tab
     * @return new BlockItem
     */
    private static BlockItem newBI(final Block block, final ItemGroup itemGroup)
    {
        return (BlockItem) new BlockItem(block, new Item.Properties().group(itemGroup)).setRegistryName(block.getRegistryName());
    }

    /**
     * Creative tab
     */
    private static class ModItemGroup extends ItemGroup
    {
        private Item icon;

        /**
         * Create default creative tab.
         */
        private ModItemGroup()
        {
            super(GeneralConstants.MOD_NAME);
        }

        /**
         * Sets tab icon.
         *
         * @param icon tab icon
         */
        private void setIcon(final Item icon)
        {
            this.icon = icon;
        }

        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(icon);
        }
    }
}
