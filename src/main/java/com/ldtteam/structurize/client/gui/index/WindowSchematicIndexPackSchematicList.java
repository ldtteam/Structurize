package com.ldtteam.structurize.client.gui.index;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.View;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.client.gui.AbstractWindowSkeleton;
import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.index.models.Pack;
import com.ldtteam.structurize.index.models.PackSchematic;
import com.ldtteam.structurize.index.packtypes.models.PackTypeSchematicRequirementSeverity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.ldtteam.structurize.api.constants.TranslationConstants.*;
import static com.ldtteam.structurize.client.gui.index.SchematicIndexWindowUtils.*;

/**
 * GUI window that shows the schematics belonging to a single {@link Pack}.
 *
 * <p>Each row is either an {@link ExistingSchematic} — a group of schematics sharing the same
 * path and name, aggregated across all levels — or a {@link RequirementWarning} representing an
 * unmet pack-level validation requirement. Schematic rows display per-severity validation issue
 * counts; requirement rows show an icon indicating whether the requirement is optional or mandatory.
 *
 * <p>Supports text-based filtering on the schematic path and name. Implements
 * {@link PackManager.PackSyncListener} so the list updates automatically when the server pushes
 * new pack data; if the current pack is no longer present, the window closes itself.
 */
public class WindowSchematicIndexPackSchematicList extends AbstractWindowSkeleton implements PackManager.PackSyncListener
{
    private static final String ID_VALIDATE_ALL_BUTTON    = "btn-validate-all";
    private static final String ID_BTN_FILTER_INFO        = "btn-filter-info";
    private static final String ID_BTN_FILTER_WARN        = "btn-filter-warn";
    private static final String ID_BTN_FILTER_ERROR       = "btn-filter-error";
    private static final String ID_SAVE_ALL_BUTTON        = "btn-save-all";
    private static final String ID_DELETE_ALL_BUTTON      = "btn-delete-all";
    private static final String ID_BACK_BUTTON            = "btn-back";
    private static final String ID_CLOSE_BUTTON           = "btn-close";
    private static final String ID_PAGE_TITLE             = "page-title";
    private static final String ID_SEARCH_INPUT           = "schematic-search";
    private static final String ID_SCHEMATICS_LIST        = "schematics";
    private static final String ID_VIEW_SCHEMATIC         = "view-schematic";
    private static final String ID_SCHEMATIC_NAVIGATE     = "schematic-navigate";
    private static final String ID_SCHEMATIC_VALIDATE     = "schematic-validate";
    private static final String ID_SCHEMATIC_SAVE         = "schematic-save";
    private static final String ID_SCHEMATIC_DELETE       = "schematic-delete";
    private static final String ID_SCHEMATIC_LABEL        = "schematic-label";
    private static final String ID_SCHEMATIC_INFO_COUNT   = "schematic-info-count";
    private static final String ID_SCHEMATIC_WARN_COUNT   = "schematic-warn-count";
    private static final String ID_SCHEMATIC_ERROR_COUNT  = "schematic-error-count";
    private static final String ID_VIEW_REQUIREMENT       = "view-requirement";
    private static final String ID_REQUIREMENT_ICON_INFO  = "requirement-icon-info";
    private static final String ID_REQUIREMENT_ICON_ERROR = "requirement-icon-error";
    private static final String ID_REQUIREMENT_LABEL      = "requirement-label";

    private static final int MAX_DISPLAY_VALIDATION_WARNINGS = 5;

    /**
     * A single entry in the schematic list — either an existing schematic group or an unmet requirement.
     */
    private sealed interface SchematicListEntry permits ExistingSchematic, RequirementWarning {}

    /**
     * An existing schematic group, keyed by (path, name), holding all levels for aggregated validation display.
     */
    private record ExistingSchematic(
        String path,
        String name,
        List<PackSchematic> levels) implements SchematicListEntry {}

    /**
     * A pack validation warning to display as a row in the schematic list.
     */
    private record RequirementWarning(
        Component message,
        boolean optional) implements SchematicListEntry {}

    @NotNull
    private Pack pack;

    @NotNull
    private final TextField searchInput;

    @NotNull
    private final ScrollingList schematicsList;

    @NotNull
    private List<SchematicListEntry> entries;

    /**
     * Opens a schematic list window for the given pack.
     *
     * @param pack the pack whose schematics are displayed
     */
    WindowSchematicIndexPackSchematicList(final @NotNull Pack pack)
    {
        super(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "gui/windowschematicindexpackschematiclist.xml"));
        this.pack = pack;

