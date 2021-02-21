package com.ldtteam.structurize.api.generation;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;

public class ModItemModelProvider extends ItemModelProvider
{
    private static ModItemModelProvider instance;

    public ModItemModelProvider(final DataGenerator generator, final String modid, final ExistingFileHelper existingFileHelper)
    {
        super(generator, modid, existingFileHelper);
        instance = this;
    }

    @Override
    protected void registerModels()
    {
        // Nothing needs to be done here.
        // Methods are called via static instance reference instead.
    }

    @Override
    public void act(DirectoryCache cache) throws IOException
    {
        registerModels();
        generateAll(cache);
    }

    public ResourceLocation findTexture(String directory, String model, Block... blocks)
    {
        for (Block block : blocks)
        {
            ResourceLocation name = block.getRegistryName();
            if (name == null) continue;

            if (!model.isEmpty() && existingFileHelper.exists(name, ResourcePackType.CLIENT_RESOURCES, "_" + model + ".png", "textures/" + directory))
            {
                return new ResourceLocation(name.getNamespace(), directory + "/" + block.getRegistryName().getPath() + "_" + model);
            }
            if (existingFileHelper.exists(name, ResourcePackType.CLIENT_RESOURCES, "_" + model + ".png", "textures/" + directory))
            {
                return new ResourceLocation(name.getNamespace(), directory + "/" + block.getRegistryName().getPath());
            }
        }

        return null;
    }

    public static ModItemModelProvider getInstance()
    {
        return instance;
    }
}
