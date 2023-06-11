package com.ldtteam.structurize.util;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.network.messages.UpdateClientRender;
import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.ldtteam.structurize.placement.AbstractBlueprintIterator.NULL_POS;

/**
 * Contains an operation, as remove block, replace block, place structure, etc.
 */
public class TickedWorldOperation
{
    /**
     * Scan tool operation types.
     */
    public enum OperationType
    {
        REMOVE_BLOCK,
        REPLACE_BLOCK,
        REMOVE_ENTITY,
        SCAN,
        PLACE_STRUCTURE,
        UNDO,
        REDO,
        LOAD_AND_OPERATE
    }

    /**
     * The operation type.
     */
    private final OperationType operation;

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
     * The creator of the operation.
     */
    @Nullable
    private Player player = null;

    /**
     * The changeStorage associated to this operation..
     */
    private final ChangeStorage storage;

    /**
     * The undostorage for undo's
     */
    private ChangeStorage undoStorage;

    /**
     * The block to remove or to replace.
     */
    private final ItemStack firstBlock;

    /**
     * The block to replace it with.
     */
    private final ItemStack secondBlock;

    /**
     * The structure wrapper if structure place.
     */
    private final StructurePlacer placer;

    /**
     * The phase the placmeent is in.
     */
    private int structurePhase = 0;

    /**
     * Operation percentage.
     */
    private int pct;

    /**
     * Create a ScanToolOperation.
     *
     * @param type        the type.
     * @param startPos    the start position.
     * @param endPos      the end position.
     * @param player      the player who triggered the event.
     * @param firstBlock  the block being altered.
     * @param secondBlock the block it will be replaced with.
     * @param pct         the percentage of positions to execute the operation on.
     */
    public TickedWorldOperation(
      final OperationType type,
      final BlockPos startPos,
      final BlockPos endPos,
      @Nullable final Player player,
      final ItemStack firstBlock,
      final ItemStack secondBlock,
      final int pct)
    {
        this.operation = type;
        this.startPos = new BlockPos(Math.min(startPos.getX(), endPos.getX()), Math.min(startPos.getY(), endPos.getY()), Math.min(startPos.getZ(), endPos.getZ()));
        this.currentPos = new BlockPos(Math.min(startPos.getX(), endPos.getX()), Math.min(startPos.getY(), endPos.getY()), Math.min(startPos.getZ(), endPos.getZ()));
        this.endPos = new BlockPos(Math.max(startPos.getX(), endPos.getX()), Math.max(startPos.getY(), endPos.getY()), Math.max(startPos.getZ(), endPos.getZ()));
        this.player = player;
        this.firstBlock = firstBlock;
        this.secondBlock = secondBlock;
        this.storage = new ChangeStorage(type.toString(), player != null ? player.getUUID() : UUID.randomUUID());
        this.placer = null;
        this.pct = pct;
    }

    /**
     * Create a ScanToolOperation for an UNDO.
     *
     * @param storage the storage for the UNDO.
     * @param player  the player.
     */
    public TickedWorldOperation(final ChangeStorage storage, @Nullable final Player player, final OperationType operation)
    {
        this.operation = operation;
        this.startPos = BlockPos.ZERO;
        this.currentPos = BlockPos.ZERO;
        this.endPos = BlockPos.ZERO;
        this.player = player;
        this.firstBlock = ItemStack.EMPTY;
        this.secondBlock = ItemStack.EMPTY;
        this.storage = storage;
        storage.resetUnRedo();
        if (operation == OperationType.UNDO && storage.getOperation().indexOf(TickedWorldOperation.OperationType.UNDO.toString()) != 0)
        {
            undoStorage = new ChangeStorage(operation.toString() + ":" + storage.getOperation(), player != null ? player.getUUID() : UUID.randomUUID());
        }
        this.placer = null;
    }

    /**
     * Create a ScanToolOperation for an structure placement.
     *
     * @param placer the structure for the placement..
     * @param player the player.
     */
    public TickedWorldOperation(final StructurePlacer placer, @Nullable final Player player)
    {
        this.operation = OperationType.PLACE_STRUCTURE;
        this.startPos = BlockPos.ZERO;
        this.currentPos = NULL_POS;
        this.endPos = BlockPos.ZERO;
        this.player = player;
        this.firstBlock = ItemStack.EMPTY;
        this.secondBlock = ItemStack.EMPTY;
        this.storage = new ChangeStorage(operation + ":" + placer.getHandler().getBluePrint().getName(), player != null ? player.getUUID() : UUID.randomUUID());
        this.placer = placer;
    }

