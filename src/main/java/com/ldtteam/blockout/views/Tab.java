package com.ldtteam.blockout.views;

import com.ldtteam.blockout.PaneParams;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

public class Tab extends View
{
    /** The icon to be shown on any corresponding tab button */
    protected ResourceLocation icon;

    /** The text to use as a tooltip, or as the label itself when icon is null */
    protected IFormattableTextComponent label;

    public Tab(PaneParams params)
    {
        super(params);

        setPosition(0, 0);
        setSize(params.getParentWidth(), params.getParentHeight());

        this.icon = params.getTexture("icon", $ -> {});
    }
}
