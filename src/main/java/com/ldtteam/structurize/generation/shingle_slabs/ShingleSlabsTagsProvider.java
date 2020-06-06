package com.ldtteam.structurize.generation.shingle_slabs;

import com.ldtteam.blockout.Log;
import com.ldtteam.datagenerators.tags.TagJson;
import com.ldtteam.structurize.blocks.types.ShingleFaceType;
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

public class ShingleSlabsTagsProvider implements IDataProvider
{
    private final DataGenerator generator;

    public ShingleSlabsTagsProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final List<ShingleFaceType> shingleFaces = Arrays.stream(ShingleFaceType.values()).filter(face -> !face.isDyed()).collect(Collectors.toList());
        for (ShingleFaceType shingleFace : shingleFaces)
        {

            final List<String> groups = new ArrayList<>();
            Arrays.stream(ShingleFaceType.values())
              .filter(face -> !face.isDyed())
              .forEach(subName -> groups.add("#structurize:shingle_slabs/" + subName.getName()));


            final TagJson shinglesJson = new TagJson(false, groups);

            final Path itemsTagsFacePath = this.generator.getOutputFolder()
                                             .resolve(DataGeneratorConstants.TAGS_DIR)
                                             .resolve("items")
                                             .resolve("shingle_slabs.json");
            final Path blocksTagsFacePath = this.generator.getOutputFolder()
                                              .resolve(DataGeneratorConstants.TAGS_DIR)
                                              .resolve("blocks")
                                              .resolve("shingle_slabs.json");

            IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(shinglesJson), itemsTagsFacePath);
            IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(shinglesJson), blocksTagsFacePath);


            final List<String> locations = new ArrayList<>();
            Arrays.stream(ShingleFaceType.values())
              .filter(subFace -> subFace.getGroup().equals(shingleFace.getGroup()))
              .forEach(subFace -> locations.add("structurize:" + subFace.getName() + "_shingle_slab"));
            final TagJson tagJson = new TagJson(false, locations);

            final String name = shingleFace.getName() + ".json";

            final Path itemsTagsPath = this.generator.getOutputFolder()
                                         .resolve(DataGeneratorConstants.TAGS_DIR)
                                         .resolve("items")
                                         .resolve("shingle_slabs")
                                         .resolve(name);
            final Path blocksTagsPath = this.generator.getOutputFolder()
                                          .resolve(DataGeneratorConstants.TAGS_DIR)
                                          .resolve("blocks")
                                          .resolve("shingle_slabs")
                                          .resolve(name);

            IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(tagJson), itemsTagsPath);
            IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(tagJson), blocksTagsPath);
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Shingle Tags Provider";
    }
}
