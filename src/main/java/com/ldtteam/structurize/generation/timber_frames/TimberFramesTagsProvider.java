package com.ldtteam.structurize.generation.timber_frames;

import com.ldtteam.datagenerators.tags.TagJson;
import com.ldtteam.structurize.blocks.types.*;
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

public class TimberFramesTagsProvider implements IDataProvider
{
    private final DataGenerator generator;

    public TimberFramesTagsProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final List<TimberFrameCentreType> centerTypes = Arrays.stream(TimberFrameCentreType.values()).collect(Collectors.toList());
        for (TimberFrameCentreType timberFrameCenter : centerTypes)
        {

            final List<String> centers = new ArrayList<>();
            Arrays.stream(TimberFrameCentreType.values())
              .forEach(subName -> centers.add("#structurize:timber_frames/" + subName.getName()));


            final TagJson timberFrameCenters = new TagJson(false, centers);

            final Path itemsTagsCenterPath = this.generator.getOutputFolder()
                                               .resolve(DataGeneratorConstants.TAGS_DIR)
                                               .resolve("items")
                                               .resolve("timber_frames.json");
            final Path blocksTagsCenterPath = this.generator.getOutputFolder()
                                                .resolve(DataGeneratorConstants.TAGS_DIR)
                                                .resolve("blocks")
                                                .resolve("timber_frames.json");

            IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(timberFrameCenters), itemsTagsCenterPath);
            IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(timberFrameCenters), blocksTagsCenterPath);

            final List<TimberFrameFrameType> frameFrames = Arrays.stream(TimberFrameFrameType.values()).collect(Collectors.toList());
            for (TimberFrameFrameType timberFrameFrame : frameFrames)
            {

                final List<String> woods = new ArrayList<>();
                Arrays.stream(timberFrameFrame.values())
                  .forEach(subName -> woods.add("#structurize:timber_frames/" + timberFrameCenter.getName() + "/" + subName.getName()));


                final TagJson woodsJson = new TagJson(false, woods);

                final Path itemsTagsWoodsPath = this.generator.getOutputFolder()
                                                  .resolve(DataGeneratorConstants.TAGS_DIR)
                                                  .resolve("items")
                                                  .resolve("timber_frames")
                                                  .resolve(timberFrameCenter.getName().concat(".json"));
                final Path blocksTagsWoodsPath = this.generator.getOutputFolder()
                                                   .resolve(DataGeneratorConstants.TAGS_DIR)
                                                   .resolve("blocks")
                                                   .resolve("timber_frames")
                                                   .resolve(timberFrameCenter.getName().concat(".json"));

                IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(woodsJson), itemsTagsWoodsPath);
                IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(woodsJson), blocksTagsWoodsPath);

                final List<String> timberFrames = new ArrayList<>();
                Arrays.stream(TimberFrameType.values())
                  .forEach(subFace -> timberFrames.add(
                    "structurize:" + subFace.getName() + "_" + timberFrameFrame.getName() + "_" + timberFrameCenter.getName() + "_timber_frame"));

                final TagJson tagJson = new TagJson(false, timberFrames);

                final Path itemsTagsPath = this.generator.getOutputFolder()
                                             .resolve(DataGeneratorConstants.TAGS_DIR)
                                             .resolve("items")
                                             .resolve("timber_frames")
                                             .resolve(timberFrameCenter.getName())
                                             .resolve(timberFrameFrame.getName().concat(".json"));
                final Path blocksTagsPath = this.generator.getOutputFolder()
                                              .resolve(DataGeneratorConstants.TAGS_DIR)
                                              .resolve("blocks")
                                              .resolve("timber_frames")
                                              .resolve(timberFrameCenter.getName())
                                              .resolve(timberFrameFrame.getName().concat(".json"));
                IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(tagJson), itemsTagsPath);
                IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(tagJson), blocksTagsPath);
            }
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Timber Frames Tags Provider";
    }
}
