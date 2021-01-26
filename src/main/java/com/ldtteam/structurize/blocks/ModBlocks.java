package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.bricks.BlockBricks;
import com.ldtteam.structurize.blocks.cactus.*;
import com.ldtteam.structurize.blocks.decorative.*;
import com.ldtteam.structurize.blocks.schematic.BlockFluidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSolidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSubstitution;
import com.ldtteam.structurize.blocks.types.*;
import com.ldtteam.structurize.creativetab.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.DyeColor;
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
    private static final List<BlockFloatingCarpet> floatingCarpets = new ArrayList<>();

    public static BlockSubstitution      blockSubstitution;
    public static BlockSolidSubstitution blockSolidSubstitution;
    public static BlockFluidSubstitution blockFluidSubstitution;

    /**
     * Utility blocks.
     */

    public static BlockBarrel blockDecoBarrel_onside;
    public static BlockBarrel blockDecoBarrel_standing;

    public static BlockBricks blockBrownBricks;
    public static BlockBricks blockBeigeBricks;
    public static BlockBricks blockCreamBricks;

    public static BlockCactusPlank         blockCactusPlank;
    public static BlockCactusDoor          blockCactusDoor;
    public static BlockCactusTrapdoor      blockCactusTrapdoor;
    public static BlockCactusStair         blockCactusStair;
    public static BlockMinecoloniesSlab<?> blockCactusSlab;
    public static BlockCactusFence         blockCactusFence;
    public static BlockCactusFenceGate     blockCactusFenceGate;

    public static MultiBlock multiBlock;

    public static PlaceholderBlock placeholderBlock;

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

    public static List<BlockFloatingCarpet> getFloatingCarpets()
    {
        return new ArrayList<>(floatingCarpets);
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
     * Also, this method registers blocks with Forge.
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
        blockCactusSlab = new BlockMinecoloniesSlab<>(Block.Properties.from(blockCactusPlank), "blockcactusslab").registerBlock(registry);

        blockCactusStair = new BlockCactusStair().registerBlock(registry);
        blockCactusFence = new BlockCactusFence().registerBlock(registry);
        blockCactusFenceGate = new BlockCactusFenceGate().registerBlock(registry);

        blockFluidSubstitution = new BlockFluidSubstitution().registerBlock(registry);
        blockSolidSubstitution = new BlockSolidSubstitution().registerBlock(registry);
        blockSubstitution = new BlockSubstitution().registerBlock(registry);

        blockDecoBarrel_onside = new BlockBarrel("blockbarreldeco_onside").registerBlock(registry);
        blockDecoBarrel_standing = new BlockBarrel("blockbarreldeco_standing").registerBlock(registry);

        blockBrownBricks = new BlockBricks().registerBlock(registry);
        blockBeigeBricks = new BlockBricks().registerBlock(registry);
        blockCreamBricks = new BlockBricks().registerBlock(registry);

        for (final PaperwallType type : PaperwallType.values())
        {
            final BlockPaperwall blockPaperWall = new BlockPaperwall(type.getName()).registerBlock(registry);
            paperwalls.add(blockPaperWall);
        }

        for (final ShingleFaceType shingleFace : ShingleFaceType.values())
        {
            shingleSlabs.add(new BlockShingleSlab(shingleFace).registerBlock(registry));

            for (final ShingleWoodType shingleWood : ShingleWoodType.values())
            {
                shingles.add(new BlockShingle(() -> Blocks.OAK_PLANKS.getDefaultState(), shingleWood, shingleFace).registerBlock(registry));
            }
        }

        for (final TimberFrameType blockType : TimberFrameType.values())
        {
            for (final TimberFrameFrameType frameType : TimberFrameFrameType.values())
            {
                for (TimberFrameCentreType centreType : TimberFrameCentreType.values())
                {
                    timberFrames.add(new BlockTimberFrame(blockType, frameType, centreType).registerBlock(registry));
                }
            }
        }

        for (final DyeColor color : DyeColor.values())
        {
            floatingCarpets.add(new BlockFloatingCarpet(color, Block.Properties.create(Material.CARPET).hardnessAndResistance(0.1F).sound(SoundType.CLOTH)).registerBlock(registry));
        }

        multiBlock = new MultiBlock().registerBlock(registry);

        placeholderBlock = new PlaceholderBlock().registerBlock(registry);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        final IForgeRegistry<Item> registry = event.getRegistry();
        final Item.Properties properties = new Item.Properties().group(ModCreativeTabs.STRUCTURIZE);
        final Item.Properties shingleProperties = new Item.Properties().group(ModCreativeTabs.SHINGLES);
        final Item.Properties timberframeProperties = new Item.Properties().group(ModCreativeTabs.TIMBER_FRAMES);

        blockFluidSubstitution.registerItemBlock(registry, properties);
        blockSolidSubstitution.registerItemBlock(registry, properties);
        blockSubstitution.registerItemBlock(registry, properties);

        for (final BlockPaperwall paperwall : paperwalls)
        {
            paperwall.registerItemBlock(registry, properties);
        }

        for (final BlockShingle shingle : shingles)
        {
            shingle.registerItemBlock(registry, shingleProperties);
        }

        for (BlockShingleSlab shingleSlab : shingleSlabs)
        {
            shingleSlab.registerItemBlock(registry, shingleProperties);
        }

        blockCactusPlank.registerItemBlock(registry, properties);
        blockCactusTrapdoor.registerItemBlock(registry, properties);
        blockCactusStair.registerItemBlock(registry, properties);
        blockCactusSlab.registerItemBlock(registry, properties);
        blockCactusFence.registerItemBlock(registry, properties);
        blockCactusFenceGate.registerItemBlock(registry, properties);

        blockDecoBarrel_onside.registerItemBlock(registry, properties);
        blockDecoBarrel_standing.registerItemBlock(registry, properties);

        blockBrownBricks.registerItemBlock(registry, properties);
        blockBeigeBricks.registerItemBlock(registry, properties);
        blockCreamBricks.registerItemBlock(registry, properties);

        for (final BlockTimberFrame frame : timberFrames)
        {
            frame.registerItemBlock(registry, timberframeProperties);
        }

        for (final BlockFloatingCarpet carpet : floatingCarpets)
        {
            carpet.registerItemBlock(registry, properties);
        }

        multiBlock.registerItemBlock(registry, properties);

        placeholderBlock.registerItemBlock(registry, properties);
    }
}
