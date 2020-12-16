package com.ldtteam.blockout.controls;

import java.util.List;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.IReorderingProcessor;
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

    // rendering
    private List<IReorderingProcessor> preparedLabel;
    private int labelWidth;
    private int labelHeight;

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

        // match textColor by default
        hoverColor = params.getColorAttribute("hovercolor", textColor);

        wrap = params.getBooleanAttribute("wrap", wrap);

        if (width == 0)
        {
            width = Math.min(mc.fontRenderer.getStringPropertyWidth(labelText), params.getParentWidth());
        }

        setLabelText(new StringTextComponent(params.getLocalizedStringAttribute("label", "")));
    }

    /**
     * Button textContent getter.
     *
     * @return button textContent.
     */
    @Deprecated
    public String getLabelText()
    {
        return getLabelTextNew().getString();
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
        setLabelText(new StringTextComponent(s));
    }

    public void setLabelText(final IFormattableTextComponent label)
    {
        this.labelText = label;

        if (scale <= 0.0d || label == null || label.getString().isEmpty())
        {
            preparedLabel = null;
            return;
        }

        final int maxWidth = (int) (width / scale);
        preparedLabel = mc.fontRenderer.trimStringToWidth(label, maxWidth);
        if (wrap)
        {
            // + Math.ceil(textScale) is to negate last pixel of vanilla font rendering
            final int maxHeight = (int) (height / scale + Math.ceil(scale));
    
            preparedLabel = preparedLabel.subList(0, Math.min(preparedLabel.size(), maxHeight / this.mc.fontRenderer.FONT_HEIGHT));
            labelWidth = (int) (preparedLabel.stream().mapToInt(mc.fontRenderer::func_243245_a).max().orElse(maxWidth) * scale);
            labelHeight = (int) (Math.min(preparedLabel.size() * this.mc.fontRenderer.FONT_HEIGHT, maxHeight) * scale);
        }
        else
        {
            preparedLabel = preparedLabel.subList(0, 1);
            labelWidth = (int) (mc.fontRenderer.func_243245_a(preparedLabel.get(0)) * scale);
            labelHeight = (int) (this.mc.fontRenderer.FONT_HEIGHT * scale);
        }
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
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
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
        ms.scale((float) scale, (float) scale, 1.0f);

        if (preparedLabel != null)
        {
            // mc.fontRenderer.func_238418_a_(labelText, 0, 0, width, color);
            final Matrix4f matrix4f = ms.getLast().getMatrix();
            int lineShift = 0;
            for (final IReorderingProcessor textLine : preparedLabel)
            {
                final int xOffset;

                if (textAlignment.isRightAligned())
                {
                    xOffset = (int) ((labelWidth - mc.fontRenderer.func_243245_a(textLine) * scale) / scale);
                }
                else if (textAlignment.isHorizontalCentered())
                {
                    xOffset = (int) ((labelWidth - mc.fontRenderer.func_243245_a(textLine) * scale) / 2 / scale);
                }
                else
                {
                    xOffset = 0;
                }

                mc.fontRenderer.func_238415_a_(textLine, xOffset, lineShift, color, matrix4f, false);
                lineShift += mc.fontRenderer.FONT_HEIGHT;
            }
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
        return labelWidth;
    }

    /**
     * Getter of the text height.
     *
     * @return the text height.
     */
    public int getTextHeight()
    {
        return labelHeight;
    }
}
