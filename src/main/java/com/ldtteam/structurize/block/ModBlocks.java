package com.ldtteam.structurize.block;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Utils for mod blocks init
 */
public class ModBlocks
{
    public static final AnyblockSubstitution ANYBLOCK_SUBSTITUTION = new AnyblockSubstitution();
    public static final VariableSubstitution VARIABLE_SUBSTITUTION = new VariableSubstitution();

    /**
     * Private constructor to hide implicit public one.
     */
    private ModBlocks()
    {
        /**
         * Intentionally left empty
         */
    }

    /**
     * Register mod blocks.
     *
     * @param registry forge block registry
     */
    public static void registerBlocks(final IForgeRegistry<Block> registry)
    {
        registry.registerAll(ANYBLOCK_SUBSTITUTION, VARIABLE_SUBSTITUTION);
    }
}
