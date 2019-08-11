package com.ldtteam.structurize.generation.shingles;

import com.google.gson.JsonObject;
import com.ldtteam.datagenerators.AbstractTagsProvider;
import com.ldtteam.structurize.blocks.types.ShingleFaceType;
import com.ldtteam.structurize.blocks.types.ShingleWoodType;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShinglesTagsProvider extends AbstractTagsProvider
{
    private final DataGenerator generator;

    public ShinglesTagsProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final List<ShingleFaceType> shingleFaces = Arrays.stream(ShingleFaceType.values()).filter(face -> !face.isDyed()).collect(Collectors.toList());
        for (final ShingleWoodType shingleWood : ShingleWoodType.values())
        {
            for (ShingleFaceType shingleFace : shingleFaces)
            {
                final List<ResourceLocation> locations = new ArrayList<>();
                Arrays.stream(ShingleFaceType.values())
                        .filter(subFace -> subFace.getGroup().equals(shingleFace.getGroup()))
                        .forEach(subFace -> locations.add(new ResourceLocation("structurize:" + subFace.getName() + "_" + shingleWood.getName() + "_shingle")));

                final JsonObject tagJson = createTagJson(false, locations.toArray(new ResourceLocation[0]));

                final String name = shingleFace.getName() + "_" + shingleWood.getName() + "_shingle.json";

                final Path itemsTagsPath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.TAGS_DIR).resolve("items").resolve(name);
                final Path blocksTagsPath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.TAGS_DIR).resolve("blocks").resolve(name);

                IDataProvider.save(DataGeneratorConstants.GSON, cache, tagJson, itemsTagsPath);
                IDataProvider.save(DataGeneratorConstants.GSON, cache, tagJson, blocksTagsPath);
            }
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Shingle Tags Provider";
    }
}
