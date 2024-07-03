package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.blocks.schematic.BlockFluidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSolidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockTagSubstitution;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

/**
 * Class to register blocks to Structurize
 */
public final class ModBlocks
{
    private ModBlocks() { /* prevent construction */ }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);

    public static final TagKey<Block> NULL_PLACEMENT = BlockTags.create(new ResourceLocation("structurize:null_placement"));

    public static final DeferredBlock<BlockSubstitution>      blockSubstitution;
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
    public static <B extends Block> DeferredBlock<B> registerWithBlockItem(String name, Supplier<B> block)
    {
        final DeferredBlock<B> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.registerSimpleBlockItem(registered);
        return registered;
    }

    /*
     *  Registration
     */

    static
    {
        blockSubstitution       = registerWithBlockItem("blockSubstitution".toLowerCase(), BlockSubstitution::new);
        blockSolidSubstitution  = registerWithBlockItem("blockSolidSubstitution".toLowerCase(), BlockSolidSubstitution::new);
        blockFluidSubstitution  = registerWithBlockItem("blockFluidSubstitution".toLowerCase(), BlockFluidSubstitution::new);
        blockTagSubstitution    = BLOCKS.register("blockTagSubstitution".toLowerCase(), BlockTagSubstitution::new);
    }
}
