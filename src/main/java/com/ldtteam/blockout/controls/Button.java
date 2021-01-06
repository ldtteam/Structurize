package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.properties.RichText;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvents;

/**
 * Base button class.
 * Has a handler for when the button is clicked.
 */
public class Button extends Pane
{
    protected ButtonHandler handler;
    public ButtonText text;

    protected Button()
    {
        super();
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
        text = new ButtonText(params) {
            @Override
            public void applyDefaults()
            {
                super.applyDefaults();
                if (params.hasAttribute("source")) color = 0x101010;
            }
        };
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
        text.draw(ms, this, mx, my);
    }

    public static Button construct(PaneParams params)
    {
        return params.hasAttribute("source")
                 ? new ButtonImage(params)
                 : new ButtonVanilla(params);
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
            color = 0xE0E0E0;
            hoverColor = 0xFFFFA0;
            disabledColor = 0xA0A0A0;
        }
    }
}
