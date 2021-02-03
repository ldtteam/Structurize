package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.creativetab.ModCreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

import java.util.function.Supplier;

/**
 * Class handling the registering of the mod items.
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S2444", "squid:S1444"})
@ObjectHolder(Constants.MOD_ID)
public final class ModItems
{
    private ModItems() { /* prevent construction */ }

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    /*
     *  Forge deferred registry object injection
     */

    public static final ItemBuildTool buildTool = null;
    public static final ItemShapeTool shapeTool = null;
    public static final ItemScanTool  scanTool  = null;
    public static final ItemTagTool   tagTool   = null;
    public static final ItemCaliper   caliper   = null;

    public static DeferredRegister<Item> getRegistry()
    {
        return ITEMS;
    }

    public static <I extends Item> RegistryObject<I> register(String name, Supplier<I> block)
    {
        return ITEMS.register(name.toLowerCase(), block);
    }

    static
    {
        final Item.Properties properties = new Item.Properties().group(ModCreativeTabs.STRUCTURIZE);

        register("sceptergold", () -> new ItemBuildTool(properties));
        register("shapetool", () -> new ItemShapeTool(properties));
        register("sceptersteel", () -> new ItemScanTool(ModCreativeTabs.STRUCTURIZE));
        register("sceptertag", () -> new ItemTagTool(ModCreativeTabs.STRUCTURIZE));
        register("caliper", () -> new ItemCaliper(properties));
    }

}
