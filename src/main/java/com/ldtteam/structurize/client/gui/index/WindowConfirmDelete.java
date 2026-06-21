package com.ldtteam.structurize.client.gui.index;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.client.gui.AbstractWindowSkeleton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Modal confirmation dialog for destructive index operations (pack and schematic deletion).
 *
 * <p>Displays a message describing the action and two buttons: confirm and cancel. The confirm
 * button is intentionally disabled for the first second after the dialog opens to prevent
 * accidental confirmation of rapid delete-button presses.
 */
class WindowConfirmDelete extends AbstractWindowSkeleton
{
    private static final String ID_CONFIRM_TEXT   = "confirm-text";
    private static final String ID_CONFIRM_BUTTON = "confirm";
    private static final String ID_CANCEL_BUTTON  = "cancel";

    private static final long CONFIRM_DELAY_MS = 3000L;

    private final Runnable onConfirm;
    private final long enableAt;

    /**
     * @param message   the text to display in the dialog body
     * @param onConfirm callback to invoke when the user confirms the action
     */
    WindowConfirmDelete(final Component message, final Runnable onConfirm)
    {
        super(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "gui/dialogconfirmdelete.xml"));
        this.onConfirm = onConfirm;
        this.enableAt = System.currentTimeMillis() + CONFIRM_DELAY_MS;

        findPaneOfTypeByID(ID_CONFIRM_TEXT, Text.class).setText(message);

        registerButton(ID_CANCEL_BUTTON, this::close);
        registerButton(ID_CONFIRM_BUTTON, this::confirm);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        final Button confirmButton = findPaneOfTypeByID(ID_CONFIRM_BUTTON, Button.class);
        if (!confirmButton.isEnabled() && System.currentTimeMillis() >= enableAt)
        {
            confirmButton.enable();
        }
    }

    private void confirm()
    {
        onConfirm.run();
        close();
    }
}
