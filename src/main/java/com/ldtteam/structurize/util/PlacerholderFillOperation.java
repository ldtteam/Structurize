package com.ldtteam.structurize.util;

import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Contains an operation, as remove block, replace block, place structure, etc.
 */
public class PlacerholderFillOperation implements ITickedWorldOperation
{
    /**
     * The current position to start iterating.
     */
    private BlockPos startPos;

    /**
     * The current position to start iterating.
     */
    private BlockPos currentPos;

    /**
     * The end position.
     */
    private final BlockPos endPos;

    /**
     * Parameters dictating the layout of the placeholders
     */
    private final double yStretch;
    private final double circleRadiusMult;
    private final int    heightOffset;
    private final int    minDistToBlocks;

    /**
     * The changeStorage associated to this operation
     */
    private final ChangeStorage storage;

    /**
     * Create a ScanToolOperation.
     *
     * @param startPos the start position.
     * @param endPos   the end position.
     * @param player   the player who triggered the event.
     */
    public PlacerholderFillOperation(
      final BlockPos startPos,
      final BlockPos endPos,
      @Nullable final Player player,
      final double yStretch, final double circleRadiusMult, final int heightOffset, final int minDistToBlocks)
    {
        this.startPos = new BlockPos(Math.min(startPos.getX(), endPos.getX()), Math.min(startPos.getY(), endPos.getY()), Math.min(startPos.getZ(), endPos.getZ()));
        this.currentPos = new BlockPos(Math.min(startPos.getX(), endPos.getX()), Math.min(startPos.getY(), endPos.getY()), Math.min(startPos.getZ(), endPos.getZ()));
        this.yStretch = yStretch;
        this.circleRadiusMult = circleRadiusMult;
        this.heightOffset = heightOffset;
        this.minDistToBlocks = minDistToBlocks;
        this.endPos = new BlockPos(Math.max(startPos.getX(), endPos.getX()), Math.max(startPos.getY(), endPos.getY()), Math.max(startPos.getZ(), endPos.getZ()));
        this.storage = new ChangeStorage("FILL_TOP_PLACEHOLDERS", player != null ? player.getUUID() : UUID.randomUUID());
    }

    @Override
    public boolean apply(final ServerLevel world)
    {
        fillSchematicTopWithPlaceholders(world, yStretch, circleRadiusMult, heightOffset, minDistToBlocks);
        return true;
    }

    @Override
    public ChangeStorage getChangeStorage()
    {
        return this.storage;
    }

    @Override
    public boolean isUndoRedo()
    {
        return false;
    }

    /**
     * Fills the top area of the schematic with placeholder, in a somewhat circular manner, covering mostly the top corners of a schematic
     *
     * @param world
     */
    private void fillSchematicTopWithPlaceholders(final ServerLevel world, final double yStretch, final double circleRadiusMult, final int heightOffset, final int minDistToBlocks)
    {
        for (int x = startPos.getX(); x <= endPos.getX(); x++)
        {
            for (int z = startPos.getZ(); z <= endPos.getZ(); z++)
            {
                // Place from top - down
                for (int y = endPos.getY(); y > startPos.getY(); y--)
                {
                    final BlockPos current = new BlockPos(x, y, z);
                    if (checkDistance(current, startPos, endPos, yStretch, circleRadiusMult, heightOffset) && checkSurroundingBlocks(world, current, minDistToBlocks))
                    {
                        storage.addPreviousDataFor(current, world);
                        world.setBlock(current, ModBlocks.blockSubstitution.get().defaultBlockState(), 3);
                        storage.addPostDataFor(current, world);
                    }
                    else
                    {
                        // Interrupt going down if one a placement condition failed
                        break;
                    }
                }
            }
        }
    }

    /**
     * Checks if the given pos is within the eligible distance to be placing placeholders for the area
     * This excludes an ellipsoid in the center of the schematic to place the placeholders around it.
     *
     * @param comparedPos
     * @param corner1
     * @param corner2
     * @param yStretch         set how thin-stretched the inner ellipsoid is, default: 1
     * @param circleRadiusMult set how large the inner ellipsoid radius is, default: 1
     * @param heightOffset     offset the height at where placeholders stop, default: 0
     * @return
     */
    private boolean checkDistance(BlockPos comparedPos, BlockPos corner1, BlockPos corner2, final double yStretch, final double circleRadiusMult, final int heightOffset)
    {
        BlockPos center = corner1.offset(corner2);
        center = new BlockPos(center.getX() / 2, (int) ((center.getY() / 2) + Math.abs(corner1.getY() - corner2.getY()) * 0.1) + heightOffset, center.getZ() / 2);

        if (comparedPos.getY() < center.getY())
        {
            return false;
        }

        int xLength = Math.abs(corner1.getX() - corner2.getX());
        int zLength = Math.abs(corner1.getZ() - corner2.getZ());

        double xDiff = (double) comparedPos.getX() + 0.5D - center.getX();
        double yDiff = (double) comparedPos.getY() + 0.5D - center.getY();
        double zDiff = (double) comparedPos.getZ() + 0.5D - center.getZ();

        if (Math.sqrt(
          (xDiff * xDiff) * ((double) zLength / xLength)
            + (yDiff * yDiff) * 2 * yStretch
            + (zDiff * zDiff) * ((double) xLength / zLength))
              < Math.min(xLength, zLength) * 0.4 * circleRadiusMult
        )
        {
            return false;
        }

        return true;
    }

    /**
     * Checks the surrounding blocks to avoid placing too close to an existing block
     *
     * @param level
     * @param checkPos
     * @param minDistToBlocks distance at which a block is too close default: 2
     * @return
     */
    private boolean checkSurroundingBlocks(Level level, BlockPos checkPos, final int minDistToBlocks)
    {
        if (minDistToBlocks <= 0)
        {
            return true;
        }

        for (int x = -minDistToBlocks; x <= minDistToBlocks; x++)
        {
            for (int y = -minDistToBlocks; y <= minDistToBlocks; y++)
            {
                for (int z = -minDistToBlocks; z <= minDistToBlocks; z++)
                {
                    final BlockState state = level.getBlockState(checkPos.offset(x, y, z));
                    if (!state.isAir() && state.getBlock() != ModBlocks.blockSubstitution.get())
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}