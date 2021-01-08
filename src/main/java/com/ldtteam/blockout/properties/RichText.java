package com.ldtteam.blockout.properties;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class RichText extends PropertyGroup
{
    protected double scale = 1.0D;
    protected Alignment alignment = Alignment.MIDDLE_LEFT;

    protected int color = 0xFFFFFF;
    protected int hoverColor = color;
    protected int disabledColor = color;

    protected boolean shadow = false;
    protected boolean wrap = false;

    /**
     * The space between lines, identical if wrapped or forced.
     */
    public int linespace = 0;

    /**
     * The text holder.
     */
    protected List<IFormattableTextComponent> text = new LinkedList<>();

    // rendering
    private List<IReorderingProcessor> preparedText;
    private int renderedWidth;
    private int renderedHeight;

    protected int x = 0;
    protected int y = 0;
    protected int width = 0;
    protected int height = 0;

    /**
     * Constructs a rich text property group from
     * manually provided properties
     * @param align default text alignment
     * @param color default text color
     * @param hoverColor default text color when hovered
     * @param disabledColor default text color if disabled
     * @param shadow if there are shadows by default
     * @param wrap if text wraps be default
     */
    public RichText(
      final Alignment align,
      final int color,
      final int hoverColor,
      final int disabledColor,
      final boolean shadow,
      final boolean wrap)
    {
        super();
        this.alignment = align;
        this.color = color;
        this.hoverColor = hoverColor;
        this.disabledColor = disabledColor;
        this.shadow = shadow;
        this.wrap = wrap;

        // setup
        calcTextRendering();
    }
    public RichText(final PaneParams params)
    {
        this(params, "text");
    }

    public RichText(final PaneParams p, final String prefix)
    {
        super(p, prefix);
        this.applyDefaults();

        alignment = p.enumeration(prefix+"align", Alignment.class, alignment);

        color = p.color(p.hasAnyAttribute("color", prefix+color), color);
        hoverColor = p.hasAttribute(prefix+"hovercolor") ? p.color(prefix+"hovercolor", hoverColor) : color;
        disabledColor = p.hasAttribute(prefix+"disabledcolor") ? p.color(prefix+"disabledcolor", disabledColor) : color;

        shadow = p.bool(prefix+"shadow", shadow);
        wrap = p.bool(prefix+"wrap", wrap);
        scale = p.numeral(prefix+"scale", scale);
        linespace = p.numeral("linespace", linespace);

        p.shorthand(prefix+"box", Parsers.INT, 2, a -> {
            width = a.get(0);
            height = a.get(1);
        });

        text = p.multiline(p.hasAnyAttribute("text", "label"), text);

        calcTextRendering();
    }

    @Override
    public void applyDefaults()
    {
        scale = 1.0D;
        alignment = Alignment.MIDDLE_LEFT;

        color = 0xFFFFFF;
        hoverColor = color;
        disabledColor = color;

        shadow = false;
        wrap = false;

        linespace = 0;
    }

    @Override
    public void draw(final MatrixStack ms, final Pane pane, final double mx, final double my)
    {
        if (preparedText.isEmpty())
        {
            return;
        }

        if (width == 0 || height == 0)
        {
            width = pane.getWidth();
            height = pane.getHeight();
            calcTextRendering();
        }

        final int color = pane.isEnabled() ? (pane.isPointInPane(mx, my) ? hoverColor : this.color) : disabledColor;

        int x = this.x;
        int y = this.y;

        if (alignment.isRightAligned())
        {
            x += width - renderedWidth;
        }
        else if (alignment.isHorizontalCentered())
        {
            x += (width - renderedWidth) / 2;
        }

        if (alignment.isBottomAligned())
        {
            y += height - renderedHeight;
            y += Math.ceil(scale);
        }
        else if (alignment.isVerticalCentered())
        {
            y += (height - renderedHeight) / 2;
            y += Math.ceil(scale);
        }

        ms.push();
        ms.translate(pane.getX() + x, pane.getY() + y, 0.0d);
        ms.scale((float) scale, (float) scale, 1.0f);

        final Matrix4f matrix4f = ms.getLast().getMatrix();
        int lineShift = 0;
        for (final IReorderingProcessor row : preparedText)
        {
            final int xOffset;

            if (alignment.isRightAligned())
            {
                xOffset = (int) ((renderedWidth - mc.fontRenderer.func_243245_a(row) * scale) / scale);
            }
            else if (alignment.isHorizontalCentered())
            {
                xOffset = (int) ((renderedWidth - mc.fontRenderer.func_243245_a(row) * scale) / 2 / scale);
            }
            else
            {
                xOffset = 0;
            }

            mc.fontRenderer.func_238415_a_(row, xOffset, lineShift, color, matrix4f, shadow);
            lineShift += mc.fontRenderer.FONT_HEIGHT + linespace;
        }

        ms.pop();
    }

    protected void calcTextRendering()
    {
        if (scale <= 0.0d || text == null || getRawText().isEmpty())
        {
            preparedText = Collections.emptyList();
            return;
        }

        final int maxWidth = (int) (width / scale);
        if (wrap)
        {
            // + 1 is to negate last pixel of vanilla font rendering
            final int maxHeight = (int) (height / scale) + 1;

            preparedText = text.stream()
                .flatMap(line -> mc.fontRenderer.trimStringToWidth(line, maxWidth).stream())
                .collect(Collectors.toList())
                .subList(0, Math.min(preparedText.size(), maxHeight / (mc.fontRenderer.FONT_HEIGHT + linespace)));
            renderedWidth = (int) (preparedText.stream().mapToInt(mc.fontRenderer::func_243245_a).max().orElse(maxWidth) * scale);
            renderedHeight = (int) (Math.min(preparedText.size() * mc.fontRenderer.FONT_HEIGHT, maxHeight) * scale);
        }
        else
        {
            preparedText = mc.fontRenderer.trimStringToWidth(this.get(), maxWidth).subList(0, 1);
            renderedWidth = (int) (mc.fontRenderer.func_243245_a(preparedText.get(0)) * scale);
            renderedHeight = (int) (mc.fontRenderer.FONT_HEIGHT * scale);
        }
    }

    public void set(int i, final IFormattableTextComponent text)
    {
        this.text.set(i, text);
        calcTextRendering();
    }

    public void set(List<IFormattableTextComponent> text)
    {
        this.text = text;
    }

    public void set(String text)
    {
        this.text = Parsers.MULTILINE.apply(text);
    }

    public String getRawText()
    {
        return text.stream()
          .map(ITextComponent::getString)
          .collect(Collectors.joining("\\n"))
          .trim();
    }

    public Alignment getTextAlignment()
    {
        return alignment;
    }

    public void setTextAlignment(final Alignment textAlignment)
    {
        this.alignment = textAlignment;
    }

    public double getTextScale()
    {
        return scale;
    }

    public void setTextScale(final double textScale)
    {
        this.scale = textScale;
        calcTextRendering();
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
        this.color = textColor;
        this.disabledColor = textDisabledColor;
        this.hoverColor = textHoverColor;
    }

    public boolean shouldWrap()
    {
        return wrap;
    }

    public void setWrap(final boolean textWrap)
    {
        this.wrap = textWrap;
        calcTextRendering();
    }

    /**
     * Sets text rendering box.
     * Is automatically shrunken to element width and height minus text offsets.
     *
     * @param w horizontal size
     * @param h vertical size
     */
    public void setRenderBox(final Pane pane, final int w, final int h)
    {
        this.width = MathHelper.clamp(w, 0, pane.getWidth() - x);
        this.height = MathHelper.clamp(h, 0, pane.getHeight() - y);
        calcTextRendering();
    }

    public void setRenderBox(final Pane pane, final int x, final int y, final int w, final int h)
    {
        this.x = x;
        this.y = y;
        setRenderBox(pane, w, h);
    }

    public IFormattableTextComponent get()
    {
        return text.isEmpty() ? new StringTextComponent("") : text.get(0);
    }

    public IFormattableTextComponent get(int i)
    {
        return text.get(i);
    }

    public void set(final IFormattableTextComponent text)
    {
        this.text.clear();
        this.text.add(text);
        calcTextRendering();
    }

    @Deprecated
    public void fromString(final String text)
    {
        this.text = Parsers.MULTILINE.apply(text);
    }

    public int getRenderedTextWidth()
    {
        return renderedWidth;
    }

    public int getRenderedTextHeight()
    {
        return renderedHeight;
    }

    public List<IReorderingProcessor> getPreparedText()
    {
        return preparedText;
    }
}
