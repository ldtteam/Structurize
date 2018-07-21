package com.copypaste.api.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Utility methods for BlockPos.
 */
public final class BlockPosUtil
{
    private BlockPosUtil()
    {
        //Hide default constructor.
    }

    /**
     * Writes a Chunk Coordinate to an NBT compound, with a specific tag name.
     *
     * @param compound Compound to write to.
     * @param name     Name of the tag.
     * @param pos      Coordinates to write to NBT.
     */
    public static void writeToNBT(@NotNull final NBTTagCompound compound, final String name, @NotNull final BlockPos pos)
    {
        @NotNull final NBTTagCompound coordsCompound = new NBTTagCompound();
        coordsCompound.setInteger("x", pos.getX());
        coordsCompound.setInteger("y", pos.getY());
        coordsCompound.setInteger("z", pos.getZ());
        compound.setTag(name, coordsCompound);
    }

    /**
     * Reads Chunk Coordinates from an NBT Compound with a specific tag name.
     *
     * @param compound Compound to read data from.
     * @param name     Tag name to read data from.
     * @return Chunk coordinates read from the compound.
     */
    @NotNull
    public static BlockPos readFromNBT(@NotNull final NBTTagCompound compound, final String name)
    {
        final NBTTagCompound coordsCompound = compound.getCompoundTag(name);
        final int x = coordsCompound.getInteger("x");
        final int y = coordsCompound.getInteger("y");
        final int z = coordsCompound.getInteger("z");
        return new BlockPos(x, y, z);
    }

    /**
     * Write a compound with chunk coordinate to a tag list.
     *
     * @param tagList Tag list to write compound with chunk coordinates to.
     * @param pos     Coordinate to write to the tag list.
     */
    public static void writeToNBTTagList(@NotNull final NBTTagList tagList, @NotNull final BlockPos pos)
    {
        @NotNull final NBTTagCompound coordsCompound = new NBTTagCompound();
        coordsCompound.setInteger("x", pos.getX());
        coordsCompound.setInteger("y", pos.getY());
        coordsCompound.setInteger("z", pos.getZ());
        tagList.appendTag(coordsCompound);
    }

    /**
     * Writes chunk coordinates to a {@link ByteBuf}.
     *
     * @param buf Buf to write to.
     * @param pos Coordinate to write.
     */
    public static void writeToByteBuf(@NotNull final ByteBuf buf, @NotNull final BlockPos pos)
    {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    /**
     * Read chunk coordinates from a {@link ByteBuf}.
     *
     * @param buf Buf to read from.
     * @return Chunk coordinate that was read.
     */
    @NotNull
    public static BlockPos readFromByteBuf(@NotNull final ByteBuf buf)
    {
        final int x = buf.readInt();
        final int y = buf.readInt();
        final int z = buf.readInt();
        return new BlockPos(x, y, z);
    }

    /**
     * this checks that you are not in liquid.  Will check for all liquids, even
     * those from other mods before TP
     *
     * @param blockPos for the current block LOC
     * @param sender   uses the player to get the world
     * @return isSafe true=safe false=water or lava
     */
    public static boolean isPositionSafe(@NotNull final ICommandSender sender, final BlockPos blockPos)
    {
        return sender.getEntityWorld().getBlockState(blockPos).getBlock() != Blocks.AIR
                 && !sender.getEntityWorld().getBlockState(blockPos).getMaterial().isLiquid()
                 && !sender.getEntityWorld().getBlockState(blockPos.up()).getMaterial().isLiquid();
    }

    /**
     * Squared distance between two BlockPos.
     *
     * @param block1 position one.
     * @param block2 position two.
     * @return squared distance.
     */
    public static long getDistanceSquared(@NotNull final BlockPos block1, @NotNull final BlockPos block2)
    {
        final long xDiff = (long) block1.getX() - block2.getX();
        final long yDiff = (long) block1.getY() - block2.getY();
        final long zDiff = (long) block1.getZ() - block2.getZ();

        final long result = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
        if (result < 0)
        {
            throw new IllegalStateException("max-sqrt is to high! Failure to catch overflow with "
                                              + xDiff + " | " + yDiff + " | " + zDiff);
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
    public static Block getBlock(@NotNull final World world, @NotNull final BlockPos coords)
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
    public static IBlockState getBlockState(@NotNull final World world, @NotNull final BlockPos coords)
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
    public static boolean setBlock(@NotNull final World worldIn, @NotNull final BlockPos coords, final IBlockState state, final int flag)
    {
        return worldIn.setBlockState(coords, state, flag);
    }

    /**
     * Create a method for using a {@link BlockPos} when using {@link
     * net.minecraft.util.math.BlockPos.MutableBlockPos#setPos(int, int, int)}.
     *
     * @param pos    {@link net.minecraft.util.math.BlockPos.MutableBlockPos}.
     * @param newPos The new position to set.
     */
    public static void set(@NotNull final BlockPos.MutableBlockPos pos, @NotNull final BlockPos newPos)
    {
        pos.setPos(newPos.getX(), newPos.getY(), newPos.getZ());
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
    public static boolean isEqual(@NotNull final BlockPos coords, final int x, final int y, final int z)
    {
        return coords.getX() == x && coords.getY() == y && coords.getZ() == z;
    }
}
