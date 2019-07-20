package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockStructurizePillar;
import com.ldtteam.structurize.blocks.types.TimberFrameType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.structurize.api.util.constant.Suppression.DEPRECATION;

/**
 * Decorative block
 */
public class BlockTimberFrame extends AbstractBlockStructurizePillar<BlockTimberFrame>
{
    /**
     * This blocks name.
     */
    public static final String                         BLOCK_NAME     = "blockTimberFrame";
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
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
    }


    /**
     * Calc the default state depending on the neighboring blocks.
     * @deprecated remove this when not needed anymore
     * @param state the state.
     * @param world the world.
     * @param pos the position.
     * @return the actual blockstate.
     */
    @Deprecated
    @Override
    public BlockState getActualState(final BlockState state, final IBlockAccess world, final BlockPos pos)
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
             || (state.getValue(AXIS) == EnumFacing.Axis.Y && !up && !down)
             || (state.getValue(AXIS) == EnumFacing.Axis.X && !left && !right)
             || (state.getValue(AXIS) == EnumFacing.Axis.Z && !straight && !back))
        {
            return state;
        }

        String name = getRegistryName().toString();
        final int underline = name.lastIndexOf('_');
        name = name.substring(0, underline + 1);

        if(state.getValue(AXIS) == EnumFacing.Axis.Y)
        {
            final BlockState returnState;
            if(up && down)
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.SIDEFRAMED.getName()).getDefaultState();
            }
            else if(down)
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.GATEFRAMED.getName()).getDefaultState();
            }
            else
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.DOWNGATED.getName()).getDefaultState();
            }
            return returnState.withProperty(AXIS, EnumFacing.Axis.Y);
        }
        else if(state.getValue(AXIS) == EnumFacing.Axis.X)
        {
            final BlockState returnState;
            if(left && right)
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.SIDEFRAMED.getName()).getDefaultState();
            }
            else if(right)
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.GATEFRAMED.getName()).getDefaultState();
            }
            else
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.DOWNGATED.getName()).getDefaultState();
            }
            return returnState.withProperty(AXIS, EnumFacing.Axis.X);
        }
        else if(state.getValue(AXIS) == EnumFacing.Axis.Z)
        {
            final BlockState returnState;
            if(straight && back)
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.SIDEFRAMED.getName()).getDefaultState();
            }
            else if(straight)
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.GATEFRAMED.getName()).getDefaultState();
            }
            else
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.DOWNGATED.getName()).getDefaultState();
            }
            return returnState.withProperty(AXIS, EnumFacing.Axis.Z);
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
