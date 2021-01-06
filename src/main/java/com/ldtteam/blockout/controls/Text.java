package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.properties.Parsers;
import com.ldtteam.blockout.properties.RichText;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.IFormattableTextComponent;

/**
 * Formatted larger textContent area.
 */
public class Text extends Pane
{
    public RichText text;

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

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        super.drawSelf(ms, mx, my);
        text.draw(ms, this, mx, my);
    }
}
