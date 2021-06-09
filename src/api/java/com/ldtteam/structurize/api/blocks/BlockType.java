package com.ldtteam.structurize.api.blocks;

import com.google.common.collect.Lists;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.block.*;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * A utility closely related to a collection that defines the different block types' instantiation and generation. Allows the identification of block types from their conventional
 * registry name suffix
 */
public enum BlockType
{
    BLOCK("", Block::new, RenderType.getSolid(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,  4, "##", "##"),
    SLAB("slab", SlabBlock::new, RenderType.getSolid(), BlockTags.SLABS, ItemTags.SLABS, 6, "###"),
    STAIRS("stairs", props -> new StairsBlock(Blocks.BRICKS::getDefaultState, props), RenderType.getSolid(), BlockTags.STAIRS, ItemTags.STAIRS, 4, "#  ", "## ", "###"),
    WALL("wall", WallBlock::new, RenderType.getSolid(), BlockTags.WALLS, ItemTags.WALLS, 6, "###", "###"),

    // TODO wood tags
    PLANKS("planks", Block::new, RenderType.getSolid(), BlockTags.PLANKS, ItemTags.PLANKS, 4, "#"),
    FENCE("fence", FenceBlock::new, RenderType.getSolid(), BlockTags.FENCES, ItemTags.FENCES, 3,  "#-#", "#-#"),
    FENCE_GATE("fence_gate", FenceGateBlock::new, RenderType.getSolid(), BlockTags.FENCE_GATES, ItemTags.FENCES, 1, "-#-", "-#-"),
    TRAPDOOR("trapdoor", TrapDoorBlock::new, RenderType.getCutout(), BlockTags.TRAPDOORS, ItemTags.TRAPDOORS, 3,  "###", "###"),
    DOOR("door", DoorBlock::new, RenderType.getCutout(), BlockTags.DOORS, ItemTags.DOORS, 3, "##", "##", "##");

    public final  String                               suffix;
    public final Function<Properties, ? extends Block> constructor;
    public final List<ITag.INamedTag<Block>>           blockTag;
    public final List<ITag.INamedTag<Item>>            itemTag;

    private final int          recipeYield;
    private final List<String> craftingPatterns;
    private final RenderType   renderType;

    BlockType(
      String suffix,
      Function<Properties, ? extends Block> constructor,
      final RenderType renderType,
      final List<ITag.INamedTag<Block>> blockTag,
      final List<ITag.INamedTag<Item>> itemTag,
      int yield,
      String... craftingPatterns)
    {
        this.suffix = suffix;
        this.constructor = constructor;
        this.renderType = renderType;
        this.blockTag = blockTag;
        this.itemTag = itemTag;
        this.recipeYield = yield;
        this.craftingPatterns = Arrays.asList(craftingPatterns.length < 4 ? craftingPatterns : new String[0]);
    }

    BlockType(
      String suffix,
      Function<Properties, ? extends Block> constructor,
      final RenderType renderType,
      final ITag.INamedTag<Block> blockTag,
      final ITag.INamedTag<Item> itemTag,
      int yield,
      String... craftingPatterns)
    {
        this(suffix, constructor, renderType, Lists.newArrayList(blockTag), Lists.newArrayList(itemTag), yield, craftingPatterns);
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
    public ShapedRecipeBuilder formRecipe(IItemProvider result, IItemProvider material, ICriterionInstance criterion)
    {
        ShapedRecipeBuilder builder = new ShapedRecipeBuilder(result, recipeYield);

        for (String line : craftingPatterns)
        {
            builder.patternLine(line);
        }

        if (this == BlockType.FENCE || this == BlockType.FENCE_GATE)
        {
            builder.key('-', Tags.Items.RODS_WOODEN);
        }

        return builder
                 .key('#', material)
                 .addCriterion("has_" + material.asItem().getRegistryName().getPath(), criterion);
    }

    public RenderType getRenderType()
    {
        return renderType;
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
