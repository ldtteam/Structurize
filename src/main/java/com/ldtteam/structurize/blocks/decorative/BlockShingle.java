package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.blocks.types.ShingleFaceType;
import com.ldtteam.structurize.blocks.types.WoodType;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.DyeColor;
import net.minecraft.state.properties.StairsShape;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import net.minecraft.block.AbstractBlock.Properties;

/**
 * Class defining the general shingle.
 */
public class BlockShingle extends StairsBlock
{
    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 3F;

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Fields defining the registered block's wood and face types, these are used by the Data Generators
     */
    private final WoodType woodType;
    private final ShingleFaceType faceType;
    private final DyeColor color;

    public BlockShingle(final Supplier<BlockState> modelState, final WoodType woodType, final ShingleFaceType faceType, final DyeColor color)
    {
        super(modelState, Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE).noOcclusion());
        this.woodType = woodType;
        this.faceType = faceType;
        this.color = color;
    }

    /**
     * Get the model type from a StairsShape object
     *
     * @param shape the StairsShape object
     * @return the model type for provided StairsShape
     */
    public static String getTypeFromShape(final StairsShape shape)
    {
        switch (shape)
        {
            case INNER_LEFT:
            case INNER_RIGHT:
                return "concave";
            case OUTER_LEFT:
            case OUTER_RIGHT:
                return "convex";
            default:
                return "straight";
        }
    }

    /**
     * Get the associate wood, used for data gen
     *
     * @return the wood type
     */
    public WoodType getWoodType()
    {
        return this.woodType;
    }

    /**
     * Get the registered ShingleFaceType, used by the Data Generators
     *
     * @return the registered ShingleFaceType
     */
    public ShingleFaceType getFaceType()
    {
        return this.faceType;
    }

    @Nullable
    public DyeColor getColor()
    {
        return color;
    }

    public String getTypeString()
    {
        return (getColor() == null ? "" : getColor().getSerializedName() + "_") + getFaceType().getGroup();
    }
}
