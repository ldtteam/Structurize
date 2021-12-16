package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonHandler;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.network.messages.OperationHistoryMessage;
import com.ldtteam.structurize.network.messages.UndoRedoMessage;
import com.ldtteam.structurize.util.TickedWorldOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.api.util.constant.WindowConstants.*;
import static com.ldtteam.structurize.client.gui.WindowScan.WHITE;

public class WindowUndoRedo extends AbstractWindowSkeleton implements ButtonHandler
{
    /**
     * The list of the last operations done for undo/redo
     */
    public static List<Tuple<String, Integer>> lastOperations = new ArrayList<>();

    /**
     * Resource scrolling list.
     */
    private final ScrollingList    operationsList;
    private       DialogDoneCancel confirmDeleteDialog = null;

    /**
     * Constructor for the WindowUndo class.
     */
    public WindowUndoRedo()
    {
        super(Constants.MOD_ID + ":gui/windowundoredo.xml");
        registerButton("cancel", this::cancel);
        registerButton(BUTTON_UNDO, b -> undoRedoClicked(b, true));
        registerButton(BUTTON_REDO, b -> undoRedoClicked(b, false));
        operationsList = findPaneOfTypeByID(LIST_OPERATIONS, ScrollingList.class);
        lastOperations = new ArrayList<>();

        if (!Minecraft.getInstance().player.isCreative())
        {
            close();
        }
    }

    private void undoRedoClicked(final Button button, boolean undo)
    {
        final int index = operationsList.getListElementIndexByPane(button);
        final Tuple<String, Integer> operation = lastOperations.get(index);
        Network.getNetwork().sendToServer(new UndoRedoMessage(operation.getB(), undo));
        close();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateOperationsList();
    }

    /**
     * Updates the operations list
     */
    private void updateOperationsList()
    {
        operationsList.enable();
        operationsList.show();

        operationsList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return lastOperations.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Tuple<String, Integer> resource = lastOperations.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID("operationname", Text.class);
                resourceLabel.setText(resource.getA());
                resourceLabel.setColors(WHITE);

                if (resource.getA().indexOf(TickedWorldOperation.OperationType.UNDO.toString()) == 0)
                {
                    rowPane.findPaneOfTypeByID("redo", Button.class).hide();
                }
                if (!Minecraft.getInstance().player.isCreative())
                {
                    rowPane.findPaneOfTypeByID("undo", Button.class).hide();
                    rowPane.findPaneOfTypeByID("redo", Button.class).hide();
                }
            }
        });
    }

    private void cancel()
    {
        close();
    }

    /**
     * Open the dialog.
     */
    @Override
    public void open()
    {
        super.open();
        setVisible(true);
        Network.getNetwork().sendToServer(new OperationHistoryMessage());
    }
}
