package com.ldtteam.structurize.generation.shingles;

import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingle;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.block.StairsBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ShinglesBlockStateProvider implements IDataProvider
{
    private final DataGenerator generator;

    public ShinglesBlockStateProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        for (final BlockShingle shingle : ModBlocks.getShingles())
        {
            createBlockstateFile(cache, shingle);
        }
    }

    private void createBlockstateFile(final DirectoryCache cache, final BlockShingle shingle) throws IOException
    {
        if (shingle.getRegistryName() == null)
            return;

        final Map<String, BlockstateVariantJson> variants = new HashMap<>();

        for (Direction facingValue : StairsBlock.FACING.getAllowedValues())
        {
            for (StairsShape shapeValue : StairsBlock.SHAPE.getAllowedValues())
            {
                for (Half halfValue : StairsBlock.HALF.getAllowedValues())
                {
                    final String variantKey = "facing=" + facingValue + ",shape=" + shapeValue + ",half=" + halfValue;

                    int y = getYFromFacing(facingValue);
                    y = y + getYFromShape(shapeValue);
                    y = y + getYFromHalf(halfValue, shapeValue);

                    int x = halfValue == Half.TOP ? 180 : 0;

                    final String modelLocation = "structurize:block/shingle/" +
                            BlockShingle.getTypeFromShape(shapeValue) + "/" +
                            shingle.getWoodType().getName() + "/" +
                            shingle.getFaceType().getName() + "_shingle";

                    final BlockstateModelJson model = new BlockstateModelJson(modelLocation, x, y);
                    final BlockstateVariantJson variant = new BlockstateVariantJson(model);

                    variants.put(variantKey, variant);
                }
            }
        }

        final BlockstateJson blockstate = new BlockstateJson(variants);

        final Path blockstateFolder = this.generator.getOutputFolder().resolve(DataGeneratorConstants.BLOCKSTATE_DIR);
        final Path blockstatePath = blockstateFolder.resolve(shingle.getRegistryName().getPath() + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, blockstate.serialize(), blockstatePath);

    }

    @NotNull
    @Override
    public String getName()
    {
        return "Shingles BlockStates Provider";
    }

    private int getYFromHalf(final Half half, final StairsShape shape)
    {
        if (half == Half.TOP)
        {
            if (shape == StairsShape.STRAIGHT)
            {
                return 180;
            }
            return 90;
        }
        return 0;
    }

    private int getYFromShape(final StairsShape shape)
    {
        switch (shape)
        {
            default:
                return 0;
            case OUTER_RIGHT:
            case INNER_RIGHT:
                return 90;
        }
    }

    private int getYFromFacing(final Direction facing)
    {
        switch (facing)
        {
            default:
                return 0;
            case WEST:
                return 90;
            case NORTH:
                return 180;
            case EAST:
                return 270;
        }
    }
}
