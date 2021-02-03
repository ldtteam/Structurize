package com.ldtteam.structurize.blocks.schematic;

import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import net.minecraft.block.material.Material;

/**
 * This block is used as a substitution block for the Builder.
 * Every solid block can be substituted by this block in schematics.
 * This helps make schematics independent from location and ground.
 */
public class BlockFluidSubstitution extends AbstractBlockStructurize<BlockFluidSubstitution>
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockfluidsubstitution";

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
