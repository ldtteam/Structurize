package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.client.gui.WindowMultiBlock;
import com.ldtteam.structurize.tileentities.TileEntityMultiBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * This Class is about the MultiBlock which takes care of pushing others around (In a non mean way).
 */
public class MultiBlock extends Block
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
    public MultiBlock()
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE));
    }

    @Override
    public InteractionResult use(
      final BlockState state,
      final Level worldIn,
      final BlockPos pos,
      final Player player,
      final InteractionHand hand,
      final BlockHitResult ray)
    {
        if (worldIn.isClientSide)
        {
            new WindowMultiBlock(pos).open();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(final BlockState state, final Level worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos, final boolean isMoving)
    {
        if(worldIn.isClientSide)
        {
            return;
        }
        final BlockEntity te = worldIn.getBlockEntity(pos);
        if(te instanceof TileEntityMultiBlock)
        {
            ((TileEntityMultiBlock) te).handleRedstone(worldIn.hasNeighborSignal(pos));
        }
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(final BlockState state, final BlockGetter world)
    {
        return new TileEntityMultiBlock();
    }
}
