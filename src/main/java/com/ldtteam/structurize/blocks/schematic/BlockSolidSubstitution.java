package com.ldtteam.structurize.blocks.schematic;

import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * This block is used as a substitution block for the Builder.
 * Every solid block can be substituted by this block in schematics.
 * This helps make schematics independent from location and ground.
 */
public class BlockSolidSubstitution extends AbstractBlockStructurize<BlockSolidSubstitution>
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blocksolidsubstitution";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * List of predicates the solid substitution block will have to replace.
     */
    public static List<Predicate<BlockState>> NOT_SOLID = new ArrayList<>();

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockSolidSubstitution()
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE));
        setRegistryName(BLOCK_NAME);
    }
}
