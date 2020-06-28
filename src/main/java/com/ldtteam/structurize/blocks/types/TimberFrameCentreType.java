package com.ldtteam.structurize.blocks.types;

import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

public enum TimberFrameCentreType  implements IStringSerializable
{
    OAK("oak", "Oak"),
    ACACIA("acacia", "Acacia"),
    BIRCH("birch", "Birch"),
    JUNGLE("jungle", "Jungle"),
    SPRUCE("spruce", "Spruce"),
    DARK_OAK("dark_oak", "Dark Oak"),
    CACTUS("cactus", "Cactus", "structurize:blocks/blockcactusplank", "structurize:blockcactusplank"),
    //OTHER
    COBBLE_STONE("cobble_stone", "Cobblestone", "block/cobblestone", "cobblestone"),
    STONE("stone", "Stone", "block/stone", "stone"),
    PAPER("paper", "Paper", "structurize:blocks/timber_frame_paper", "paper"),
    BRICK("brick", "Brick", "block/bricks", "brick");

    final String name;
    final String langName;
    final String textureLocation;
    final String recipeIngredient;

    TimberFrameCentreType(final String name, final String langName)
    {
        this(name, langName, "minecraft:block/" + name + "_planks", "minecraft:" + name + "_planks");
    }

    TimberFrameCentreType(final String name, final String langName, final String textureLocation, final String recipeIngredient)
    {
        this.name = name;
        this.langName = langName;
        this.textureLocation = textureLocation;
        this.recipeIngredient = recipeIngredient;
    }

    @Override
    public String func_176610_l()
    {
        return this.name;
    }

    @NotNull
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
    public String getTextureLocation()
    {
        return this.textureLocation;
    }

    /**
     * ResourceLocation for the wood's item, used in data generator for recipes
     *
     * @return recipeIngredient
     */
    public String getRecipeIngredient()
    {
        return this.recipeIngredient;
    }
}
