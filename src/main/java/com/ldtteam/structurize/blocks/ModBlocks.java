package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.blocks.IBlockCollection;
import com.ldtteam.structurize.api.blocks.IBlockList;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.decorative.*;
import com.ldtteam.structurize.blocks.schematic.BlockFluidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSolidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSubstitution;
import com.ldtteam.structurize.blocks.types.BrickType;
import com.ldtteam.structurize.blocks.types.ShingleFaceType;
import com.ldtteam.structurize.blocks.types.TimberFrameType;
import com.ldtteam.structurize.generation.DefaultBlockLootTableProvider;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Class to register blocks to Structurize
 *
 * Don't forget to add them to the generators!
 * Minimum is a save call in {@link DefaultBlockLootTableProvider}.
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

    public static final IBlockCollection       CACTI_BLOCKS = new CactusCollection();
    public static final List<IBlockCollection> BRICKS       = Arrays.asList(BrickType.values());

    /*
     *  Block mass registration lists
     */

    public static final IBlockList<BlockPaperWall>      paperWalls      = new PaperWallList();
    public static final IBlockList<BlockFloatingCarpet> floatingCarpets = new FloatingCarpetList();
    public static final List<TimberFrameType>           timberFrames    = TimberFrameType.getAll();
    public static final List<ShingleFaceType>           shingles        = Arrays.asList(ShingleFaceType.values());
    public static final IBlockList<BlockShingleSlab> shingleSlabs = new ShingleSlabList();

    public static final ITag.INamedTag<Block> NULL_PLACEMENT = BlockTags.bind("structurize:null_placement");

    public static List<BlockTimberFrame> getTimberFrames()
    {
        return getBlocks(timberFrames);
    }

    public static List<BlockPaperWall> getPaperWalls()
    {
        return paperWalls.getBlocks();
    }

    public static List<BlockShingle> getShingles()
    {
        return getBlocks(shingles);
    }

    public static List<BlockShingleSlab> getShingleSlabs()
    {
        return shingleSlabs.getBlocks();
    }

    public static List<BlockFloatingCarpet> getFloatingCarpets()
    {
        return floatingCarpets.getBlocks();
    }

    public static <B extends Block> List<B> getList(List<RegistryObject<B>> list)
    {
        return list.stream().map(RegistryObject::get).collect(Collectors.toList());
    }

    public static <B extends Block, L extends IBlockList<B>> List<B> getBlocks(List<L> list)
    {
        return list.stream().flatMap(type -> type.getBlocks().stream()).collect(Collectors.toList());
    }

    /**
     * Utility shorthand to register blocks using the deferred registry
     * @param name the registry name of the block
     * @param block a factory / constructor to create the block on demand
     * @param group the {@link ItemGroup} this belongs to (sets creative tab)
     * @param <B> the block subclass for the factory response
     * @return the block entry saved to the registry
     */
    public static <B extends Block> RegistryObject<B> register(String name, Supplier<B> block, ItemGroup group)
    {
        RegistryObject<B> registered = BLOCKS.register(name.toLowerCase(), block);
        ModItems.getRegistry().register(name.toLowerCase(), () -> new BlockItem(registered.get(), new Item.Properties().tab(group)));
        return registered;
    }

    /*
     *  Registration
     */

    static
    {
        blockSubstitution       = register("blockSubstitution", BlockSubstitution::new, ModItemGroups.STRUCTURIZE);
        blockSolidSubstitution  = register("blockSolidSubstitution", BlockSolidSubstitution::new, ModItemGroups.STRUCTURIZE);
        blockFluidSubstitution  = register("blockFluidSubstitution", BlockFluidSubstitution::new, ModItemGroups.STRUCTURIZE);
        multiBlock              = register("multiblock", MultiBlock::new, ModItemGroups.STRUCTURIZE);
        blockDecoBarrel_onside  = register("blockbarreldeco_onside", BlockBarrel::new, ModItemGroups.STRUCTURIZE);
        blockDecoBarrel_standing = register("blockbarreldeco_standing", BlockBarrel::new, ModItemGroups.STRUCTURIZE);
    }
}
