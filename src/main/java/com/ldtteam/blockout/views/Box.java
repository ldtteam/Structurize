package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.Render;
import org.jetbrains.annotations.NotNull;

/**
 * Simple box element.
 */
public class Box extends View
{
    private float lineWidth = 1.0F;
    private int color = 0xff000000;

    /**
     * Required default constructor.
     */
    public Box()
    {
        super();
    }

    /**
     * Loads box from xml.
     *
     * @param params xml parameters.
     */
    public Box(@NotNull final PaneParams params)
    {
        super(params);
        lineWidth = params.getFloatAttribute("linewidth", lineWidth);
        color = params.getColorAttribute("color", color);
    }

    /**
     * Set the color of the box.
     * @param red the red.
     * @param green the green.
     * @param blue the blue.
     */
    public void setColor(final int red, final int green, final int blue)
    {
        this.color = Color.rgbaToInt(red, green, blue, 255);
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        Render.drawOutlineRect(x, y, x + getWidth(), y + getHeight(), lineWidth, color);

        super.drawSelf(mx, my);
    }
}
