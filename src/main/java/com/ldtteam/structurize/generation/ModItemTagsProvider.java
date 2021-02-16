package com.ldtteam.structurize.generation;

import com.ldtteam.structurize.blocks.BlockType;
import net.minecraft.block.Block;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModItemTagsProvider extends ItemTagsProvider
{
    private static ModItemTagsProvider instance;

    private Map<ResourceLocation, ITag.Builder> tags;

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
    protected void registerTags()
    {
        tagToBuilder.putAll(tags);

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
    public void act(@NotNull final DirectoryCache cache)
    {
        // Store the singleton builds
        tags = new LinkedHashMap<>(this.tagToBuilder);
        super.act(cache);
    }

    public Builder<Item> buildTag(ITag.INamedTag<Item> tag)
    {
        return this.getOrCreateBuilder(tag);
    }

    public Builder<Item> buildTag(String name)
    {
        return this.getOrCreateBuilder(ItemTags.makeWrapperTag(name));
    }

    @Override
    public void copy(@NotNull ITag.INamedTag<Block> blockTag, @NotNull ITag.INamedTag<Item> itemTag)
    {
        super.copy(blockTag, itemTag);
    }

    public static ModItemTagsProvider getInstance()
    {
        return instance;
    }
}
