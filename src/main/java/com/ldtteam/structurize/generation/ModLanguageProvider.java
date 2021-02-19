package com.ldtteam.structurize.generation;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * A central provider and utilities for creating default language keys.
 * Makes all other language providers obsolete.
 * Must be initialised at the start of the lifecycle
 * to allow other providers to add keys.
 */
public final class ModLanguageProvider extends LanguageProvider
{
    private static ModLanguageProvider instance;
    protected final DataGenerator gen;

    public ModLanguageProvider(final DataGenerator gen, final String modid, final String locale)
    {
        super(gen, modid, locale);
        instance = this;
        this.gen = gen;
    }

    @Override
    protected void addTranslations()
    {
        this.add("AUTO-GENERATED TRANSLATION OBJECT", "Coder, leave those keys alone! *TOUCH THEM AT YOUR PERIL* (use the data generators)!");
    }

    /**
     * Establishes a default translation for a list of blocks according to a function
     * @param blocks the list of blocks to make entries for
     * @param getLangValue the function to respond with the translation value
     * @param <B> a subclass of block
     */
    public <B extends Block> void translate(List<B> blocks, Function<B, String> getLangValue)
    {
        for (B block : blocks)
        {
            if (block.getRegistryName() == null || getLangValue.apply(block) == null) continue;
            add(block.getTranslationKey(), getLangValue.apply(block));
        }
    }

    /**
     * Transforms the registry name into a default translation
     * @param blocks the list of blocks to add entries for
     * @param <B> the block subclass
     */
    public <B extends Block> void autoTranslate(List<B> blocks)
    {
        for (B block : blocks)
        {
            if (block.getRegistryName() == null) continue;
            add(block.getTranslationKey(), format(block.getRegistryName().getPath()));
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

    @Override
    public String getName()
    {
        return "Mass Language Writer";
    }

    public static ModLanguageProvider getInstance()
    {
        return instance;
    }
}
