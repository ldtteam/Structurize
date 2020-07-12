package com.ldtteam.structurize.blocks.types;

import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

//Creates types for TimberFrame with different variants of wood and texture

public enum TimberFrameType implements IStringSerializable
{
    PLAIN("plain", "Plain", false),
    DOUBLE_CROSSED("double_crossed", "Double Crossed", false),
    FRAMED("framed", "Framed", false),
    SIDE_FRAMED("side_framed", "Side Framed", true),
    UP_GATED("up_gated", "Up Gate Framed", true),
    DOWN_GATED("down_gated", "Down Gate Framed", true),
    ONE_CROSSED_LR("one_crossed_lr", "Left Right Crossed", false),
    ONE_CROSSED_RL("one_crossed_rl", "Right Left Crossed", false),
    HORIZONTAL_PLAIN("horizontal_plain", "Plain Horizontal", false),
    SIDE_FRAMED_HORIZONTAL("side_framed_horizontal", "Side Framed Horizontal", true);

    private final String name;
    private final String langName;
    private final boolean rotatable;

    TimberFrameType(final String name, final String langName, final boolean rotatable)
    {
        this.name = name;
        this.langName = langName;
        this.rotatable = rotatable;
    }

    /**
     * Get the Type previous to the current (used by data generators for recipes)
     * @return the previous type.
     */
    public TimberFrameType getPrevious()
    {
        if (this.ordinal() - 1 < 0)
            return values()[values().length - 1];
        return values()[(this.ordinal() - 1) % values().length];
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

    public String getLangName()
    {
        return this.langName;
    }

    public boolean isRotatable()
    {
        return this.rotatable;
    }
}
