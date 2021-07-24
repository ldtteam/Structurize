package com.ldtteam.structurize.api.generation;

import net.minecraft.world.level.block.Block;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModBlockTagsProvider extends BlockTagsProvider
{
    private static ModBlockTagsProvider instance;
    private Map<ResourceLocation, Tag.Builder> tags;

    public ModBlockTagsProvider(
      final DataGenerator generatorIn,
      final String modId,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(generatorIn, modId, existingFileHelper);
        instance = this;
    }

    @Override
    protected void addTags()
    {
        builders.putAll(tags);
    }

    @Override
    public void run(@NotNull final HashCache cache)
    {
        // Store the singleton builds
        tags = new LinkedHashMap<>(this.builders);
        super.run(cache);
    }

    public TagsProvider.TagAppender<Block> buildTag(Tag.Named<Block> tag)
    {
        return this.tag(tag);
    }

    public TagsProvider.TagAppender<Block> buildTag(String name)
    {
        return this.tag(BlockTags.bind(name));
    }

    public Tag.Named<Block> createTag(String path)
    {
        return BlockTags.bind(modId + ":" + path);
    }

    public static ModBlockTagsProvider getInstance()
    {
        return instance;
    }
}
