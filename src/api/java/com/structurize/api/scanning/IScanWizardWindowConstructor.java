package com.structurize.api.scanning;

import com.structurize.blockout.controls.Button;
import com.structurize.blockout.views.Window;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

/**
 * Construction handler used to build the window that represents the scanning wizard.
 */
@SideOnly(Side.CLIENT)
public interface IScanWizardWindowConstructor
{

    /**
     * Register a button on the window.
     *
     * @param id     Button ID.
     * @param action Consumer with the action to be performed.
     */
    default void registerButton(final String id, final Runnable action)
    {
        registerButton(id, (button -> action.run()));
    }

    /**
     * Register a button on the window.
     *
     * @param id     Button ID.
     * @param action Consumer with the action to be performed.
     */
    void registerButton(final String id, final Consumer<Button> action);

    /**
     * The scan wizard window.
     *
     * @return The window for the scan wizard.
     */
    Window getWindow();

}
