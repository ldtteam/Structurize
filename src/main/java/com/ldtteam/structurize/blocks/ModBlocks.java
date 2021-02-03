package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.util.constant.Constants;
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
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.items.ModItems.ITEMS;

/**
 * Class to create the modBlocks.
 * References to the blocks can be made here
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */

@ObjectHolder(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBlocks
{
    private ModBlocks() { /* prevent construction */ }

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);

    /*
     *  Forge deferred registry injection
     */

    public static final BlockSubstitution      blockSubstitution        = null;
    public static final BlockSolidSubstitution blockSolidSubstitution   = null;
    public static final BlockFluidSubstitution blockFluidSubstitution   = null;
    public static final BlockBarrel            blockDecoBarrel_onside   = null;
    public static final BlockBarrel            blockDecoBarrel_standing = null;
    public static final MultiBlock             multiBlock               = null;

    /*
     *  Block Collections
     */

    public static final IBlockCollection CACTI_BLOCKS = new CactusCollection();
    public static final List<IBlockCollection> BRICKS = IBlockCollection.each(
      BrickType.values(), BLOCKS, ITEMS,
      IBlockCollection.BlockType.BLOCK,
      IBlockCollection.BlockType.SLAB,
      IBlockCollection.BlockType.STAIRS,
      IBlockCollection.BlockType.WALL
    );

    /*
     *  Non-collection Block lists for mass registration
     */

    private static final List<RegistryObject<BlockTimberFrame>> timberFrames = new ArrayList<>();
    private static final List<RegistryObject<BlockPaperWall>>   paperWalls   = new ArrayList<>();
    private static final List<RegistryObject<BlockShingle>>     shingles     = new ArrayList<>();
    private static final List<RegistryObject<BlockShingleSlab>>     shingleSlabs    = new ArrayList<>();
    private static final List<RegistryObject<BlockFloatingCarpet>>  floatingCarpets = new ArrayList<>();

    public static List<BlockTimberFrame> getTimberFrames()
    {
        return getList(timberFrames);
    }

    public static List<BlockPaperWall> getPaperWalls()
    {
        return getList(paperWalls);
    }

    public static List<BlockShingle> getShingles()
    {
        return getList(shingles);
    }

    public static List<BlockShingleSlab> getShingleSlabs()
    {
        return getList(shingleSlabs);
    }

    public static List<BlockFloatingCarpet> getFloatingCarpets()
    {
        return getList(floatingCarpets);
    }

    public static <B extends Block> List<B> getList(List<RegistryObject<B>> list)
    {
        return list.stream().map(RegistryObject::get).collect(Collectors.toList());
    }

    public static <B extends Block> RegistryObject<B> register(String name, Supplier<B> block, ItemGroup group)
    {
        RegistryObject<B> registered = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties().group(group)));
        return registered;
    }

    public static <T extends IBlockList<B>, B extends Block> List<RegistryObject<B>> register(T[] values, ItemGroup group)
    {
        List<RegistryObject<B>> blocks = new LinkedList<>();
        for (T block : values)
        {
            blocks.add(register(block.getString(), block::construct, group));
        }

        return blocks;
    }

    static
    {
        register("blockSubstitution", BlockSubstitution::new, ModCreativeTabs.STRUCTURIZE);
        register("blockSolidSubstitution", BlockSolidSubstitution::new, ModCreativeTabs.STRUCTURIZE);
        register("blockFluidSubstitution", BlockFluidSubstitution::new, ModCreativeTabs.STRUCTURIZE);
        register("multiBlock", MultiBlock::new, ModCreativeTabs.STRUCTURIZE);
        register("blockbarreldeco_onside", BlockBarrel::new, ModCreativeTabs.STRUCTURIZE);
        register("blockbarreldeco_standing", BlockBarrel::new, ModCreativeTabs.STRUCTURIZE);

        for (final PaperWallType paper : PaperWallType.values())
        {
            paperWalls.add(register(
              paper.getName() + "_blockpaperwall",
              () -> new BlockPaperWall(paper.getName()),
              ModCreativeTabs.STRUCTURIZE
            ));
        }

        for (final ShingleFaceType shingleFace : ShingleFaceType.values())
        {
            shingleSlabs.add(register(
              shingleFace.getName() + "_shingle_slab",
              () -> new BlockShingleSlab(shingleFace),
              ModCreativeTabs.SHINGLES));

            for (final ShingleWoodType shingleWood : ShingleWoodType.values())
            {
                shingles.add(register(
                  String.format("%s_%s_shingle", shingleFace.getName(), shingleWood.getName()),
                  () -> new BlockShingle(Blocks.OAK_PLANKS::getDefaultState, shingleWood, shingleFace),
                  ModCreativeTabs.SHINGLES));
            }
        }

        for (final TimberFrameType blockType : TimberFrameType.values())
        {
            for (final TimberFrameFrameType frameType : TimberFrameFrameType.values())
            {
                for (TimberFrameCentreType centreType : TimberFrameCentreType.values())
                {
                    timberFrames.add(register(
                      BlockTimberFrame.getName(blockType, frameType, centreType),
                      () -> new BlockTimberFrame(blockType, frameType, centreType),
                      ModCreativeTabs.TIMBER_FRAMES));
                }
            }
        }

        for (final DyeColor color : DyeColor.values())
        {
            floatingCarpets.add(register(
              color.getTranslationKey() + "_floating_carpet",
              () -> new BlockFloatingCarpet(color, Block.Properties.create(Material.CARPET).hardnessAndResistance(0.1F).sound(SoundType.CLOTH)),
              ModCreativeTabs.STRUCTURIZE));
        }
    }
}
