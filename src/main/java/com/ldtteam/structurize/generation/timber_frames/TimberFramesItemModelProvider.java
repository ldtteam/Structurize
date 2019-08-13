package com.ldtteam.structurize.generation.timber_frames;

import com.ldtteam.datagenerators.models.ModelDisplayPositionJson;
import com.ldtteam.datagenerators.models.ModelDisplayPositionsEnum;
import com.ldtteam.datagenerators.models.XYZDoubleListJson;
import com.ldtteam.datagenerators.models.XYZIntListJson;
import com.ldtteam.datagenerators.models.item.ItemModelJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockTimberFrame;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TimberFramesItemModelProvider implements IDataProvider
{
    private final DataGenerator generator;

    public TimberFramesItemModelProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final ItemModelJson modelJson = new ItemModelJson();
        modelJson.setDisplay(getDisplay());

        for (BlockTimberFrame timberFrame : ModBlocks.getTimberFrames())
        {
            final String parent = "structurize:block/timber_frames/" +
                    timberFrame.getTimberFrameType().getName() + "_" +
                    timberFrame.getFrameType().getName() + "_" +
                    timberFrame.getCentreType().getName() + "_timber_frame";

            modelJson.setParent(parent);

            if (timberFrame.getRegistryName() == null)
                continue;

            final String name = timberFrame.getRegistryName().getPath();
            IDataProvider.save(DataGeneratorConstants.GSON, cache, modelJson.serialize(), generator.getOutputFolder().resolve(DataGeneratorConstants.ITEM_MODEL_DIR).resolve(name + ".json"));
        }
    }

    private Map<ModelDisplayPositionsEnum, ModelDisplayPositionJson> getDisplay()
    {
        final Map<ModelDisplayPositionsEnum, ModelDisplayPositionJson> display = new HashMap<>();

        // GUI

        final XYZIntListJson guiRotation = new XYZIntListJson(30, 215, 0);
        final XYZDoubleListJson guiScale = new XYZDoubleListJson(0.5, 0.5, 0.5);

        final ModelDisplayPositionJson guiPosition = new ModelDisplayPositionJson(guiRotation, null, guiScale);
        display.put(ModelDisplayPositionsEnum.GUI, guiPosition);

        // THIRD PERSON

        final XYZIntListJson thirdPersonRotation = new XYZIntListJson(0, 180, 0);
        final XYZDoubleListJson thirdPersonScale = new XYZDoubleListJson(0.5, 0.5, 0.5);

        final ModelDisplayPositionJson thirdPersonPosition = new ModelDisplayPositionJson(thirdPersonRotation, null, thirdPersonScale);
        display.put(ModelDisplayPositionsEnum.THIRD_PERSON_LEFT_HAND, thirdPersonPosition);
        display.put(ModelDisplayPositionsEnum.THIRD_PERSON_RIGHT_HAND, thirdPersonPosition);

        // FIRST PERSON && GROUND

        final XYZIntListJson firstPersonRotation = new XYZIntListJson(0, 180, 0);
        final XYZDoubleListJson firstPersonScale = new XYZDoubleListJson(0.3, 0.3, 0.3);

        final ModelDisplayPositionJson firstPersonPosition = new ModelDisplayPositionJson(firstPersonRotation, null, firstPersonScale);
        display.put(ModelDisplayPositionsEnum.FIRST_PERSON_LEFT_HAND, firstPersonPosition);
        display.put(ModelDisplayPositionsEnum.FIRST_PERSON_RIGHT_HAND, firstPersonPosition);
        display.put(ModelDisplayPositionsEnum.GROUND, firstPersonPosition);

        // FIXED

        final XYZIntListJson fixedTranslation = new XYZIntListJson(0, 2, 0);
        final XYZDoubleListJson fixedScale = new XYZDoubleListJson(1, 1, 1);

        final ModelDisplayPositionJson fixedPosition = new ModelDisplayPositionJson(null, fixedTranslation, fixedScale);
        display.put(ModelDisplayPositionsEnum.FIXED, fixedPosition);

        return display;
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Timber Frames Item Model Provider";
    }
}
