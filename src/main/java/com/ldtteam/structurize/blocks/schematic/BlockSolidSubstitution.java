package com.ldtteam.structurize.blocks.schematic;

import net.minecraft.world.level.block.Block;

/**
 * This block is used as a substitution block for the Builder.
 * Every solid block can be substituted by this block in schematics.
 * This helps make schematics independent from location and ground.
 */
public class BlockSolidSubstitution extends Block
{
    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockSolidSubstitution()
    {
        super(BlockSubstitution.defaultSubstitutionProperties());
    }
}
