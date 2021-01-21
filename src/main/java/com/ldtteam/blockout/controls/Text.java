package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.PaneParams;

/**
 * Formatted larger textContent area.
 */
public class Text extends AbstractTextElement
{
    /**
     * Standard constructor which instantiates the textField.
     */
    public Text()
    {
        super();
        setTextWrap(true);
        // Required default constructor.
    }

    /**
     * Create text from xml.
     *
     * @param params xml parameters.
     */
    public Text(final PaneParams params)
    {
        super(params, DEFAULT_TEXT_ALIGNMENT, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_SHADOW, true);
    }
}