package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.decorative.BlockFloatingCarpet;
import com.ldtteam.structurize.generation.*;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.RegistryObject;

import java.util.LinkedList;
import java.util.List;

public class FloatingCarpetList implements IBlockList<BlockFloatingCarpet>
{
    public static final ITag.INamedTag<Block>               BLOCK_TAG = BlockTags.makeWrapperTag("structurize:floating_carpets");
    public static final ITag.INamedTag<Item>                ITEM_TAG  = ItemTags.makeWrapperTag("structurize:floating_carpets");
    private final List<RegistryObject<BlockFloatingCarpet>> blocks    = new LinkedList<>();

    public FloatingCarpetList()
    {
        for (DyeColor color : DyeColor.values())
        {
            blocks.add(ModBlocks.register(
              color.getTranslationKey() + "_floating_carpet",
              () -> new BlockFloatingCarpet(color),
              ModItemGroups.STRUCTURIZE
            ));
        }
    }

    @Override
    public List<RegistryObject<BlockFloatingCarpet>> getRegisteredBlocks()
    {
        return blocks;
    }

    @Override
    public void generateBlockStates(final ModBlockStateProvider states)
    {
        getBlocks().forEach(
          block -> states.getVariantBuilder(block)
            .partialState()
            .setModels(new ConfiguredModel(new ModelFile.UncheckedModelFile("minecraft:block/" + block.getColor().getTranslationKey() + "_carpet")))
        );
    }

    @Override
    public void generateItemModels(final ModItemModelProvider models)
    {
        getBlocks().forEach(
          block -> models.withExistingParent(
            block.getRegistryName().getPath(),
            "minecraft:" + block.getColor().getTranslationKey() + "_carpet")
        );
    }

    @Override
    public void generateRecipes(final ModRecipeProvider provider)
    {
        getBlocks().forEach(block -> {
            provider.add(consumer -> new ShapedRecipeBuilder(block, 1)
                .patternLine("B")
                .patternLine("C")
                .patternLine("S")
                .key('B', ModItems.buildTool.get())
                .key('C', Registry.BLOCK.getOrDefault(new ResourceLocation(block.getColor().getTranslationKey() + "_carpet")))
                .key('S', Tags.Items.STRING)
                .addCriterion("has_"+block.getRegistryName().getPath(), ModRecipeProvider.getDefaultCriterion(block))
                .build(consumer));

            provider.add(consumer -> {
                ShapelessRecipeBuilder builder = new ShapelessRecipeBuilder(block, 8);
                for (int i = 0; i < 8; i++) builder.addIngredient(ITEM_TAG);

                builder
                  .addIngredient(DyeItem.getItem(block.getColor()))
                  .addCriterion("can_dye_"+block.getRegistryName().getPath(), ModRecipeProvider.getDefaultCriterion(block))
                  .build(consumer, new ResourceLocation(Constants.MOD_ID, block.getRegistryName().getPath() + "_dye"));
            });
        });
    }

    @Override
    public void generateTags(final ModBlockTagsProvider blocks, final ModItemTagsProvider items)
    {
        getBlocks().forEach(blocks.buildTag(BLOCK_TAG)::add);
        items.copy(BLOCK_TAG, ITEM_TAG);
    }
}
