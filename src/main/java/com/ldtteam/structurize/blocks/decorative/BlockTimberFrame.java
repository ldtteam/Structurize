package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.blocks.types.TimberFrameCentreType;
import com.ldtteam.structurize.blocks.types.TimberFrameType;
import com.ldtteam.structurize.blocks.types.WoodType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * Decorative block
 */
public class BlockTimberFrame extends DirectionalBlock
{
    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 3F;

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE     = 1F;

    /**
     * Fields defining the registered block's wood and face types, these are used by the Data Generators
     */
    private final TimberFrameType timberFrameType;
    private final WoodType        frameType;
    private final TimberFrameCentreType centreType;

    /**
     * Constructor for the TimberFrame
     */
    public BlockTimberFrame(final TimberFrameType timberFrameType, final WoodType frameType, final TimberFrameCentreType centreType)
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE).noOcclusion());
        this.timberFrameType = timberFrameType;
        this.frameType = frameType;
        this.centreType = centreType;
    }

    public static String getName(final TimberFrameType timberFrameType, final WoodType frameType, final TimberFrameCentreType centreType)
    {
        return String.format("%s_%s_%s_timber_frame", timberFrameType.getName(), frameType.getSerializedName(), centreType.getSerializedName());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot)
    {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    /**
     * Get the registered TimberFrameType, used by the Data Generators
     *
     * @return the registered TimberFrameType
     */
    public TimberFrameType getTimberFrameType()
    {
        return this.timberFrameType;
    }

    /**
     * Get the associated wood, used for data gen
     *
     * @return the wood type
     */
    public WoodType getFrameType()
    {
        return this.frameType;
    }

    /**
     * Get the registered TimberFrameCentreType, used by the Data Generators
     *
     * @return the registered TimberFrameCentreType
     */
    public TimberFrameCentreType getCentreType()
    {
        return this.centreType;
    }
}
