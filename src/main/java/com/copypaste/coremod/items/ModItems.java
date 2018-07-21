package com.copypaste.coremod.items;

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
    public static Item scanTool;
    public static Item caliper;

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
        scanTool = new ItemScanTool();
        caliper = new ItemCaliper();

        registry.register(buildTool);
        registry.register(scanTool);
        registry.register(caliper);
    }
}