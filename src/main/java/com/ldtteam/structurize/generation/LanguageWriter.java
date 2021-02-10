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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * A central provider and utilities for creating default language keys.
 * Makes all other language providers obsolete.
 * Must be statically loaded at the start of the generation lifecycle
 * and constructed at the end (to allow other providers to add keys).
 */
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

    @Override
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        autoTranslate(ModBlocks.getFloatingCarpets());
        autoTranslate(ModBlocks.getShingles());
        autoTranslate(ModBlocks.getShingleSlabs());
        translate(ModBlocks.getTimberFrames(), block ->
          block.getTimberFrameType().getLangName() + " " +
          block.getFrameType().getLangName() + " " +
         (block.getCentreType().getLangName().equals(block.getFrameType().getLangName()) ? "" : block.getCentreType().getLangName() + " ") +
          "Timber Frame");

        IDataProvider.save(DataGeneratorConstants.GSONLang, cache, lang.serialize(), en_us.toPath());
    }

    /**
     * Establishes a default translation for a list of blocks according to a function
     * @param blocks the list of blocks to make entries for
     * @param getLangValue the function to respond with the translation value
     * @param <B> a subclass of block
     */
    public static <B extends Block> void translate(List<B> blocks, Function<B, String> getLangValue)
    {
        for (B block : blocks)
        {
            if (block.getRegistryName() == null || getLangValue.apply(block) == null) continue;
            lang.put(block.getTranslationKey(), getLangValue.apply(block));
        }
    }

    /**
     * Transforms the registry name into a default translation
     * @param blocks the list of blocks to add entries for
     * @param <B> the block subclass
     */
    public static <B extends Block> void autoTranslate(List<B> blocks)
    {
        for (B block : blocks)
        {
            if (block.getRegistryName() == null) continue;
            lang.put(block.getTranslationKey(), format(block.getRegistryName().getPath()));
        }
    }



    /**
     * Formats a registry key, e.g. from cactus_planks to Cactus Planks
     * @param key the name to format
     * @return the formatted name
     */
    public static String format(String key)
    {
        List<String> name = new ArrayList<>();
        for (String word : key.split("[_. /]"))
        {
            name.add(word.substring(0,1).toUpperCase(Locale.US) + word.substring(1));
        }
        return String.join(" ", name);
    }

    /**
     * MUST be called before any other generators that use it.
     * Pulls the default language file in to the lifecycle
     * @param gen the generator
     * @return the loaded language json
     */
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
    public String getName()
    {
        return "Mass Language Writer";
    }
}
