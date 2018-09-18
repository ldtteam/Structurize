package com.structurize.coremod.items;

import com.structurize.coremod.blocks.ModBlocks;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Class handling the registering of the mod items.
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S2444", "squid:S1444"})
public final class ModItems
{
    public static Item buildTool;
    public static Item shapeTool;
    public static Item scanTool;
    public static Item caliper;
    public static Item itemCactusDoor;

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModItems()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Initates all the blocks. At the correct time.
     */
    public static void init(final IForgeRegistry<Item> registry)
    {
        buildTool = new ItemBuildTool();
        shapeTool = new ItemShapeTool();
        scanTool = new ItemScanTool();
        caliper = new ItemCaliper();
        itemCactusDoor = new ItemCactusDoor(ModBlocks.blockCactusDoor, "cactusdoor");

        registry.register(buildTool);
        registry.register(shapeTool);
        registry.register(scanTool);
        registry.register(itemCactusDoor);
        registry.register(caliper);
    }
}