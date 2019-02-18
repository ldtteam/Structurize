package com.ldtteam.structurize.util;

import com.mojang.authlib.GameProfile;
import com.ldtteam.structurize.api.configuration.Configurations;
import com.ldtteam.structurize.api.util.BlockUtils;
import com.ldtteam.structurize.api.util.ChangeStorage;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

/**
 * Contains one scan tool operation, as remove block, replace block, etc.
 */
public class ScanToolOperation
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
        UNDO
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
    private final EntityPlayer player;

    /**
     * The changeStorage associated to this operation..
     */
    private final ChangeStorage storage;

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
    private final InstantStructurePlacer wrapper;

    /**
     * Create a ScanToolOperation.
     *
     * @param type        the type.
     * @param startPos    the start position.
     * @param endPos      the end position.
     * @param player      the player who triggered the event.
     * @param firstBlock  the block being altered.
     * @param secondBlock the block it will be replaced with.
     */
    public ScanToolOperation(
      final OperationType type,
      final BlockPos startPos,
      final BlockPos endPos,
      final EntityPlayer player,
      final ItemStack firstBlock,
      final ItemStack secondBlock)
    {
        this.operation = type;
        this.startPos = new BlockPos(Math.min(startPos.getX(), endPos.getX()), Math.min(startPos.getY(), endPos.getY()), Math.min(startPos.getZ(), endPos.getZ()));
        this.currentPos = new BlockPos(Math.min(startPos.getX(), endPos.getX()), Math.min(startPos.getY(), endPos.getY()), Math.min(startPos.getZ(), endPos.getZ()));
        this.endPos = new BlockPos(Math.max(startPos.getX(), endPos.getX()), Math.max(startPos.getY(), endPos.getY()), Math.max(startPos.getZ(), endPos.getZ()));
        this.player = player;
        this.firstBlock = firstBlock;
        this.secondBlock = secondBlock;
        this.storage = new ChangeStorage(player);
        this.wrapper = null;
    }

    /**
     * Create a ScanToolOperation for an UNDO.
     * @param storage the storage for the UNDO.
     * @param player the player.
     */
    public ScanToolOperation(final ChangeStorage storage, final EntityPlayer player)
    {
        this.operation = OperationType.UNDO;
        this.startPos = BlockPos.ORIGIN;
        this.currentPos = BlockPos.ORIGIN;
        this.endPos = BlockPos.ORIGIN;
        this.player = player;
        this.firstBlock = ItemStack.EMPTY;
        this.secondBlock = ItemStack.EMPTY;
        this.storage = storage;
        this.wrapper = null;
    }

    /**
     * Create a ScanToolOperation for an structure placement.
     * @param wrapper the structure wrapper for the placement..
     * @param player the player.
     */
    public ScanToolOperation(final InstantStructurePlacer wrapper, final EntityPlayer player)
    {
        this.operation = OperationType.PLACE_STRUCTURE;
        this.startPos = BlockPos.ORIGIN;
        this.currentPos = BlockPos.ORIGIN;
        this.endPos = BlockPos.ORIGIN;
        this.player = player;
        this.firstBlock = ItemStack.EMPTY;
        this.secondBlock = ItemStack.EMPTY;
        this.storage = new ChangeStorage(player);
        this.wrapper = wrapper;
    }

    /**
     * Apply the operation on the world.
     *
     * @param world the world to apply them on.
     * @return true if finished.
     */
    public boolean apply(final WorldServer world)
    {
        if (player.dimension != world.provider.getDimension())
        {
            return false;
        }

        if (operation == OperationType.UNDO)
        {
            return storage.undo(world);
        }

        if (operation == OperationType.PLACE_STRUCTURE)
        {
            currentPos = wrapper.placeStructure(world, storage, currentPos);
            return currentPos == null;
        }

        return run(world);
    }

    /**
     * Run the operation up to a max count.
     *
     * @param world the world to run it in.
     * @return true if finished.
     */
    private boolean run(final WorldServer world)
    {
        final FakePlayer fakePlayer = new FakePlayer(world, new GameProfile(player.getUniqueID(), "placeStuffForMePl0x"));
        int count = 0;
        for (int y = currentPos.getY(); y <= endPos.getY(); y++)
        {
            for (int x = currentPos.getX(); x <= endPos.getX(); x++)
            {
                for (int z = currentPos.getZ(); z <= endPos.getZ(); z++)
                {
                    final BlockPos here = new BlockPos(x, y, z);
                    final IBlockState blockState = world.getBlockState(here);
                    final ItemStack stack = BlockUtils.getItemStackFromBlockState(blockState);
                    if (correctBlockToRemoveOrReplace(stack, blockState, firstBlock))
                    {
                        if ((blockState.getBlock() instanceof BlockDoor && blockState.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER)
                              || (blockState.getBlock() instanceof BlockBed && blockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD))
                        {
                            continue;
                        }
                        count++;

                        storage.addPositionStorage(here, world);
                        world.setBlockToAir(here);
                        if (operation == OperationType.REPLACE_BLOCK)
                        {
                            BlockUtils.handleCorrectBlockPlacement(world, fakePlayer, secondBlock, blockState, here);
                        }

                        if (count >= Configurations.gameplay.maxOperationsPerTick)
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
        return true;
    }

    /**
     * Is this the correct block to remove it or replace it.
     *
     * @param worldStack   the world stack to check.
     * @param worldState   the world state to check.
     * @param compareStack the comparison stack.
     * @return true if so.
     */
    private static boolean correctBlockToRemoveOrReplace(final ItemStack worldStack, final IBlockState worldState, final ItemStack compareStack)
    {
        return (worldStack != null && worldStack.isItemEqual(compareStack)
                  || (compareStack.getItem() == Items.LAVA_BUCKET && (worldState.getBlock() == Blocks.LAVA || worldState.getBlock() == Blocks.FLOWING_LAVA))
                  || (compareStack.getItem() == Items.WATER_BUCKET && (worldState.getBlock() == Blocks.WATER || worldState.getBlock() == Blocks.FLOWING_WATER))
                  || (compareStack.getItem() == Items.AIR && (worldState.getBlock() == Blocks.AIR)));
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
     * @return true if so.
     */
    public boolean isUndo()
    {
        return operation == OperationType.UNDO;
    }
}
