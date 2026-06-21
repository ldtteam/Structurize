package com.ldtteam.structurize.client.gui.index;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.structurize.api.Registries;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.client.gui.AbstractWindowSkeleton;
import com.ldtteam.structurize.index.packtypes.models.PackType;
import com.ldtteam.structurize.network.messages.index.CreatePackMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * GUI window for creating a new schematic pack.
 *
 * <p>Presents a name text field and a drop-down list populated from the registered
 * {@link PackType}s. The "create" button is only enabled when a non-blank name and a valid type
 * are selected. On submission a {@link CreatePackMessage} is sent to the server.
 */
public class WindowSchematicIndexPackCreate extends AbstractWindowSkeleton
{
    private static final String ID_CLOSE_BUTTON  = "btn-close";
    private static final String ID_BACK_BUTTON   = "btn-back";
    private static final String ID_CANCEL_BUTTON = "btn-cancel";
    private static final String ID_CREATE_BUTTON = "btn-create";
    private static final String ID_INPUT_NAME    = "pack-name-input";
    private static final String ID_INPUT_TYPE    = "pack-type-input";

    private final List<Holder.Reference<PackType>> packTypes;

    @NotNull
    private final TextField nameInput;

    @NotNull
    private final DropDownList typeInput;

    /**
     * Opens the pack creation window, populating the type drop-down from the registry.
     */
    WindowSchematicIndexPackCreate()
    {
        super(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "gui/windowschematicindexpackcreate.xml"));

        this.packTypes = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.SCHEMATIC_INDEX_PACK_TYPES).holders().toList();

        this.nameInput = findPaneOfTypeByID(ID_INPUT_NAME, TextField.class);
        this.typeInput = findPaneOfTypeByID(ID_INPUT_TYPE, DropDownList.class);
        this.typeInput.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return packTypes.size();
            }

            @Override
            public MutableComponent getLabel(final int index)
            {
                return packTypes.get(index).value().getName().copy();
            }
        });

        registerButton(ID_CLOSE_BUTTON, this::closeAll);
        registerButton(ID_BACK_BUTTON, this::close);
        registerButton(ID_CANCEL_BUTTON, this::close);
        registerButton(ID_CREATE_BUTTON, this::submit);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        final String name = this.nameInput.getText();
        final int selectedIndex = this.typeInput.getSelectedIndex();

        findPaneOfTypeByID(ID_CREATE_BUTTON, Button.class).setEnabled(!(name == null || name.isBlank() || selectedIndex == -1));
    }

    private void submit()
    {
        final String name = this.nameInput.getText();
        final ResourceKey<PackType> type = this.packTypes.get(this.typeInput.getSelectedIndex()).getKey();

        new CreatePackMessage(name, type).sendToServer();
        close();
    }
}
