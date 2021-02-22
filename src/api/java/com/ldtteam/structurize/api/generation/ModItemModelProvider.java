package com.ldtteam.structurize.api.generation;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * A singleton manifestation of a data gen provider that can be used during the lifecycle
 * instead of explicitly during the register functions.
 *
 * Has helper methods for some added automation.
 *
 * Use directly in the lifecycle before providing blocks, or extend once and use that.
 */
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
    public void act(DirectoryCache cache)
    {
        registerModels();
        generateAll(cache);
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

    /**
     * Models may not exist yet, so this will set up an item model for any parent location
     * @param path the path associated with this model
     * @param loc the location of the parent, assuming the mod namespace
     * @return the builder, so textures can be altered, etc
     */
    public ModelBuilder<? extends ItemModelBuilder> withUncheckedParent(String path, String loc)
    {
        return this.getBuilder(path).parent(new ModelFile.UncheckedModelFile(modLoc(loc)));
    }

    public static ModItemModelProvider getInstance()
    {
        return instance;
    }
}
