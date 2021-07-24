package com.ldtteam.structurize.api.blocks;

import com.google.common.collect.Lists;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;

/**
 * A utility closely related to a collection that defines the different block types' instantiation and generation. Allows the identification of block types from their conventional
 * registry name suffix
 */
public enum BlockType
{
    BLOCK("", Block::new, Collections.EMPTY_LIST, Collections.EMPTY_LIST,  4, "##", "##"),
    SLAB("slab", SlabBlock::new, BlockTags.SLABS, ItemTags.SLABS, 6, "###"),
    STAIRS("stairs", props -> new StairBlock(Blocks.BRICKS::defaultBlockState, props), BlockTags.STAIRS, ItemTags.STAIRS, 4, "#  ", "## ", "###"),
    WALL("wall", WallBlock::new, BlockTags.WALLS, ItemTags.WALLS, 6, "###", "###"),

    // TODO wood tags
    PLANKS("planks", Block::new, BlockTags.PLANKS, ItemTags.PLANKS, 4, "#"),
    FENCE("fence", FenceBlock::new, BlockTags.FENCES, ItemTags.FENCES, 3,  "#-#", "#-#"),
    FENCE_GATE("fence_gate", FenceGateBlock::new, BlockTags.FENCE_GATES, ItemTags.FENCES, 1, "-#-", "-#-"),
    TRAPDOOR("trapdoor", TrapDoorBlock::new, BlockTags.TRAPDOORS, ItemTags.TRAPDOORS, 3,  "###", "###"),
    DOOR("door", DoorBlock::new, BlockTags.DOORS, ItemTags.DOORS, 3, "##", "##", "##");

    public final  String                               suffix;
    public final Function<Properties, ? extends Block> constructor;
    public final List<Tag.Named<Block>>           blockTag;
    public final List<Tag.Named<Item>>            itemTag;

    private final int          recipeYield;
    private final List<String> craftingPatterns;

    BlockType(
      String suffix,
      Function<Properties, ? extends Block> constructor,
      final List<Tag.Named<Block>> blockTag,
      final List<Tag.Named<Item>> itemTag,
      int yield,
      String... craftingPatterns)
    {
        this.suffix = suffix;
        this.constructor = constructor;
        this.blockTag = blockTag;
        this.itemTag = itemTag;
        this.recipeYield = yield;
        this.craftingPatterns = Arrays.asList(craftingPatterns.length < 4 ? craftingPatterns : new String[0]);
    }

    BlockType(
      String suffix,
      Function<Properties, ? extends Block> constructor,
      final Tag.Named<Block> blockTag,
      final Tag.Named<Item> itemTag,
      int yield,
      String... craftingPatterns)
    {
        this(suffix, constructor, Lists.newArrayList(blockTag), Lists.newArrayList(itemTag), yield, craftingPatterns);
    }

    public String withSuffix(String name, String pluralName)
    {
        if (this == BLOCK)
        {
            name = pluralName;
        }
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
    public ShapedRecipeBuilder formRecipe(ItemLike result, ItemLike material, CriterionTriggerInstance criterion)
    {
        ShapedRecipeBuilder builder = new ShapedRecipeBuilder(result, recipeYield);

        for (String line : craftingPatterns)
        {
            builder.pattern(line);
        }

        if (this == BlockType.FENCE || this == BlockType.FENCE_GATE)
        {
            builder.define('-', Tags.Items.RODS_WOODEN);
        }

        return builder
                 .define('#', material)
                 .unlockedBy("has_" + material.asItem().getRegistryName().getPath(), criterion);
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
            if (!cut.suffix.isEmpty() && path.endsWith(cut.suffix))
            {
                return cut;
            }
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
        if (name == null)
        {
            return "";
        }

        return fromSuffix(source).withSuffix(name.getPath(), "");
    }
}
