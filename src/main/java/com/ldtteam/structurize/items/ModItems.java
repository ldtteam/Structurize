package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.util.constant.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.AirItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Class to register items to Structurize
 */
public final class ModItems
{
    private ModItems() { /* prevent construction */ }

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    private static final DeferredRegister<Item> VANILLA_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");

    public static DeferredRegister<Item> getRegistry()
    {
        return ITEMS;
    }

    public static DeferredRegister<Item> getVanillaOverrideRegistry()
    {
        return VANILLA_ITEMS;
    }

    /*
     *  Items
     */

    public static final RegistryObject<ItemBuildTool> buildTool;
    public static final RegistryObject<ItemShapeTool> shapeTool;
    public static final RegistryObject<ItemScanTool> scanTool;
    public static final RegistryObject<ItemTagTool>  tagTool;
    public static final RegistryObject<ItemCaliper>  caliper;

    public static final RegistryObject<AirItem> vanillaCaveAir;
    public static final RegistryObject<AirItem> vanillaVoidAir;

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

    public static <I extends Item> RegistryObject<I> registerVanillaOverride(Block vanillaBlock, Supplier<I> item)
    {
        return VANILLA_ITEMS.register(ForgeRegistries.BLOCKS.getKey(vanillaBlock).getPath(), item);
    }

    static
    {
        final Item.Properties properties = new Item.Properties().group(ModItemGroups.STRUCTURIZE);

        buildTool = register("sceptergold", () -> new ItemBuildTool(properties));
        shapeTool = register("shapetool", () -> new ItemShapeTool(properties));
        scanTool  = register("sceptersteel", () -> new ItemScanTool(ModItemGroups.STRUCTURIZE));
        tagTool   = register("sceptertag", () -> new ItemTagTool(ModItemGroups.STRUCTURIZE));
        caliper   = register("caliper", () -> new ItemCaliper(properties));

        vanillaCaveAir = registerVanillaOverride(Blocks.CAVE_AIR, () -> new AirItem(Blocks.CAVE_AIR, new Item.Properties()));
        vanillaVoidAir = registerVanillaOverride(Blocks.VOID_AIR, () -> new AirItem(Blocks.VOID_AIR, new Item.Properties()));
    }
}
