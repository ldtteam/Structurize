package com.ldtteam.structurize.generation.floating_carpets;

import com.ldtteam.datagenerators.lang.LangJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockFloatingCarpet;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class FloatingCarpetsLangEntryProvider implements IDataProvider
{
    private final LangJson backingLangJson;

    public FloatingCarpetsLangEntryProvider(LangJson backingLangJson)
    {
        this.backingLangJson = backingLangJson;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        for (final BlockFloatingCarpet floatingCarpet : ModBlocks.getFloatingCarpets())
        {
            if (floatingCarpet.getRegistryName() == null) continue;

            final String reference = "block.structurize." + floatingCarpet.getRegistryName().getPath();
            final String value = dyeToString(floatingCarpet.getColor()) + " Floating Carpet";

            backingLangJson.put(reference, value);
        }
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
