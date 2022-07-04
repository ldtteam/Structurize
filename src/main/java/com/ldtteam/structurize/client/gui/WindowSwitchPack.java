package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.storage.StructurePackMeta;
import com.ldtteam.structurize.storage.StructurePacks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;
import static com.ldtteam.structurize.api.util.constant.WindowConstants.BUTTON_CANCEL;
import static com.ldtteam.structurize.api.util.constant.WindowConstants.BUTTON_SELECT;

/**
 * Window class for the style picker.
 */
public class WindowSwitchPack extends AbstractWindowSkeleton
{
    private static final String WINDOW_TAG_TOOL    = ":gui/windowswitchpack.xml";

    /**
     * The parent window that opened this one.
     */
    private final Supplier<BOWindow> prevWindow;

    /**
     * Levels scrolling list.
     */
    private ScrollingList  packList;

    /**
     * List of packs.
     */
    private List<StructurePackMeta> packMetas;

    /**
     * Constructor for this window.
     * @param prevWindow the origin window.
     */
    public WindowSwitchPack(final Supplier<BOWindow> prevWindow)
    {
        super(Constants.MOD_ID + WINDOW_TAG_TOOL);
        registerButton(BUTTON_SELECT, this::selectClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);

        this.prevWindow = prevWindow;
    }

    /**
     * On clicking the cancel button.
     */
    public void cancelClicked()
    {
        if (prevWindow == null)
        {
            close();
            return;
        }
        prevWindow.get().open();
    }

    /**
     * On choosing a style.
     * @param button the clicked button.
     */
    public void selectClicked(final Button button)
    {
        if (prevWindow == null)
        {
            close();
            return;
        }

        StructurePacks.selectedPack = packMetas.get(packList.getListElementIndexByPane(button));
        prevWindow.get().open();
    }

    @Override
    public void onOpened()
    {
        while (!StructurePacks.finishedLoading)
        {
            // Wait until finished loading!
        }

        // Here we would query from the online schematic server additional styles then, which, on select, we'd download to the server side.

        packList = findPaneOfTypeByID("packs", ScrollingList.class);
        packMetas = new ArrayList<>(StructurePacks.packMetas.values());

        updatePacks();
        super.onOpened();
    }

    /**
     * Updates the current pack list.
     */
    public void updatePacks()
    {
        packList.enable();
        packList.show();

        packList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return packMetas.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                final StructurePackMeta packMeta = packMetas.get(index);
                rowPane.findPaneOfTypeByID("name", Text.class).setText(new TextComponent(packMeta.getName()));
                rowPane.findPaneOfTypeByID("desc", Text.class).setText(new TextComponent(packMeta.getDesc()));
                StringBuilder author = new StringBuilder("Authors: ");
                for (int i = 0; i < packMeta.getAuthors().size(); i++)
                {
                    author.append(packMeta.getAuthors().get(i));
                    if (i + 1 < packMeta.getAuthors().size())
                    {
                        author.append(", ");
                    }
                }
                rowPane.findPaneOfTypeByID("authors", Text.class).setText(new TextComponent(author.toString()));
                if (!packMeta.getIconPath().isEmpty())
                {
                    rowPane.findPaneOfTypeByID("icon", Image.class).setImage(OutOfJarResourceLocation.of(MOD_ID, packMeta.getPath().resolve(packMeta.getIconPath())), false);
                }
                rowPane.findPaneOfTypeByID("select", Button.class).setTextColor(ChatFormatting.BLACK.getColor());
            }
        });
    }
}
