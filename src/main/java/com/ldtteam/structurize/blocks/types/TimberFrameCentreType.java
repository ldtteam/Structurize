package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.api.generation.ModLanguageProvider;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.RegistryObject;
import org.jetbrains.annotations.NotNull;

public enum TimberFrameCentreType implements IStringSerializable
{
    // Wood
    OAK(Blocks.OAK_PLANKS, "Oak"),
    ACACIA(Blocks.ACACIA_PLANKS, "Acacia"),
    BIRCH(Blocks.BIRCH_PLANKS, "Birch"),
    JUNGLE(Blocks.JUNGLE_PLANKS, "Jungle"),
    SPRUCE(Blocks.SPRUCE_PLANKS, "Spruce"),
    DARK_OAK(Blocks.DARK_OAK_PLANKS, "Dark Oak"),
    CRIMSON(Blocks.CRIMSON_PLANKS, "Crimson"),
    WARPED(Blocks.WARPED_PLANKS, "Warped"),
    CACTUS(ModBlocks.CACTI_BLOCKS.getMainRegisteredBlock(), "Cactus", "structurize:blocks/cactus/blockcactusplank"),
    // Terracotta
    TERRACOTTA(Blocks.TERRACOTTA),
    WHITE_TERRACOTTA(Blocks.WHITE_TERRACOTTA),
    ORANGE_TERRACOTTA(Blocks.ORANGE_TERRACOTTA),
    MAGENTA_TERRACOTTA(Blocks.MAGENTA_TERRACOTTA),
    LIGHT_BLUE_TERRACOTTA(Blocks.LIGHT_BLUE_TERRACOTTA),
    YELLOW_TERRACOTTA(Blocks.YELLOW_TERRACOTTA),
    LIME_TERRACOTTA(Blocks.LIME_TERRACOTTA),
    PINK_TERRACOTTA(Blocks.PINK_TERRACOTTA),
    GRAY_TERRACOTTA(Blocks.GRAY_TERRACOTTA),
    LIGHT_GRAY_TERRACOTTA(Blocks.LIGHT_GRAY_TERRACOTTA),
    CYAN_TERRACOTTA(Blocks.CYAN_TERRACOTTA),
    PURPLE_TERRACOTTA(Blocks.PURPLE_TERRACOTTA),
    BLUE_TERRACOTTA(Blocks.BLUE_TERRACOTTA),
    BROWN_TERRACOTTA(Blocks.BROWN_TERRACOTTA),
    GREEN_TERRACOTTA(Blocks.GREEN_TERRACOTTA),
    RED_TERRACOTTA(Blocks.RED_TERRACOTTA),
    BLACK_TERRACOTTA(Blocks.BLACK_TERRACOTTA),
    // Bricks
    BRICK(Blocks.BRICKS, "Bricks"),
    STONE_BRICK(Blocks.STONE_BRICKS, "Stone Bricks"),
    CREAM_BRICK(BrickType.CREAM.getMainRegisteredBlock(), "Cream Brick", "structurize:blocks/bricks/cream_bricks"),
    BEIGE_BRICK(BrickType.BEIGE.getMainRegisteredBlock(), "Beige Brick", "structurize:blocks/bricks/beige_bricks"),
    BROWN_BRICK(BrickType.BROWN.getMainRegisteredBlock(), "Brown Brick", "structurize:blocks/bricks/brown_bricks"),

    CREAM_STONE_BRICK(BrickType.CREAM_STONE.getMainRegisteredBlock(), "Cream Stone Brick", "structurize:blocks/bricks/cream_stone_bricks"),
    BEIGE_STONE_BRICK(BrickType.BEIGE_STONE.getMainRegisteredBlock(), "Beige Stone Brick", "structurize:blocks/bricks/beige_stone_bricks"),
    BROWN_STONE_BRICK(BrickType.BROWN_STONE.getMainRegisteredBlock(), "Brown Stone Brick", "structurize:blocks/bricks/brown_stone_bricks"),
    // Other
    PAPER(Items.PAPER, "Paper", "structurize:blocks/timber_frame_paper"),
    COBBLESTONE(Blocks.COBBLESTONE),
    BLACKSTONE(Blocks.BLACKSTONE),
    STONE(Blocks.STONE);

    private IItemProvider         block           = null;
    private RegistryObject<Block> registeredBlock = null;
    final String langName;
    final String textureLocation;

    TimberFrameCentreType(final Block block)
    {
        this(block, ModLanguageProvider.format(block.getRegistryName().getPath()));
    }

    TimberFrameCentreType(final Block block, final String langName)
    {
        this(block, langName, "minecraft:block/" + block.getRegistryName().getPath());
    }

    TimberFrameCentreType(final IItemProvider block, final String langName, final String textureLocation)
    {
        this.block = block;
        this.langName = langName;
        this.textureLocation = textureLocation;
    }

    TimberFrameCentreType(final RegistryObject<Block> block, final String langName, final String textureLocation)
    {
        this.registeredBlock = block;
        this.langName = langName;
        this.textureLocation = textureLocation;
    }

    @NotNull
    @Override
    public String getString()
    {
        // This gets used before the registry is properly populated
        // so ensure that RegistryObjects don't get called
        return (this.block == null
                 ? this.langName.replace(' ', '_').toLowerCase()
                 : this.getMaterial().asItem().getRegistryName().getPath()).replace("_planks", "");
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
    public IItemProvider getMaterial()
    {
        return this.block == null && this.registeredBlock != null ? registeredBlock.get() : this.block;
    }
}
