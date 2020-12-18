package com.ldtteam.blockout.controls;

import java.util.Collections;
import java.util.List;
import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Contains any code common to text controls.
 */
public abstract class AbstractTextElement extends Pane
{
    public static final double DEFAULT_TEXT_SCALE = 1.0d;
    public static final Alignment DEFAULT_TEXT_ALIGNMENT = Alignment.MIDDLE_LEFT;
    public static final int DEFAULT_TEXT_COLOR = 0xffffff; // white
    public static final boolean DEFAULT_TEXT_SHADOW = false;
    public static final boolean DEFAULT_TEXT_WRAP = false;
    public static final int DEFAULT_TEXT_LINESPACE = 0;

    /**
     * The text scale.
     */
    protected double textScale = DEFAULT_TEXT_SCALE;

    /**
     * How the text aligns in it.
     */
    protected Alignment textAlignment = DEFAULT_TEXT_ALIGNMENT;

    /**
     * The standard text color.
     */
    protected int textColor = DEFAULT_TEXT_COLOR;

    /**
     * The hover text color.
     */
    protected int textHoverColor = DEFAULT_TEXT_COLOR;

    /**
     * The disabled text color.
     */
    protected int textDisabledColor = DEFAULT_TEXT_COLOR;

    /**
     * The default state for shadows.
     */
    protected boolean textShadow = DEFAULT_TEXT_SHADOW;

    /**
     * The default state for wrapping.
     */
    protected boolean textWrap = DEFAULT_TEXT_WRAP;

    /**
     * The linespace of the text.
     */
    protected int textLinespace = DEFAULT_TEXT_LINESPACE;

    /**
     * The text holder.
     */
    protected IFormattableTextComponent text;

    // rendering
    private List<IReorderingProcessor> preparedText;
    private int renderedTextWidth;
    private int renderedTextHeight;

    /**
     * Creates an instance of the abstractTextElement.
     */
    public AbstractTextElement(final Alignment defaultTextAlignment,
        final int defaultTextColor,
        final int defaultTextHoverColor,
        final int defaultTextDisabledColor,
        final boolean defaultTextShadow,
        final boolean defaultTextWrap)
    {
        super();

        this.textAlignment = defaultTextAlignment;
        this.textColor = defaultTextColor;
        this.textHoverColor = defaultTextHoverColor;
        this.textDisabledColor = defaultTextDisabledColor;
        this.textShadow = defaultTextShadow;
        this.textWrap = defaultTextWrap;

        text = (IFormattableTextComponent) StringTextComponent.EMPTY;

        // setup
        recalcTextRendering();
    }


    /**
     * Create from xml.
     *
     * @param params xml parameters.
     */
    public AbstractTextElement(final PaneParams params,
        final Alignment defaultTextAlignment,
        final int defaultTextColor,
        final int defaultTextHoverColor,
        final int defaultTextDisabledColor,
        final boolean defaultTextShadow,
        final boolean defaultTextWrap)
    {
        super(params);

        textAlignment = params.getEnumAttribute("textalign", Alignment.class, defaultTextAlignment);
        if (params.hasAttribute("color"))
        {
            // provide fast way to set all colors
            setColors(params.getColorAttribute("color", defaultTextColor));
        }
        else
        {
            textColor = params.getColorAttribute("textcolor", defaultTextColor);
            textHoverColor = params.getColorAttribute("texthovercolor", defaultTextHoverColor);
            textDisabledColor = params.getColorAttribute("textdisabledcolor", defaultTextDisabledColor);
        }
        textShadow = params.getBooleanAttribute("shadow", defaultTextShadow);
        textWrap = params.getBooleanAttribute("wrap", defaultTextWrap);
        textScale = params.getDoubleAttribute("textscale", textScale);
        textLinespace = params.getIntAttribute("linespace", textLinespace);

        // both label and text are allowed to merge label and text elements
        text = new StringTextComponent(params.getLocalizedStringAttribute(params.hasAnyAttribute("label", "text"), ""));

        // setup
        recalcTextRendering();
    }

    protected void recalcTextRendering()
    {
        if (textScale <= 0.0d || text == null || text.getString().isEmpty())
        {
            preparedText = Collections.emptyList();
            return;
        }

        final int maxWidth = (int) (width / textScale);
        preparedText = mc.fontRenderer.trimStringToWidth(text, maxWidth);
        if (textWrap)
        {
            // + Math.ceil(textScale) is to negate last pixel of vanilla font rendering
            final int maxHeight = (int) (height / textScale + Math.ceil(textScale));

            preparedText = preparedText.subList(0, Math.min(preparedText.size(), maxHeight / (this.mc.fontRenderer.FONT_HEIGHT + textLinespace)));
            renderedTextWidth = (int) (preparedText.stream().mapToInt(mc.fontRenderer::func_243245_a).max().orElse(maxWidth) * textScale);
            renderedTextHeight = (int) (Math.min(preparedText.size() * this.mc.fontRenderer.FONT_HEIGHT, maxHeight) * textScale);
        }
        else
        {
            preparedText = preparedText.subList(0, 1);
            renderedTextWidth = (int) (mc.fontRenderer.func_243245_a(preparedText.get(0)) * textScale);
            renderedTextHeight = (int) (this.mc.fontRenderer.FONT_HEIGHT * textScale);
        }
    }

