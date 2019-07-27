package com.ldtteam.structurize.generation.shingle_slabs;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingle;
import com.ldtteam.structurize.blocks.decorative.BlockShingleSlab;
import com.ldtteam.structurize.generation.AbstractLangEntryProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class ShingleSlabsLangEntryProvider extends AbstractLangEntryProvider
{
    private final DataGenerator generator;

    public ShingleSlabsLangEntryProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        for (BlockShingleSlab shingleSlab : ModBlocks.getShingleSlabs())
        {
            if (shingleSlab.getRegistryName() == null)
                continue;

            final String reference = "block.structurize." + shingleSlab.getRegistryName().getPath();
            final String value = shingleSlab.getFaceType().getLangName() +  " Shingle Slab";

            addLangEntry(cache, inputPath, reference, value);
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Shingle Slabs Lang Provider";
    }
}
