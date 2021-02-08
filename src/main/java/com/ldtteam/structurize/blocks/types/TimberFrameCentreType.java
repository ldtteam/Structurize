package com.ldtteam.structurize.blocks.types;

import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

public enum TimberFrameCentreType  implements IStringSerializable
{
    // Wood
    OAK("oak", "Oak"),
    ACACIA("acacia", "Acacia"),
    BIRCH("birch", "Birch"),
    JUNGLE("jungle", "Jungle"),
    SPRUCE("spruce", "Spruce"),
    DARK_OAK("dark_oak", "Dark Oak"),
    CRIMSON("crimson", "Crimson"),
    WARPED("warped", "Warped"),
    CACTUS("cactus", "Cactus", "structurize:blocks/blockcactusplank", "structurize:blockcactusplank"),
    // Terracotta
    TERRACOTTA("terracotta", "Terracotta", "block/terracotta", "terracotta"),
    WHITE_TERRACOTTA("white_terracotta", "White Terracotta", "block/white_terracotta", "white_terracotta"),
    ORANGE_TERRACOTTA("orange_terracotta", "Orange Terracotta", "block/orange_terracotta", "orange_terracotta"),
    MAGENTA_TERRACOTTA("magenta_terracotta", "Magenta Terracotta", "block/magenta_terracotta", "magenta_terracotta"),
    LIGHT_BLUE_TERRACOTTA("light_blue_terracotta", "Light Blue Terracotta", "block/light_blue_terracotta", "light_blue_terracotta"),
    YELLOW_TERRACOTTA("yellow_terracotta", "Yellow Terracotta", "block/yellow_terracotta", "yellow_terracotta"),
    LIME_TERRACOTTA("lime_terracotta", "Lime Terracotta", "block/lime_terracotta", "lime_terracotta"),
    PINK_TERRACOTTA("pink_terracotta", "Pink Terracotta", "block/pink_terracotta", "pink_terracotta"),
    GRAY_TERRACOTTA("gray_terracotta", "Gray Terracotta", "block/gray_terracotta", "gray_terracotta"),
    LIGHT_GRAY_TERRACOTTA("light_gray_terracotta", "Light Gray Terracotta", "block/light_gray_terracotta", "light_gray_terracotta"),
    CYAN_TERRACOTTA("cyan_terracotta", "Cyan Terracotta", "block/cyan_terracotta", "cyan_terracotta"),
    PURPLE_TERRACOTTA("purple_terracotta", "Purple Terracotta", "block/purple_terracotta", "purple_terracotta"),
    BLUE_TERRACOTTA("blue_terracotta", "Blue Terracotta", "block/blue_terracotta", "blue_terracotta"),
    BROWN_TERRACOTTA("brown_terracotta", "Brown Terracotta", "block/brown_terracotta", "brown_terracotta"),
    GREEN_TERRACOTTA("green_terracotta", "Green Terracotta", "block/green_terracotta", "green_terracotta"),
    RED_TERRACOTTA("red_terracotta", "Red Terracotta", "block/red_terracotta", "red_terracotta"),
    BLACK_TERRACOTTA("black_terracotta", "Black Terracotta", "block/black_terracotta", "black_terracotta"),
    // Bricks
    BRICK("brick", "Brick", "block/bricks", "brick"),
    STONE_BRICK("stone_brick", "Stone Brick", "block/stone_bricks", "stone_brick"),
    CREAM_BRICK("cream_brick", "Cream Brick", "structurize:blocks/bricks/bricks_cream", "structurize:blockcreambricks"),
    BEIGE_BRICK("beige_brick", "Beige Brick", "structurize:blocks/bricks/bricks_beige", "structurize:blockbeigebricks"),
    BROWN_BRICK("brown_brick", "Brown Brick", "structurize:blocks/bricks/bricks_brown", "structurize:blockbrownbricks"),
    // Other
    PAPER("paper", "Paper", "structurize:blocks/timber_frame_paper", "paper"),
    COBBLE_STONE("cobble_stone", "Cobblestone", "block/cobblestone", "cobblestone"),
    BLACKSTONE("blackstone", "Blackstone", "block/blackstone", "blackstone"),
    STONE("stone", "Stone", "block/stone", "stone");

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
     * Name used in the lang data generator
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