    /**
     * Apply the operation on the world.
     *
     * @param world the world to apply them on.
     * @return true if finished.
     */
    public boolean apply(final ServerLevel world)
    {
        if (placer != null && !placer.isReady())
        {
            return false;
        }

        if (player != null && player.level().dimension() != world.dimension())
        {
            return false;
        }

        if (operation == OperationType.UNDO)
        {
            return storage.undo(world, undoStorage);
        }

        if (operation == OperationType.REDO)
        {
            return storage.redo(world);
        }

        if (operation == OperationType.PLACE_STRUCTURE)
        {
            if (placer.getHandler().getWorld().dimension().location().equals(world.dimension().location()))
            {
                StructurePhasePlacementResult result;
                switch (structurePhase)
                {
                    case 0:
                        //water
                        result = placer.executeStructureStep(world, storage, currentPos, StructurePlacer.Operation.WATER_REMOVAL,
                          () -> placer.getIterator().decrement((info, pos, handler) -> info.getBlockInfo().getState().canOcclude()), false);

                        currentPos = result.getIteratorPos();
                        break;
                    case 1:
                        //structure
                        result = placer.executeStructureStep(world, storage, currentPos, StructurePlacer.Operation.BLOCK_PLACEMENT,
                          () -> placer.getIterator().increment((info, pos, handler) -> !BlockUtils.canBlockFloatInAir(info.getBlockInfo().getState())), false);

                        currentPos = result.getIteratorPos();
                        break;
                    case 2:
                        // weak solid
                        result = placer.executeStructureStep(world, storage, currentPos, StructurePlacer.Operation.BLOCK_PLACEMENT,
                            () -> placer.getIterator().increment((info, pos, handler) -> !BlockUtils.isWeakSolidBlock(info.getBlockInfo().getState())), false);

                        currentPos = result.getIteratorPos();
                        break;
                    case 3:
                        // not solid
                        result = placer.executeStructureStep(world, storage, currentPos, StructurePlacer.Operation.BLOCK_PLACEMENT,
                          () -> placer.getIterator().increment((info, pos, handler) -> BlockUtils.isAnySolid(info.getBlockInfo().getState())), false);
                        currentPos = result.getIteratorPos();
                        break;
                    default:
                        // entities
                        result = placer.executeStructureStep(world, storage, currentPos, StructurePlacer.Operation.BLOCK_PLACEMENT,
                          () -> placer.getIterator().increment((info, pos, handler) -> info.getEntities().length == 0), true);
                        currentPos = result.getIteratorPos();
                        break;
                }

                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    structurePhase++;
                    if (structurePhase > 4)
                    {
                        structurePhase = 0;
                        currentPos = null;
                    }
                }

                return currentPos == null;
            }
            return false;
        }

        return run(world);
    }

    /**
     * Run the operation up to a max count.
     *
     * @param world the world to run it in.
     * @return true if finished.
     */
    private boolean run(final ServerLevel world)
    {
        final FakePlayer fakePlayer = new FakePlayer(world, new GameProfile(player == null ? UUID.randomUUID() : player.getUUID(), "structurizefakeplayer"));
        int count = 0;
        for (int y = currentPos.getY(); y <= endPos.getY(); y++)
        {
            for (int x = currentPos.getX(); x <= endPos.getX(); x++)
            {
                for (int z = currentPos.getZ(); z <= endPos.getZ(); z++)
                {
                    final BlockPos here = new BlockPos(x, y, z);
                    final BlockState blockState = world.getBlockState(here);
                    final BlockEntity tileEntity = world.getBlockEntity(here);
                    boolean isMatch = false;
                    boolean handled = false;

                    if (firstBlock.getItem() == Items.AIR && blockState.isAir())
                    {
                        isMatch = true;
                    }
                    else
                    {
                        for (final IPlacementHandler handler : PlacementHandlers.handlers)
                        {
                            if (handler.canHandle(world, BlockPos.ZERO, blockState))
                            {
                                final List<ItemStack> itemList = handler.getRequiredItems(world, here, blockState, tileEntity == null ? null : tileEntity.saveWithFullMetadata(), true);
                                if (!itemList.isEmpty() && ItemStackUtils.compareItemStacksIgnoreStackSize(itemList.get(0), firstBlock))
                                {
                                    isMatch = true;
                                }
                                handled = true;
                                break;
                            }
                        }

                        if (!handled && ItemStackUtils.compareItemStacksIgnoreStackSize(BlockUtils.getItemStackFromBlockState(blockState), firstBlock))
                        {
                            isMatch = true;
                        }
                    }

                    if (isMatch)
                    {
                        if (pct < 100 && fakePlayer.getRandom().nextInt(100) > pct)
                        {
                            continue;
                        }

                        if ((blockState.getBlock() instanceof DoorBlock && blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
                              || (blockState.getBlock() instanceof BedBlock && blockState.getValue(BedBlock.PART) == BedPart.HEAD))
                        {
                            continue;
                        }
                        count++;

                        storage.addPreviousDataFor(here, world);
                        if (operation != OperationType.REPLACE_BLOCK && (blockState.getBlock() instanceof BucketPickup
                                                                           || BlockUtils.isLiquidOnlyBlock(blockState.getBlock())))
                        {
                            BlockUtils.removeFluid(world, here);
                            if (firstBlock.getItem() instanceof BucketItem && !BlockUtils.isLiquidOnlyBlock(blockState.getBlock()))
                            {
                                if (count >= Structurize.getConfig().getServer().maxOperationsPerTick.get())
                                {
                                    currentPos = new BlockPos(x, y, z);
                                    return false;
                                }
                                else
                                {
                                    continue;
                                }
                            }
                        }

                        if (operation == OperationType.REPLACE_BLOCK)
                        {
                            BlockUtils.handleCorrectBlockPlacement(world, fakePlayer, secondBlock, blockState, here);
                        }
                        else
                        {
                            world.removeBlock(here, false);
                        }

                        storage.addPostDataFor(here, world);

                        if (count >= Structurize.getConfig().getServer().maxOperationsPerTick.get())
                        {
                            currentPos = new BlockPos(x, y, z);
                            return false;
                        }
                    }
                }
                currentPos = new BlockPos(x, y, startPos.getZ());
            }
            currentPos = new BlockPos(startPos.getX(), y, startPos.getZ());
        }
        Network.getNetwork().sendToEveryone(new UpdateClientRender(startPos, endPos));

        return true;
    }

    /**
     * Get the current change storage of this operation.
     *
     * @return the ChangeStorage object.
     */
    public ChangeStorage getChangeStorage()
    {
        return this.storage;
    }

    /**
     * Check if operation is an undo already.
     *
     * @return true if so.
     */
    public boolean isUndoRedo()
    {
        return operation == OperationType.UNDO || operation == OperationType.REDO;
    }
}
