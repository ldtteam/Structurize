package com.ldtteam.structurize.blocks.types;

import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Face types used by both Shingles and Shingle Slabs.
 *
 * IF YOU CHANGE THIS FILE, OR ADD ENTRIES, RUN THE DATA GENERATORS.
 *
 *  -> gradle runData <-
 */
public enum ShingleFaceType implements IStringSerializable
{
    // Clay
    CLAY("clay", "clay", "Clay", "minecraft:brick", false),
    BLACK_CLAY("black_clay", "clay", "Black Clay", "minecraft:black_dye"),
    BLUE_CLAY("blue_clay", "clay", "Blue Clay", "minecraft:blue_dye"),
    BROWN_CLAY("brown_clay", "clay", "Brown Clay", "minecraft:brown_dye"),
    CYAN_CLAY("cyan_clay", "clay", "Cyan Clay", "minecraft:cyan_dye"),
    GRAY_CLAY("gray_clay", "clay", "Gray Clay", "minecraft:gray_dye"),
    GREEN_CLAY("green_clay", "clay", "Green Clay", "minecraft:green_dye"),
    LIGHT_BLUE_CLAY("light_blue_clay", "clay", "Light Blue Clay", "minecraft:light_blue_dye"),
    LIGHT_GRAY_CLAY("light_gray_clay", "clay", "Light Gray Clay", "minecraft:light_gray_dye"),
    LIME_CLAY("lime_clay", "clay", "Lime Clay", "minecraft:lime_dye"),
    MAGENTA_CLAY("magenta_clay", "clay", "Magenta Clay", "minecraft:magenta_dye"),
    ORANGE_CLAY("orange_clay", "clay", "Orange Clay", "minecraft:orange_dye"),
    PINK_CLAY("pink_clay", "clay", "Pink Clay", "minecraft:pink_dye"),
    PURPLE_CLAY("purple_clay", "clay", "Purple Clay", "minecraft:purple_dye"),
    RED_CLAY("red_clay", "clay", "Red Clay", "minecraft:red_dye"),
    WHITE_CLAY("white_clay", "clay", "White Clay", "minecraft:white_dye"),
    YELLOW_CLAY("yellow_clay", "clay", "Yellow Clay", "minecraft:yellow_dye"),
    // Slate
    SLATE("slate", "slate", "Slate", "minecraft:cobblestone", false),
    BLUE_SLATE("blue_slate", "slate", "Blue Slate", "minecraft:blue_dye"),
    GREEN_SLATE("green_slate", "slate", "Green Slate", "minecraft:green_dye"),
    PURPLE_SLATE("purple_slate", "slate", "Purple Slate", "minecraft:purple_dye"),
    // Special
    MOSS_SLATE("moss_slate", "moss", "Moss Slate", "minecraft:mossy_cobblestone", false),
    THATCHED("thatched", "thatched", "Thatched", "minecraft:wheat", false);

    final String name;
    final String group;
    final String langName;
    final String textureLocation;
    final String recipeIngredient;
    final boolean isDyed;

    ShingleFaceType(final String name, final String group, final String langName, final String recipeIngredient)
    {
        this(name, group, langName, recipeIngredient, true);
    }

    ShingleFaceType(final String name, final String group, final String langName, final String recipeIngredient, final boolean isDyed)
    {
        this(name, group, langName, "structurize:blocks/shingle/" + name + "_shingle_", recipeIngredient, isDyed);
    }

    ShingleFaceType(final String name, final String group, final String langName, final String textureLocation, final String recipeIngredient, final boolean isDyed)
    {
        this.name = name;
        this.group = group;
        this.langName = langName;
        this.textureLocation = textureLocation;
        this.recipeIngredient = recipeIngredient;
        this.isDyed = isDyed;
    }

    @Override
    public String getString()
    {
        return this.name;
    }

    @NotNull
    public String getName()
    {
        return this.name;
    }

    /**
     * The face group the type belongs to, e.g. Clay, or Slate. used by data generators.
     *
     * @return group
     */
    public String getGroup()
    {
        return this.group;
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
     * ResourceLocation for the wood type's texture, plus a suffix used in texture locations, used in data generator for models
     *
     * @return textureLocation
     */
    public String getTexture(final int suffix)
    {
        return getTextureLocation() + suffix;
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
     * ResourceLocation for the face's item, used in data generator for recipes
     *
     * @return recipeIngredient
     */
    public String getRecipeIngredient()
    {
        return this.recipeIngredient;
    }

    /**
     * Whether this is a dyed version of another shingle in the same group, or if this is the group's "parent" per se
     *
     * @return isDyed
     */
    public boolean isDyed()
    {
        return this.isDyed;
    }
}
