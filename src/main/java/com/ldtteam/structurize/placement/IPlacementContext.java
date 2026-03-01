package com.ldtteam.structurize.placement;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface IPlacementContext
{
    /**
     * Getter for the placement settings.
     * @return the settings object.
     */
    PlacementSettings getRotationMirror();

    /**
     * If this is supposed to be fancy placement (player facing) or builder facing (complete).
     * @return true if fancy placement.
     */
    boolean fancyPlacement();

    /**
     * Get the solid worldgen block for given pos while using data from handler.
     *
     * @param  worldPos      the world pos.
     * @param  virtualBlocks blueprint blocks, fnc may return null if virtual block is not available (then use level instead for getting surrounding block states), pos argument is using world coords
     * @return               the solid worldgen block (classically biome dependent).
     */
    BlockState getSolidBlockForPos(BlockPos worldPos, Function<BlockPos, @Nullable BlockState> virtualBlocks);

    /**
     * Get the world position this is placed at.
     * @return the position.
     */
    BlockPos getCenterPos();

    /**
     * Get the bluerint from the handler.
     * @return the blueprint
     */
    Blueprint getBluePrint();
}
