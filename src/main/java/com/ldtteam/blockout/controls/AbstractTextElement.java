package com.ldtteam.blockout.controls;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
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
    protected List<IFormattableTextComponent> text;

    // rendering
    protected List<IReorderingProcessor> preparedText;
    protected int renderedTextWidth;
    protected int renderedTextHeight;

    protected int textOffsetX = 0;
    protected int textOffsetY = 0;
    protected int textWidth = width;
    protected int textHeight = height;

    /**
     * Creates a stock text element using the programmed defaults
     */
    public AbstractTextElement()
    {
        this(
          DEFAULT_TEXT_ALIGNMENT,
          DEFAULT_TEXT_COLOR,
          DEFAULT_TEXT_COLOR,
          DEFAULT_TEXT_COLOR,
          DEFAULT_TEXT_SHADOW,
          DEFAULT_TEXT_WRAP
        );
    }

    /**
     * Creates an instance of the abstractTextElement.
     */
    public AbstractTextElement(
      final Alignment defaultTextAlignment,
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

        setText((IFormattableTextComponent) StringTextComponent.EMPTY);

        // setup
        recalcTextRendering();
    }

    public AbstractTextElement(final PaneParams params)
    {
        this(
          params,
          DEFAULT_TEXT_ALIGNMENT,
          DEFAULT_TEXT_COLOR,
          DEFAULT_TEXT_COLOR,
          DEFAULT_TEXT_COLOR,
          DEFAULT_TEXT_SHADOW,
          DEFAULT_TEXT_WRAP
        );
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

        textAlignment = params.getEnum("textalign", Alignment.class, defaultTextAlignment);
        if (params.hasAttribute("color"))
        {
            // provide fast way to set all colors
            setColors(params.getColor("color", defaultTextColor));
        }
        else
        {
            textColor = params.getColor("textcolor", defaultTextColor);
            textHoverColor = params.getColor("texthovercolor", defaultTextHoverColor);
            textDisabledColor = params.getColor("textdisabledcolor", defaultTextDisabledColor);
        }
        textShadow = params.getBoolean("shadow", defaultTextShadow);
        textWrap = params.getBoolean("wrap", defaultTextWrap);
        textScale = params.getDouble("textscale", textScale);
        textLinespace = params.getInteger("linespace", textLinespace);

        // both label and text are allowed to merge label and text elements
        setText(params.getTextComponent(params.hasAnyAttribute("label", "text"), new StringTextComponent("")));

        // setup
        recalcTextRendering();
    }

    protected void recalcTextRendering()
    {
        if (textScale <= 0.0d || textWidth < 1 || textHeight < 1 || isTextEmpty())
        {
            preparedText = Collections.emptyList();
            return;
        }

        final int maxWidth = (int) (textWidth / textScale);
        preparedText = text.stream()
            .flatMap(textBlock -> textBlock == StringTextComponent.EMPTY ? Stream.of(textBlock.func_241878_f())
                : mc.fontRenderer.trimStringToWidth(textBlock, maxWidth).stream())
            .collect(Collectors.toList());
        if (textWrap)
        {
            // + Math.ceil(textScale) / textScale is to negate last pixel of vanilla font rendering
            final int maxHeight = (int) (textHeight / textScale) + 1;
            final int lineHeight = this.mc.fontRenderer.FONT_HEIGHT + textLinespace;

            preparedText = preparedText.subList(0, Math.min(preparedText.size(), maxHeight / lineHeight));
            renderedTextWidth = (int) (preparedText.stream().mapToInt(mc.fontRenderer::func_243245_a).max().orElse(maxWidth) * textScale);
            renderedTextHeight = (int) ((Math.min(preparedText.size() * lineHeight, maxHeight) - 1 - textLinespace) * textScale);
        }
        else
        {
            preparedText = preparedText.subList(0, 1);
            renderedTextWidth = (int) (mc.fontRenderer.func_243245_a(preparedText.get(0)) * textScale);
            renderedTextHeight = (int) ((this.mc.fontRenderer.FONT_HEIGHT - 1) * textScale);
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

        int offsetX = textOffsetX;
        int offsetY = textOffsetY;

        if (textAlignment.isRightAligned())
        {
            offsetX += textWidth - renderedTextWidth;
        }
        else if (textAlignment.isHorizontalCentered())
        {
            offsetX += (textWidth - renderedTextWidth) / 2;
        }

        if (textAlignment.isBottomAligned())
        {
            offsetY += textHeight - renderedTextHeight;
        }
        else if (textAlignment.isVerticalCentered())
        {
            offsetY += (textHeight - renderedTextHeight) / 2;
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
     * @param textColor         Standard textContent color.
     * @param textDisabledColor Disabled textContent color.
     * @param textHoverColor    Hover textContent color.
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

    public void setTextDisabledColor(final int textDisabledColor)
    {
        this.textDisabledColor = textDisabledColor;
    }

    public int getTextLinespace()
    {
        return textLinespace;
    }

    public void setTextLinespace(final int textLinespace)
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

    @Nullable
    public List<IFormattableTextComponent> getTextAsList()
    {
        return text;
    }

    /**
     * @return null if empty, first line otherwise
     */
    @Nullable
    public IFormattableTextComponent getText()
    {
        return isTextEmpty() ? null : text.get(0);
    }

    public void setText(final List<IFormattableTextComponent> text)
    {
        this.text = text;
        recalcTextRendering();
    }

    public void setText(final ITextComponent text)
    {
        setText((IFormattableTextComponent) text);
    }

    public void setText(final IFormattableTextComponent text)
    {
        this.text = Collections.singletonList(text);
        recalcTextRendering();
    }

    public void clearText()
    {
        setText(Collections.emptyList());
    }

    /**
     * @return null if empty, otherwise first line as string
     */
    @Nullable
    public String getTextAsStringStrict()
    {
        return isTextEmpty() ? null : text.get(0).getString();
    }

    /**
     * @return emptyString if empty, otherwise first line as string
     */
    @NotNull
    public String getTextAsString()
    {
        return isTextEmpty() ? "" : text.get(0).getString();
    }

    @Deprecated
    public void setText(final String text)
    {
        setText(new StringTextComponent(text));
    }

    /**
     * @return true if has no text or all lines are empty strings, false otherwise
     */
    public boolean isTextEmpty()
    {
        return text == null || text.stream().allMatch(t -> t.getString().isEmpty());
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

        textWidth = width;
        textHeight = height;
        recalcTextRendering();
    }
}
