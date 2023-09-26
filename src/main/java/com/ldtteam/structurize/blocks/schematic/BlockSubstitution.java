package com.ldtteam.structurize.blocks.schematic;

import com.ldtteam.structurize.items.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * This block is used as a substitution block for the Builder. Every solid block can be substituted by this block in schematics. This helps make schematics independent from
 * location and ground.
 */
public class BlockSubstitution extends Block
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
     * Constructor for the Substitution block. sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockSubstitution()
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE));
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return Shapes.box(.125D, .125D, .125D, .875D, .875D, .875D);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext context) {
        // Allow players to move through placeholders with a scantool in hand
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player player)
        {
            if (player.getMainHandItem().getItem() == ModItems.scanTool.get() || player.getOffhandItem().getItem() == ModItems.scanTool.get())
            {
                return Shapes.empty();
            }
        }

        return super.getCollisionShape(state,blockGetter,blockPos,context);
    }
}
