package com.ldtteam.blockout.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

/**
 * Reusable builder for multiline text elements.
 */
public abstract class AbstractTextBuilder<P extends AbstractTextElement, R extends AbstractTextBuilder<P, R>>
{
    private final Supplier<P> paneFactory;

    private int defaultColor = 0;
    private int color = 0;
    private boolean bold = false;
    private boolean italic = false;
    private boolean underlined = false;
    private boolean strikeThrough = false;
    private boolean obfuscated = false;
    private ClickEvent clickEvent = null;
    private String insertionEvent = null;

    private IFormattableTextComponent currentComponent;
    private final List<IFormattableTextComponent> text = new ArrayList<>();
    private int lastParagraphStart = 0;

    @SuppressWarnings("unchecked")
    private R thiz = (R) this;

    protected AbstractTextBuilder(final Supplier<P> paneFactory, final int defaultColor)
    {
        this.paneFactory = paneFactory;
        this.color = defaultColor;
        this.defaultColor = defaultColor;
    }

    // =============== TEXT ===============

    /**
     * Appends new line symbol plus the given text in this order.
     *
     * @param text text to append
     */
    public R appendNL(final ITextComponent text)
    {
        return appendNL((IFormattableTextComponent) text);
    }

    /**
     * Appends new line symbol plus the given text in this order.
     *
     * @param text text to append
     */
    public R appendNL(final IFormattableTextComponent text)
    {
        newLine();
        append(text);
        return thiz;
    }

    /**
     * Appends the given text to the current text line.
     * Basically sentence appending.
     *
     * @param text text to append
     */
    public R append(final ITextComponent text)
    {
        return append((IFormattableTextComponent) text);
    }

    /**
     * Appends the given text to the current text line.
     * Basically sentence appending.
     *
     * @param text text to append
     */
    public R append(final IFormattableTextComponent text)
    {
        currentComponent = currentComponent == null ? text : currentComponent.append(text);
        return thiz;
    }

    /**
     * Appends new line symbol if there is any text in the current line.
     */
    public R newLine()
    {
        if (currentComponent != null)
        {
            text.add(currentComponent);
            currentComponent = null;
        }
        return thiz;
    }

    /**
     * Appends <code>count</code> new line symbols.
     * Ends current line if there is any.
     * Works like appending <code>count</code> empty lines.
     */
    public R emptyLines(final int count)
    {
        newLine();
        for (int i = 0; i < count; i++)
        {
            text.add((IFormattableTextComponent) StringTextComponent.EMPTY);
        }
        return thiz;
    }

    /**
     * Ends the current paragraph by adding new line symbol and applying + resetting style.
     */
    public R paragraphBreak()
    {
        return paragraphBreak(false);
    }

    /**
     * Ends the current paragraph by adding new line symbol and applying + resetting style.
     *
     * @param forceStyle if true replace all styles since last paragraph break, if false replace all {@link Style#EMPTY} only
     */
    public R paragraphBreak(final boolean forceStyle)
    {
        newLine();

        final Style style = new Style(Color.toVanilla(color), bold, italic, underlined, strikeThrough, obfuscated, clickEvent, null, insertionEvent, null);
        for (int i = lastParagraphStart; i < text.size(); i++)
        {
            if (forceStyle || text.get(i).getStyle().equals(Style.EMPTY))
            {
                text.get(i).setStyle(style);
            }
        }
        lastParagraphStart = text.size();

        resetStyle();

        return thiz;
    }

    // =============== FORMATTING ===============

    /**
     * Resets style of current paragraph and current text line.
     *
     * @see #paragraphBreak()
     */
    public R resetStyle()
    {
        color = defaultColor;
        bold = false;
        italic = false;
        underlined = false;
        strikeThrough = false;
        obfuscated = false;
        clickEvent = null;
        insertionEvent = null;
        return thiz;
    }

    /**
     * Sets every style thing according given style. If null calls {@link #resetStyle()}
     */
    public R style(final Style style)
    {
        if (style == null)
        {
            return resetStyle();
        }

        color = style.getColor().getValue();
        bold = style.isBold();
        italic = style.isItalic();
        underlined = style.isUnderlined();
        strikeThrough = style.isStrikethrough();
        obfuscated = style.isObfuscated();
        clickEvent = style.getClickEvent();
        insertionEvent = style.getInsertion();
        return thiz;
    }

