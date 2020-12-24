package com.ldtteam.blockout.hooks;

import com.ldtteam.blockout.hooks.TriggerMechanism.Type;
import com.ldtteam.blockout.views.Window;

/**
 * Callback for gui open/close action.
 */
@FunctionalInterface
public interface IGuiActionCallback<T>
{
    void onAction(final T thing, final Window window, final Type triggerType);
}
