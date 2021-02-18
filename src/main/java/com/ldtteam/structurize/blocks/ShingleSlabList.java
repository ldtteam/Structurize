package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.blocks.decorative.BlockShingleSlab;
import com.ldtteam.structurize.blocks.types.ShingleFaceType;
import com.ldtteam.structurize.generation.*;
import com.ldtteam.structurize.items.ModItemGroups;
import net.minecraft.block.Block;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fml.RegistryObject;

import java.util.*;
import java.util.stream.Collectors;

public class ShingleSlabList implements IBlockList<BlockShingleSlab>
{
    public static final Map<ShingleFaceType, List<RegistryObject<BlockShingleSlab>>> SLABS = new HashMap<>();
    public static final Map<ShingleFaceType, ITag.INamedTag<Block>> blockTags = new HashMap<>();

    public ShingleSlabList()
    {
        for (ShingleFaceType type : ShingleFaceType.values())
        {
            List<RegistryObject<BlockShingleSlab>> typeList = new LinkedList<>();

            for (int i = -1; i < type.getColors().length; i++)
            {
                DyeColor color = i < 0 ? null : type.getColors()[i];

                typeList.add(ModBlocks.register(
                  (color == null ? "" : color.getString() + "_") + type.getGroup() + "_shingle_slab",
                  () -> new BlockShingleSlab(type, color),
                  ModItemGroups.SHINGLES
                ));
            }

            SLABS.put(type, typeList);
        }
    }

    @Override
    public List<RegistryObject<BlockShingleSlab>> getRegisteredBlocks()
    {
        return SLABS.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }


    private ModelFile makeBlockModel(BlockModelProvider models, BlockShingleSlab shingle, String shape)
    {
        String location = (shingle.getColor() == null ? "" : shingle.getColor() + "_") + shingle.getFaceType().getGroup() + "_shingle";

        // Not appending "top" allows for the default item model generation
        return models.withExistingParent(
          "block/shingle_slab/" + location + "_slab_" + shape,
          models.modLoc("block/shingle_slab/shingle_slab_" + shape))
                 .texture("1", "blocks/shingle/" + location + "_1")
                 .texture("2", "blocks/shingle/" + location + "_2")
                 .texture("3", "blocks/shingle/" + location + "_3")
                 .texture("particle", "blocks/shingle/" + location + "_1");
    }

    @Override
    public void generateBlockStates(final ModBlockStateProvider states)
    {
        getRegisteredBlocks().forEach(block -> {
            BlockShingleSlab shingle = block.get();

            states.horizontalBlock(
              shingle,
              state -> makeBlockModel(states.models(), shingle, state.get(BlockShingleSlab.SHAPE).getString()));
        });
    }

    @Override
    public void generateItemModels(final ModItemModelProvider models)
    {
        getBlocks().forEach(
          block -> models.getBuilder(block.getRegistryName().getPath())
                     .parent(new ModelFile.UncheckedModelFile(models.modLoc("block/shingle_slab/" + block.getRegistryName().getPath() + "_top")))
        );
    }

    @Override
    public void generateRecipes(final ModRecipeProvider provider)
    {
        getRegisteredBlocks().forEach(
          slab -> provider.add(consumer -> {
              if (slab.get().getColor() == null)
              {
                  new ShapedRecipeBuilder(slab.get(), 8)
                    .patternLine("III")
                    .patternLine("SSS")
                    .key('I', slab.get().getFaceType().getMaterial())
                    .key('S', Items.STICK)
                    .addCriterion("has_" + slab.get().getRegistryName().getPath(), ModRecipeProvider.getDefaultCriterion(slab.get()))
                    .build(consumer);
              }
              else
              {
                  new ShapelessRecipeBuilder(slab.get(), 8)
                    .addIngredient(SLABS.get(slab.get().getFaceType()).get(0).get(), 8)
                    .addIngredient(DyeItem.getItem(slab.get().getColor()))
                    .addCriterion("has_" + slab.get().getRegistryName().getPath(), ModRecipeProvider.getDefaultCriterion(slab.get()))
                    .build(consumer);
              }
          }));
    }

    @Override
    public void generateTags(final ModBlockTagsProvider blocks, final ModItemTagsProvider items)
    {
        getRegisteredBlocks().forEach(block -> {
            blockTags.putIfAbsent(block.get().getFaceType(), blocks.createTag("shingle_slabs/" + block.get().getFaceType().getGroup()));
            blocks.buildTag(blockTags.get(block.get().getFaceType())).add(block.get());
        });

        blockTags.values().forEach(items::copy);
    }
}
