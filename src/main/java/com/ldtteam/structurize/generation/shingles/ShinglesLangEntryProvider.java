package com.ldtteam.structurize.generation.shingles;

import com.ldtteam.datagenerators.lang.LangJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingle;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ShinglesLangEntryProvider implements IDataProvider
{
    private final LangJson backingLangJson;

    public ShinglesLangEntryProvider(LangJson backingLangJson)
    {
        this.backingLangJson = backingLangJson;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        for (BlockShingle shingle : ModBlocks.getShingles())
        {
            if (shingle.getRegistryName() == null)
                continue;

            final String reference = "block.structurize." + shingle.getRegistryName().getPath();
            final String value = shingle.getFaceType().getLangName() + " " + shingle.getWoodType().getLangName() + " Shingle";

            backingLangJson.put(reference, value);
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Shingles Lang Provider";
    }
}
