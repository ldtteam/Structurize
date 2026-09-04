package com.ldtteam.structurize.items;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.api.util.constant.Constants;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Class to register items to Structurize
 */
public final class ModItems
{
    private ModItems() { /* prevent construction */ }

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);

    public static DeferredRegister.Items getRegistry()
    {
        return ITEMS;
    }

    /*
     *  Items
     */

    public static final DeferredHolder<Item, ItemBuildTool> buildTool;
    public static final DeferredHolder<Item, ItemShapeTool> shapeTool;
    public static final DeferredHolder<Item, ItemScanTool>  scanTool;
    public static final DeferredHolder<Item, ItemTagTool>   tagTool;
    public static final DeferredHolder<Item, ItemCaliper>  caliper;
    public static final DeferredHolder<Item, ItemTagSubstitution> blockTagSubstitution;

    /**
     * Utility method to register an item
     * @param name the registry key for the item
     * @param item a factory/constructor to produce the item on demand
     * @param <I> any item subclass
     * @return the item entry saved to the registry
     */
    public static <I extends Item> DeferredItem<I> register(String name, java.util.function.Function<Item.Properties, I> item)
    {
        return ITEMS.registerItem(name.toLowerCase(), item, () -> new Item.Properties());
    }

    static
    {
        buildTool = register("sceptergold", ItemBuildTool::new);
        shapeTool = register("shapetool", ItemShapeTool::new);
        scanTool  = register("sceptersteel", ItemScanTool::new);
        tagTool   = register("sceptertag", ItemTagTool::new);
        caliper   = register("caliper", ItemCaliper::new);
        blockTagSubstitution = register("blockTagSubstitution", ItemTagSubstitution::new);
    }
}
