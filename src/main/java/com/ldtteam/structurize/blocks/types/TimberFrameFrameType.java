package com.ldtteam.structurize.blocks.types;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public enum TimberFrameFrameType implements IStringSerializable
{
    OAK("oak", "Oak"),
    ACACIA("acacia", "Acacia"),
    BIRCH("birch", "Birch"),
    JUNGLE("jungle", "Jungle"),
    SPRUCE("spruce", "Spruce"),
    DARK_OAK("dark_oak", "Dark Oak"),
    CACTUS("cactus", "Cactus", new ResourceLocation("structurize:blocks/blockcactusplank"), new ResourceLocation("structurize:blockcactusplank"));

    final String name;
    final String langName;
    final ResourceLocation textureLocation;
    final ResourceLocation recipeIngredient;

    TimberFrameFrameType(final String name, final String langName)
    {
        this(name, langName, new ResourceLocation("minecraft:block/" + name + "_planks"), new ResourceLocation("minecraft:" + name + "_planks"));
    }

    TimberFrameFrameType(final String name, final String langName, final ResourceLocation textureLocation, final ResourceLocation recipeIngredient)
    {
        this.name = name;
        this.langName = langName;
        this.textureLocation = textureLocation;
        this.recipeIngredient = recipeIngredient;
    }

    @NotNull
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * Name used in the Lang data generator
     *
     * @return langName
     */
    public String getLangName()
    {
        return this.langName;
    }

    /**
     * ResourceLocation for the wood type's texture, used in data generator for models
     *
     * @return textureLocation
     */
    public ResourceLocation getTextureLocation()
    {
        return this.textureLocation;
    }

    /**
     * ResourceLocation for the wood's item, used in data generator for recipes
     *
     * @return recipeIngredient
     */
    public ResourceLocation getRecipeIngredient()
    {
        return this.recipeIngredient;
    }

}
