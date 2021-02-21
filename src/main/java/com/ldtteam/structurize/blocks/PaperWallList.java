package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.blocks.*;
import com.ldtteam.structurize.api.generation.*;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.decorative.BlockPaperWall;
import com.ldtteam.structurize.blocks.types.WoodType;
import com.ldtteam.structurize.items.ModItemGroups;
import net.minecraft.block.Block;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaperWallList implements IBlockList<BlockPaperWall>
{
    public static final ITag.INamedTag<Block> BLOCK_TAG = BlockTags.makeWrapperTag("structurize:paper_walls");
    public static final ITag.INamedTag<Item>  ITEM_TAG  = ItemTags.makeWrapperTag("structurize:paper_walls");

    public static final Map<RegistryObject<BlockPaperWall>, WoodType> blocks = new HashMap<>();

    public PaperWallList()
    {
        for (WoodType type : WoodType.values())
        {
            RegistryObject<BlockPaperWall> block = ModBlocks.register(
              type.getString() + "_blockpaperwall",
              BlockPaperWall::new,
              ModItemGroups.CONSTRUCTION
            );

            blocks.put(block, type);
        }
    }

    @Override
    public List<RegistryObject<BlockPaperWall>> getRegisteredBlocks()
    {
        return new ArrayList<>(blocks.keySet());
    }

    @Override
    public void generateBlockStates(final ModBlockStateProvider states)
    {
        getRegisteredBlocks().forEach(
          block -> states.paneBlock(
            block.get(),
            new ResourceLocation(Constants.MOD_ID, "blocks/paperwall/" + blocks.get(block).getString() + "_pane"),
            new ResourceLocation(Constants.MOD_ID, "blocks/paperwall/" + blocks.get(block).getString() + "_edge"))
        );
    }

    @Override
    public void generateItemModels(final ModItemModelProvider models)
    {
        getRegisteredBlocks().forEach(
          block -> models.withExistingParent(block.get().getRegistryName().getPath(), "item/generated")
            .texture("layer0", models.modLoc("blocks/paperwall/" + blocks.get(block).getString() + "_pane")));
    }

    @Override
    public void generateRecipes(final ModRecipeProvider provider)
    {
        getRegisteredBlocks().forEach(block -> provider.add(
          consumer -> ShapedRecipeBuilder.shapedRecipe(block.get(), 8)
            .patternLine("###")
            .patternLine("PPP")
            .patternLine("###")
            .key('#', blocks.get(block).getMaterial())
            .key('P', Items.PAPER)
            .addCriterion("has_"+block.get().getRegistryName().getPath(), provider.getCriterion(block.get()))
            .build(consumer)));
    }

    @Override
    public void generateTags(final ModBlockTagsProvider blocks, final ModItemTagsProvider items)
    {
        blocks.buildTag(BLOCK_TAG).add(getBlocks().toArray(new Block[0]));
        items.copy(BLOCK_TAG, ITEM_TAG);
    }
}
