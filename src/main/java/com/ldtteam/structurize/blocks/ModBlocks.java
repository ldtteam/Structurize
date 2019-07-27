package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.cactus.*;
import com.ldtteam.structurize.blocks.decorative.*;
import com.ldtteam.structurize.blocks.schematic.BlockSolidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSubstitution;
import com.ldtteam.structurize.blocks.types.PaperwallType;
import com.ldtteam.structurize.blocks.types.ShingleFaceType;
import com.ldtteam.structurize.blocks.types.ShingleWoodType;
import com.ldtteam.structurize.blocks.types.TimberFrameType;
import com.ldtteam.structurize.creativetab.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to create the modBlocks.
 * References to the blocks can be made here
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S2444", "squid:S1444", "squid:S1820",})

@ObjectHolder(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBlocks
{
    /*
     * Creating objects for all blocks in the mod.
     * References can be made to here.
     */

    private static final List<BlockTimberFrame> timberFrames = new ArrayList<>();
    private static final List<BlockPaperwall>   paperwalls   = new ArrayList<>();
    private static final List<BlockShingle> shingles = new ArrayList<>();
    private static final List<BlockShingleSlab> shingleSlabs = new ArrayList<>();

    public static BlockSubstitution      blockSubstitution;
    public static BlockSolidSubstitution blockSolidSubstitution;

    /**
     * Utility blocks.
     */

    public static BlockCactusPlank      blockCactusPlank;
    public static BlockCactusDoor       blockCactusDoor;
    public static BlockCactusTrapdoor   blockCactusTrapdoor;
    public static BlockCactusStair      blockCactusStair;
    public static BlockMinecoloniesSlab blockCactusSlab;
    public static BlockCactusFence      blockCactusFence;
    public static BlockCactusFenceGate  blockCactusFenceGate;

    public static MultiBlock multiBlock;

    public static List<BlockTimberFrame> getTimberFrames()
    {
        return new ArrayList<>(timberFrames);
    }

    public static List<BlockPaperwall> getPaperwalls()
    {
        return new ArrayList<>(paperwalls);
    }

    public static List<BlockShingle> getShingles()
    {
        return new ArrayList<>(shingles);
    }

    public static List<BlockShingleSlab> getShingleSlabs()
    {
        return new ArrayList<>(shingleSlabs);
    }

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModBlocks()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Make sure to add any new blocks to {@link com.ldtteam.structurize.generation.defaults.DefaultBlockLootTableProvider}
     * Also this method registeres blocks with forge. kinda obvious.
     *
     * @param event block registering event
     */
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        final IForgeRegistry<Block> registry = event.getRegistry();
        blockCactusPlank = new BlockCactusPlank().registerBlock(registry);
        blockCactusDoor = new BlockCactusDoor().registerBlock(registry);
        blockCactusTrapdoor = new BlockCactusTrapdoor().registerBlock(registry);
        blockCactusSlab = new BlockMinecoloniesSlab(Block.Properties.from(blockCactusPlank), "blockcactusslab").registerBlock(registry);

        blockCactusStair = new BlockCactusStair().registerBlock(registry);
        blockCactusFence = new BlockCactusFence().registerBlock(registry);
        blockCactusFenceGate = new BlockCactusFenceGate().registerBlock(registry);

        blockSolidSubstitution = new BlockSolidSubstitution().registerBlock(registry);
        blockSubstitution = new BlockSubstitution().registerBlock(registry);

        for (final PaperwallType type : PaperwallType.values())
        {
            final BlockPaperwall blockPaperWall = new BlockPaperwall(type.getName()).registerBlock(registry);
            paperwalls.add(blockPaperWall);
        }

        for (final ShingleFaceType shingleFace : ShingleFaceType.values())
        {
            final BlockShingleSlab shingleSlab = new BlockShingleSlab(shingleFace);
            shingleSlab.registerBlock(registry);
            shingleSlabs.add(shingleSlab);

            for (final ShingleWoodType shingleWood : ShingleWoodType.values())
            {
                final BlockShingle shingle = new BlockShingle(Blocks.OAK_PLANKS.getDefaultState(), shingleWood, shingleFace);
                shingle.registerBlock(registry);
                shingles.add(shingle);
            }
        }

        for (final TimberFrameType frameType : TimberFrameType.values())
        {
            timberFrames.add(new BlockTimberFrame(BlockTimberFrame.BLOCK_NAME + "_" + "birch" + "_" + frameType.getName()).registerBlock(registry));
        }

        for (final TimberFrameType frameType : TimberFrameType.values())
        {
            timberFrames.add(new BlockTimberFrame(BlockTimberFrame.BLOCK_NAME + "_" + "jungle" + "_" + frameType.getName()).registerBlock(registry));
        }

        for (final TimberFrameType frameType : TimberFrameType.values())
        {
            timberFrames.add(new BlockTimberFrame(BlockTimberFrame.BLOCK_NAME + "_" + "oak" + "_" + frameType.getName()).registerBlock(registry));
        }

        for (final TimberFrameType frameType : TimberFrameType.values())
        {
            timberFrames.add(new BlockTimberFrame(BlockTimberFrame.BLOCK_NAME + "_" + "dark_oak" + "_" + frameType.getName()).registerBlock(registry));
        }

        for (final TimberFrameType frameType : TimberFrameType.values())
        {
            timberFrames.add(new BlockTimberFrame(BlockTimberFrame.BLOCK_NAME + "_" + "acacia" + "_" + frameType.getName()).registerBlock(registry));
        }

        for (final TimberFrameType frameType : TimberFrameType.values())
        {
            timberFrames.add(new BlockTimberFrame(BlockTimberFrame.BLOCK_NAME + "_" + "spruce" + "_" + frameType.getName()).registerBlock(registry));
        }

        multiBlock = new MultiBlock().registerBlock(registry);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        final IForgeRegistry<Item> registry = event.getRegistry();
        final Item.Properties properties = new Item.Properties().group(ModCreativeTabs.STRUCTURIZE);
        blockSolidSubstitution.registerItemBlock(registry, properties);
        blockSubstitution.registerItemBlock(registry, properties);

        for (final BlockPaperwall paperwall : paperwalls)
        {
            paperwall.registerItemBlock(registry, properties);
        }

        for (final BlockShingle shingle : shingles)
        {
            shingle.registerItemBlock(registry, properties);
        }

        for (BlockShingleSlab shingleSlab : shingleSlabs)
        {
            shingleSlab.registerItemBlock(registry, properties);
        }

        blockCactusPlank.registerItemBlock(registry, properties);
        blockCactusTrapdoor.registerItemBlock(registry, properties);
        blockCactusStair.registerItemBlock(registry, properties);
        blockCactusSlab.registerItemBlock(registry, properties);
        blockCactusFence.registerItemBlock(registry, properties);
        blockCactusFenceGate.registerItemBlock(registry, properties);

        for (final BlockTimberFrame frame : timberFrames)
        {
            frame.registerItemBlock(registry, properties);
        }

        multiBlock.registerItemBlock(registry, properties);
    }
}
