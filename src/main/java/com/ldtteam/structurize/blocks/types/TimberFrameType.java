package com.ldtteam.structurize.blocks.types;

import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

//Creates types for TimberFrame with different variants of wood and texture

public enum TimberFrameType implements IStringSerializable
{
    PLAIN("plain", "Plain"),
    DOUBLE_CROSSED("double_crossed", "Double Crossed"),
    FRAMED("framed", "Framed"),
    SIDE_FRAMED("side_framed", "Side Framed"),
    UP_GATED("up_gated", "Upper Gate Framed"),
    ONE_CROSSED_LR("one_crossed_lr", "Left Right Crossed"),
    ONE_CROSSED_RL("one_crossed_rl", "Right Left Crossed"),
    DOWN_GATED("down_gated", "Lower Gate Framed"),
    HORIZONTAL_PLAIN("horizontal_plain", "Plain Horizontal"),
    SIDE_FRAMED_HORIZONTAL("side_framed_horizontal", "Side Framed Horizontal");

    private final String name;
    private final String langName;

    TimberFrameType(final String name, final String langName)
    {
        this.name = name;
        this.langName = langName;
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

    @NotNull
    public String getName()
    {
        return this.name;
    }

    public String getLangName()
    {
        return this.langName;
    }
}
