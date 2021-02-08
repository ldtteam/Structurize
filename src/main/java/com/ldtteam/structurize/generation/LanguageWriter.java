package com.ldtteam.structurize.generation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ldtteam.datagenerators.lang.LangJson;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraftforge.fml.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.*;

public final class LanguageWriter implements IDataProvider
{
    private static File en_us = null;

    public static final LangJson lang  = new LangJson()
    {
        @NotNull
        @Override
        public JsonElement serialize()
        {
            JsonObject result = new JsonObject();
            new TreeMap<>(this.getLang()).forEach(result::addProperty);
            return result;
        }
    };

    public static <B extends Block> void autoTranslate(List<B> blocks)
    {
        for (B block : blocks)
        {
            if (block.getRegistryName() == null) continue;
            lang.put(block.getTranslationKey(), format(block.getRegistryName().getPath()));
        }
    }

    public static void autoTranslate(RegistryObject<Block>[] blocks)
    {
        autoTranslate(ModBlocks.getList(Arrays.asList(blocks)));
    }

    public static String format(String key)
    {
        List<String> name = new ArrayList<>();
        for (String word : key.split("[_. /]"))
        {
            name.add(word.substring(0,1).toUpperCase(Locale.US) + word.substring(1));
        }
        return String.join(" ", name);
    }

    public static LangJson load(DataGenerator gen)
    {
        try
        {
            final Path inputPath = gen.getInputFolders().stream().findFirst().orElse(null);
            if (inputPath == null) return lang;

            en_us = inputPath.resolve(DataGeneratorConstants.EN_US_LANG).toFile();
            final Reader reader = new FileReader(en_us);

            lang.deserialize(new JsonParser().parse(reader));
        }
        catch (IOException e)
        {
            Log.getLogger().error("Missing default lang file (en_us)");
        }

        return lang;
    }

    @Override
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        autoTranslate(ModBlocks.getFloatingCarpets());
        autoTranslate(ModBlocks.getShingles());
        autoTranslate(ModBlocks.getShingleSlabs());
        autoTranslate(ModBlocks.getTimberFrames());

        IDataProvider.save(DataGeneratorConstants.GSONLang, cache, lang.serialize(), en_us.toPath());
    }

    @Override
    public String getName()
    {
        return "Mass Language Writer";
    }
}