        // Header
        findPaneOfTypeByID(ID_PAGE_TITLE, Text.class).setText(Component.translatable(SCHEMATIC_INDEX_SCHEMATIC_OVERVIEW_PAGE_TITLE, this.pack.name()));
        registerButton(ID_BACK_BUTTON, this::close);
        registerButton(ID_CLOSE_BUTTON, this::closeAll);

        // Subheader
        this.searchInput = findPaneOfTypeByID(ID_SEARCH_INPUT, TextField.class);
        this.searchInput.setHandler(input -> this.updateEntries());
        registerButton(ID_VALIDATE_ALL_BUTTON, () -> SchematicIndexActions.validatePack(pack.id()));
        registerButton(ID_SAVE_ALL_BUTTON, () -> SchematicIndexActions.savePack(pack.id()));
        registerButton(ID_DELETE_ALL_BUTTON, () -> SchematicIndexActions.deletePack(pack.id()));
        simpleTooltip(findPaneOfTypeByID(ID_VALIDATE_ALL_BUTTON, Button.class), Component.translatable(SCHEMATIC_INDEX_SCHEMATIC_BUTTON_VALIDATE_ALL));
        simpleTooltip(findPaneOfTypeByID(ID_SAVE_ALL_BUTTON, Button.class), Component.translatable(SCHEMATIC_INDEX_SCHEMATIC_BUTTON_SAVE_ALL));
        simpleTooltip(findPaneOfTypeByID(ID_DELETE_ALL_BUTTON, Button.class), Component.translatable(SCHEMATIC_INDEX_SCHEMATIC_BUTTON_DELETE_ALL));
        tooltipWithDescription(findPaneOfTypeByID(ID_BTN_FILTER_INFO, Button.class),
            Component.translatable(SCHEMATIC_INDEX_SEVERITY_INFO_TITLE),
            Component.translatable(SCHEMATIC_INDEX_SEVERITY_INFO_DESC));
        tooltipWithDescription(findPaneOfTypeByID(ID_BTN_FILTER_WARN, Button.class),
            Component.translatable(SCHEMATIC_INDEX_SEVERITY_WARN_TITLE),
            Component.translatable(SCHEMATIC_INDEX_SEVERITY_WARN_DESC));
        tooltipWithDescription(findPaneOfTypeByID(ID_BTN_FILTER_ERROR, Button.class),
            Component.translatable(SCHEMATIC_INDEX_SEVERITY_ERROR_TITLE),
            Component.translatable(SCHEMATIC_INDEX_SEVERITY_ERROR_DESC));

        // List items
        registerButton(ID_SCHEMATIC_NAVIGATE, this::handleSchematicNavigation);
        registerButton(ID_SCHEMATIC_VALIDATE, this::handleSchematicValidation);
        registerButton(ID_SCHEMATIC_SAVE, this::handleSchematicSave);
        registerButton(ID_SCHEMATIC_DELETE, this::handleSchematicDelete);

        this.entries = buildEntries();

        this.schematicsList = findPaneOfTypeByID(ID_SCHEMATICS_LIST, ScrollingList.class);
        this.schematicsList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return entries.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                rowPane.findPaneOfTypeByID(ID_VIEW_SCHEMATIC, View.class).off();
                rowPane.findPaneOfTypeByID(ID_VIEW_REQUIREMENT, View.class).off();

