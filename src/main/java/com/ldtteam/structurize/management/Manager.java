package com.ldtteam.structurize.management;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.Shape;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.ChangeStorage;
import com.ldtteam.structurize.util.TickedWorldOperation;
import com.ldtteam.structurize.placement.StructurePlacementUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Singleton class that links colonies to minecraft.
 */
public final class Manager
{
    /**
     * Indicate if a schematic have just been downloaded.
     * Client only
     */
    private static boolean schematicDownloaded = false;

    /**
     * List of the last changes to the world.
     */
    private static LinkedList<ChangeStorage> changeQueue = new LinkedList<>();

    /**
     * List of scanTool operations.
     */
    private static LinkedList<TickedWorldOperation> scanToolOperationPool = new LinkedList<>();

    /**
     * Pseudo unique id for the server
     */
    private static volatile UUID serverUUID = null;

    private Manager()
    {
        //Hides default constructor.
    }

    /**
     * Method called on world tick to run cached operations.
     *
     * @param world the world which is ticking.
     */
    public static void onWorldTick(final ServerWorld world)
    {
        if (!scanToolOperationPool.isEmpty())
        {
            final TickedWorldOperation operation = scanToolOperationPool.peek();
            if (operation != null && operation.apply(world))
            {
                scanToolOperationPool.pop();
                if (!operation.isUndo())
                {
                    addToUndoCache(operation.getChangeStorage());
                }
            }
        }
    }

    /**
     * Add a new item to the scanTool operation queue.
     *
     * @param operation the operation to add.
     */
    public static void addToQueue(final TickedWorldOperation operation)
    {
        scanToolOperationPool.push(operation);
    }

    /**
     * Add a new item to the queue.
     *
     * @param storage the storage to add.
     */
    public static void addToUndoCache(final ChangeStorage storage)
    {
        if (changeQueue.size() >= Structurize.getConfig().getCommon().maxCachedChanges.get())
        {
            changeQueue.pop();
        }
        changeQueue.push(storage);
    }

    /**
     * Paste a structure into the world.
     *
     * @param server         the server world.
     * @param pos            the position.
     * @param width          the width.
     * @param length         the length.
     * @param height         the height.
     * @param frequency      the frequency.
     * @param equation       the equation.
     * @param shape          the shape.
     * @param inputBlock     the input block.
     * @param inputFillBlock the fill block.
     * @param hollow         if hollow or not.
     * @param player         the player.
     * @param mirror         the mirror.
     * @param rotation       the rotation.
     */
    public static void pasteStructure(
      final ServerWorld server,
      final BlockPos pos,
      final int width,
      final int length,
      final int height,
      final int frequency,
      final String equation,
      final Shape shape,
      final ItemStack inputBlock,
      final ItemStack inputFillBlock,
      final boolean hollow,
      final ServerPlayerEntity player,
      final Mirror mirror,
      final Rotation rotation)
    {
        final Blueprint blueprint = Manager.getStructureFromFormula(width, length, height, frequency, equation, shape, inputBlock, inputFillBlock, hollow);
        StructurePlacementUtils.loadAndPlaceStructureWithRotation(server, blueprint, pos, rotation, mirror, true, player);
    }

    /**
     * Just returns a cube for now, I can tinker this later.
     *
     * @param width          the width.
     * @param length         the length.
     * @param height         the height.
     * @param frequency      the frequency.
     * @param equation       the equation.
     * @param shape          the shape.
     * @param inputBlock     the input block.
     * @param inputFillBlock the fill block.
     * @param hollow         if hollow or not.
     * @return the new blueprint.
     */
    public static Blueprint getStructureFromFormula(
      final int width,
      final int length,
      final int height,
      final int frequency,
      final String equation,
      final Shape shape,
      final ItemStack inputBlock,
      final ItemStack inputFillBlock, final boolean hollow)
    {
        final Blueprint blueprint;
        final BlockState mainBlock = BlockUtils.getBlockStateFromStack(inputBlock);
        final BlockState fillBlock = BlockUtils.getBlockStateFromStack(inputFillBlock);

        if (shape == Shape.SPHERE || shape == Shape.HALF_SPHERE || shape == Shape.BOWL)
        {
            blueprint = generateSphere(height / 2, mainBlock, fillBlock, hollow, shape);
        }
        else if (shape == Shape.CUBE)
        {
            blueprint = generateCube(height, width, length, mainBlock, fillBlock, hollow);
        }
        else if (shape == Shape.WAVE)
        {
            blueprint = generateWave(height, width, length, frequency, mainBlock, true);
        }
        else if (shape == Shape.WAVE_3D)
        {
            blueprint = generateWave(height, width, length, frequency, mainBlock, false);
        }
        else if (shape == Shape.CYLINDER)
        {
            blueprint = generateCylinder(height, width, mainBlock, fillBlock, hollow);
        }
        else if (shape == Shape.PYRAMID || shape == Shape.UPSIDE_DOWN_PYRAMID || shape == Shape.DIAMOND)
        {
            blueprint = generatePyramid(height, mainBlock, fillBlock, hollow, shape);
        }
        else
        {
            blueprint = generateRandomShape(height, width, length, equation, mainBlock);
        }
        return blueprint;
    }

