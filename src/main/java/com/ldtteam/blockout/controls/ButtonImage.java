package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.properties.Texture;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;

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
        super();

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
        super(params);

        image = new Texture(params);
        imageHighlight = new Texture(params, "highlight");
        imageDisabled = new Texture(params, "disabled");
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
}
