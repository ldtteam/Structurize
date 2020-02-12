package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import com.ldtteam.structurize.blocks.types.TimberFrameCentreType;
import com.ldtteam.structurize.blocks.types.TimberFrameFrameType;
import com.ldtteam.structurize.blocks.types.TimberFrameType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

/**
 * Decorative block
 */
public class BlockTimberFrame extends AbstractBlockStructurize<BlockTimberFrame>
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    /**
     * The hardness this block has.
     */
    private static final float                         BLOCK_HARDNESS = 3F;

    /**
     * The resistance this block has.
     */
    private static final float                         RESISTANCE     = 1F;

    /**
     * Fields defining the registered block's wood and face types, these are used by the Data Generators
     */
    private final TimberFrameType timberFrameType;
    private final TimberFrameFrameType frameType;
    private final TimberFrameCentreType centreType;

    /**
     * Constructor for the TimberFrame
     */
    public BlockTimberFrame(final TimberFrameType timberFrameType, final TimberFrameFrameType frameType, final TimberFrameCentreType centreType)
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE).notSolid());
        setRegistryName(getName(timberFrameType, frameType, centreType));
        this.timberFrameType = timberFrameType;
        this.frameType = frameType;
        this.centreType = centreType;
    }

    public static String getName(final TimberFrameType timberFrameType, final TimberFrameFrameType frameType, final TimberFrameCentreType centreType)
    {
        return timberFrameType.getName() + "_" + frameType.getName() + "_" + centreType.getName() + "_timber_frame";
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite());
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
     * Get the registered TimberFrameFrameType, used by the Data Generators
     *
     * @return the registered TimberFrameFrameType
     */
    public TimberFrameFrameType getFrameType()
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
