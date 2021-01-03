package com.ldtteam.blockout;

import com.ldtteam.blockout.controls.AbstractTextBuilder.TextBuilder;
import com.ldtteam.blockout.controls.AbstractTextBuilder.TooltipBuilder;

public final class PaneBuilders
{
    private PaneBuilders()
    {
        // utility class
    }

    /**
     * Tooltip element builder.
     * Don't forget to set hoverPane.
     * 
     * @see TooltipBuilder#hoverPane(Pane)
     */
    public static TooltipBuilder tooltipBuilder()
    {
        return new TooltipBuilder();
    }

    /**
     * Text element builder.
     */
    public static TextBuilder textBuilder()
    {
        return new TextBuilder();
    }
}
