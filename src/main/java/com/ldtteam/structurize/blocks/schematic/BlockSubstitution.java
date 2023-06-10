package com.ldtteam.structurize.blocks.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * This block is used as a substitution block for the Builder. Every solid block can be substituted by this block in schematics. This helps make schematics independent from
 * location and ground.
 */
public class BlockSubstitution extends Block
{
    /**
     * Constructor for the Substitution block. sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockSubstitution()
    {
        super(defaultSubstitutionProperties());
    }

    public static Properties defaultSubstitutionProperties()
    {
        return Properties.of()
            .mapColor(MapColor.WOOD)
            .sound(SoundType.WOOD)
            .instabreak() // must be before explosionResistance
            .explosionResistance(Blocks.OAK_PLANKS.getExplosionResistance())
            .noOcclusion();
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return Shapes.box(.125D, .125D, .125D, .875D, .875D, .875D);
    }
}
