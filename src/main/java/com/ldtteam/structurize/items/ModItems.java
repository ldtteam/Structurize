package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.util.constant.Constants;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Class to register items to Structurize
 */
public final class ModItems
{
    private ModItems() { /* prevent construction */ }

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    public static DeferredRegister<Item> getRegistry()
    {
        return ITEMS;
    }

    /*
     *  Items
     */

    public static final RegistryObject<ItemBuildTool> buildTool;
    public static final RegistryObject<ItemShapeTool> shapeTool;
    public static final RegistryObject<ItemScanTool>  scanTool;
    public static final RegistryObject<ItemTagTool>   tagTool;
    public static final RegistryObject<ItemCaliper>  caliper;
    public static final RegistryObject<ItemTagSubstitution> blockTagSubstitution;

    /**
     * Utility method to register an item
     * @param name the registry key for the item
     * @param item a factory/constructor to produce the item on demand
     * @param <I> any item subclass
     * @return the item entry saved to the registry
     */
    public static <I extends Item> RegistryObject<I> register(String name, Supplier<I> item)
    {
        return ITEMS.register(name.toLowerCase(), item);
    }

    static
    {
        final Item.Properties properties = new Item.Properties();

        buildTool = register("sceptergold", () -> new ItemBuildTool(properties));
        shapeTool = register("shapetool", () -> new ItemShapeTool(properties));
        scanTool  = register("sceptersteel", ItemScanTool::new);
        tagTool   = register("sceptertag", () -> new ItemTagTool());
        caliper   = register("caliper", () -> new ItemCaliper(properties));
        blockTagSubstitution = register("blockTagSubstitution", () -> new ItemTagSubstitution(properties));
    }
}
