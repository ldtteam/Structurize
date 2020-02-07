package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * BlockOut label pane. Used to render a piece of text.
 */
public class Label extends AbstractTextElement
{
    /**
     * The text of the label.
     */
    protected String labelText;

    /**
     * The color the label has when hovering it with the mouse.
     */
    protected int hoverColor = 0xffffff;

    /**
     * Whether to wrap text around or not
     */
    protected boolean wrap = false;

    /**
     * Standard constructor which instantiates a new label.
     */
    public Label()
    {
        super();
        // Required default constructor.
    }

    /**
     * Create a label from xml.
     *
     * @param params xml parameters.
     */
    public Label(final PaneParams params)
    {
        super(params);
        labelText = params.getLocalizedStringAttribute("label", labelText);

        // match textColor by default
        hoverColor = params.getColorAttribute("hovercolor", textColor);

        wrap = params.getBooleanAttribute("wrap", wrap);

        if (width == 0)
        {
            width = Math.min(mc.fontRenderer.getStringWidth(labelText), params.getParentWidth());
        }
    }

    public String getLabelText()
    {
        return labelText;
    }

    public void setLabelText(final String s)
    {
        labelText = s;
    }

    public int getHoverColor()
    {
        return hoverColor;
    }

    /**
     * Set the default and hover color for the label.
     *
     * @param c default color.
     * @param h hover color.
     */
    public void setColor(final int c, final int h)
    {
        setColor(c);
        hoverColor = h;
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        final int color = isPointInPane(mx, my) ? hoverColor : textColor;

        int offsetX = 0;
        int offsetY = 0;

        if (textAlignment.isRightAligned())
        {
            offsetX = getWidth() - getStringWidth();
        }
        else if (textAlignment.isHorizontalCentered())
        {
            offsetX = (getWidth() - getStringWidth()) / 2;
        }

        if (textAlignment.isBottomAligned())
        {
            offsetY = getHeight() - getTextHeight();
        }
        else if (textAlignment.isVerticalCentered())
        {
            offsetY = (getHeight() - getTextHeight()) / 2;
        }

        RenderSystem.pushMatrix();
        RenderSystem.translated((double) (getX() + offsetX), (double) (getY() + offsetY), 0D);
        RenderSystem.scalef((float) scale, (float) scale, (float) scale);
        mc.getTextureManager().bindTexture(TEXTURE);

        if (labelText != null && wrap)
        {
            mc.fontRenderer.drawSplitString(labelText, 0, 0, width, color);
        }
        else
        {
            drawString(labelText, 0, 0, color, shadow);
        }

        RenderSystem.popMatrix();
    }

    /**
     * Getter of the width of the string.
     *
     * @return the width.
     */
    public int getStringWidth()
    {
        return (int) (mc.fontRenderer.getStringWidth(labelText) * scale);
    }

    /**
     * Getter of the text height.
     *
     * @return the text height.
     */
    public int getTextHeight()
    {
        return (int) (mc.fontRenderer.FONT_HEIGHT * scale);
    }
}
