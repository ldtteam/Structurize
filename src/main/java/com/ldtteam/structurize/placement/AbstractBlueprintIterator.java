package com.ldtteam.structurize.placement;

import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.TriPredicate;

import java.util.Collections;
import java.util.function.Supplier;

public abstract class AbstractBlueprintIterator implements IBlueprintIterator
{
    public Result increment(final TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> skipCondition)
    {
        return iterateWithCondition(skipCondition, this::increment);
    }

    @Override
    public Result decrement(final TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> skipCondition)
    {
        return iterateWithCondition(skipCondition, this::decrement);
    }

    /**
     * Execute a supplier function to avoid duplicate code for increment and decrement functions.
     *
     * @param skipCondition the skipCondition.
     * @param function      the supplier function.
     * @return the Result.
     */
    private Result iterateWithCondition(final TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> skipCondition, final Supplier<Result> function)
    {
        int count = 0;
        final IStructureHandler structureHandler = getStructureHandler();
        do
        {
            if (function.get() == Result.AT_END)
            {
                return Result.AT_END;
            }
            final BlockPos progressPos = getProgressPos();
            final BlockPos worldPos = structureHandler.getProgressPosInWorld(progressPos);
            final BlueprintPositionInfo info = getBluePrintPositionInfo(progressPos);

            if (skipCondition.test(info, worldPos, structureHandler))
            {
                continue;
            }
            else if (!isRemoving() && BlockUtils.areBlockStatesEqual(info.getBlockInfo().getState(),
              structureHandler.getWorld().getBlockState(worldPos),
              structureHandler::replaceWithSolidBlock,
              structureHandler.fancyPlacement(),
              structureHandler::shouldBlocksBeConsideredEqual,
              info.getBlockInfo().getTileEntityData(),
              info.getBlockInfo().getTileEntityData() == null ? null : structureHandler.getWorld().getBlockEntity(worldPos)) && info.getEntities().length == 0)
            {
                structureHandler.triggerSuccess(progressPos, Collections.emptyList(), false);
                continue;
            }
            return Result.NEW_BLOCK;
        }
        while (count++ < structureHandler.getMaxBlocksCheckedPerCall());

        return Result.CONFIG_LIMIT;
    }

    protected abstract IStructureHandler getStructureHandler();
}
