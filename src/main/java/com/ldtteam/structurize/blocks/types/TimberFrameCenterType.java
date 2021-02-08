package com.ldtteam.structurize.blocks.types;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public enum TimberFrameCenterType implements IStringSerializable
{
    // Wood
    OAK("oak", "Oak"),
    ACACIA("acacia", "Acacia"),
    BIRCH("birch", "Birch"),
    JUNGLE("jungle", "Jungle"),
    SPRUCE("spruce", "Spruce"),
    DARK_OAK("dark_oak", "Dark Oak"),
    CACTUS("cactus", "Cactus", "structurize:blocks/blockcactusplank", "structurize:blockcactusplank"),
    // Terracotta
    TERRACOTTA(Blocks.TERRACOTTA, "Terracotta"),
    WHITE_TERRACOTTA(Blocks.WHITE_TERRACOTTA, "White Terracotta"),
    ORANGE_TERRACOTTA(Blocks.ORANGE_TERRACOTTA, "Orange Terracotta"),
    MAGENTA_TERRACOTTA(Blocks.MAGENTA_TERRACOTTA, "Magenta Terracotta"),
    LIGHT_BLUE_TERRACOTTA(Blocks.LIGHT_BLUE_TERRACOTTA, "Light Blue Terracotta"),
    YELLOW_TERRACOTTA(Blocks.YELLOW_TERRACOTTA, "Yellow Terracotta"),
    LIME_TERRACOTTA(Blocks.LIME_TERRACOTTA, "Lime Terracotta"),
    PINK_TERRACOTTA(Blocks.PINK_TERRACOTTA, "Pink Terracotta"),
    GRAY_TERRACOTTA(Blocks.GRAY_TERRACOTTA, "Gray Terracotta"),
    LIGHT_GRAY_TERRACOTTA(Blocks.LIGHT_GRAY_TERRACOTTA, "Light Gray Terracotta"),
    CYAN_TERRACOTTA(Blocks.CYAN_TERRACOTTA, "Cyan Terracotta"),
    PURPLE_TERRACOTTA(Blocks.PURPLE_TERRACOTTA, "Purple Terracotta"),
    BLUE_TERRACOTTA(Blocks.BLUE_TERRACOTTA, "Blue Terracotta"),
    BROWN_TERRACOTTA(Blocks.BROWN_TERRACOTTA, "Brown Terracotta"),
    GREEN_TERRACOTTA(Blocks.GREEN_TERRACOTTA, "Green Terracotta"),
    RED_TERRACOTTA(Blocks.RED_TERRACOTTA, "Red Terracotta"),
    BLACK_TERRACOTTA(Blocks.BLACK_TERRACOTTA, "Black Terracotta"),
    // Bricks
    BRICK(Blocks.BRICKS, "Bricks"),
    STONE_BRICK(Blocks.STONE_BRICKS, "Stone Bricks"),
    CREAM_BRICK("cream_brick", "Cream Brick", "structurize:blocks/bricks/cream_bricks", "structurize:cream_bricks"),
    BEIGE_BRICK("beige_brick", "Beige Brick", "structurize:blocks/bricks/beige_bricks", "structurize:beige_bricks"),
    BROWN_BRICK("brown_brick", "Brown Brick", "structurize:blocks/bricks/brown_bricks", "structurize:brown_bricks"),
    // Other
    PAPER("paper", "Paper", "structurize:blocks/timber_frame_paper", "paper"),
    COBBLE_STONE("cobble_stone", "Cobblestone", "block/cobblestone", "cobblestone"),
    STONE(Blocks.STONE, "Stone");

    final String name;
    final String langName;
    final String textureLocation;
    final String recipeIngredient;

    TimberFrameCenterType(final String name, final String langName)
    {
        this(name, langName, "minecraft:block/" + name + "_planks", "minecraft:" + name + "_planks");
    }

    TimberFrameCenterType(final Block block, final String langName)
    {
        this(Objects.requireNonNull(block.getRegistryName()).getPath(), langName, "block/" + block.getRegistryName().getPath(), block.getRegistryName().toString());
    }

    TimberFrameCenterType(final String name, final String langName, final String textureLocation, final String recipeIngredient)
    {
        this.name = name;
        this.langName = langName;
        this.textureLocation = textureLocation;
        this.recipeIngredient = recipeIngredient;
    }

    @NotNull
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
