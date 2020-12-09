package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;

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
        this.gradientStart = Color.rgbaToInt(red, green, blue, alpha);
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
        this.gradientEnd = Color.rgbaToInt(red, green, blue, alpha);
    }

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        ms.push();
        fillGradient(ms, getX(), getY(), getX() + width, getY() + height, gradientStart, gradientEnd);
        ms.pop();
    }
}
