package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.api.constants.Constants;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Manage windows and their events.
 */
public abstract class AbstractWindowSkeleton extends BOWindow implements ButtonHandler
{
        private final HashMap<String, Consumer<Button>> buttons;

    /**
     * Constructor for the skeleton class of the windows.
     *
     * @param resource Resource location string.
     */
    public AbstractWindowSkeleton(final String resource)
    {
        super(Constants.resLocStruct(resource));
        buttons = new HashMap<>();
    }

    /**
     * Register a button on the window.
     *
     * @param id     Button ID.
     * @param action Consumer with the action to be performed.
     */
    public final void registerButton(final String id, final Runnable action)
    {
        registerButton(id, (button) -> action.run());
    }

    /**
     * Register a button on the window.
     *
     * @param id     Button ID.
     * @param action Consumer with the action to be performed.
     */
    public final void registerButton(final String id, final Consumer<Button> action)
    {
        buttons.put(id, action);
    }

    /**
     * Handle a button clicked event.
     * Find the registered event and execute that.
     * <p>
     * todo: make final once migration is complete
     *
     * @param button the button that was clicked.
     */
    @Override
    public void onButtonClicked(final Button button)
    {
        if (buttons.containsKey(button.getID()))
        {
            buttons.get(button.getID()).accept(button);
        }
        else
        {
            Log.getLogger().warn(this.getClass().getName() + ": Unhandled Button ID:" + button.getID());
        }
    }

    /**
     * Button clicked without an action. Method does nothing.
     *
     * @param ignored Parameter is ignored. Since some actions require a button, we must accept a button parameter.
     */
    public final void doNothing(final Button ignored)
    {
        //do nothing with that event
    }
}
