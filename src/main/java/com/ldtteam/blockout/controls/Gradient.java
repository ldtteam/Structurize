package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.platform.GlStateManager;

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

        // match textColor by default
        gradientEnd = params.getColorAttribute("gradientend", gradientEnd);
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        GlStateManager.pushMatrix();
        this.fillGradient(getX(), getY(), getX() + width, getY() + height, gradientStart, gradientEnd);
        GlStateManager.popMatrix();
    }
}