    private static Blueprint generatePyramid(
      final int inputHeight,
      final BlockState block,
      final BlockState fillBlock,
      final boolean hollow,
      final Shape shape)
    {
        final int height = shape == Shape.DIAMOND ? inputHeight : inputHeight * 2;
        final int hHeight = height / 2;

        final Map<BlockPos, BlockState> posList = new HashMap<>();
        for (int y = 0; y < hHeight; y++)
        {
            for (int x = 0; x < hHeight; x++)
            {
                for (int z = 0; z < hHeight; z++)
                {
                    if (((x == z && x >= y) || (x == y && x >= z) || ((hollow ? y == z : y >= z) && y >= x)) && x * z <= y * y)
                    {
                        final BlockState blockToUse = x == z && x >= y || x == y || y == z ? block : fillBlock;
                        if (shape == Shape.UPSIDE_DOWN_PYRAMID || shape == Shape.DIAMOND)
                        {
                            addPosToList(new BlockPos(hHeight + x, y, hHeight + z), blockToUse, posList);
                            addPosToList(new BlockPos(hHeight + x, y, hHeight - z), blockToUse, posList);
                            addPosToList(new BlockPos(hHeight - x, y, hHeight + z), blockToUse, posList);
                            addPosToList(new BlockPos(hHeight - x, y, hHeight - z), blockToUse, posList);
                        }

                        if (shape == Shape.PYRAMID || shape == Shape.DIAMOND)
                        {
                            addPosToList(new BlockPos(hHeight + x, -y + height - (shape == Shape.DIAMOND ? 2 : inputHeight), hHeight + z), blockToUse, posList);
                            addPosToList(new BlockPos(hHeight + x, -y + height - (shape == Shape.DIAMOND ? 2 : inputHeight), hHeight - z), blockToUse, posList);
                            addPosToList(new BlockPos(hHeight - x, -y + height - (shape == Shape.DIAMOND ? 2 : inputHeight), hHeight + z), blockToUse, posList);
                            addPosToList(new BlockPos(hHeight - x, -y + height - (shape == Shape.DIAMOND ? 2 : inputHeight), hHeight - z), blockToUse, posList);
                        }
                    }
                }
            }
        }

        final Blueprint blueprint = new Blueprint((short) height, (short) (shape == Shape.DIAMOND ? height : inputHeight + 2), (short) height);
        posList.forEach(blueprint::addBlockState);
        return blueprint;
    }

