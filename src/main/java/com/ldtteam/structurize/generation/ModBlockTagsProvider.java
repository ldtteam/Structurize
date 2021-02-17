package com.ldtteam.structurize.generation;

import net.minecraft.block.Block;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModBlockTagsProvider extends BlockTagsProvider
{
    private static ModBlockTagsProvider instance;
    private Map<ResourceLocation, ITag.Builder> tags;

    public ModBlockTagsProvider(
      final DataGenerator generatorIn,
      final String modId,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(generatorIn, modId, existingFileHelper);
        instance = this;
    }

    @Override
    protected void registerTags()
    {
        tagToBuilder.putAll(tags);
    }

    @Override
    public void act(@NotNull final DirectoryCache cache)
    {
        // Store the singleton builds
        tags = new LinkedHashMap<>(this.tagToBuilder);
        super.act(cache);
    }

    public TagsProvider.Builder<Block> buildTag(ITag.INamedTag<Block> tag)
    {
        return this.getOrCreateBuilder(tag);
    }

    public TagsProvider.Builder<Block> buildTag(String name)
    {
        return this.getOrCreateBuilder(BlockTags.makeWrapperTag(name));
    }

    public ITag.INamedTag<Block> createTag(String path)
    {
        return BlockTags.makeWrapperTag(modId + ":" + path);
    }

    public static ModBlockTagsProvider getInstance()
    {
        return instance;
    }
}
