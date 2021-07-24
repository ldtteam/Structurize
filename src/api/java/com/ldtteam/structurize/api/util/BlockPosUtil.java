package com.ldtteam.structurize.api.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.structurize.api.util.constant.Constants.*;

/**
 * Utility methods for BlockPos.
 */
public final class BlockPosUtil
{
    private BlockPosUtil()
    {
        // Hide default constructor.
    }

    /**
     * Writes a Chunk Coordinate to an NBT compound, with a specific tag name.
     *
     * @param compound Compound to write to.
     * @param name     Name of the tag.
     * @param pos      Coordinates to write to NBT.
     */
    public static void writeToNBT(final CompoundTag compound, final String name, final BlockPos pos)
    {
        final CompoundTag coordsCompound = new CompoundTag();
        coordsCompound.putInt("x", pos.getX());
        coordsCompound.putInt("y", pos.getY());
        coordsCompound.putInt("z", pos.getZ());
        compound.put(name, coordsCompound);
    }

    /**
     * Reads Chunk Coordinates from an NBT Compound with a specific tag name.
     *
     * @param compound Compound to read data from.
     * @param name     Tag name to read data from.
     * @return Chunk coordinates read from the compound.
     */
        public static BlockPos readFromNBT(final CompoundTag compound, final String name)
    {
        final CompoundTag coordsCompound = compound.getCompound(name);
        final int x = coordsCompound.getInt("x");
        final int y = coordsCompound.getInt("y");
        final int z = coordsCompound.getInt("z");
        return new BlockPos(x, y, z);
    }

    /**
     * Write a compound with chunk coordinate to a tag list.
     *
     * @param tagList Tag list to write compound with chunk coordinates to.
     * @param pos     Coordinate to write to the tag list.
     */
    public static void writeToNBTTagList(final ListTag tagList, final BlockPos pos)
    {
        final CompoundTag coordsCompound = new CompoundTag();
        coordsCompound.putInt("x", pos.getX());
        coordsCompound.putInt("y", pos.getY());
        coordsCompound.putInt("z", pos.getZ());
        tagList.add(coordsCompound);
    }

    /**
     * this checks that you are not in liquid. Will check for all liquids, even those from other mods before TP
     *
     * @param blockPos for the current block LOC
     * @param sender   uses the player to get the world
     * @return isSafe true=safe false=water or lava
     */
    public static boolean isPositionSafe(final CommandSourceStack sender, final BlockPos blockPos)
    {
        return sender.getLevel().getBlockState(blockPos).getBlock() != Blocks.AIR
                 && !sender.getLevel().getBlockState(blockPos).getMaterial().isLiquid()
                 && !sender.getLevel().getBlockState(blockPos.above()).getMaterial().isLiquid();
    }

    /**
     * Squared distance between two BlockPos.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return squared distance.
     */
    public static long getDistanceSquared(final BlockPos block1, final BlockPos block2)
    {
        final long xDiff = (long) block1.getX() - block2.getX();
        final long yDiff = (long) block1.getY() - block2.getY();
        final long zDiff = (long) block1.getZ() - block2.getZ();

        final long result = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
        if (result < 0)
        {
            throw new IllegalStateException("max-sqrt is to high! Failure to catch overflow with " + xDiff + " | " + yDiff + " | " + zDiff);
        }
        return result;
    }

    /**
     * Returns the block at a specific chunk coordinate.
     *
     * @param world  World the block is in.
     * @param coords Coordinates of the block.
     * @return Block at the given coordinates.
     */
    public static Block getBlock(final Level world, final BlockPos coords)
    {
        return world.getBlockState(coords).getBlock();
    }

    /**
     * Returns the metadata of a block at a specific chunk coordinate.
     *
     * @param world  World the block is in.
     * @param coords Coordinates of the block.
     * @return Metadata of the block at the given coordinates.
     */
    public static BlockState getBlockState(final Level world, final BlockPos coords)
    {
        return world.getBlockState(coords);
    }

    /**
     * Sets a block in the world, with specific metadata and flags.
     *
     * @param worldIn World the block needs to be set in.
     * @param coords  Coordinate to place block.
     * @param state   BlockState to be placed.
     * @param flag    Flag to set.
     * @return True if block is placed, otherwise false.
     */
    public static boolean setBlock(final Level worldIn, final BlockPos coords, final BlockState state, final int flag)
    {
        return worldIn.setBlock(coords, state, flag);
    }

    /**
     * Create a method for using a {@link BlockPos} when using {@link BlockPos.MutableBlockPos#set(int, int, int)}.
     *
     * @param pos    {@link BlockPos.MutableBlockPos}.
     * @param newPos The new position to set.
     */
    public static void set(final BlockPos.MutableBlockPos pos, final BlockPos newPos)
    {
        pos.set(newPos.getX(), newPos.getY(), newPos.getZ());
    }

    /**
     * Returns whether a chunk coordinate is equals to (x, y, z).
     *
     * @param coords Chunk Coordinate    (point 1).
     * @param x      x-coordinate        (point 2).
     * @param y      y-coordinate        (point 2).
     * @param z      z-coordinate        (point 2).
     * @return True when coordinates are equal, otherwise false.
     */
    public static boolean isEqual(final BlockPos coords, final int x, final int y, final int z)
    {
        return coords.getX() == x && coords.getY() == y && coords.getZ() == z;
    }

    /**
     * Get the rotation enum value from the amount of rotations.
     *
     * @param rotations the amount of rotations.
     * @return the enum Rotation.
     */
    public static Rotation getRotationFromRotations(final int rotations)
    {
        switch (rotations)
        {
            case ROTATE_ONCE:
                return Rotation.CLOCKWISE_90;
            case ROTATE_TWICE:
                return Rotation.CLOCKWISE_180;
            case ROTATE_THREE_TIMES:
                return Rotation.COUNTERCLOCKWISE_90;
            default:
                return Rotation.NONE;
        }
    }

