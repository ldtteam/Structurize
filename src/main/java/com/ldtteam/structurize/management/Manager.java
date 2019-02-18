package com.ldtteam.structurize.management;

import com.ldtteam.structurize.api.configuration.Configurations;
import com.ldtteam.structurize.api.util.BlockUtils;
import com.ldtteam.structurize.api.util.ChangeStorage;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.Shape;
import com.ldtteam.structurize.util.ScanToolOperation;
import com.ldtteam.structurize.util.StructurePlacementUtils;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;

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
    private static LinkedList<ScanToolOperation> scanToolOperationPool = new LinkedList<>();

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
    public static void onWorldTick(final WorldServer world)
    {
        if (!scanToolOperationPool.isEmpty())
        {
            final ScanToolOperation operation = scanToolOperationPool.peek();
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
    public static void addToQueue(final ScanToolOperation operation)
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
        if (changeQueue.size() >= Configurations.gameplay.maxCachedChanges)
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
     * @param shape          the shape.
     * @param inputBlock     the input block.
     * @param inputFillBlock the fill block.
     * @param hollow         if hollow or not.
     * @param player         the player.
     * @param mirror         the mirror.
     * @param rotation       the rotation.
     */
    public static void pasteStructure(
      final WorldServer server,
      final BlockPos pos,
      final int width,
      final int length,
      final int height,
      final int frequency,
      final Shape shape,
      final ItemStack inputBlock,
      final ItemStack inputFillBlock,
      final boolean hollow,
      final EntityPlayerMP player,
      final Mirror mirror,
      final Rotation rotation)
    {
        final Blueprint blueprint = Manager.getStructureFromFormula(width, length, height, frequency, shape, inputBlock, inputFillBlock, hollow);
        StructurePlacementUtils.loadAndPlaceShapeWithRotation(server, blueprint, pos, rotation, mirror, player);
    }

    /**
     * Just returns a cube for now, I can tinker this later.
     *
     * @param width          the width.
     * @param length         the length.
     * @param height         the height.
     * @param shape          the shape.
     * @param inputBlock     the input block.
     * @param inputFillBlock the fill block.
     * @param hollow         if hollow or not.
     */
    public static Blueprint getStructureFromFormula(
      final int width,
      final int length,
      final int height,
      final int frequency,
      final Shape shape,
      final ItemStack inputBlock,
      final ItemStack inputFillBlock, final boolean hollow)
    {
        final Blueprint blueprint;
        final IBlockState mainBlock = BlockUtils.getBlockStateFromStack(inputBlock);
        final IBlockState fillBlock = BlockUtils.getBlockStateFromStack(inputFillBlock);

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
        else
        {
            blueprint = generatePyramid(height, mainBlock, fillBlock, hollow, shape);
        }
        return blueprint;
    }

    private static Blueprint generatePyramid(
      final int inputHeight,
      final IBlockState block,
      final IBlockState fillBlock,
      final boolean hollow,
      final Shape shape)
    {
        final int height = shape == Shape.DIAMOND ? inputHeight : inputHeight * 2;
        final Map<BlockPos, IBlockState> posList = new HashMap<>();
        for (int y = 0; y < height / 2; y++)
        {
            for (int x = 0; x < height / 2; x++)
            {
                for (int z = 0; z < height / 2; z++)
                {
                    if (((x == z && x >= y) || (x == y && x >= z) || ((hollow ? y == z : y >= z) && y >= x)) && x * z <= y * y)
                    {
                        final IBlockState blockToUse = x == z && x >= y || x == y || y == z ? block : fillBlock;
                        if (shape == Shape.UPSIDE_DOWN_PYRAMID || shape == Shape.DIAMOND)
                        {
                            addPosToList(new BlockPos(x, y, z), blockToUse, posList);
                            addPosToList(new BlockPos(x, y, -z), blockToUse, posList);
                            addPosToList(new BlockPos(-x, y, z), blockToUse, posList);
                            addPosToList(new BlockPos(-x, y, -z), blockToUse, posList);
                        }

                        if (shape == Shape.PYRAMID || shape == Shape.DIAMOND)
                        {
                            addPosToList(new BlockPos(x, -y + height - 2, z), blockToUse, posList);
                            addPosToList(new BlockPos(x, -y + height - 2, -z), blockToUse, posList);
                            addPosToList(new BlockPos(-x, -y + height - 2, z), blockToUse, posList);
                            addPosToList(new BlockPos(-x, -y + height - 2, -z), blockToUse, posList);
                        }
                    }
                }
            }
        }

        final Blueprint blueprint = new Blueprint((short)height, (short) height, (short)height);
        posList.forEach(blueprint::addBlockState);
        return blueprint;
    }

    /**
     * Generates a cube with the specific size and adds it to the blueprint provided.
     * @param height    the height.
     * @param width     the width.
     * @param length    the length.
     * @param block     the block to use.
     * @param fillBlock the fill block.
     * @param hollow    if full.
     */
    private static Blueprint generateCube(
      final int height,
      final int width,
      final int length,
      final IBlockState block,
      final IBlockState fillBlock,
      final boolean hollow)
    {
        final Map<BlockPos, IBlockState> posList = new HashMap<>();
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                for (int z = 0; z < length; z++)
                {
                    if (!hollow || ((x == 0 || x == width - 1) || (y == 0 || y == height - 1) || (z == 0 || z == length - 1)))
                    {
                        final IBlockState blockToUse = ((x == 0 || x == width - 1) || (y == 0 || y == height - 1) || (z == 0 || z == length - 1)) ? block : fillBlock;
                        posList.put(new BlockPos(x, y, z), blockToUse);
                    }
                }
            }
        }
        final Blueprint blueprint = new Blueprint((short)height, (short) height, (short)height);
        posList.forEach(blueprint::addBlockState);
        return blueprint;
    }

    /**
     * Generates a hollow sphere with the specific size and adds it to the blueprint provided.
     * @param height    the height.
     * @param block     the block to use.
     * @param fillBlock the fill block.
     * @param hollow    if hollow.
     * @param shape     the type of shape.
     */
    private static Blueprint generateSphere(
      final int height,
      final IBlockState block,
      final IBlockState fillBlock,
      final boolean hollow,
      final Shape shape)
    {
        final Map<BlockPos, IBlockState> posList = new HashMap<>();
        for (int y = 0; y <= height + 1; y++)
        {
            for (int x = 0; x <= height + 1; x++)
            {
                for (int z = 0; z <= height + 1; z++)
                {
                    int sum = x * x + z * z + y * y;
                    if (sum < height * height && (!hollow || sum > height * height - 2 * height))
                    {
                        final IBlockState blockToUse = (sum > height * height - 2 * height) ? block : fillBlock;
                        if (shape == Shape.HALF_SPHERE || shape == Shape.SPHERE)
                        {
                            addPosToList(new BlockPos(x, y, z), blockToUse, posList);
                            addPosToList(new BlockPos(x, y, -z), blockToUse, posList);
                            addPosToList(new BlockPos(-x, y, z), blockToUse, posList);
                            addPosToList(new BlockPos(-x, y, -z), blockToUse, posList);
                        }
                        if (shape == Shape.BOWL || shape == Shape.SPHERE)
                        {
                            addPosToList(new BlockPos(x, -y, z), blockToUse, posList);
                            addPosToList(new BlockPos(x, -y, -z), blockToUse, posList);
                            addPosToList(new BlockPos(-x, -y, z), blockToUse, posList);
                            addPosToList(new BlockPos(-x, -y, -z), blockToUse, posList);
                        }
                    }
                }
            }
        }

        final Blueprint blueprint = new Blueprint((short)(height * 2), (short) (height * 2), (short)(height * 2));
        posList.forEach(blueprint::addBlockState);
        return blueprint;
    }

    /**
     * Generates a cube with the specific size and adds it to the blueprint provided.
     * @param height   the height.
     * @param width    the width.
     * @param block    the block to use.
     * @param fillBlock the fill block.
     * @param hollow   if full.
     */
    private static Blueprint generateCylinder(
      final int height,
      final int width,
      final IBlockState block,
      final IBlockState fillBlock,
      final boolean hollow)
    {
        final Map<BlockPos, IBlockState> posList = new HashMap<>();
        for (int x = 0; x < width; x++)
        {
            for (int z = 0; z < width; z++)
            {
                for (int y = 0; y < height; y++)
                {
                    int sum = x * x + z * z;
                    if (sum < (width * width) / 4 && (!hollow || sum > (width * width) / 4 - width))
                    {
                        final IBlockState blockToUse = (sum > (width * width) / 4 - width) ? block : fillBlock;
                        addPosToList(new BlockPos(x, y, z), blockToUse, posList);
                        addPosToList(new BlockPos(x, y, -z), blockToUse, posList);
                        addPosToList(new BlockPos(-x, y, z), blockToUse, posList);
                        addPosToList(new BlockPos(-x, y, -z), blockToUse, posList);
                    }
                }
            }
        }

        final Blueprint blueprint = new Blueprint((short)width, (short) height, (short)width);
        posList.forEach(blueprint::addBlockState);
        return blueprint;
    }

    /**
     * Generates a wave with the specific size and adds it to the blueprint provided.
     * @param height   the height.
     * @param width    the width.
     * @param length   the length.
     * @param block    the block to use.
     */
    private static Blueprint generateWave(
      final int height,
      final int width,
      final int length,
      final int frequency,
      final IBlockState block,
      final boolean flat)
    {
        final Map<BlockPos, IBlockState> posList = new HashMap<>();
        for (int x = 0; x < length; x++)
        {
            for (int z = 0; z < width; z++)
            {
                final double yVal = (flat ? 0 : z) + (double) frequency * Math.sin(x / (double) height);
                addPosToList(new BlockPos(x, yVal, z), block, posList);
                if (!flat)
                {
                    addPosToList(new BlockPos(x, yVal, -z), block, posList);
                    addPosToList(new BlockPos(x, yVal + width - 1, z - width + 1), block, posList);
                    addPosToList(new BlockPos(x, yVal + width - 1, -z + width - 1), block, posList);
                }
            }
        }

        final Blueprint blueprint = new Blueprint((short)length, (short) (height * length + 1), (short) (width * 2 + 1));
        posList.forEach(blueprint::addBlockState);
        return blueprint;
    }

    public static void generateRandomShape(final int height, final int width, final int length)
    {
        final double radiusX = 20;
        final double radiusY = 26;
        final double radiusZ = 5;
        final List<Tuple<BlockPos, IBlockState>> posList = new ArrayList<>();

        for (double x = 0; x <= radiusX; x++)
        {
            for (double y = 0; y <= radiusY; y++)
            {
                for (double z = 0; z <= radiusZ; z++)
                {

                }
            }
        }
    }

    /**
     * Add the position to list if not already.
     *
     * @param blockPos the pos to add.
     * @param blockToUse the block to use.
     * @param posList  the list to add it to.
     */
    private static void addPosToList(final BlockPos blockPos, final IBlockState blockToUse, final Map<BlockPos, IBlockState> posList)
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
    public static void undo(final EntityPlayer player)
    {
        final Iterable<ChangeStorage> iterable = () -> changeQueue.iterator();
        final Stream<ChangeStorage> storageStream = StreamSupport.stream(iterable.spliterator(), false);
        final Optional<ChangeStorage> theStorage = storageStream.filter(storage -> storage.isOwner(player)).findFirst();
        if (theStorage.isPresent())
        {
            addToQueue(new ScanToolOperation(theStorage.get(), player));
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
        final MapStorage storage = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getMapStorage();
        UUIDStorage instance = (UUIDStorage) storage.getOrLoadData(UUIDStorage.class, UUIDStorage.DATA_NAME);

        if (instance == null)
        {
            if (serverUUID == null)
            {
                Manager.setServerUUID(UUID.randomUUID());
                Log.getLogger().info(String.format("New Server UUID %s", serverUUID));
            }
            storage.setData(UUIDStorage.DATA_NAME, new UUIDStorage());
        }
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
