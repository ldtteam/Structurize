package com.ldtteam.structurize.blocks;

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
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IBlockCollection
{
    String getName();

    default String getPluralName() { return getName() + "s"; }

    List<RegistryObject<Block>> getBlocks();

    default Block getMainBlock()
    {
        return getBlocks().get(0).get();
    }

    default AbstractBlock.Properties getProperties()
    {
        // A generic wood default
        return AbstractBlock.Properties.create(Material.WOOD, MaterialColor.WOOD)
                 .hardnessAndResistance(2.0F, 3.0F)
                 .sound(SoundType.WOOD);
    }

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
                      return new TallBlockItem(block.get(), new Item.Properties().maxStackSize(16).group(type.group));
                  }
                  return new BlockItem(block.get(), new Item.Properties().group(group));
              }
            );

            results.add(block);
        }

        return results;
    }

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

    enum BlockType
    {
        BLOCK("", Block::new, ItemGroup.BUILDING_BLOCKS, 4, "##", "##"),
        SLAB("slab", SlabBlock::new, ItemGroup.BUILDING_BLOCKS, 6, "###"),
        STAIRS("stairs", props -> new StairsBlock(Blocks.BRICKS.getDefaultState(), props), ItemGroup.BUILDING_BLOCKS, 4, "#  ", "## ", "###"),
        WALL("wall", WallBlock::new, ItemGroup.BUILDING_BLOCKS, 6, "###", "###"),

        PLANKS("planks", Block::new,  ItemGroup.BUILDING_BLOCKS, 4, "#"),
        FENCE("fence", FenceBlock::new, ItemGroup.DECORATIONS, 3, "#-#", "#-#"),
        FENCE_GATE("fence_gate", FenceGateBlock::new, ItemGroup.DECORATIONS, 1, "-#-", "-#-"),
        DOOR("door", DoorBlock::new, ItemGroup.REDSTONE, 3, "##", "##", "##"),
        TRAPDOOR("trapdoor", TrapDoorBlock::new, ItemGroup.REDSTONE, 3, "###", "###");

        public final String suffix;
        public final ItemGroup group;
        public final Function<AbstractBlock.Properties, ? extends Block> constructor;
        private final int recipeYield;
        private final String[] patterns;

        BlockType(String suffix, Function<AbstractBlock.Properties, ? extends Block> constructor, ItemGroup group, int yield, String... patterns)
        {
            this.suffix = suffix;
            this.constructor = constructor;
            this.group = group;
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
