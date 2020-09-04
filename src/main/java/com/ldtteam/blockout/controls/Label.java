package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * BlockOut label pane. Used to render a piece of text.
 */
public class Label extends AbstractTextElement
{
    /**
     * The text of the label.
     */
    protected IFormattableTextComponent labelText;

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
        labelText = new StringTextComponent(params.getLocalizedStringAttribute("label", ""));

        // match textColor by default
        hoverColor = params.getColorAttribute("hovercolor", textColor);

        wrap = params.getBooleanAttribute("wrap", wrap);

        if (width == 0)
        {
            width = Math.min(mc.fontRenderer.func_238414_a_(labelText), params.getParentWidth());
        }
    }

    /**
     * Button textContent getter.
     *
     * @return button textContent.
     */
    @Deprecated
    public String getLabelText()
    {
        return labelText.getString();
    }

    public IFormattableTextComponent getLabelTextNew()
    {
        return labelText;
    }

    /**
     * Button textContent setter.
     *
     * @param s new textContent.
     */
    @Deprecated
    public void setLabelText(final String s)
    {
        labelText = new StringTextComponent(s);
    }

    public void setLabelText(final IFormattableTextComponent s)
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
    public void drawSelf(final MatrixStack ms, final int mx, final int my)
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

        ms.push();
        ms.translate((double) (getX() + offsetX), (double) (getY() + offsetY), 0D);
        ms.scale((float) scale, (float) scale, (float) scale);
        mc.getTextureManager().bindTexture(TEXTURE);

        if (labelText != null && wrap)
        {
            // mc.fontRenderer.func_238418_a_(labelText, 0, 0, width, color);
            final Matrix4f matrix4f = ms.getLast().getMatrix();
            int lineShift = 0;
            for (final ITextProperties itextproperties : mc.fontRenderer.func_238425_b_(labelText, width))
            {
                mc.fontRenderer.func_238415_a_(itextproperties, 0, lineShift, color, matrix4f, false);
                lineShift += 9;
            }
        }
        else
        {
            drawString(ms, labelText, 0, 0, color, shadow);
        }

        ms.pop();
    }

    /**
     * Getter of the width of the string.
     *
     * @return the width.
     */
    public int getStringWidth()
    {
        return (int) (mc.fontRenderer.func_238414_a_(labelText) * scale);
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
