package com.ldtteam.structurize.blocks.types;

import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

//Creates types for TimberFrame with different variants of wood and texture

public enum TimberFrameType implements IStringSerializable
{
    PLAIN("plain"),
    DOUBLECROSSED("doublecrossed"),
    FRAMED("framed"),
    SIDEFRAMED("sideframed"),
    GATEFRAMED("gateframed"),
    ONECROSSEDLR("onecrossedlr"),
    ONECROSSEDRL("onecrossedrl"),
    DOWNGATED("downgated"),
    HORIZONTALPLAIN("horizontalplain"),
    HORIZONTALNOCAP("horizontalnocap");

    private final String name;

    TimberFrameType(final String nameIn)
    {
        this.name = nameIn;
    }

    @NotNull
    public String getName()
    {
        return this.name;
    }

}
