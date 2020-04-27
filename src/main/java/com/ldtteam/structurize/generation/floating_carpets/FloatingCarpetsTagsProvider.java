package com.ldtteam.structurize.generation.floating_carpets;

import com.ldtteam.datagenerators.tags.TagJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockFloatingCarpet;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FloatingCarpetsTagsProvider implements IDataProvider
{
    private final DataGenerator generator;

    public FloatingCarpetsTagsProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final List<String> carpets = new ArrayList<>();
        for (final BlockFloatingCarpet floatingCarpet : ModBlocks.getFloatingCarpets())
        {
            if (floatingCarpet.getRegistryName() == null) continue;
            carpets.add(floatingCarpet.getRegistryName().toString());
        }
        TagJson tagJson = new TagJson(false, carpets);

        final Path itemsTagsPath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.TAGS_DIR).resolve("items").resolve("floating_carpet.json");
        final Path blocksTagsPath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.TAGS_DIR).resolve("blocks").resolve("floating_carpet.json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(tagJson), itemsTagsPath);
        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(tagJson), blocksTagsPath);
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Floating Carpet Tags Provider";
    }
}
