package com.ldtteam.structurize.blocks.schematic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

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
        super(Properties.of(Material.WOOD).noOcclusion().strength(BLOCK_HARDNESS, RESISTANCE));
    }
}
