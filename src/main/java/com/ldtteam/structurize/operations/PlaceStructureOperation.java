package com.ldtteam.structurize.operations;

import com.ldtteam.structurize.placement.BlockPlacementResult.Result;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.StructurePlacer.Operation;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.ChangeStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Operation for placing structures.
 */
public class PlaceStructureOperation extends BaseOperation
{
    /**
     * The structure wrapper.
     */
    @NotNull
    private final StructurePlacer placer;

    /**
     * The phase the placement is in.
     */
    private int structurePhase = 0;

    /**
     * The current position to start iterating.
     */
    private BlockPos currentPos;

    /**
     * Default constructor.
     *
     * @param placer the structure wrapper.
     * @param player the player who placed the structure.
     */
    public PlaceStructureOperation(@NotNull final StructurePlacer placer, @Nullable final Player player)
    {
        super(new ChangeStorage(Component.translatable("com.ldtteam.structurize.place_structure", placer.getHandler().getBluePrint().getName()),
          player != null ? player.getUUID() : UUID.randomUUID()));
        this.placer = placer;
    }

    @Override
    public boolean apply(final ServerLevel world)
    {
        if (placer.isReady() && placer.getHandler().getWorld().dimension().location().equals(world.dimension().location()))
        {
            StructurePhasePlacementResult result;
            switch (structurePhase)
            {
                case 0:
                    //structure
                    result = placer.executeStructureStep(world, storage, currentPos, Operation.BLOCK_PLACEMENT,
                      () -> placer.getIterator().increment((info, pos, handler) -> !BlockUtils.canBlockFloatInAir(info.getBlockInfo().getState())), false);

                    currentPos = result.getIteratorPos();
                    break;
                case 1:
                    // weak solid
                    result = placer.executeStructureStep(world, storage, currentPos, Operation.BLOCK_PLACEMENT,
                      () -> placer.getIterator().increment((info, pos, handler) -> !BlockUtils.isWeakSolidBlock(info.getBlockInfo().getState())), false);

                    currentPos = result.getIteratorPos();
                    break;
                case 2:
                    //water
                    result = placer.clearWaterStep(world, currentPos);
                    currentPos = result.getIteratorPos();
                    if (result.getBlockResult().getResult() == Result.FINISHED)
                    {
                        currentPos = placer.getIterator().getProgressPos();
                    }
                    break;
                case 3:
                    // not solid
                    result = placer.executeStructureStep(world, storage, currentPos, Operation.BLOCK_PLACEMENT,
                      () -> placer.getIterator().increment((info, pos, handler) -> BlockUtils.isAnySolid(info.getBlockInfo().getState())), false);
                    currentPos = result.getIteratorPos();
                    break;
                default:
                    // entities
                    result = placer.executeStructureStep(world, storage, currentPos, Operation.SPAWN_ENTITY,
                      () -> placer.getIterator().increment((info, pos, handler) -> info.getEntities().length == 0), true);
                    currentPos = result.getIteratorPos();
                    break;
            }

            if (result.getBlockResult().getResult() == Result.FINISHED)
            {
                structurePhase++;
                if (structurePhase > 4)
                {
                    structurePhase = 0;
                    currentPos = null;
                    placer.getHandler().onCompletion();
                }
            }

            return currentPos == null;
        }
        return false;
    }
}
