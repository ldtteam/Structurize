package com.ldtteam.structurize.generation.shingle_slabs;

import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingleSlab;
import com.ldtteam.structurize.blocks.types.ShingleSlabShapeType;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ShingleSlabsBlockStateProvider implements IDataProvider
{
    private final DataGenerator generator;

    public ShingleSlabsBlockStateProvider(final DataGenerator generator)
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
            createBlockstateFile(cache, shingleSlab);
        }
    }

    private void createBlockstateFile(final DirectoryCache cache, final BlockShingleSlab shingleSlab) throws IOException
    {
        if (shingleSlab.getRegistryName() == null)
            return;

        final Map<String, BlockstateVariantJson> variants = new HashMap<>();

        for (ShingleSlabShapeType shingleSlabShape : BlockShingleSlab.SHAPE.getAllowedValues())
        {
            for (Direction shingleSlabFacing : BlockShingleSlab.HORIZONTAL_FACING.getAllowedValues())
            {
                final String variantKey = "shape=" + shingleSlabShape.getName() + ",facing=" + shingleSlabFacing.getName();
                int y = getYFromFacing(shingleSlabFacing);

                final String modelLocation = "structurize:block/shingle_slab/" + shingleSlab.getRegistryName().getPath() + "_" + shingleSlabShape.getName();

                final BlockstateModelJson model = new BlockstateModelJson(modelLocation, 0, y);
                final BlockstateVariantJson variant = new BlockstateVariantJson(model);

                variants.put(variantKey, variant);
            }
        }

        final BlockstateJson blockstate = new BlockstateJson(variants);

        final Path blockstateFolder = this.generator.getOutputFolder().resolve(DataGeneratorConstants.BLOCKSTATE_DIR);
        final Path blockstatePath = blockstateFolder.resolve(shingleSlab.getRegistryName().getPath() + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, blockstate.serialize(), blockstatePath);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Shingle Slab BlockStates Provider";
    }

    private int getYFromFacing(final Direction facing)
    {
        switch (facing)
        {
            default:
                return 270;
            case EAST:
                return 0;
            case SOUTH:
                return 90;
            case WEST:
                return 180;
        }
    }
}
