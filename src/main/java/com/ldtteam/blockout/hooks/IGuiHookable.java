package com.ldtteam.blockout.hooks;

import com.ldtteam.blockout.hooks.TriggerMechanism.Type;
import com.ldtteam.blockout.views.Window;

/**
 * Provides callbacks for fine tuning hook mechanism and for window setuping.
 */
public interface IGuiHookable
{
    /**
     * <p>
     * Gets called whenever a new gui is about to be opened.
     * </p><p>
     * Returning false means that this trigger check the window won't be opened nor rendered.
     * This also means that you will receive a lot of invokations for the same trigger type
     * (as long as the trigger condition is satisfied), so this method shouldn't be performance heavy.
     * </p>
     *
     * @param triggerType type of trigger condition
     * @return false if the window shouldn't be opened, true otherwise
     */
    default boolean shouldOpen(final Type triggerType)
    {
        return true;
    }

    /**
     * Gets called whenever a new gui is opened.
     * Logically equals to Window.onOpened() override.
     *
     * @param window      new window
     * @param triggerType type of trigger condition
     * @see {@link #shouldOpen(Type)}
     */
    default void onOpen(final Window window, final Type triggerType)
    {
    }

    /**
     * Gets called whenever a current gui's trigger stops being satisfied and expiration time expired.
     * Logically equals to Window.onClosed() override.
     * 
     * @param window      window about to be closed
     * @param triggerType type of trigger condition
     */
    default void onClose(final Window window, final Type triggerType)
    {
    }
}
