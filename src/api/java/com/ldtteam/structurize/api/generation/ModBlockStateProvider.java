package com.ldtteam.structurize.api.generation;

import net.minecraft.block.Block;
import net.minecraft.block.StairsBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A singleton manifestation of a data gen provider that can be used during the lifecycle
 * instead of explicitly during the register functions.
 *
 * Has helper methods for some added automation.
 *
 * Use directly in the lifecycle before providing blocks, or extend once and use that.
 */
public class ModBlockStateProvider extends BlockStateProvider
{
    private static ModBlockStateProvider                    instance;
    private        Map<ResourceLocation, BlockModelBuilder> blocks = models().generatedModels;
    private        Map<ResourceLocation, ItemModelBuilder>  items  = itemModels().generatedModels;
    private        Map<Block, IGeneratedBlockstate>         states = registeredBlocks;

    public ModBlockStateProvider(final DataGenerator gen, final String modid, final ExistingFileHelper exFileHelper)
    {
        super(gen, modid, exFileHelper);
        instance = this;
    }

    @Override
    protected void registerStatesAndModels()
    {
        // Restore what the super class clears
        models().generatedModels.putAll(blocks);
        itemModels().generatedModels.putAll(items);
        registeredBlocks.putAll(states);
    }

    @Override
    public void run(final DirectoryCache cache) throws IOException
    {
        blocks = new HashMap<>(models().generatedModels);
        items = new HashMap<>(itemModels().generatedModels);
        states = new LinkedHashMap<>(registeredBlocks);

        super.run(cache);
    }

    /**
     * Finds an existing texture, searching using the model suffix for each block in turn
     * @param directory the directory to search in
     * @param model an suffix to find specific variants if possible
     * @param blocks a list of blocks specifying the names of where to search, in order of preference
     * @return the first texture it finds, or the last location possible from the provided blocks
     */
    public ResourceLocation findTexture(String directory, String model, Block... blocks)
    {
        ResourceLocation name = new ResourceLocation("");

        for (Block block : blocks)
        {
            if (block.getRegistryName() == null) continue; // <- should never happen
            name = block.getRegistryName();

            if (!model.isEmpty() && models().existingFileHelper.exists(name, ResourcePackType.CLIENT_RESOURCES, "_" + model + ".png", "textures/" + directory))
            {
                return new ResourceLocation(name.getNamespace(), directory + "/" + name.getPath() + "_" + model);
            }
            if (models().existingFileHelper.exists(name, ResourcePackType.CLIENT_RESOURCES, (model.isEmpty() ? "" : "_" + model) + ".png", "textures/" + directory))
            {
                return new ResourceLocation(name.getNamespace(), directory + "/" + name.getPath());
            }
        }

        // Take the last one when none are found. It gives a clearer error message
        return new ResourceLocation(name.getNamespace(), directory + "/" + name.getPath());
    }

    public void stairsBlockUnlockUV(StairsBlock block, ModelFile stairs, ModelFile stairsInner, ModelFile stairsOuter)
    {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            Direction facing = state.getValue(StairsBlock.FACING);
            Half half = state.getValue(StairsBlock.HALF);
            StairsShape shape = state.getValue(StairsBlock.SHAPE);
            int yRot = (int) facing.getClockWise().toYRot(); // Stairs model is rotated 90 degrees clockwise for some reason

            if (shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT)
            {
                yRot += 270; // Left facing stairs are rotated 90 degrees clockwise
            }
            if (shape != StairsShape.STRAIGHT && half == Half.TOP)
            {
                yRot += 90; // Top stairs are rotated 90 degrees clockwise
            }
            yRot %= 360;

            return ConfiguredModel.builder()
              .modelFile(shape == StairsShape.STRAIGHT ? stairs : shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT ? stairsInner : stairsOuter)
              .rotationX(half == Half.BOTTOM ? 0 : 180)
              .rotationY(yRot)
              .uvLock(false)
              .build();
        }, StairsBlock.WATERLOGGED);
    }

    public static ModBlockStateProvider getInstance()
    {
        return instance;
    }
}
