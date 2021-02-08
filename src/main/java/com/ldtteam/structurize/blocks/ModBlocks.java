package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.decorative.*;
import com.ldtteam.structurize.blocks.schematic.BlockFluidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSolidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSubstitution;
import com.ldtteam.structurize.blocks.types.*;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Class to create the modBlocks.
 * References to the blocks can be made here
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
public final class ModBlocks
{
    private ModBlocks() { /* prevent construction */ }

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);

    public static DeferredRegister<Block> getRegistry()
    {
        return BLOCKS;
    }

    /*
     *  Lone Blocks
     */

    public static final RegistryObject<BlockSubstitution> blockSubstitution;
    public static final RegistryObject<BlockSolidSubstitution> blockSolidSubstitution;
    public static final RegistryObject<BlockFluidSubstitution> blockFluidSubstitution;
    public static final RegistryObject<MultiBlock>  multiBlock;
    public static final RegistryObject<BlockBarrel> blockDecoBarrel_onside;
    public static final RegistryObject<BlockBarrel> blockDecoBarrel_standing;

    /*
     *  Block Collections
     */

    public static IBlockCollection CACTI_BLOCKS = new CactusCollection();
    public static List<IBlockCollection> BRICKS = Arrays.asList(BrickType.values());

    /*
     *  Block mass registration lists
     */

    public static final List<RegistryObject<BlockTimberFrame>>    timberFrames = new ArrayList<>();
    public static final List<RegistryObject<BlockPaperWall>>      paperWalls   = new ArrayList<>();
    public static final List<RegistryObject<BlockShingle>>        shingles     = new ArrayList<>();
    public static final List<RegistryObject<BlockShingleSlab>>    shingleSlabs    = new ArrayList<>();
    public static final List<RegistryObject<BlockFloatingCarpet>> floatingCarpets = new ArrayList<>();

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
        RegistryObject<B> registered = BLOCKS.register(name.toLowerCase(), block);
        ModItems.getRegistry().register(name.toLowerCase(), () -> new BlockItem(registered.get(), new Item.Properties().group(group)));
        return registered;
    }

    static
    {
        blockSubstitution       = register("blockSubstitution", BlockSubstitution::new, ModItemGroups.STRUCTURIZE);
        blockSolidSubstitution  = register("blockSolidSubstitution", BlockSolidSubstitution::new, ModItemGroups.STRUCTURIZE);
        blockFluidSubstitution  = register("blockFluidSubstitution", BlockFluidSubstitution::new, ModItemGroups.STRUCTURIZE);
        multiBlock              = register("multiBlock", MultiBlock::new, ModItemGroups.STRUCTURIZE);
        blockDecoBarrel_onside  = register("blockbarreldeco_onside", BlockBarrel::new, ModItemGroups.STRUCTURIZE);
        blockDecoBarrel_standing = register("blockbarreldeco_standing", BlockBarrel::new, ModItemGroups.STRUCTURIZE);

        for (final PaperWallType paper : PaperWallType.values())
        {
            paperWalls.add(register(
              paper.getName() + "_blockpaperwall",
              () -> new BlockPaperWall(paper.getName()),
              ModItemGroups.CONSTRUCTION
            ));
        }

        for (final ShingleFaceType shingleFace : ShingleFaceType.values())
        {
            shingleSlabs.add(register(
              shingleFace.getName() + "_shingle_slab",
              () -> new BlockShingleSlab(shingleFace),
              ModItemGroups.SHINGLES));

            for (final ShingleWoodType shingleWood : ShingleWoodType.values())
            {
                shingles.add(register(
                  String.format("%s_%s_shingle", shingleFace.getName(), shingleWood.getName()),
                  () -> new BlockShingle(Blocks.OAK_PLANKS::getDefaultState, shingleWood, shingleFace),
                  ModItemGroups.SHINGLES));
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
                      ModItemGroups.TIMBER_FRAMES));
                }
            }
        }

        for (final DyeColor color : DyeColor.values())
        {
            floatingCarpets.add(register(
              color.getTranslationKey() + "_floating_carpet",
              () -> new BlockFloatingCarpet(color, Block.Properties.create(Material.CARPET).hardnessAndResistance(0.1F).sound(SoundType.CLOTH)),
              ModItemGroups.STRUCTURIZE));
        }
    }
}