                final SchematicListEntry entry = entries.get(index);
                if (entry instanceof ExistingSchematic(String path, String name, List<PackSchematic> levels))
                {
                    rowPane.findPaneOfTypeByID(ID_VIEW_SCHEMATIC, View.class).on();

                    final String label = path.isEmpty() ? name : path + "/" + name;
                    rowPane.findPaneOfTypeByID(ID_SCHEMATIC_LABEL, Text.class).setText(buildFilteredText(label, searchInput.getText()));

                    simpleTooltip(rowPane.findPaneOfTypeByID(ID_SCHEMATIC_VALIDATE, Button.class), Component.translatable(SCHEMATIC_INDEX_LEVEL_BUTTON_VALIDATE_ALL));
                    simpleTooltip(rowPane.findPaneOfTypeByID(ID_SCHEMATIC_SAVE, Button.class), Component.translatable(SCHEMATIC_INDEX_LEVEL_BUTTON_SAVE_ALL));
                    simpleTooltip(rowPane.findPaneOfTypeByID(ID_SCHEMATIC_DELETE, Button.class), Component.translatable(SCHEMATIC_INDEX_LEVEL_BUTTON_DELETE_ALL));

                    renderValidationCountButton(rowPane.findPaneOfTypeByID(ID_SCHEMATIC_INFO_COUNT, Button.class),
                        PackTypeSchematicRequirementSeverity.INFORMATIONAL,
                        levels.stream().flatMap(s -> s.validationState().getIssues(PackTypeSchematicRequirementSeverity.INFORMATIONAL).stream()).toList(),
                        MAX_DISPLAY_VALIDATION_WARNINGS);
                    renderValidationCountButton(rowPane.findPaneOfTypeByID(ID_SCHEMATIC_WARN_COUNT, Button.class),
                        PackTypeSchematicRequirementSeverity.ISSUE,
                        levels.stream().flatMap(s -> s.validationState().getIssues(PackTypeSchematicRequirementSeverity.ISSUE).stream()).toList(),
                        MAX_DISPLAY_VALIDATION_WARNINGS);
                    renderValidationCountButton(rowPane.findPaneOfTypeByID(ID_SCHEMATIC_ERROR_COUNT, Button.class),
                        PackTypeSchematicRequirementSeverity.ERROR,
                        levels.stream().flatMap(s -> s.validationState().getIssues(PackTypeSchematicRequirementSeverity.ERROR).stream()).toList(),
                        MAX_DISPLAY_VALIDATION_WARNINGS);
                }
                else if (entry instanceof RequirementWarning(Component message, boolean optional))
                {
                    rowPane.findPaneOfTypeByID(ID_VIEW_REQUIREMENT, View.class).on();

                    rowPane.findPaneOfTypeByID(ID_REQUIREMENT_ICON_INFO, Image.class).setVisible(optional);
                    rowPane.findPaneOfTypeByID(ID_REQUIREMENT_ICON_ERROR, Image.class).setVisible(!optional);
                    rowPane.findPaneOfTypeByID(ID_REQUIREMENT_LABEL, Text.class).setText(message.plainCopy());
                }
            }
        });

        PackManager.addSyncListener(this);
    }

    @Override
    public void onPackSync()
    {
        final Pack found = PackManager.getClientPack(pack.id());
        if (found == null)
        {
            this.close();
            return;
        }
        this.pack = found;
        updateEntries();
    }

    private void handleSchematicNavigation(final Button button)
    {
        final SchematicListEntry listEntry = entries.get(schematicsList.getListElementIndexByPane(button));
        if (listEntry instanceof ExistingSchematic schematic)
        {
            new WindowSchematicIndexSchematicLevelList(pack, schematic.path(), schematic.name()).openAsLayer();
        }
    }

    private void handleSchematicValidation(final Button button)
    {
        final SchematicListEntry listEntry = entries.get(schematicsList.getListElementIndexByPane(button));
        if (listEntry instanceof ExistingSchematic schematic)
        {
            SchematicIndexActions.validateSchematic(pack.id(), schematic.path, schematic.name);
        }
    }

    private void handleSchematicSave(final Button button)
    {
        final SchematicListEntry listEntry = entries.get(schematicsList.getListElementIndexByPane(button));
        if (listEntry instanceof ExistingSchematic schematic)
        {
            SchematicIndexActions.saveSchematic(pack.id(), schematic.path, schematic.name);
        }
    }

    private void handleSchematicDelete(final Button button)
    {
        final SchematicListEntry listEntry = entries.get(schematicsList.getListElementIndexByPane(button));
        if (listEntry instanceof ExistingSchematic schematic)
        {
            SchematicIndexActions.deleteSchematic(pack.id(), schematic.path, schematic.name);
        }
    }

    private void updateEntries()
    {
        this.entries = buildEntries();
    }

    private List<SchematicListEntry> buildEntries()
    {
        final String query = searchInput.getText();
        final String queryLower = (query == null || query.isEmpty()) ? null : query.toLowerCase(Locale.ROOT);

        // Group existing schematics by (path, name)
        final Map<String, List<PackSchematic>> schematicsByKey = new HashMap<>();
        for (final PackSchematic schematic : pack.schematics())
        {
            schematicsByKey.computeIfAbsent(schematic.path() + " " + schematic.name(), k -> new ArrayList<>()).add(schematic);
        }

        final List<SchematicListEntry> result = new ArrayList<>();

        // Add schematic entries, filtered by search query against path/name
        for (final Map.Entry<String, List<PackSchematic>> entry : schematicsByKey.entrySet())
        {
            final PackSchematic first = entry.getValue().getFirst();
            if (queryLower != null)
            {
                final String label = first.path() + "/" + first.name();
                if (!label.toLowerCase(Locale.ROOT).contains(queryLower))
                {
                    continue;
                }
            }
            result.add(new ExistingSchematic(first.path(), first.name(), entry.getValue()));
        }

        // Append pack-level validation warnings as rows (always shown regardless of search)
        for (final Component message : pack.validationState().getRequiredErrors())
        {
            result.add(new RequirementWarning(message, false));
        }
        for (final Component message : pack.validationState().getOptionalWarnings())
        {
            result.add(new RequirementWarning(message, true));
        }

        return result;
    }
}
