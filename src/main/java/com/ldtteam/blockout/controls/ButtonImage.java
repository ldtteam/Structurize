package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.properties.Parsers;
import com.ldtteam.blockout.properties.Texture;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * Clickable image.
 */
public class ButtonImage extends Button
{
    /**
     * Default size is a small square button.
     */
    private static final int DEFAULT_BUTTON_SIZE = 20;

    protected Texture image;
    protected Texture imageHighlight;
    protected Texture imageDisabled;

    /**
     * Default constructor. Makes a small square button.
     */
    public ButtonImage()
    {
        super(Alignment.MIDDLE, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_SHADOW, DEFAULT_TEXT_WRAP);

        width = DEFAULT_BUTTON_SIZE;
        height = DEFAULT_BUTTON_SIZE;
    }

    /**
     * Constructor called by the xml loader.
     *
     * @param params PaneParams provided in the xml.
     */
    public ButtonImage(final PaneParams params)
    {
        super(params, Alignment.MIDDLE, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_SHADOW, DEFAULT_TEXT_WRAP);

        image = new Texture(params);
        imageHighlight = new Texture(params, "highlight");
        imageDisabled = new Texture(params, "disabled");

        loadTextInfo(params);
    }

    /**
     * Loads the parameters for the button textContent.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadTextInfo(final PaneParams params)
    {
        textColor = params.numeral("textcolor", textColor);
        // match textColor by default
        textHoverColor = params.numeral("texthovercolor", textColor);
        // match textColor by default
        textDisabledColor = params.numeral("textdisabledcolor", textColor);

        params.shorthand("textoffset", Parsers.INT, 2, a -> {
            textOffsetX = a.get(0);
            textOffsetY = a.get(0);
        });

        params.shorthand("textbox", Parsers.INT, 2, a -> {
            textWidth = a.get(0);
            textHeight = a.get(0);
        });

        recalcTextRendering();
    }

    public void setImage(final Texture tex)
    {
        image = tex;
    }

    public void setImage(final ResourceLocation loc)
    {
        this.image.setImage(loc);
    }

    public void setImageHighlight(final Texture tex)
    {
        imageHighlight = tex;
    }

    public void setImageDisabled(final Texture tex)
    {
        imageDisabled = tex;
    }

    /**
     * Draw the button.
     * Decide what image to use, and possibly draw textContent.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        Texture bind = image;

        if (!enabled)
        {
            if (imageDisabled.hasSource())
            {
                bind = imageDisabled;
            }
            else
            {
                return;
            }
        }
        else if (isPointInPane(mx, my) && imageHighlight.hasSource())
        {
            bind = imageHighlight;
        }

        bind.draw(ms, this, !enabled && !imageDisabled.hasSource());

        super.drawSelf(ms, mx, my);
    }

    @Override
    public void setSize(final int w, final int h)
    {
        final int newTextWidth = (int) ((double) (textWidth * w) / width);
        final int newTextHeight = (int) ((double) (textHeight * h) / height);

        super.setSize(w, h);

        textWidth = newTextWidth;
        textHeight = newTextHeight;
        recalcTextRendering();
    }

    /**
     * Sets text offset for rendering, relative to element start.
     * Is automatically shrinked to element width and height.
     *
     * @param textOffsetX left offset
     * @param textOffsetY top offset
     */
    public void setTextOffset(final int textOffsetX, final int textOffsetY)
    {
        this.textOffsetX = MathHelper.clamp(textOffsetX, 0, width);
        this.textOffsetY = MathHelper.clamp(textOffsetY, 0, height);
    }

    /**
     * Sets text rendering box.
     * Is automatically shrinked to element width and height minus text offsets.
     *
     * @param textWidth  horizontal size
     * @param textHeight vertical size
     */
    public void setTextRenderBox(final int textWidth, final int textHeight)
    {
        this.textWidth = MathHelper.clamp(textWidth, 0, width - textOffsetX);
        this.textHeight = MathHelper.clamp(textHeight, 0, height - textOffsetY);
    }
}
