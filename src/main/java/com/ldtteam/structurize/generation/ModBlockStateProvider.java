package com.ldtteam.structurize.generation;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.IGeneratedBlockstate;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public void act(final DirectoryCache cache) throws IOException
    {
        blocks = new HashMap<>(models().generatedModels);
        items = new HashMap<>(itemModels().generatedModels);
        states = new LinkedHashMap<>(registeredBlocks);

        super.act(cache);
    }

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
            if (models().existingFileHelper.exists(name, ResourcePackType.CLIENT_RESOURCES, "_" + model + ".png", "textures/" + directory))
            {
                return new ResourceLocation(name.getNamespace(), directory + "/" + name.getPath());
            }
        }

        // Take the last one when none are found. It gives a clearer error message
        return new ResourceLocation(name.getNamespace(), directory + "/" + name.getPath());
    }

    public static ModBlockStateProvider getInstance()
    {
        return instance;
    }
}
