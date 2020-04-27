package com.ldtteam.structurize.generation.floating_carpets;

import com.google.gson.JsonParser;
import com.ldtteam.datagenerators.lang.LangJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockFloatingCarpet;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

public class FloatingCarpetsLangEntryProvider implements IDataProvider
{
    private final DataGenerator generator;

    public FloatingCarpetsLangEntryProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        final File langFile = inputPath.resolve(DataGeneratorConstants.EN_US_LANG).toFile();
        final Reader reader = new FileReader(langFile);

        final LangJson langJson = new LangJson();
        langJson.deserialize(new JsonParser().parse(reader));

        for (final BlockFloatingCarpet floatingCarpet : ModBlocks.getFloatingCarpets())
        {
            if (floatingCarpet.getRegistryName() == null) continue;

            final String reference = "block.structurize." + floatingCarpet.getRegistryName().getPath();
            final String value = dyeToString(floatingCarpet.getColor()) + " Floating Carpet";

            langJson.getLang().put(reference, value);
        }

        IDataProvider.save(DataGeneratorConstants.GSONLang, cache, langJson.serialize(), langFile.toPath());
    }

    public String dyeToString(DyeColor dyeColor)
    {
        switch (dyeColor)
        {
            case RED:
                return "Red";
            case BLUE:
                return "Blue";
            case CYAN:
                return "Cyan";
            case GRAY:
                return "Gray";
            case LIME:
                return "Lime";
            case PINK:
                return "Pink";
            case BLACK:
                return "Black";
            case BROWN:
                return "Brown";
            case GREEN:
                return "Green";
            case WHITE:
                return "White";
            case ORANGE:
                return "Orange";
            case PURPLE:
                return "Purple";
            case YELLOW:
                return "Yellow";
            case MAGENTA:
                return "Magenta";
            case LIGHT_BLUE:
                return "Light Blue";
            case LIGHT_GRAY:
                return "Light Gray";
        }
        return "Null";
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Floating Carpets Lang Provider";
    }
}
