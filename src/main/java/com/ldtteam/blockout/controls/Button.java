package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import org.jetbrains.annotations.NotNull;

/**
 * Base button class.
 * Has a handler for when the button is clicked.
 */
public abstract class Button extends AbstractTextElement
{
    protected ButtonHandler handler;

    /**
     * Default constructor.
     */
    public Button()
    {
        super();
    }

    /**
     * Constructor used when loading from xml.
     *
     * @param params PaneParams from xml file.
     */
    public Button(final PaneParams params)
    {
        super(params);
    }

    /**
     * Construct a button from the parameters according to set out text defaults
     */
    public Button(
      final PaneParams params,
      final Alignment alignment,
      final int enabledColor,
      final int hoverColor,
      final int disabledColor,
      final boolean hasShadow,
      final boolean shouldWrap)
    {
        super(params, alignment, enabledColor, hoverColor, disabledColor, hasShadow, shouldWrap);
    }

    /**
     * Construct a button according to set out text defaults
     */
    public Button(
      final Alignment alignment,
      final int enabledColor,
      final int hoverColor,
      final int disabledColor,
      final boolean hasShadow,
      final boolean shouldWrap)
    {
        super(alignment, enabledColor, hoverColor, disabledColor, hasShadow, shouldWrap);
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
        mc.getSoundManager().play(SimpleSoundInstance.forMusic(SoundEvents.UI_BUTTON_CLICK));

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

    /**
     * Selects and constructs a Button type based on its parameters
     * @param params the patameters
     * @return a freshly constructed Button
     */
    public static Button construct(PaneParams params)
    {
        return params.hasAttribute("source")
          ? new ButtonImage(params)
          : new ButtonVanilla(params);
    }
}
