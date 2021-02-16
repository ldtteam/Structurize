package com.ldtteam.structurize.generation.timber_frames;

import com.ldtteam.datagenerators.lang.LangJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockTimberFrame;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TimberFramesLangEntryProvider implements IDataProvider
{
    private final LangJson backingLangJson;

    public TimberFramesLangEntryProvider(LangJson backingLangJson)
    {
        this.backingLangJson = backingLangJson;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        for (BlockTimberFrame timberFrame : ModBlocks.getTimberFrames())
        {
            if (timberFrame.getRegistryName() == null)
                continue;

            final String reference = "block.structurize." + timberFrame.getRegistryName().getPath();
            final String value = timberFrame.getTimberFrameType().getLangName() + " " + timberFrame.getFrameType().getLangName() + " " + timberFrame.getCentreType().getLangName() + " Timber Frame";

            backingLangJson.put(reference, value);
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Timber Frames Lang Provider";
    }
}
