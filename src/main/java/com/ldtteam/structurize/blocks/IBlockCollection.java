package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.event.LifecycleSubscriber;
import com.ldtteam.structurize.generation.collections.CollectionProviderSet;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.TallBlockItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A block collection is any set of blocks with a common material,
 * for example a brick collection consists of brick stairs, brick slab, etc.
 *
 * Implementing allows a collection to be rapidly registered, referenced,
 * and generated. Simply add a variable to store the reference,then invoke
 * a {@link CollectionProviderSet} in the data gen {@link LifecycleSubscriber}.
 *
 * Implements effectively as both a class and enum (for many related sets, like brick variants).
 */
public interface IBlockCollection
{
    /**
     * Gets the name applied to the whole collection as an id and prefix for registry names.
     * @return the name identifying the collection
     */
    String getName();

    /**
     * Gets the name to be used when there is no suffix, e.g. bricks instead of brick_slab
     * @return the plural form of the name
     */
    default String getPluralName() { return getName() + "s"; }

    /**
     * Retrieves the registry entries of all the blocks in this collection,
     * with the first being considered the default in some circumstances.
     * Will likely require a field when implementing.
     * @return the blocks
     */
    List<RegistryObject<Block>> getBlocks();

    /**
     * Selects which block in the collection is the fallback, or primary block.
     * For recipe purposes mostly
     * @return the main block
     */
    default Block getMainBlock()
    {
        return getBlocks().get(0).get();
    }

    /**
     * Defines the properties that should be applied to each block in the collection
     * @return the properties to use for block construction
     */
    default AbstractBlock.Properties getProperties()
    {
        // A generic wood default
        return AbstractBlock.Properties.create(Material.WOOD, MaterialColor.WOOD)
                 .hardnessAndResistance(2.0F, 3.0F)
                 .sound(SoundType.WOOD);
    }

    /**
     * Constructs and registers each block in the collection
     * @param registrar the DeferredRegistry instance to apply the block to
     * @param itemRegistrar the DeferredRegistry instance to apply the item to
     * @param group the item group (or creative tab) to place this block in
     * @param types a selection of each block type that is part of the collection
     * @return each registered block in the collection
     */
    default List<RegistryObject<Block>> create(DeferredRegister<Block> registrar, DeferredRegister<Item> itemRegistrar, ItemGroup group, BlockType... types)
    {
        List<RegistryObject<Block>> results = new LinkedList<>();

        for (BlockType type : types)
        {
            RegistryObject<Block> block = registrar.register(
              type.withSuffix(getName(), getPluralName()),
              () -> type.constructor.apply(getProperties()));

            itemRegistrar.register(
              type.withSuffix(getName(), getPluralName()),
              () -> {
                  if (type == BlockType.DOOR)
                  {
                      return new TallBlockItem(block.get(), new Item.Properties().maxStackSize(16).group(group));
                  }
                  return new BlockItem(block.get(), new Item.Properties().group(group));
              }
            );

            results.add(block);
        }

        return results;
    }

    /**
     * Specifies the recipe of the main block, which is then the block
     * used to craft all the others.
     * @param consumer the generation method to save the recipe json
     * @param obtainment an unfortunately mandatory criterion - MUST be applied to the recipe
     */
    void provideMainRecipe(Consumer<IFinishedRecipe> consumer, ICriterionInstance obtainment);

    static <B extends Block> B get(BlockType type, List<RegistryObject<B>> blocks)
    {
        for (RegistryObject<B> ro : blocks)
        {
            if (BlockType.fromSuffix(ro.get()) == type)
            {
                return ro.get();
            }
        }
        return blocks.get(0).get();
    }

