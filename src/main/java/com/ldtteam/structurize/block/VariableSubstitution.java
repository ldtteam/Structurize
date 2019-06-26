package com.ldtteam.structurize.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;

/**
 * Variable substitution block class
 */
public class VariableSubstitution extends Block
{
    /**
     * Creates default variable substitution block.
     */
    public VariableSubstitution()
    {
        this(Properties.create(new Material(MaterialColor.WOOD, false, true, true, true, true, false, false, PushReaction.BLOCK)).hardnessAndResistance(1.0F));
    }

    /**
     * MC constructor.
     *
     * @param properties properties
     */
    public VariableSubstitution(final Properties properties)
    {
        super(properties);
        setRegistryName("variablesubstitution");
    }
}
