package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonHandler;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.Parsers;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A DropDownList is a Button which when click display a ScrollingList below it.
 */
public class DropDownList extends View implements ButtonHandler
{
    /**
     * View in which the list will be displayed.
     */
    protected OverlayView overlay;

    /**
     * button to access to the list.
     */
    protected Button button;

    /**
     * List to choose from.
     */
    protected ScrollingList list;
    /**
     * date required to fill the list.
     */
    protected DataProvider dataProvider;

    /**
     * handler for the accept method.
     */
    protected Consumer<DropDownList> handler;

    /**
     * width of the scrolling list, by default it is the same as the DropDownList width.
     */
    protected int dropDownWidth;

    /**
     * maximum height of the scrolling list, by default it is the same as the DropDownList width.
     */
    protected int dropDownHeight;

    /**
     * index of the selected item.
     */
    protected int selectedIndex = -1;

    /**
     * Temporary fix until new release
     */
    protected int dropDownFixX = 0;

    /**
     * Default constructor required by Blockout.
     */
    public DropDownList()
    {
        super();
    }

    /**
     * Constructs a DropDownList from PaneParams.
     *
     * @param params Params for the ScrollingList
     */
    public DropDownList(final PaneParams params)
    {
        super(params);
        dropDownWidth = width;
        dropDownHeight = (width);
        params.applyShorthand("dropDownSize", Parsers.INT, 2, a -> {
            dropDownWidth = a.get(0);
            dropDownHeight = a.get(1);
        });

        dropDownFixX = params.getInteger("dropfixx", dropDownFixX);

        button = Button.construct(params);
        button.putInside(this);

        overlay = new OverlayView();
        overlay.setVisible(false);
        overlay.setPosition(0, 0);

        list = new ScrollingList(params);
        if (params.getInteger("maxContentHeight", 0) != 0)
        {
            list.setMaxHeight(params.getInteger("maxContentHeight", 0));
        }
        list.setSize(dropDownWidth, dropDownHeight);
        list.setPosition((x + width / 2) - dropDownWidth / 2 + dropDownFixX, y + height);
        list.putInside(overlay);
        list.parseChildren(params);

        button.setHandler(this);
    }

    /**
     * handle when the button is clicked on.
     * <p>
     * The list is shown or hidden depending of the previous state.
     *
     * @param buttonIn which have been clicked on.
     */
    public void onButtonClicked(@NotNull final Button buttonIn)
    {
        if (buttonIn == button)
        {
            if (overlay.isVisible())
            {
                close();
            }
            else
            {
                overlay.setSize(this.getWindow().getInteriorWidth(), this.getWindow().getInteriorHeight());
                overlay.putInside(buttonIn.getWindow());
                open();
            }
        }
        else
        {
            onButtonClickedFromList(buttonIn);
        }
    }

    /**
     * close the dropdown list.
     */
    public void close()
    {
        overlay.setVisible(false);
    }

    /**
     * open the dropdown list.
     */
    public void open()
    {
        refreshElementPanes();
        overlay.setVisible(true);
        overlay.setFocus();
    }

    /**
     * handle when a button in the list have been clicked on.
     *
     * @param buttonIn which have been clicked on.
     */
    private void onButtonClickedFromList(final Button buttonIn)
    {
        final int index = list.getListElementIndexByPane(buttonIn);
        setSelectedIndex(index);
        close();
    }

    /**
     * Use the data provider to update all the element panes.
     */
    public void refreshElementPanes()
    {
        list.refreshElementPanes();
        list.setSize(dropDownWidth, Math.min(list.getContentHeight(), dropDownHeight));
    }

    /**
     * get the index of the selected item in the list.
     *
     * @return the index of the selected ietem.
     */
    public int getSelectedIndex()
    {
        return selectedIndex;
    }

    /**
     * set the index of the selected item in the list.
     *
     * @param index of the selected item
     */
    public void setSelectedIndex(final int index)
    {
        if (index < 0 || index >= dataProvider.getElementCount())
        {
            return;
        }
        selectedIndex = index;

        button.setText(dataProvider.getLabelNew(selectedIndex));
        if (handler != null)
        {
            handler.accept(this);
        }
    }

    /**
     * Select the previous Item in the list.
     */
    public void selectPrevious()
    {
        if (dataProvider.getElementCount() == 0)
        {
            setSelectedIndex(0);
        }
        else
        {
            setSelectedIndex((selectedIndex + dataProvider.getElementCount() - 1) % dataProvider.getElementCount());
        }
    }

    /**
     * Select the next item in the list.
     */
    public void selectNext()
    {
        if (dataProvider.getElementCount() == 0)
        {
            setSelectedIndex(0);
        }
        else
        {
            setSelectedIndex((selectedIndex + 1) % dataProvider.getElementCount());
        }
    }

    /**
     * Set the data provider to fill the list.
     *
     * @param p is the data provider for the list.
     */
    public void setDataProvider(final DataProvider p)
    {
        dataProvider = p;
        list.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return dataProvider.getElementCount();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                updateDropDownItem(rowPane, index, dataProvider.getLabelNew(index));
            }
        });

        refreshElementPanes();
    }

    /**
     * Update an pane item in the list.
     *
     * @param rowPane which need the update
     * @param index   of the item
     * @param label   use for this item
     */
    private void updateDropDownItem(@NotNull final Pane rowPane, final int index, final IFormattableTextComponent label)
    {
        final Button choiceButton = rowPane.findPaneOfTypeByID("button", Button.class);
        if (choiceButton != null)
        {
            choiceButton.setText(label);
            choiceButton.setHandler(this);
        }
    }

    @Override
    public void setVisible(final boolean v)
    {
        super.setVisible(v);
        button.setVisible(v);
        list.setVisible(v);
    }

    @Override
    public void setEnabled(final boolean e)
    {
        super.setEnabled(e);
        button.setEnabled(e);
        list.setEnabled(e);
    }

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        button.drawSelf(ms, mx, my);
    }

    @Override
    public void drawSelfLast(final MatrixStack ms, final double mx, final double my)
    {
        button.drawSelfLast(ms, mx, my);
    }

    @Override
    public boolean click(final double mx, final double my)
    {
        return button.click(mx, my);
    }

    @Override
    public boolean canHandleClick(final double mx, final double my)
    {
        return button.canHandleClick(mx, my);
    }

    /**
     * Set the button handler for this button.
     *
     * @param h The new handler.
     */
    public void setHandler(final Consumer<DropDownList> h)
    {
        handler = h;
    }

    /**
     * Interface for a data provider that updates pane scrolling list pane info.
     */
    public interface DataProvider
    {
        int getElementCount();

        @Deprecated
        String getLabel(final int index);

        default IFormattableTextComponent getLabelNew(final int index)
        {
            return new StringTextComponent(getLabel(index));
        }
    }

    @Override
    public boolean isPointInPane(final double mx, final double my)
    {
        return super.isPointInPane(mx, my) || (overlay.isVisible() && list.isPointInPane(mx, my));
    }
}
