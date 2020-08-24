package com.ldtteam.structurize.generation.shingles;

import com.ldtteam.blockout.Log;
import com.ldtteam.datagenerators.tags.TagJson;
import com.ldtteam.structurize.blocks.types.ShingleFaceType;
import com.ldtteam.structurize.blocks.types.ShingleWoodType;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShinglesTagsProvider implements IDataProvider
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
        for (ShingleFaceType shingleFace : shingleFaces)
        {
            Log.getLogger().warn(shingleFace);
            final List<String> groups = new ArrayList<>();
            Arrays.stream(ShingleFaceType.values())
              .filter(face -> !face.isDyed())
              .forEach(subName -> groups.add("#structurize:shingles/" + subName.getName()));


            final TagJson shinglesJson = new TagJson(false, groups);

            final Path itemsTagsFacePath = this.generator.getOutputFolder()
                                             .resolve(DataGeneratorConstants.TAGS_DIR)
                                             .resolve("items")
                                             .resolve("shingles.json");
            final Path blocksTagsFacePath = this.generator.getOutputFolder()
                                              .resolve(DataGeneratorConstants.TAGS_DIR)
                                              .resolve("blocks")
                                              .resolve("shingles.json");

            IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(shinglesJson), itemsTagsFacePath);
            IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(shinglesJson), blocksTagsFacePath);

            final List<ShingleWoodType> shingleWoods = Arrays.stream(ShingleWoodType.values()).collect(Collectors.toList());
            for (ShingleWoodType shingleWood : shingleWoods)
            {


                final List<String> woods = new ArrayList<>();
                Arrays.stream(ShingleWoodType.values())
                  .forEach(subName -> woods.add("#structurize:shingles/" + shingleFace.getName() + "/" + subName.getName()));


                final TagJson woodsJson = new TagJson(false, woods);

                final Path itemsTagsWoodsPath = this.generator.getOutputFolder()
                                                  .resolve(DataGeneratorConstants.TAGS_DIR)
                                                  .resolve("items")
                                                  .resolve("shingles")
                                                  .resolve(shingleFace.getName().concat(".json"));
                final Path blocksTagsWoodsPath = this.generator.getOutputFolder()
                                                   .resolve(DataGeneratorConstants.TAGS_DIR)
                                                   .resolve("blocks")
                                                   .resolve("shingles")
                                                   .resolve(shingleFace.getName().concat(".json"));

                IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(woodsJson), itemsTagsWoodsPath);
                IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(woodsJson), blocksTagsWoodsPath);

                final List<String> shingles = new ArrayList<>();
                Arrays.stream(ShingleFaceType.values())
                  .filter(subFace -> subFace.getGroup().equals(shingleFace.getGroup()))
                  .forEach(subFace -> shingles.add("structurize:" + subFace.getName() + "_" + shingleWood.getName() + "_shingle"));

                final TagJson tagJson = new TagJson(false, shingles);


                final Path itemsTagsPath = this.generator.getOutputFolder()
                                             .resolve(DataGeneratorConstants.TAGS_DIR)
                                             .resolve("items")
                                             .resolve("shingles")
                                             .resolve(shingleFace.getName())
                                             .resolve(shingleWood.getName().concat(".json"));
                final Path blocksTagsPath = this.generator.getOutputFolder()
                                              .resolve(DataGeneratorConstants.TAGS_DIR)
                                              .resolve("blocks")
                                              .resolve("shingles")
                                              .resolve(shingleFace.getName())
                                              .resolve(shingleWood.getName().concat(".json"));
                IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(tagJson), itemsTagsPath);
                IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(tagJson), blocksTagsPath);
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
