package com.ldtteam.structurize.generation.floating_carpets;

import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockFloatingCarpet;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FloatingCarpetsBlockStateProvider implements IDataProvider
{
    private final DataGenerator generator;

    public FloatingCarpetsBlockStateProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull final DirectoryCache directoryCache) throws IOException
    {
        for (final BlockFloatingCarpet floatingCarpet : ModBlocks.getFloatingCarpets())
        {
            if (floatingCarpet.getRegistryName() == null) continue;

            BlockstateJson blockstateJson = new BlockstateJson();

            Map<String, BlockstateVariantJson> variants = new HashMap<>();
            variants.put("", new BlockstateVariantJson(new BlockstateModelJson("minecraft:block/" + floatingCarpet.getColor().getTranslationKey() + "_carpet")));
            blockstateJson.setVariants(variants);

            final Path blockStateFolder = this.generator.getOutputFolder().resolve(DataGeneratorConstants.BLOCKSTATE_DIR);
            final Path blockStatePath = blockStateFolder.resolve(floatingCarpet.getRegistryName().getPath() + ".json");

            IDataProvider.save(DataGeneratorConstants.GSON, directoryCache, DataGeneratorConstants.serialize(blockstateJson), blockStatePath);
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Floating Carpet BlockStates Provider";
    }
}
