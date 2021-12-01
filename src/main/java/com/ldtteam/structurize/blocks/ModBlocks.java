package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.schematic.BlockFluidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSolidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockTagSubstitution;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Class to register blocks to Structurize
 */
public final class ModBlocks
{
    private ModBlocks() { /* prevent construction */ }

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);

    public static DeferredRegister<Block> getRegistry()
    {
        return BLOCKS;
    }

    public static final Tag.Named<Block> NULL_PLACEMENT = BlockTags.bind("structurize:null_placement");

    public static final RegistryObject<BlockSubstitution>      blockSubstitution;
    public static final RegistryObject<BlockSolidSubstitution> blockSolidSubstitution;
    public static final RegistryObject<BlockFluidSubstitution> blockFluidSubstitution;
    public static final RegistryObject<BlockTagSubstitution> blockTagSubstitution;

    /**
     * Utility shorthand to register blocks using the deferred registry
     * @param name the registry name of the block
     * @param block a factory / constructor to create the block on demand
     * @param group the {@link CreativeModeTab} this belongs to (sets creative tab)
     * @param <B> the block subclass for the factory response
     * @return the block entry saved to the registry
     */
    public static <B extends Block> RegistryObject<B> register(String name, Supplier<B> block, CreativeModeTab group)
    {
        RegistryObject<B> registered = BLOCKS.register(name.toLowerCase(), block);
        ModItems.getRegistry().register(name.toLowerCase(), () -> new BlockItem(registered.get(), new Item.Properties().tab(group)));
        return registered;
    }

    /*
     *  Registration
     */

    static
    {
        blockSubstitution       = register("blockSubstitution", BlockSubstitution::new, ModItemGroups.STRUCTURIZE);
        blockSolidSubstitution  = register("blockSolidSubstitution", BlockSolidSubstitution::new, ModItemGroups.STRUCTURIZE);
        blockFluidSubstitution  = register("blockFluidSubstitution", BlockFluidSubstitution::new, ModItemGroups.STRUCTURIZE);
        blockTagSubstitution    = register("blockTagSubstitution", BlockTagSubstitution::new, ModItemGroups.STRUCTURIZE);
    }
}
