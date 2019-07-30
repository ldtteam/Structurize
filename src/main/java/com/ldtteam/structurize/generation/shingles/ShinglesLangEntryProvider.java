package com.ldtteam.structurize.generation.shingles;

import com.ldtteam.datagenerators.AbstractLangEntryProvider;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingle;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class ShinglesLangEntryProvider extends AbstractLangEntryProvider
{
    private final DataGenerator generator;

    public ShinglesLangEntryProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        for (BlockShingle shingle : ModBlocks.getShingles())
        {
            if (shingle.getRegistryName() == null)
                continue;

            final String reference = "block.structurize." + shingle.getRegistryName().getPath();
            final String value = shingle.getFaceType().getLangName() + " " + shingle.getWoodType().getLangName() + " Shingle";

            addLangEntry(cache, inputPath, reference, value);
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Shingles Lang Provider";
    }
}
