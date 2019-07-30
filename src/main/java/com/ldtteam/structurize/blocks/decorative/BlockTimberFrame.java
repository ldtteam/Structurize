package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.blocks.AbstractBlockStructurizePillar;
import com.ldtteam.structurize.blocks.types.TimberFrameType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Decorative block
 */
public class BlockTimberFrame extends AbstractBlockStructurizePillar<BlockTimberFrame>
{
    /**
     * This blocks name.
     */
    public static final String                         BLOCK_NAME     = "blocktimberframe";
    /**
     * The hardness this block has.
     */
    private static final float                         BLOCK_HARDNESS = 3F;
    /**
     * The resistance this block has.
     */
    private static final float                         RESISTANCE     = 1F;
    /**
     * Constructor for the TimberFrame
     * @param name the name to register it to.
     */
    public BlockTimberFrame(final String name)
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE));
        setRegistryName(name);
    }

    @Override
    public BlockState updatePostPlacement(final BlockState stateIn, final Direction HORIZONTAL_FACING, final BlockState HORIZONTAL_FACINGState, final IWorld worldIn, final BlockPos currentPos, final BlockPos HORIZONTAL_FACINGPos)
    {
        return getActualState(stateIn, worldIn, currentPos);
    }

    @Override
    public BlockState getStateForPlacement(
      final BlockState state,
      final Direction HORIZONTAL_FACING,
      final BlockState state2,
      final IWorld world,
      final BlockPos pos1,
      final BlockPos pos2,
      final Hand hand)
    {
        return getActualState(state, world, pos1);
    }

    /**
     * Calc the default state depending on the neighboring blocks.
     * @param state the state.
     * @param world the world.
     * @param pos the position.
     * @return the actual blockstate.
     */
    public BlockState getActualState(final BlockState state, final IWorld world, final BlockPos pos)
    {
        final BlockState upState = world.getBlockState(pos.up());
        final BlockState downState = world.getBlockState(pos.down());

        final BlockState leftState = world.getBlockState(pos.east());
        final BlockState rightState = world.getBlockState(pos.west());

        final BlockState straightState = world.getBlockState(pos.south());
        final BlockState backState = world.getBlockState(pos.north());

        final boolean up = isConnectable(upState);
        final boolean down = isConnectable(downState);

        final boolean left = isConnectable(leftState);
        final boolean right = isConnectable(rightState);

        final boolean straight = isConnectable(straightState);
        final boolean back = isConnectable(backState);

        if(!isConnectable(state) || state.getBlock().getTranslationKey().contains(TimberFrameType.HORIZONTALNOCAP.getName())
             || (state.get(AXIS) == Direction.Axis.Y && !up && !down)
             || (state.get(AXIS) == Direction.Axis.X && !left && !right)
             || (state.get(AXIS) == Direction.Axis.Z && !straight && !back))
        {
            return state;
        }

        String name = getRegistryName().toString();
        final int underline = name.lastIndexOf('_');
        name = name.substring(0, underline + 1);

        if(state.get(AXIS) == Direction.Axis.Y)
        {
            final BlockState returnState;
            if(up && down)
            {
                //todo cache them by name in the list
                returnState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name + TimberFrameType.SIDEFRAMED.getName())).getDefaultState();
            }
            else if(down)
            {
                returnState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name + TimberFrameType.GATEFRAMED.getName())).getDefaultState();
            }
            else
            {
                returnState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name + TimberFrameType.DOWNGATED.getName())).getDefaultState();
            }
            return returnState.with(AXIS, Direction.Axis.Y);
        }
        else if(state.get(AXIS) == Direction.Axis.X)
        {
            final BlockState returnState;
            if(left && right)
            {
                returnState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name + TimberFrameType.SIDEFRAMED.getName())).getDefaultState();
            }
            else if(right)
            {
                returnState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name + TimberFrameType.GATEFRAMED.getName())).getDefaultState();
            }
            else
            {
                returnState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name + TimberFrameType.DOWNGATED.getName())).getDefaultState();
            }
            return returnState.with(AXIS, Direction.Axis.X);
        }
        else if(state.get(AXIS) == Direction.Axis.Z)
        {
            final BlockState returnState;
            if(straight && back)
            {
                returnState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name + TimberFrameType.SIDEFRAMED.getName())).getDefaultState();
            }
            else if(straight)
            {
                returnState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name + TimberFrameType.GATEFRAMED.getName())).getDefaultState();
            }
            else
            {
                returnState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name + TimberFrameType.DOWNGATED.getName())).getDefaultState();
            }
            return returnState.with(AXIS, Direction.Axis.Z);
        }
        return state;
    }

    private static boolean isConnectable(final BlockState state)
    {
        return state.getBlock() instanceof BlockTimberFrame && (state.getBlock().getTranslationKey().contains(TimberFrameType.SIDEFRAMED.getName())
                || state.getBlock().getTranslationKey().contains(TimberFrameType.GATEFRAMED.getName())
                || state.getBlock().getTranslationKey().contains(TimberFrameType.DOWNGATED.getName())
                || state.getBlock().getTranslationKey().contains(TimberFrameType.HORIZONTALNOCAP.getName()));
    }
}
