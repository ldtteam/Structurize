package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * BlockOut gradient pane. Used to render a gradient.
 */
public class Gradient extends AbstractTextElement
{
    /**
     * Default Gradients. Some transparent gray value.
     */
    private int gradientStart = -1072689136;
    private int gradientEnd = -804253680;

    /**
     * Standard constructor which instantiates a new label.
     */
    public Gradient()
    {
        super();
        // Required default constructor.
    }

    /**
     * Create a label from xml.
     *
     * @param params xml parameters.
     */
    public Gradient(final PaneParams params)
    {
        super(params);
        gradientStart = params.getIntAttribute("gradientstart", gradientStart);
        gradientEnd = params.getColorAttribute("gradientend", gradientEnd);
    }

    /**
     * Set the gradient start color.
     * @param red the red.
     * @param blue the blue.
     * @param green the green.
     * @param alpha the alpha value.
     */
    public void setGradientStart(final int red, final int green, final int blue, final int alpha)
    {
        this.gradientStart = rgbaToInt(red, green, blue, alpha);
    }

    /**
     * Set the gradient end color.
     * @param red the red.
     * @param blue the blue.
     * @param green the green.
     * @param alpha the alpha value.
     */
    public void setGradientEnd(final int red, final int green, final int blue, final int alpha)
    {
        this.gradientEnd = rgbaToInt(red, green, blue, alpha);
    }

    private int rgbaToInt(final int red, final int green, final int blue, final int alpha)
    {
        int color = alpha;
        color = (color << 8) + red;
        color = (color << 8) + green;
        color = (color << 8) + blue;
        return color;
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        RenderSystem.pushMatrix();
        this.fillGradient(getX(), getY(), getX() + width, getY() + height, gradientStart, gradientEnd);
        RenderSystem.popMatrix();
    }
}
