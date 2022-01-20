package com.ldtteam.structurize.blocks.types;

import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Shape types used by the Shingle Slabs.
 *
 * IF YOU CHANGE THIS FILE, OR ADD ENTRIES, RUN THE DATA GENERATORS.
 *
 *  - gradle runData -
 */
public enum ShingleSlabShapeType implements IStringSerializable
{
    TOP("top"),
    ONE_WAY("one_way"),
    TWO_WAY("two_way"),
    THREE_WAY("three_way"),
    FOUR_WAY("four_way"),
    CURVED("curved");

    private final String name;

    ShingleSlabShapeType(final String nameIn)
    {
        this.name = nameIn;
    }

    @Override
    public String getSerializedName()
    {
        return this.name;
    }

    @NotNull
    public String getName()
    {
        return this.name;
    }
}
