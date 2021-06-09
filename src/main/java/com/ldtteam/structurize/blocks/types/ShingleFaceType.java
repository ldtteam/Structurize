package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.api.blocks.*;
import com.ldtteam.structurize.api.generation.*;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingle;
import com.ldtteam.structurize.items.ModItemGroups;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fml.RegistryObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Face types used by both Shingles and Shingle Slabs.
 *
 * IF YOU CHANGE THIS FILE, OR ADD ENTRIES, RUN THE DATA GENERATORS.
 *
 *  -> gradle runData <-
 */
public enum ShingleFaceType implements IBlockList<BlockShingle>
{
    //CLAY
    CLAY("clay", "Clay", Items.BRICK, DyeColor.values()),
    SLATE("slate", "Slate", Blocks.COBBLESTONE, DyeColor.BLUE, DyeColor.GREEN, DyeColor.PURPLE),
    MOSS_SLATE("moss_slate", "Moss Slate", Blocks.MOSSY_COBBLESTONE),
    THATCHED("thatched", "Thatched", Items.WHEAT),
    BLACKSTONE("blackstone", "Blackstone", Blocks.BLACKSTONE),
    GILDED_BLACKSTONE("gilded_blackstone", "Gilded Blackstone", Blocks.GILDED_BLACKSTONE);

    private final Map<WoodType, List<RegistryObject<BlockShingle>>> blocks    = new LinkedHashMap<>();
    private final Map<WoodType, ITag.INamedTag<Block>> blockTags = new LinkedHashMap<>();

    final String group;
    final String langName;
    final IItemProvider ingredient;
    final DyeColor[] colors;

    ShingleFaceType(final String group, final String langGroup, IItemProvider material, DyeColor... colors)
    {
        this.group = group;
        this.langName = langGroup;
        this.ingredient = material;
        this.colors = colors;

        for (int i = -1; i < colors.length; i++)
        {
            DyeColor color = i < 0 ? null : colors[i];
            String prefix = (i < 0 ? "" : color.getSerializedName() + "_") + group;

            for (WoodType wood : WoodType.values())
            {
                this.blocks.putIfAbsent(wood, new LinkedList<>());
                this.blocks.get(wood).add(ModBlocks.register(
                  String.format("%s_%s_shingle", prefix, wood.getSerializedName()),
                  () -> new BlockShingle(Blocks.OAK_PLANKS::defaultBlockState, wood, this, color),
                  ModItemGroups.SHINGLES));
            }
        }
    }

    /**
     * The face group the type belongs to, e.g. Clay or Slate. Used by data generators.
     *
     * @return group
     */
    public String getGroup()
    {
        return this.group;
    }

    /**
     * Name used in the lang data generator
     *
     * @return langName
     */
    public String getLangName()
    {
        return this.langName;
    }

    public IItemProvider getMaterial()
    {
        return ingredient;
    }

    public DyeColor[] getColors()
    {
        return colors;
    }

    @Override
    public List<RegistryObject<BlockShingle>> getRegisteredBlocks()
    {
        return blocks.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private ModelFile makeBlockModel(BlockModelProvider models, BlockShingle shingle, String shape)
    {
        String location = shingle.getTypeString() + "_shingle";

        return models.withExistingParent(
          String.format("block/shingle/%s/%s/%s", shape, shingle.getWoodType().getSerializedName(), location),
          models.modLoc("block/shingle/shingle_" + shape))
                 .texture("1", "blocks/shingle/" + location + "_1")
                 .texture("2", "blocks/shingle/" + location + "_2")
                 .texture("3", "blocks/shingle/" + location + "_3")
                 .texture("plank", shingle.getWoodType() == WoodType.CACTUS ? "blocks/cactus/blockcactusplank" : "minecraft:block/" + shingle.getWoodType().getMaterial().getRegistryName().getPath())
                 .texture("particle", "blocks/shingle/" + location + "_1");
    }

    @Override
    public void generateBlockStates(final ModBlockStateProvider states)
    {
        getRegisteredBlocks().forEach(block -> {
          BlockShingle shingle = block.get();

          states.stairsBlockUnlockUV(shingle,
            makeBlockModel(states.models(), shingle, "straight"),
            makeBlockModel(states.models(), shingle, "concave"),
            makeBlockModel(states.models(), shingle, "convex"));
        });
    }

    @Override
    public void generateItemModels(final ModItemModelProvider models)
    {
        getBlocks().forEach(
          block -> models.getBuilder(block.getRegistryName().getPath())
            .parent(new ModelFile.UncheckedModelFile(
              models.modLoc(String.format(
                "block/shingle/straight/%s/%s_shingle",
                block.getWoodType().getSerializedName(),
                block.getTypeString()))))
        );
    }

    @Override
    public void generateRecipes(final ModRecipeProvider provider)
    {
        getRegisteredBlocks().forEach(
          block -> provider.add(consumer -> {
            DyeColor color = block.get().getColor();

            if (color == null)
            {
                new ShapedRecipeBuilder(block.get(), 8)
                  .pattern("I  ")
                  .pattern("SI ")
                  .pattern("PSI")
                  .define('I', block.get().getFaceType().getMaterial())
                  .define('S', Items.STICK)
                  .define('P', block.get().getWoodType().getMaterial())
                  .unlockedBy("has_" + block.get().getRegistryName().getPath(), ModRecipeProvider.getDefaultCriterion(block.get()))
                  .save(consumer);
            }
            else
            {
                new ShapelessRecipeBuilder(block.get(), 8)
                  .requires(blocks.get(block.get().getWoodType()).get(0).get(), 8)
                  .requires(DyeItem.byColor(color))
                  .unlockedBy("has_" + block.get().getRegistryName().getPath(), ModRecipeProvider.getDefaultCriterion(block.get()))
                  .save(consumer);
            }
          }));
    }

    @Override
    public void generateTags(final ModBlockTagsProvider blocks, final ModItemTagsProvider items)
    {
        getRegisteredBlocks().forEach(block -> {
            if (!blockTags.containsKey(block.get().getWoodType()))
            {
                blockTags.put(block.get().getWoodType(), blocks.createTag("shingles/" + getGroup() + "/" + block.get().getWoodType().getSerializedName()));
            }

            blocks.buildTag(blockTags.get(block.get().getWoodType())).add(block.get());
        });

        ITag.INamedTag<Block> groupTag = blocks.createTag("shingles/" + getGroup());
        blockTags.values().forEach(blocks.buildTag(groupTag)::addTag);
        blockTags.values().forEach(items::copy);

        items.copy(groupTag);
    }
}