    /**
     * A utility closely related to a collection that defines the different block types'
     * instantiation and generation. Allows the identification of block types from their conventional
     * registry name suffix
     */
    enum BlockType
    {
        BLOCK("", Block::new, null, null, 4, "##", "##"),
        SLAB("slab", SlabBlock::new, BlockTags.SLABS, ItemTags.SLABS, 6, "###"),
        STAIRS("stairs", props -> new StairsBlock(Blocks.BRICKS::getDefaultState, props), BlockTags.STAIRS, ItemTags.STAIRS, 4, "#  ", "## ", "###"),
        WALL("wall", WallBlock::new, BlockTags.WALLS, ItemTags.WALLS, 6, "###", "###"),

        // TODO wood tags
        PLANKS("planks", Block::new, BlockTags.PLANKS, ItemTags.PLANKS, 4, "#"),
        FENCE("fence", FenceBlock::new, BlockTags.FENCES, ItemTags.FENCES, 3, "#-#", "#-#"),
        FENCE_GATE("fence_gate", FenceGateBlock::new, BlockTags.FENCE_GATES, ItemTags.FENCES, 1, "-#-", "-#-"),
        DOOR("door", DoorBlock::new, BlockTags.DOORS, ItemTags.DOORS, 3, "##", "##", "##"),
        TRAPDOOR("trapdoor", TrapDoorBlock::new, BlockTags.TRAPDOORS, ItemTags.TRAPDOORS, 3, "###", "###");

        public final String suffix;
        public final Function<AbstractBlock.Properties, ? extends Block> constructor;
        public final ITag.INamedTag<Block> blockTag;
        public final ITag.INamedTag<Item> itemTag;
        private final int recipeYield;
        private final String[] patterns;

        BlockType(
          String suffix,
          Function<AbstractBlock.Properties, ? extends Block> constructor,
          final ITag.INamedTag<Block> blockTag,
          final ITag.INamedTag<Item> itemTag, int yield,
          String... patterns)
        {
            this.suffix = suffix;
            this.constructor = constructor;
            this.blockTag = blockTag;
            this.itemTag = itemTag;
            this.recipeYield = yield;
            this.patterns = patterns.length < 4 ? patterns : new String[0];
        }

        public String withSuffix(String name, String pluralName)
        {
            if (this == BLOCK) name = pluralName;
            String result = suffix.isEmpty() ? name : name + "_" + suffix;

            // TODO 1.17 remove cactus magic override
            if (name.equals("blockcactus"))
            {
                result = result
                  .replace("_", "")
                  .replace("stairs", "stair")
                  .replace("planks", "plank");
            }

            return result;
        }

        // TODO enable stone cutter recipes
        public ShapedRecipeBuilder formRecipe(IItemProvider result, IItemProvider material, ITag<Item> rod, ICriterionInstance criterion)
        {
            ShapedRecipeBuilder builder = new ShapedRecipeBuilder(result, recipeYield);

            for (String line : patterns)
            {
                builder.patternLine(line);
            }

            if (this == BlockType.FENCE || this == BlockType.FENCE_GATE)
            {
                builder.key('-', rod);
            }

            return builder
              .key('#', material)
              .addCriterion("has_" + material.asItem().getRegistryName().getPath(), criterion);
        }

        public static BlockType fromSuffix(String path)
        {
            // TODO 1.17 remove cactus magic override
            if (path.startsWith("blockcactus"))
            {
                path = path
                  .replace("stair", "stairs")
                  .replace("plank", "planks")
                  .replace("fencegate", "fence_gate");
            }

            for (BlockType cut : BlockType.values())
            {
                if (!cut.suffix.isEmpty() && path.endsWith(cut.suffix)) return cut;
            }

            return BlockType.BLOCK;
        }

        public static BlockType fromSuffix(Block block)
        {
            return block.getRegistryName() != null ? fromSuffix(block.getRegistryName().getPath()) : BlockType.BLOCK;
        }

        public static String copySuffix(Block source, Block copycat)
        {
            ResourceLocation name = copycat.getRegistryName();
            if (name == null) return "";

            return fromSuffix(source).withSuffix(name.getPath(), "");
        }
    }
}
