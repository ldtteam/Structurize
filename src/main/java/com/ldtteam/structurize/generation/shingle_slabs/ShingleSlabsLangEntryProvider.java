package com.ldtteam.structurize.generation.shingle_slabs;

import com.ldtteam.datagenerators.lang.LangJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingleSlab;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;

public class ShingleSlabsLangEntryProvider implements IDataProvider
{
    private final LangJson backingLangJson;

    public ShingleSlabsLangEntryProvider(LangJson backingLangJson)
    {
        this.backingLangJson = backingLangJson;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        for (BlockShingleSlab shingleSlab : ModBlocks.getShingleSlabs())
        {
            if (shingleSlab.getRegistryName() == null)
                continue;

            final String reference = "block.structurize." + shingleSlab.getRegistryName().getPath();
            final String value = shingleSlab.getFaceType().getLangName() + " Shingle Slab";

            backingLangJson.put(reference, value);
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Shingle Slabs Lang Provider";
    }
}