    protected int getTextRenderingColor(final double mx, final double my)
    {
        return isPointInPane(mx, my) ? textHoverColor : textColor;
    }

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        if (preparedText.isEmpty())
        {
            return;
        }

        final int color = enabled ? (isPointInPane(mx, my) ? textHoverColor : textColor) : textDisabledColor;

        int offsetX = 0;
        int offsetY = 0;

        if (textAlignment.isRightAligned())
        {
            offsetX = width - renderedTextWidth;
        }
        else if (textAlignment.isHorizontalCentered())
        {
            offsetX = (width - renderedTextWidth) / 2;
        }

        if (textAlignment.isBottomAligned())
        {
            offsetY = height - renderedTextHeight;
        }
        else if (textAlignment.isVerticalCentered())
        {
            offsetY = (height - renderedTextHeight) / 2;
        }

        ms.push();
        ms.translate(x + offsetX, y + offsetY, 0.0d);
        ms.scale((float) textScale, (float) textScale, 1.0f);

        final Matrix4f matrix4f = ms.getLast().getMatrix();
        int lineShift = 0;
        for (final IReorderingProcessor row : preparedText)
        {
            final int xOffset;

            if (textAlignment.isRightAligned())
            {
                xOffset = (int) ((renderedTextWidth - mc.fontRenderer.func_243245_a(row) * textScale) / textScale);
            }
            else if (textAlignment.isHorizontalCentered())
            {
                xOffset = (int) ((renderedTextWidth - mc.fontRenderer.func_243245_a(row) * textScale) / 2 / textScale);
            }
            else
            {
                xOffset = 0;
            }

            mc.fontRenderer.func_238415_a_(row, xOffset, lineShift, color, matrix4f, textShadow);
            lineShift += mc.fontRenderer.FONT_HEIGHT + textLinespace;
        }

        ms.pop();
    }

    public Alignment getTextAlignment()
    {
        return textAlignment;
    }

    public void setTextAlignment(final Alignment textAlignment)
    {
        this.textAlignment = textAlignment;
    }

    public double getTextScale()
    {
        return textScale;
    }

    public void setTextScale(final double textScale)
    {
        this.textScale = textScale;
        recalcTextRendering();
    }

    /**
     * Set all text colors to the same value.
     *
     * @param color new text colors.
     */
    public void setColors(final int color)
    {
        setColors(color, color, color);
    }

    /**
     * Set all textContent colors.
     *
     * @param textColor Standard textContent color.
     * @param textDisabledColor Disabled textContent color.
     * @param textHoverColor Hover textContent color.
     */
    public void setColors(final int textColor, final int textDisabledColor, final int textHoverColor)
    {
        this.textColor = textColor;
        this.textDisabledColor = textDisabledColor;
        this.textHoverColor = textHoverColor;
    }

    public int getTextColor()
    {
        return textColor;
    }

    public void setTextColor(final int textColor)
    {
        this.textColor = textColor;
    }

    public int getTextHoverColor()
    {
        return textHoverColor;
    }

    public void setTextHoverColor(final int textHoverColor)
    {
        this.textHoverColor = textHoverColor;
    }

    public int getTextDisabledColor()
    {
        return textDisabledColor;
    }

    public void setTextDisabledColor(int textDisabledColor)
    {
        this.textDisabledColor = textDisabledColor;
    }

    public int getTextLinespace()
    {
        return textLinespace;
    }

    public void setTextLinespace(int textLinespace)
    {
        this.textLinespace = textLinespace;
    }

    public boolean isTextShadow()
    {
        return textShadow;
    }

    public void setTextShadow(final boolean textShadow)
    {
        this.textShadow = textShadow;
    }

    public boolean isTextWrap()
    {
        return textWrap;
    }

    public void setTextWrap(final boolean textWrap)
    {
        this.textWrap = textWrap;
        recalcTextRendering();
    }

    public IFormattableTextComponent getText()
    {
        return text;
    }

    public void setText(final IFormattableTextComponent text)
    {
        this.text = text;
        recalcTextRendering();
    }

    public String getTextAsString()
    {
        return text.getString();
    }

    @Deprecated
    public void setText(final String text)
    {
        setText(new StringTextComponent(text));
    }

    public int getRenderedTextWidth()
    {
        return renderedTextWidth;
    }

    public int getRenderedTextHeight()
    {
        return renderedTextHeight;
    }

    public List<IReorderingProcessor> getPreparedText()
    {
        return preparedText;
    }

    @Override
    public void setSize(final int w, final int h)
    {
        super.setSize(w, h);
        recalcTextRendering();
    }
}
