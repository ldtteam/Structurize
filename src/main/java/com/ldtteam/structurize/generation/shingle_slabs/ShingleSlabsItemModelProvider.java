package com.ldtteam.structurize.generation.shingle_slabs;

import com.ldtteam.datagenerators.models.ModelDisplayPositionJson;
import com.ldtteam.datagenerators.models.ModelDisplayPositionsEnum;
import com.ldtteam.datagenerators.models.XYZDoubleListJson;
import com.ldtteam.datagenerators.models.XYZIntListJson;
import com.ldtteam.datagenerators.models.item.ItemModelJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingleSlab;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShingleSlabsItemModelProvider implements IDataProvider
{
    private final DataGenerator generator;

    public ShingleSlabsItemModelProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final ItemModelJson modelJson = new ItemModelJson();
        modelJson.setDisplay(getDisplay());

        for (BlockShingleSlab shingleSlab : ModBlocks.getShingleSlabs())
        {
            if (shingleSlab.getRegistryName() == null)
                continue;

            modelJson.setParent("structurize:block/shingle_slab/" + shingleSlab.getRegistryName().getPath() + "_top");

            final String name = shingleSlab.getRegistryName().getPath();
            IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(modelJson), generator.getOutputFolder().resolve(DataGeneratorConstants.ITEM_MODEL_DIR).resolve(name + ".json"));
        }
    }

    private Map<ModelDisplayPositionsEnum, ModelDisplayPositionJson> getDisplay()
    {
        final Map<ModelDisplayPositionsEnum, ModelDisplayPositionJson> display = new HashMap<>();

        // GUI

        final XYZIntListJson guiRotation = new XYZIntListJson(30, -55, 0);
        final XYZDoubleListJson guiTranslation = new XYZDoubleListJson(0, 0.5, 0);
        final XYZDoubleListJson guiScale = new XYZDoubleListJson(0.51, 0.51, 0.51);

        final ModelDisplayPositionJson guiPosition = new ModelDisplayPositionJson(guiRotation, guiTranslation, guiScale);
        display.put(ModelDisplayPositionsEnum.GUI, guiPosition);

        // THIRD PERSON

        final XYZIntListJson thirdPersonRotation = new XYZIntListJson(75, 45, 0);
        final XYZDoubleListJson thirdTranslation = new XYZDoubleListJson(0, 2.5, 0);
        final XYZDoubleListJson thirdPersonScale = new XYZDoubleListJson(0.375, 0.375, 0.375);

        final ModelDisplayPositionJson thirdPersonPosition = new ModelDisplayPositionJson(thirdPersonRotation, thirdTranslation, thirdPersonScale);
        display.put(ModelDisplayPositionsEnum.THIRD_PERSON_LEFT_HAND, thirdPersonPosition);
        display.put(ModelDisplayPositionsEnum.THIRD_PERSON_RIGHT_HAND, thirdPersonPosition);

        // FIRST PERSON

        final XYZIntListJson firstPersonRotation = new XYZIntListJson(0, 45, 0);
        final XYZDoubleListJson firstPersonTranslation = new XYZDoubleListJson(0, 2, 0);
        final XYZDoubleListJson firstPersonScale = new XYZDoubleListJson(0.4, 0.4, 0.4);

        final ModelDisplayPositionJson firstPersonPosition = new ModelDisplayPositionJson(firstPersonRotation, firstPersonTranslation, firstPersonScale);
        display.put(ModelDisplayPositionsEnum.FIRST_PERSON_LEFT_HAND, firstPersonPosition);
        display.put(ModelDisplayPositionsEnum.FIRST_PERSON_RIGHT_HAND, firstPersonPosition);

        // GROUND

        final XYZDoubleListJson groundTranslation = new XYZDoubleListJson(0, 3, 0);
        final XYZDoubleListJson groundScale = new XYZDoubleListJson(0.25, 0.25, 0.25);

        final ModelDisplayPositionJson groundPosition = new ModelDisplayPositionJson(null, groundTranslation, groundScale);
        display.put(ModelDisplayPositionsEnum.GROUND, groundPosition);

        // FIXED

        final XYZDoubleListJson fixedScale = new XYZDoubleListJson(0.5, 0.5, 0.5);

        final ModelDisplayPositionJson fixedPosition = new ModelDisplayPositionJson(null, null, fixedScale);
        display.put(ModelDisplayPositionsEnum.FIXED, fixedPosition);

        // HEAD

        final XYZDoubleListJson headTranslation = new XYZDoubleListJson(0, 13.75, 0);

        final ModelDisplayPositionJson headPosition = new ModelDisplayPositionJson(null, headTranslation, null);
        display.put(ModelDisplayPositionsEnum.FIXED, headPosition);

        return display;
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Shingle Slabs Item Model Provider";
    }
}
