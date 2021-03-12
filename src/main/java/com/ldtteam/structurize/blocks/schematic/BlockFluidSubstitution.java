package com.ldtteam.structurize.blocks.schematic;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/**
 * This block is used as a substitution block for the Builder.
 * Every solid block can be substituted by this block in schematics.
 * This helps make schematics independent from location and ground.
 */
public class BlockFluidSubstitution extends Block
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockFluidSubstitution()
    {
        super(Properties.create(Material.WOOD).notSolid().hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE));
    }
}