    /**
     * Process given {@link TextFormatting}. If null calls {@link #resetStyle()}
     */
    public R style(final TextFormatting textFormatting)
    {
        if (textFormatting == null)
        {
            return resetStyle();
        }

        switch (textFormatting)
        {
            case BOLD:
                return bold();

            case ITALIC:
                return italic();

            case OBFUSCATED:
                return obfuscated();

            case RESET:
                return resetStyle();

            case STRIKETHROUGH:
                return strikeThrough();

            case UNDERLINE:
                return underlined();

            default:
                if (!textFormatting.isColor())
                {
                    throw new IllegalArgumentException("Unknown non-color textformatting.");
                }
                return color(textFormatting.getColor());
        }
    }

    /**
     * Sets color according to vanilla formatting system.
     * Valid input is anything between 0-9 and a-f/A-F, anything else resets the color to default.
     *
     * @param code char representing vanilla color code
     */
    public R colorVanillaCode(final char code)
    {
        final TextFormatting tf = TextFormatting.getByCode(code);
        return color(tf == null || tf.getColor() == null ? defaultColor : tf.getColor());
    }

    /**
     * Tries to set color according the given human-readable color name.
     * If no color with this name is found in vanilla or our color list then the color remains unchanged.
     *
     * @param name human-readable color name
     */
    public R colorName(final String name)
    {
        final TextFormatting tf = TextFormatting.getByName(name);
        return color(Color.getByName(name, tf.isColor() ? tf.getColor() : color));
    }

    /**
     * Parses color string using BlockOut xml parser.
     *
     * @param colorIn any valid xml color format
     */
    public R colorParse(final String colorIn)
    {
        return color(Color.parse(colorIn, color));
    }

    public R color(final int color)
    {
        this.color = color;
        return thiz;
    }

    public R bold()
    {
        return bold(true);
    }

    public R bold(final boolean bold)
    {
        this.bold = bold;
        return thiz;
    }

    public R italic()
    {
        return italic(true);
    }

    public R italic(final boolean italic)
    {
        this.italic = italic;
        return thiz;
    }

    public R underlined()
    {
        return underlined(true);
    }

    public R underlined(final boolean underlined)
    {
        this.underlined = underlined;
        return thiz;
    }

    public R strikeThrough()
    {
        return strikeThrough(true);
    }

    public R strikeThrough(final boolean strikeThrough)
    {
        this.strikeThrough = strikeThrough;
        return thiz;
    }

    public R obfuscated()
    {
        return obfuscated(true);
    }

    public R obfuscated(final boolean obfuscated)
    {
        this.obfuscated = obfuscated;
        return thiz;
    }

    /**
     * Currently not used.
     */
    public R clickEvent(final ClickEvent clickEvent)
    {
        this.clickEvent = clickEvent;
        return thiz;
    }

    /**
     * Currently not used.
     */
    public R insertionEvent(final String insertionEvent)
    {
        this.insertionEvent = insertionEvent;
        return thiz;
    }

    // =============== Builders ===============

    /**
     * Builds the pane.
     *
     * @return new pane with text set according to this builder
     */
    public P build()
    {
        paragraphBreak();

        final P pane = paneFactory.get();
        pane.setText(this.getText());
        return pane;
    }

    /**
     * @return unique array list with current text
     */
    public List<IFormattableTextComponent> getText()
    {
        return new ArrayList<>(text); // copy, so different elements are not backed by the same list
    }

    public static class TooltipBuilder extends AbstractTextBuilder<Tooltip, TooltipBuilder>
    {
        private Pane hoverPane;

        @SuppressWarnings("deprecation")
        public TooltipBuilder()
        {
            super(Tooltip::new, Tooltip.DEFAULT_TEXT_COLOR);
        }

        /**
         * The given pane hover function will show/hide this tooltip.
         *
         * @param hoverPane the pane to be applied on hover
         */
        public TooltipBuilder hoverPane(final Pane hoverPane)
        {
            this.hoverPane = hoverPane;
            return this;
        }

        @Override
        public Tooltip build()
        {
            if (hoverPane == null)
            {
                throw new IllegalStateException("No hover pane specified.");
            }
            if (hoverPane.getWindow() == null)
            {
                throw new IllegalStateException("Hover pane does not have parent window specified.");
            }

            final Tooltip tooltipPane = super.build();
            hoverPane.setHoverPane(tooltipPane);
            return tooltipPane;
        }
    }

    public static class TextBuilder extends AbstractTextBuilder<Text, TextBuilder>
    {
        public TextBuilder()
        {
            super(Text::new, Text.DEFAULT_TEXT_COLOR);
        }
    }
}
