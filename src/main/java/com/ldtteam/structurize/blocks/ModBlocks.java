package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.schematic.BlockFluidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSolidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockTagSubstitution;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Class to register blocks to Structurize
 */
public final class ModBlocks
{
    private ModBlocks() { /* prevent construction */ }

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);

    public static DeferredRegister.Blocks getRegistry()
    {
        return BLOCKS;
    }

    public static final TagKey<Block> NULL_PLACEMENT =
        BlockTags.create(Identifier.fromNamespaceAndPath("structurize", "null_placement"));

    public static final DeferredBlock<BlockSubstitution> blockSubstitution;
    public static final DeferredBlock<BlockSolidSubstitution> blockSolidSubstitution;
    public static final DeferredBlock<BlockFluidSubstitution> blockFluidSubstitution;
    public static final DeferredBlock<BlockTagSubstitution> blockTagSubstitution;

    /**
     * Utility shorthand to register blocks using the deferred registry
     * @param name the registry name of the block
     * @param block a factory / constructor to create the block on demand
     * @param <B> the block subclass for the factory response
     * @return the block entry saved to the registry
     */
    public static <B extends Block> DeferredBlock<B> register(String name, java.util.function.Function<BlockBehaviour.Properties, B> block)
    {
        final DeferredBlock<B> registered =
            BLOCKS.registerBlock(name.toLowerCase(), block, () -> BlockBehaviour.Properties.of());
        // DeferredRegister.Blocks only registers the block.  Keep the
        // corresponding BlockItem registration explicit so recipes, creative
        // tabs, and ItemStack lookups all resolve the same id.
        ModItems.getRegistry().registerSimpleBlockItem(name.toLowerCase(), registered::get);
        return registered;
    }

    /*
     *  Registration
     */

    static
    {
        blockSubstitution       = register("blockSubstitution", BlockSubstitution::new);
        blockSolidSubstitution  = register("blockSolidSubstitution", BlockSolidSubstitution::new);
        blockFluidSubstitution  = register("blockFluidSubstitution", BlockFluidSubstitution::new);
        // ItemTagSubstitution is the custom BlockItem for this block and is
        // registered by ModItems, so do not create a duplicate simple item.
        blockTagSubstitution = BLOCKS.registerBlock("blocktagsubstitution", BlockTagSubstitution::new,
            () -> BlockBehaviour.Properties.of());
    }
}