    /**
     * Check if the given position is inside the given corners, corner order does not matter
     *
     * @param inside  inside position to check
     * @param corner1 first corner
     * @param corner2 second corner
     * @return
     */
    public static boolean isInbetween(final BlockPos inside, final BlockPos corner1, final BlockPos corner2)
    {
        final int maxX = Math.max(corner1.getX(), corner2.getX());
        final int minX = Math.min(corner1.getX(), corner2.getX());

        final int maxY = Math.max(corner1.getY(), corner2.getY());
        final int minY = Math.min(corner1.getY(), corner2.getY());

        final int maxZ = Math.max(corner1.getZ(), corner2.getZ());
        final int minZ = Math.min(corner1.getZ(), corner2.getZ());

        return inside.getX() <= maxX && inside.getX() >= minX && inside.getY() <= maxY && inside.getY() >= minY && inside.getZ() <= maxZ && inside.getZ() >= minZ;
    }

    /**
     * Gets the next position to use in a circular-creeping inward snake manner. Its iterating the rectangle of corners in clockwise rotation
     *
     * @param start   current positions
     * @param corner1 rectangle top left corner
     * @param corner2 rectangle bottom right corner
     * @param ringHeight the y height we iterate before advancing inwards
     * @return next position
     */
    public static BlockPos getNextPosInCircleFrom(final BlockPos start, final BlockPos corner1, final BlockPos corner2, final int ringHeight)
    {
        if (corner1.getX() > corner2.getX() || corner1.getZ() > corner2.getZ() || Math.abs(corner1.getX() - corner2.getX()) < 0 || Math.abs(corner1.getZ() - corner2.getZ()) < 0)
        {
            Log.getLogger().warn("Insufficient dimensions for:" + corner1 + corner2);
            return start;
        }

        // Distances to all borders
        final int wDist = Math.abs(corner1.getX() - start.getX());
        final int nDist = Math.abs(corner1.getZ() - start.getZ());
        final int eDist = Math.abs(corner2.getX() - start.getX());
        final int sDist = Math.abs(corner2.getZ() - start.getZ());


        // Find the closest direction in clockwise rotation
        Direction closestDir = Direction.NORTH;
        int closest = nDist;

        if (eDist < closest || eDist == closest && closestDir.getClockWise() == Direction.EAST)
        {
            closest = eDist;
            closestDir = Direction.EAST;
        }

        if (sDist < closest || sDist == closest && closestDir.getClockWise() == Direction.SOUTH)
        {
            closest = sDist;
            closestDir = Direction.SOUTH;
        }

        if (wDist < closest || wDist == closest && closestDir.getClockWise() == Direction.WEST)
        {
            closest = wDist;
            closestDir = Direction.WEST;
        }

        // Determines the amount of outer circles, to see if we're in the last circle
        final int outerRings = (Math.min(Math.abs(corner2.getX() - corner1.getX()), Math.abs(corner2.getZ() - corner1.getZ()))) >> 1;

        // Special case to advance into the next ring:
        if ((closestDir == Direction.WEST && (nDist - wDist) == 1)
              // Check if there exists another ring
              && wDist != outerRings)
        {
            /**
             * Here we could advance to a higher Y level first before going into the next inner ring, to e.g. do two blocks high first outwards
             * Should be simple to add with just a %2 modulo
             * Ringheight specific stuff
             */

            // Jump row we're currently in
            int traverseRow = ((start.getY() - corner1.getY()) + 1) % ringHeight;
            if (traverseRow != 0)
            {
                // Jump up to the same ring next Y level
                if (start.getY() < corner2.getY())
                {
                    return start.relative(Direction.NORTH).offset(0, 1, 0);
                }
                else
                // We can't jump up, at boundary: jump down into next smaller circle
                {
                    return start.relative(Direction.EAST).offset(0, -((traverseRow) - 1), 0);
                }
            }
            else
            {
                return start.relative(Direction.EAST).offset(0, -(ringHeight - 1), 0);
            }
        }

        // Advance in clockwise rotations
        final Direction advancingDir = closestDir.getClockWise();
        final BlockPos next = start.relative(advancingDir);

        /**
         * End conditions:
         * Within the final circle we're at the end under two conditions:
         * 1. We're trying to go top or left
         * 2. We'll end up on the top boundary when doing so
         * Or
         * 3. We're at the central bottom block of the 1 wide inner circle
         */

        // Within the last circle we're not allowed to go up or left when that would put us right onto the circle's edge
        // Check within mostinner-circle
        if (nDist >= outerRings && eDist >= outerRings && sDist >= outerRings && wDist >= outerRings
              // Check if we hit the top boundary with the next
              && (Math.abs(corner1.getZ() - next.getZ()) == outerRings
                    || (eDist == outerRings && sDist == outerRings && wDist == outerRings))
              // Check if we're trying to go top/left to hit it
              && (advancingDir == Direction.WEST || advancingDir == Direction.NORTH))
        {
            // Repeat the last ring until we're at the highest end
            if (start.getY() < corner2.getY())
            {
                if (((start.getY() - corner1.getY()) + 1) % ringHeight != 0)
                {
                    // Ringheight specific stuff
                    return new BlockPos(corner1.getX() + outerRings, Math.min(corner2.getY(), start.getY() + 1), corner1.getZ() + outerRings);
                }
                else
                {
                    return new BlockPos(corner1.getX(), Math.min(corner2.getY(), start.getY() + 1), corner1.getZ());
                }
            }

            return start;
        }
        return next;
    }
}
