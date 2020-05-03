package com.ldtteam.structurize.generation.floating_carpets;

import com.ldtteam.datagenerators.models.item.ItemModelJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockFloatingCarpet;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class FloatingCarpetsItemModelProvider implements IDataProvider
{
    private final DataGenerator generator;

    public FloatingCarpetsItemModelProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull final DirectoryCache directoryCache) throws IOException
    {
        for (final BlockFloatingCarpet floatingCarpet : ModBlocks.getFloatingCarpets())
        {
            if (floatingCarpet.getRegistryName() == null) continue;

            final ItemModelJson itemModelJson = new ItemModelJson();
            itemModelJson.setParent("minecraft:block/" + floatingCarpet.getColor().getTranslationKey() + "_carpet");

            final Path saveFile = this.generator.getOutputFolder().resolve(DataGeneratorConstants.ITEM_MODEL_DIR + floatingCarpet.getRegistryName().getPath() + ".json");

            IDataProvider.save(DataGeneratorConstants.GSON, directoryCache, DataGeneratorConstants.serialize(itemModelJson), saveFile);
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Floating Carpets Block Model Provider";
    }
}
