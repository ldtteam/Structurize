package com.ldtteam.structurize.client.gui.index;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.AbstractTextBuilder;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.client.gui.AbstractWindowSkeleton;
import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.index.models.Pack;
import com.ldtteam.structurize.index.packtypes.models.PackTypeSchematicRequirementSeverity;
import com.ldtteam.structurize.network.messages.ValidatePackMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static com.ldtteam.structurize.api.constants.TranslationConstants.PACK_TYPE_VALIDATION_TOOLTIP_MORE;
import static com.ldtteam.structurize.client.gui.index.SchematicIndexWindowUtils.buildFilteredText;

/**
 * GUI window that displays the list of schematic packs known to the client.
 *
 * <p>Supports text-based filtering and shows per-pack validation issue counts for each severity level.
 * From this window the player can navigate into a pack's schematic list or trigger a server-side
 * re-validation, or open the pack creation dialog.
 *
 * <p>Implements {@link PackManager.PackSyncListener} to refresh automatically whenever the server
 * pushes an updated pack list.
 */
public class WindowSchematicIndexPackList extends AbstractWindowSkeleton implements PackManager.PackSyncListener
{
    private static final String ID_ADD_BUTTON         = "btn-add";
    private static final String ID_CLOSE_BUTTON       = "btn-close";
    private static final String ID_INPUT_SEARCH_PACKS = "pack-search";
    private static final String ID_PACKS_LIST         = "packs";
    private static final String ID_PACK_NAVIGATE      = "pack-navigate";
    private static final String ID_PACK_NAME          = "pack-name";
    private static final String ID_PACK_VALIDATE      = "pack-validate";
    private static final String ID_PACK_INFO_COUNT    = "pack-info-count";
    private static final String ID_PACK_WARN_COUNT    = "pack-warn-count";
    private static final String ID_PACK_ERROR_COUNT   = "pack-error-count";

    private static final int MAX_DISPLAY_VALIDATION_WARNINGS = 5;

    @NotNull
    private final TextField searchInput;

    @NotNull
    private final ScrollingList packsList;

    @NotNull
    private List<Pack> filteredPacks;

    public WindowSchematicIndexPackList()
    {
        super(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "gui/windowschematicindexpacklist.xml"));
        this.filteredPacks = PackManager.getClientPacks();

        registerButton(ID_ADD_BUTTON, () -> new WindowSchematicIndexPackCreate().openAsLayer());
        registerButton(ID_CLOSE_BUTTON, this::close);

        registerButton(ID_PACK_NAVIGATE, this::handlePackNavigation);
        registerButton(ID_PACK_VALIDATE, this::handlePackValidation);

        this.searchInput = findPaneOfTypeByID(ID_INPUT_SEARCH_PACKS, TextField.class);
        this.searchInput.setHandler(input -> this.updatePacks());

        this.packsList = findPaneOfTypeByID(ID_PACKS_LIST, ScrollingList.class);
        this.packsList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return filteredPacks.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                final Pack pack = filteredPacks.get(index);

                rowPane.findPaneOfTypeByID(ID_PACK_NAME, Text.class).setText(buildFilteredText(pack.name(), searchInput.getText()));

                renderPackRequirementButton(rowPane, ID_PACK_INFO_COUNT, aggregatePackIssues(pack, PackTypeSchematicRequirementSeverity.INFORMATIONAL));
                renderPackRequirementButton(rowPane, ID_PACK_WARN_COUNT, aggregatePackIssues(pack, PackTypeSchematicRequirementSeverity.ISSUE));
                renderPackRequirementButton(rowPane, ID_PACK_ERROR_COUNT, aggregatePackIssues(pack, PackTypeSchematicRequirementSeverity.ERROR));
            }
        });

        PackManager.addSyncListener(this);
    }

    private void handlePackNavigation(final Button button)
    {
        final Pack pack = this.filteredPacks.get(this.packsList.getListElementIndexByPane(button));
        new WindowSchematicIndexPackSchematicList(pack).openAsLayer();
    }

    private void handlePackValidation(final Button button)
    {
        final Pack pack = this.filteredPacks.get(this.packsList.getListElementIndexByPane(button));
        new ValidatePackMessage(pack.id()).sendToServer();
    }

    private List<Component> aggregatePackIssues(final Pack pack, final PackTypeSchematicRequirementSeverity severity)
    {
        final Stream<Component> packIssues = switch (severity)
        {
            case INFORMATIONAL -> pack.validationState().getOptionalWarnings().stream();
            case ISSUE -> Stream.empty();
            case ERROR -> pack.validationState().getRequiredErrors().stream();
        };

        final Stream<Component> schematicIssues = pack.schematics().stream()
            .flatMap(s -> s.validationState().getIssues(severity).stream());

        return Stream.concat(packIssues, schematicIssues).toList();
    }


    private void renderPackRequirementButton(final Pane parent, final String buttonId, final List<Component> validationIssues)
    {
        final Button button = parent.findPaneOfTypeByID(buttonId, Button.class);
        button.setText(Component.literal(validationIssues.isEmpty() ? "" : String.valueOf(validationIssues.size())));

        final AbstractTextBuilder.TooltipBuilder tooltipBuilder = PaneBuilders.tooltipBuilder().hoverPane(button);
        validationIssues.stream().limit(MAX_DISPLAY_VALIDATION_WARNINGS).forEach(tooltipBuilder::appendNL);
        if (validationIssues.size() > MAX_DISPLAY_VALIDATION_WARNINGS)
        {
            tooltipBuilder.appendNL(Component.translatable(PACK_TYPE_VALIDATION_TOOLTIP_MORE, validationIssues.size() - MAX_DISPLAY_VALIDATION_WARNINGS));
        }
        tooltipBuilder.build();
    }

    @Override
    public void onPackSync()
    {
        updatePacks();
    }

    private void updatePacks()
    {
        final String query = searchInput.getText();
        if (query == null || query.isEmpty())
        {
            filteredPacks = PackManager.getClientPacks();
        }
        else
        {
            filteredPacks = PackManager.getClientPacks()
                .stream()
                .filter(pack -> pack.name().toLowerCase(Locale.ROOT).contains(searchInput.getText().toLowerCase(Locale.ROOT)))
                .toList();
        }
    }
}
