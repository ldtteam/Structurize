package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.properties.Parsers;
import com.ldtteam.blockout.properties.RichText;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

/**
 * Formatted larger textContent area.
 */
public class Text extends Pane
{
    protected RichText text;

    /**
     * Create text from xml.
     *
     * @param params xml parameters.
     */
    public Text(final PaneParams params)
    {
        super(params);
        text = new RichText(params);
    }

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
    public Text(
      final Alignment align,
      final int color,
      final int hoverColor,
      final int disabledColor,
      final boolean shadow,
      final boolean wrap)
    {
        super();
        text = new RichText(
          align,
          color,
          hoverColor,
          disabledColor,
          shadow,
          wrap);
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
        return text.getTextScale();
    }

    public void setTextScale(final double textScale)
    {
        this.text.setTextScale(textScale);
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
        text.setColors(textColor, textDisabledColor, textHoverColor);
    }

    public IFormattableTextComponent getText()
    {
        return text.get();
    }

    public void setText(IFormattableTextComponent text)
    {
        this.text.set(text);
    }

    public void setText(String text)
    {
        this.text.set(new StringTextComponent(text));
    }

    public int getTextColor()
    {
        return text.color;
    }

    public void setTextColor(final int textColor)
    {
        text.color = textColor;
    }

    public int getTextHoverColor()
    {
        return text.hoverColor;
    }

    public void setTextHoverColor(final int textHoverColor)
    {
        text.hoverColor = textHoverColor;
    }

    public int getTextDisabledColor()
    {
        return text.disabledColor;
    }

    public void setTextDisabledColor(final int textDisabledColor)
    {
        text.disabledColor = textDisabledColor;
    }

    public int getTextLinespace()
    {
        return text.linespace;
    }

    public void setTextLinespace(final int textLinespace)
    {
        text.linespace = textLinespace;
    }

    public boolean isTextShadow()
    {
        return text.shadow;
    }

    public void setTextShadow(final boolean textShadow)
    {
        text.shadow = textShadow;
    }

    public boolean shouldTextWrap()
    {
        return text.shouldWrap();
    }

    public void setTextWrap(final boolean textWrap)
    {
        this.text.setWrap(textWrap);
    }

    /**
     * Sets text rendering box.
     * Is automatically shrinked to element width and height minus text offsets.
     *
     * @param w horizontal size
     * @param h vertical size
     */
    public void setTextRenderBox(final int w, final int h)
    {
        this.text.setTextRenderBox(w, h);
    }

    public IFormattableTextComponent get()
    {
        return text.get(0);
    }

    public IFormattableTextComponent get(int i)
    {
        return text.get(i);
    }

    public void set(final IFormattableTextComponent text)
    {
        this.text.set(text);
    }

    public void set(int i, final IFormattableTextComponent text)
    {
        this.text.set(i, text);
    }

    public String getRawText()
    {
        return text.getRawText();
    }

    @Deprecated
    public void fromString(final String text)
    {
        this.text.set(Parsers.MULTILINE.apply(text));
    }

    public List<IReorderingProcessor> getPreparedText()
    {
        return text.getPreparedText();
    }

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        super.drawSelf(ms, mx, my);
        text.draw(ms, this, mx, my);
    }
}
