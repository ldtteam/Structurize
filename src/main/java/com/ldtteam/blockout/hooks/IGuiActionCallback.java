package com.ldtteam.blockout.hooks;

import com.ldtteam.blockout.hooks.TriggerMechanism.Type;
import com.ldtteam.blockout.views.Window;

/**
 * Callback for gui open/close action.
 */
@FunctionalInterface
public interface IGuiActionCallback<T>
{
    /**
     * Default impl with no action.
     */
    public static IGuiActionCallback<?> NO_ACTION = (t, w, tt) -> {};

    /**
     * @param thing       instance of Forge-registered type
     * @param window      window attached to instance above
     * @param triggerType trigger condition type
     */
    void onAction(final T thing, final Window window, final Type triggerType);

    /**
     * @return default impl with no action
     */
    @SuppressWarnings("unchecked")
    public static <T> IGuiActionCallback<T> noAction()
    {
        return (IGuiActionCallback<T>) NO_ACTION;
    }
}
