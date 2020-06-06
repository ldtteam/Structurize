package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.client.gui.WindowMultiBlock;
import com.ldtteam.structurize.tileentities.TileEntityMultiBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * This Class is about the MultiBlock which takes care of pushing others around (In a non mean way).
 */
public class MultiBlock extends AbstractBlockStructurize<MultiBlock>
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "multiblock";

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
        super(Properties.create(Material.WOOD).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE));
        setRegistryName(BLOCK_NAME);
    }

    @Override
    public ActionResultType onBlockActivated(
      final BlockState state,
      final World worldIn,
      final BlockPos pos,
      final PlayerEntity player,
      final Hand hand,
      final BlockRayTraceResult ray)
    {
        if (worldIn.isRemote)
        {
            new WindowMultiBlock(pos).open();
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void neighborChanged(final BlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos, final boolean isMoving)
    {
        if(worldIn.isRemote)
        {
            return;
        }
        final TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof TileEntityMultiBlock)
        {
            ((TileEntityMultiBlock) te).handleRedstone(worldIn.isBlockPowered(pos));
        }
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TileEntityMultiBlock();
    }
}
