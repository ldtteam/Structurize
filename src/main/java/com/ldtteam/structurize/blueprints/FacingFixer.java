package com.ldtteam.structurize.blueprints;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.core.Direction.WEST;

/**
 * Used during mirroring of blueprint palette
 */

public record FacingFixer(Predicate<BlockState> test, DirectionProperty property, Function<Direction, Direction> mapping)
{
    public static final List<FacingFixer> MIRROR_FIXERS = new ArrayList<>();

    public static final FacingFixer GLAZED_TERRACOTA_SPECIAL = mirror(bs -> bs.getBlock() == Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA ||
            bs.getBlock() == Blocks.PINK_GLAZED_TERRACOTTA ||
            bs.getBlock() == Blocks.BLUE_GLAZED_TERRACOTTA ||
            bs.getBlock() == Blocks.CYAN_GLAZED_TERRACOTTA,
        GlazedTerracottaBlock.FACING,
        FacingMapping.SOUTH_EAST_AND_NORTH_WEST);

    public static final FacingFixer GLAZED_TERRACOTA_MAJORITY = mirror(bs -> bs.getBlock() instanceof GlazedTerracottaBlock &&
            bs.getBlock() != Blocks.MAGENTA_GLAZED_TERRACOTTA,
        GlazedTerracottaBlock.FACING,
        FacingMapping.NORTH_EAST_AND_SOUTH_WEST);

    /**
     * @param test to which blocks should fixer apply
     * @param property which property is being fixed
     * @param mapping fix function
     * @return fixer registered as mirror fixer
     * @see #MIRROR_FIXERS
     */
    public static FacingFixer mirror(final Predicate<BlockState> test, final DirectionProperty property, final Function<Direction, Direction> mapping)
    {
        final FacingFixer result = new FacingFixer(test, property, mapping);
        MIRROR_FIXERS.add(result);
        return result;
    }

    /**
     * @param blockstate after applying mirror
     * @param unmirrored before applying mirror
     * @return using first matching fixer fixes the blockstate property
     * @see #MIRROR_FIXERS
     */
    public static BlockState fixMirroredFacing(final BlockState blockstate, final BlockState unmirrored)
    {
        for (final FacingFixer fixer : MIRROR_FIXERS)
        {
            if (fixer.test.test(blockstate))
            {
                return blockstate.setValue(fixer.property, fixer.mapping.apply(unmirrored.getValue(fixer.property)));
            }
        }

        return blockstate;
    }

    public record FacingMapping(Direction north, Direction east, Direction south, Direction west, Direction up, Direction down) implements Function<Direction, Direction>
    {
        public static final FacingMapping NORTH_EAST_AND_SOUTH_WEST = new FacingMapping(EAST, NORTH, WEST, SOUTH);
        public static final FacingMapping SOUTH_EAST_AND_NORTH_WEST = new FacingMapping(WEST, SOUTH, EAST, NORTH);
        public static final FacingMapping KEEP_ORIGINAL = new FacingMapping(NORTH, EAST, SOUTH, WEST);
    
        public FacingMapping(final Direction north, final Direction east, final Direction south, final Direction west)
        {
            this(north, east, south, west, UP, DOWN);
        }
    
        @Override
        public Direction apply(final Direction direction)
        {
            return switch (direction)
            {
                case NORTH -> north;
                case EAST -> east;
                case SOUTH -> south;
                case WEST -> west;
                case UP -> up;
                case DOWN -> down;
            };
        }
    }
}