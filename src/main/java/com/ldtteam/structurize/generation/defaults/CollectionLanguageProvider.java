package com.ldtteam.structurize.generation.defaults;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CollectionLanguageProvider extends LanguageProvider
{
    protected final List<Block> blocks;

    public CollectionLanguageProvider(final DataGenerator gen, final String modid, final List<Block> collection)
    {
        super(gen, modid, "en_us");
        this.blocks = collection;
    }

    @Override
    protected void addTranslations()
    {
        for (Block block : blocks)
        {
            if (block.getRegistryName() != null)
            {
                add(block, fromUnformatted(block.getRegistryName().getPath()));
            }
        }
    }

    protected String fromUnformatted(String key)
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
        return "Collection Default Translation (en_us) Provider";
    }
}
