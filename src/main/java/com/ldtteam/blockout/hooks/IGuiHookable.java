package com.ldtteam.blockout.hooks;

import com.ldtteam.blockout.hooks.TriggerMechanism.Type;
import com.ldtteam.blockout.views.Window;

/**
 * Target things can implement this.
 */
public interface IGuiHookable
{
    /**
     * Gets called whenever a new gui is opened.
     *
     * @param window      new window
     * @param triggerType how was the window opened
     */
    default void onOpen(final Window window, final Type triggerType)
    {
    }

    /**
     * Gets called whenever a current gui's trigger stops being satisfied and expiration time expired.
     * 
     * @param window      closed window
     * @param triggerType how was the window opened
     */
    default void onClose(final Window window, final Type triggerType)
    {
    }
}
