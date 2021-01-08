package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.properties.RichText;
import com.ldtteam.blockout.properties.Texture;
import com.ldtteam.blockout.properties.TextureRepeatable;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;

/**
 * Base button class. Has a handler for when the button is clicked.
 */
public class Button extends Pane
{
    /**
     * Texture map that contains the button texture.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/widgets.png");

    private static final int TEXTURE_INNER_U_OFFSET = 2;
    private static final int TEXTURE_INNER_V_OFFSET = 2;
    private static final int TEXTURE_INNER_U_WIDTH  = 196;
    private static final int TEXTURE_INNER_V_HEIGHT = 15;

    private static final int DEFAULT_BUTTON_WIDTH  = 200;
    private static final int DEFAULT_BUTTON_HEIGHT = 20;
    private static final int DEFAULT_BUTTON_SQUARE = 20;

    private static final int ENABLED_COLOR  = 0xE0E0E0;
    private static final int HOVER_COLOR    = 0xFFFFA0;
    private static final int DISABLED_COLOR = 0xA0A0A0;

    private static final int ENABLED_TEXTURE_V  = 66;
    private static final int HOVER_TEXTURE_V    = 86;
    private static final int DISABLED_TEXTURE_V = 46;

    protected static final TextureRepeatable VANILLA_TEXTURE = new TextureRepeatable(
      TEXTURE,
      0, ENABLED_TEXTURE_V,
      DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT,
      TEXTURE_INNER_U_OFFSET, TEXTURE_INNER_V_OFFSET,
      TEXTURE_INNER_U_WIDTH, TEXTURE_INNER_V_HEIGHT
    );

    public    ButtonText        text;
    protected ButtonHandler     handler;
    protected TextureRepeatable image = VANILLA_TEXTURE;
    protected TextureRepeatable imageHighlight;
    protected TextureRepeatable imageDisabled;

    private boolean useVanilla = true;

    protected Button()
    {
        super();
        width = DEFAULT_BUTTON_SQUARE;
        height = DEFAULT_BUTTON_SQUARE;
    }

    /**
     * Default constructor.
     */
    public Button(
      final Alignment alignment,
      final int color,
      final int hoverColor,
      final int disabledColor,
      final boolean hasShadow,
      final boolean shouldWrap)
    {
        text = new ButtonText(
          alignment,
          color,
          hoverColor,
          disabledColor,
          hasShadow,
          shouldWrap);
    }

    /**
     * Constructor used when loading from xml.
     *
     * @param params PaneParams from xml file.
     */
    public Button(final PaneParams params)
    {
        super(params);

        if (params.hasAttribute("source"))
        {
            image = new TextureRepeatable(params);
            imageHighlight = new TextureRepeatable(params, "highlight");
            imageDisabled = new TextureRepeatable(params, "disabled");
            useVanilla = false;
        }

        text = new ButtonText(params)
        {
            @Override
            public void applyDefaults()
            {
                super.applyDefaults();
                if (params.hasAttribute("source"))
                {
                    color = 0x101010;
                }
            }
        };
    }


    public void setImage(final Texture tex)
    {
        image = (TextureRepeatable) tex;
    }

    public void setImage(final ResourceLocation loc)
    {
        this.image.setImage(loc);
    }

    public void setImageHighlight(final Texture tex)
    {
        imageHighlight = (TextureRepeatable) tex;
    }

    public void setImageDisabled(final Texture tex)
    {
        imageDisabled = (TextureRepeatable) tex;
    }

    /**
     * Set the button handler for this button.
     *
     * @param h The new handler.
     */
    public void setHandler(final ButtonHandler h)
    {
        handler = h;
    }

    /**
     * Play click sound and find the proper handler.
     *
     * @param mx mouse X coordinate, relative to Pane's top-left
     * @param my mouse Y coordinate, relative to Pane's top-left
     */
    @Override
    public boolean handleClick(final double mx, final double my)
    {
        mc.getSoundHandler().play(SimpleSound.music(SoundEvents.UI_BUTTON_CLICK));

        ButtonHandler delegatedHandler = handler;

        if (delegatedHandler == null)
        {
            // If we do not have a designated handler, find the closest ancestor that is a Handler
            for (Pane p = parent; p != null; p = p.getParent())
            {
                if (p instanceof ButtonHandler)
                {
                    delegatedHandler = (ButtonHandler) p;
                    break;
                }
            }
        }

        if (delegatedHandler != null)
        {
            delegatedHandler.onButtonClicked(this);
        }
        return true;
    }

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        super.drawSelf(ms, mx, my);

        if (useVanilla)
        {
            image.setOffset(0,
              enabled
                ? isPointInPane(mx, my) ? HOVER_TEXTURE_V : ENABLED_TEXTURE_V
                : DISABLED_TEXTURE_V);

            image.draw(ms, this, mx, my);
        }
        else
        {
            TextureRepeatable bind = image;

            if (!enabled)
            {
                if (!imageDisabled.hasSource()) return;
                bind = imageDisabled;
            }
            else if (isPointInPane(mx, my) && imageHighlight.hasSource())
            {
                bind = imageHighlight;
            }

            bind.draw(ms, this, !enabled && !imageDisabled.hasSource());
        }

        text.draw(ms, this, mx, my);
    }

    public static class ButtonText extends RichText
    {
        public ButtonText(final Alignment align, final int color, final int hoverColor, final int disabledColor, final boolean shadow, final boolean wrap)
        {
            super(align, color, hoverColor, disabledColor, shadow, wrap);
        }

        public ButtonText(final PaneParams params)
        {
            super(params);
        }

        public ButtonText(final PaneParams p, final String prefix)
        {
            super(p, prefix);
        }

        @Override
        public void applyDefaults()
        {
            super.applyDefaults();

            alignment = Alignment.MIDDLE;
            color = ENABLED_COLOR;
            hoverColor = HOVER_COLOR;
            disabledColor = DISABLED_COLOR;
        }
    }
}
