package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.OverlayView;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.function.ObjIntConsumer;

/**
 * Manage windows and their events.
 */
public class DialogDoneCancel extends OverlayView implements ButtonHandler
{
    /**
     * buttonid when cancel is pressed.
     */
    public static final int CANCEL = 0;

    /**
     * buttonid when done is pressed.
     */
    public static final int DONE = 1;

    /**
     * Resource suffix.
     */
    private static final String DIALOG_OK_CANCEL_SUFFIX = ":gui/dialogdonecancel.xml";

    /**
     * Label for the title of the dialog.
     */
    protected final Text titleLabel;

    /**
     * Text for the text content of the dialog.
     */
    protected final Text contentText;

    /**
     * Done button.
     */
    protected final Button doneButton;

    /**
     * Cancel button.
     */
    protected final Button cancelButton;

    /**
     * Handler for the onCloseDialog event.
     */
    protected ObjIntConsumer<DialogDoneCancel> handler;

    /**
     * Constructor for the DialogDoneCancel class.
     *
     * @param window in which the dialog will be displayed
     */
    public DialogDoneCancel(final BOWindow window)
    {
        super();
        putInside(window);
        setPosition(0, 0);
        setSize(window.getInteriorWidth(), window.getInteriorHeight());
        off();

        Loader.createFromXMLFile(new ResourceLocation(Constants.MOD_ID + DIALOG_OK_CANCEL_SUFFIX), this);
        titleLabel = findPaneOfTypeByID("title", Text.class);
        contentText = findPaneOfTypeByID("textcontent", Text.class);
        doneButton = findPaneOfTypeByID("done", Button.class);
        cancelButton = findPaneOfTypeByID("cancel", Button.class);
        doneButton.setHandler(this);
        cancelButton.setHandler(this);
    }

    /**
     * Get the title of the dialog.
     *
     * @return title for the dialog
     */
    public MutableComponent getTitleNew()
    {
        return titleLabel.getText();
    }

    /**
     * Set the title of the dialog.
     *
     * @param title for the dialog
     */
    public void setTitle(final MutableComponent title)
    {
        titleLabel.setText(title);
    }

    /**
     * Get the textual content of the dialog.
     *
     * @return The textual content displayed in the dialog
     */
    public String getTextContent()
    {
        return contentText.getTextAsString();
    }

    /**
     * Set the textual content of the dialog.
     *
     * @param content to display in the dialog
     */
    public void setTextContent(final MutableComponent content)
    {
        contentText.setText(content);
    }

    /**
     * When a button have been cicked on.
     *
     * @param button which have been clicked on.
     */
    public void onButtonClicked(final Button button)
    {
        close();
        if (handler == null)
        {
            Log.getLogger().error("DialogDoneCancel does not have a handler.");
            return;
        }
        if (button == doneButton)
        {
            handler.accept(this, DONE);
        }
        else
        {
            handler.accept(this, CANCEL);
        }
    }

    @Override
    public void setVisible(final boolean visible)
    {
        super.setVisible(visible);
        if (!visible)
        {
            putInside(null);
        }
    }

    /**
     * Open the dialog.
     */
    public void open()
    {
        on();
    }

    /**
     * Close the dialog.
     */
    public void close()
    {
        off();
    }

    /**
     * Set the button handler for this button.
     *
     * @param h The new handler.
     */
    public void setHandler(final ObjIntConsumer<DialogDoneCancel> h)
    {
        handler = h;
    }
}
