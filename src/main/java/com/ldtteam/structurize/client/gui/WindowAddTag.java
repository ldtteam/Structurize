package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonHandler;
import com.ldtteam.blockout.controls.TextField;
import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.api.util.constant.Constants;

/**
 * Popup window for adding tags
 */
public class WindowAddTag extends Window implements ButtonHandler
{
    /**
     * Button names
     */
    private static final String BUTTON_DONE    = "done";
    private static final String INPUT_NAME     = "name";
    private static final String WINDOW_ADD_TAG = ":gui/windowinputtag.xml";

    /**
     * Parent window to open on finish
     */
    private final WindowPlaceholderblock parent;

    public WindowAddTag(final WindowPlaceholderblock parent)
    {
        super(Constants.MOD_ID + WINDOW_ADD_TAG);
        this.parent = parent;
    }

    @Override
    public void onButtonClicked(final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            final String tag = findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();
            if (!tag.isEmpty())
            {
                parent.addTag(tag);
            }
        }

        close();
        parent.open();
    }
}
