package com.ldtteam.structurize.api.generation;

import com.ldtteam.structurize.api.blocks.BlockType;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.data.tags.TagsProvider.TagAppender;

public class ModItemTagsProvider extends ItemTagsProvider
{
    private static ModItemTagsProvider instance;

    private Map<ResourceLocation, Tag.Builder> tags;

    public ModItemTagsProvider(
      final DataGenerator dataGenerator,
      final BlockTagsProvider blockTagProvider,
      final String modId,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(dataGenerator, blockTagProvider, modId, existingFileHelper);
        instance = this;
    }

    @Override
    protected void addTags()
    {
        builders.putAll(tags);

        // Handle all block collections' types in one go
        for (BlockType type : BlockType.values())
        {
            for (int i = 0; i < type.blockTag.size(); i ++)
            {
                if (type.itemTag.size() <= i) break;
                copy(type.blockTag.get(i), type.itemTag.get(i));
            }
        }
    }

    @Override
    public void run(@NotNull final HashCache cache)
    {
        // Store the singleton builds
        tags = new LinkedHashMap<>(this.builders);
        super.run(cache);
    }

    public TagAppender<Item> buildTag(Tag.Named<Item> tag)
    {
        return this.tag(tag);
    }

    public TagAppender<Item> buildTag(String name)
    {
        return this.tag(ItemTags.bind(name));
    }

    @Override
    public void copy(@NotNull Tag.Named<Block> blockTag, @NotNull Tag.Named<Item> itemTag)
    {
        super.copy(blockTag, itemTag);
    }

    public void copy(Tag.Named<Block> blockTag)
    {
        copy(blockTag, ItemTags.bind(blockTag.getName().toString()));
    }

    public static ModItemTagsProvider getInstance()
    {
        return instance;
    }
}
