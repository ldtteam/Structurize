package com.ldtteam.structurize.blocks.types;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public enum TimberFrameCentreType  implements IStringSerializable
{
    OAK("oak", "Oak"),
    ACACIA("acacia", "Acacia"),
    BIRCH("birch", "Birch"),
    JUNGLE("jungle", "Jungle"),
    SPRUCE("spruce", "Spruce"),
    DARK_OAK("dark_oak", "Dark Oak"),
    CACTUS("cactus", "Cactus", new ResourceLocation("structurize:blocks/blockcactusplank"), new ResourceLocation("structurize:blockcactusplank")),
    //OTHER
    COBBLE_STONE("cobble_stone", "Cobblestone", new ResourceLocation("block/cobblestone"), new ResourceLocation("cobblestone")),
    STONE("stone", "Stone", new ResourceLocation("block/stone"), new ResourceLocation("stone")),
    PAPER("paper", "Paper", new ResourceLocation("structurize:blocks/timber_frame_paper"), new ResourceLocation("paper"));

    final String name;
    final String langName;
    final ResourceLocation textureLocation;
    final ResourceLocation recipeIngredient;

    TimberFrameCentreType(final String name, final String langName)
    {
        this(name, langName, new ResourceLocation("minecraft:block/" + name + "_planks"), new ResourceLocation("minecraft:" + name + "_planks"));
    }

    TimberFrameCentreType(final String name, final String langName, final ResourceLocation textureLocation, final ResourceLocation recipeIngredient)
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
