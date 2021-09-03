package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import net.minecraft.resources.ResourceLocation;

/**
 * Window for a town hall name entry.
 */
public class WindowStructureNameEntry extends BOWindow implements ButtonHandler
{
    private static final String BUTTON_DONE                    = "done";
    private static final String BUTTON_CANCEL                  = "cancel";
    private static final String INPUT_NAME                     = "name";
    private static final String STRUCTURE_NAME_RESOURCE_SUFFIX = ":gui/windowstructurenameentry.xml";

    private final StructureName structureName;
    private final TextField     inputName;

    /**
     * Constructor for a structure rename entry window.
     *
     * @param s {@link StructureName}
     */
    public WindowStructureNameEntry(final StructureName s)
    {
        super(new ResourceLocation(Constants.MOD_ID + STRUCTURE_NAME_RESOURCE_SUFFIX));
        this.structureName = s;
        inputName = findPaneOfTypeByID(INPUT_NAME, TextField.class);
    }

    @Override
    public void onOpened()
    {
        inputName.setText(structureName.getStyle() + '/' + structureName.getSchematic());
    }

    @Override
    public void onButtonClicked(final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            final String name = inputName.getText();
            if (!name.isEmpty())
            {
                final StructureName newStructureName = Structures.renameScannedStructure(structureName, name);
                if (newStructureName != null)
                {
                    Settings.instance.setStructureName(newStructureName.toString());
                }
            }
        }
        else if (!button.getID().equals(BUTTON_CANCEL))
        {
            return;
        }

        close();
        Structurize.proxy.openBuildToolWindow(null);
    }
}