    /**
     * Generates a cube with the specific size and adds it to the blueprint provided.
     *
     * @param height    the height.
     * @param width     the width.
     * @param length    the length
     * @param block     the block to use.
     * @param fillBlock the fill block.
     * @param hollow    if full.
     */
    private static Blueprint generateCube(
      final int height,
      final int width,
      final int length,
      final BlockState block,
      final BlockState fillBlock,
      final boolean hollow)
    {
        final Map<BlockPos, BlockState> posList = new HashMap<>();
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                for (int z = 0; z < length; z++)
                {
                    if ((x == 0 || x == width - 1) || (y == 0 || y == height - 1) || (z == 0 || z == length - 1))
                    {
                        posList.put(new BlockPos(x, y, z), block);
                    }
                    else if (!hollow)
                    {
                        posList.put(new BlockPos(x, y, z), fillBlock);
                    }
                }
            }
        }
        final Blueprint blueprint = new Blueprint((short) width, (short) height, (short) length);
        posList.forEach(blueprint::addBlockState);
        return blueprint;
    }

    /**
     * Generates a hollow sphere with the specific size and adds it to the blueprint provided.
     *
     * @param height    the height.
     * @param block     the block to use.
     * @param fillBlock the fill block.
     * @param hollow    if hollow.
     * @param shape     the type of shape.
     */
    private static Blueprint generateSphere(
      final int height,
      final BlockState block,
      final BlockState fillBlock,
      final boolean hollow,
      final Shape shape)
    {
        final Map<BlockPos, BlockState> posList = new HashMap<>();
        for (int y = 0; y <= height + 1; y++)
        {
            for (int x = 0; x <= height + 1; x++)
            {
                for (int z = 0; z <= height + 1; z++)
                {
                    int sum = x * x + z * z + y * y;
                    if (sum < height * height && (!hollow || sum > height * height - 2 * height))
                    {
                        final BlockState blockToUse = (sum > height * height - 2 * height) ? block : fillBlock;
                        if (shape == Shape.HALF_SPHERE || shape == Shape.SPHERE)
                        {
                            addPosToList(new BlockPos(height + x, height + y, height + z), blockToUse, posList);
                            addPosToList(new BlockPos(height + x, height + y, height - z), blockToUse, posList);
                            addPosToList(new BlockPos(height - x, height + y, height + z), blockToUse, posList);
                            addPosToList(new BlockPos(height - x, height + y, height - z), blockToUse, posList);
                        }
                        if (shape == Shape.BOWL || shape == Shape.SPHERE)
                        {
                            addPosToList(new BlockPos(height + x, height - y, height + z), blockToUse, posList);
                            addPosToList(new BlockPos(height + x, height - y, height - z), blockToUse, posList);
                            addPosToList(new BlockPos(height - x, height - y, height + z), blockToUse, posList);
                            addPosToList(new BlockPos(height - x, height - y, height - z), blockToUse, posList);
                        }
                    }
                }
            }
        }

        final Blueprint blueprint = new Blueprint((short) ((height + 2) * 2), (short) ((height + 2) * 2), (short) ((height + 2) * 2));
        posList.forEach(blueprint::addBlockState);
        return blueprint;
    }

    /**
     * Generates a cube with the specific size and adds it to the blueprint provided.
     *
     * @param height    the height.
     * @param width     the width.
     * @param block     the block to use.
     * @param fillBlock the fill block.
     * @param hollow    if full.
     */
    private static Blueprint generateCylinder(
      final int height,
      final int width,
      final BlockState block,
      final BlockState fillBlock,
      final boolean hollow)
    {
        final Map<BlockPos, BlockState> posList = new HashMap<>();
        for (int x = 0; x < width; x++)
        {
            for (int z = 0; z < width; z++)
            {
                for (int y = 0; y < height; y++)
                {
                    int sum = x * x + z * z;
                    if (sum < (width * width) / 4 && (!hollow || sum > (width * width) / 4 - width))
                    {
                        final BlockState blockToUse = (sum > (width * width) / 4 - width) ? block : fillBlock;
                        addPosToList(new BlockPos(width + x, y, width + z), blockToUse, posList);
                        addPosToList(new BlockPos(width + x, y, width - z), blockToUse, posList);
                        addPosToList(new BlockPos(width - x, y, width + z), blockToUse, posList);
                        addPosToList(new BlockPos(width - x, y, width - z), blockToUse, posList);
                    }
                }
            }
        }

        final Blueprint blueprint = new Blueprint((short) (width * 2), (short) height, (short) (width * 2));
        posList.forEach(blueprint::addBlockState);
        return blueprint;
    }

    /**
     * Generates a wave with the specific size and adds it to the blueprint provided.
     *
     * @param height the height.
     * @param width  the width.
     * @param length the length.
     * @param block  the block to use.
     */
    private static Blueprint generateWave(
      final int height,
      final int width,
      final int length,
      final int frequency,
      final BlockState block,
      final boolean flat)
    {
        final Map<BlockPos, BlockState> posList = new HashMap<>();
        for (int x = 0; x < length; x++)
        {
            for (int z = 0; z < width; z++)
            {
                final double yVal = (flat ? 0 : z) + (double) frequency * Math.sin(x / (double) height);
                addPosToList(new BlockPos(x, yVal + frequency, (flat ? 0 : width) + z), block, posList);
                if (!flat)
                {
                    addPosToList(new BlockPos(x, yVal + frequency, width - z), block, posList);
                    addPosToList(new BlockPos(x, yVal + width - 1 + frequency, width + z - width + 1), block, posList);
                    addPosToList(new BlockPos(x, yVal + width - 1 + frequency, width - z + width - 1), block, posList);
                }
            }
        }

        final Blueprint blueprint = new Blueprint((short) length, (short) (frequency * 2 + 1 + (!flat ? width * 2 : 0)), (short) (width * 2 + 1));
        posList.forEach(blueprint::addBlockState);
        return blueprint;
    }

    /**
     * Randomly generates shape based on an equation.
     *
     * @param height   the height.
     * @param width    the width.
     * @param length   the length.
     * @param equation the equation.
     * @param block    the block.
     * @return the created blueprint
     */
    public static Blueprint generateRandomShape(final int height, final int width, final int length, final String equation, final BlockState block)
    {
        Expression e = new Expression(equation);
        final Argument argumentX = new Argument("x = 0");
        final Argument argumentY = new Argument("y = 0");
        final Argument argumentZ = new Argument("z = 0");
        final Argument argumentH = new Argument("h = " + height);
        final Argument argumentW = new Argument("w = " + width);
        final Argument argumentL = new Argument("l = " + length);

        e.addArguments(argumentX, argumentY, argumentZ, argumentH, argumentW, argumentL);

        final Map<BlockPos, BlockState> posList = new HashMap<>();
        for (double x = -length / 2.0; x <= length / 2; x++)
        {
            for (double y = -height / 2.0; y <= height / 2; y++)
            {
                for (double z = -width / 2.0; z <= width / 2; z++)
                {
                    argumentX.setArgumentValue(x);
                    argumentY.setArgumentValue(y);
                    argumentZ.setArgumentValue(z);
                    if (e.calculate() == 1)
                    {
                        addPosToList(new BlockPos(x + length / 2.0, y + height / 2.0, z + width / 2.0), block, posList);
                    }
                }
            }
        }
        final Blueprint blueprint = new Blueprint((short) (length + 1), (short) (height + 1), (short) (width + 1));
        posList.forEach(blueprint::addBlockState);
        return blueprint;
    }

    /**
     * Add the position to list if not already.
     *
     * @param blockPos   the pos to add.
     * @param blockToUse the block to use.
     * @param posList    the list to add it to.
     */
    private static void addPosToList(final BlockPos blockPos, final BlockState blockToUse, final Map<BlockPos, BlockState> posList)
    {
        if (!posList.containsKey(blockPos))
        {
            posList.put(blockPos, blockToUse);
        }
    }

    /**
     * Undo a change to the world made by a player.
     *
     * @param player the player who made it.
     */
    public static void undo(final PlayerEntity player)
    {
        final Iterable<ChangeStorage> iterable = () -> changeQueue.iterator();
        final Stream<ChangeStorage> storageStream = StreamSupport.stream(iterable.spliterator(), false);
        final Optional<ChangeStorage> theStorage = storageStream.filter(storage -> storage.isOwner(player)).findFirst();
        if (theStorage.isPresent())
        {
            addToQueue(new TickedWorldOperation(theStorage.get(), player));
            changeQueue.remove(theStorage.get());
        }
    }

    /**
     * Get the Universal Unique ID for the server.
     *
     * @return the server Universal Unique ID for ther
     */
    public static UUID getServerUUID()
    {
        if (serverUUID == null)
        {
            return generateOrRetrieveUUID();
        }
        return serverUUID;
    }

    /**
     * Generate or retrieve the UUID of the server.
     *
     * @return the UUID.
     */
    private static UUID generateOrRetrieveUUID()
    {
        final DimensionSavedDataManager storage = ServerLifecycleHooks.getCurrentServer().func_241755_D_().getSavedData();
        final UUIDStorage instance = storage.getOrCreate(UUIDStorage::new, UUIDStorage.DATA_NAME);
        if (serverUUID == null)
        {
            Manager.setServerUUID(UUID.randomUUID());
            Log.getLogger().info(String.format("New Server UUID %s", serverUUID));
        }
        storage.set(instance);

        return serverUUID;
    }

    /**
     * Set the server UUID.
     *
     * @param uuid the universal unique id
     */
    public static void setServerUUID(final UUID uuid)
    {
        serverUUID = uuid;
    }

    /**
     * Whether or not a new schematic have been downloaded.
     *
     * @return True if a new schematic have been received.
     */
    public static boolean isSchematicDownloaded()
    {
        return schematicDownloaded;
    }

    /**
     * Set the schematic downloaded
     *
     * @param downloaded True if a new schematic have been received.
     */
    public static void setSchematicDownloaded(final boolean downloaded)
    {
        schematicDownloaded = downloaded;
    }
}
