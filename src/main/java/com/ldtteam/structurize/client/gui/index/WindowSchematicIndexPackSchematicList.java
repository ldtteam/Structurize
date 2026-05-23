package com.ldtteam.structurize.client.gui.index;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.View;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.client.gui.AbstractWindowSkeleton;
import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.index.models.Pack;
import com.ldtteam.structurize.index.models.PackSchematic;
import com.ldtteam.structurize.index.packtypes.models.PackTypeSchematicRequirementSeverity;
import com.ldtteam.structurize.network.messages.ValidatePackMessage;
import com.ldtteam.structurize.network.messages.ValidateSchematicMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.ldtteam.structurize.api.constants.TranslationConstants.PACK_TYPE_VALIDATION_TOOLTIP_MORE;
import static com.ldtteam.structurize.api.constants.TranslationConstants.SCHEMATIC_INDEX_SCHEMATIC_OVERVIEW_PAGE_TITLE;
import static com.ldtteam.structurize.client.gui.index.SchematicIndexWindowUtils.buildFilteredText;

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
 * new pack data; if the current pack is no longer present the window closes itself.
 */
public class WindowSchematicIndexPackSchematicList extends AbstractWindowSkeleton implements PackManager.PackSyncListener
{
    private static final String ID_VALIDATE_BUTTON        = "btn-validate";
    private static final String ID_BACK_BUTTON            = "btn-back";
    private static final String ID_CLOSE_BUTTON           = "btn-close";
    private static final String ID_PAGE_TITLE             = "page-title";
    private static final String ID_SEARCH_INPUT           = "schematic-search";
    private static final String ID_SCHEMATICS_LIST        = "schematics";
    private static final String ID_VIEW_SCHEMATIC         = "view-schematic";
    private static final String ID_SCHEMATIC_NAVIGATE     = "schematic-navigate";
    private static final String ID_SCHEMATIC_VALIDATE     = "schematic-validate";
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

        findPaneOfTypeByID(ID_PAGE_TITLE, Text.class).setText(Component.translatable(SCHEMATIC_INDEX_SCHEMATIC_OVERVIEW_PAGE_TITLE, this.pack.name()));

        registerButton(ID_VALIDATE_BUTTON, this::handlePackValidation);
        registerButton(ID_BACK_BUTTON, this::close);
        registerButton(ID_CLOSE_BUTTON, () -> Minecraft.getInstance().setScreen(null));
        registerButton(ID_SCHEMATIC_NAVIGATE, button -> {});
        registerButton(ID_SCHEMATIC_VALIDATE, this::handleSchematicValidation);

        this.searchInput = findPaneOfTypeByID(ID_SEARCH_INPUT, TextField.class);
        this.searchInput.setHandler(input -> this.updateEntries());

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
                    rowPane.findPaneOfTypeByID(ID_SCHEMATIC_LABEL, Text.class)
                        .setText(buildFilteredText(label, searchInput.getText()));

                    renderValidationButton(rowPane, ID_SCHEMATIC_INFO_COUNT, levels, PackTypeSchematicRequirementSeverity.INFORMATIONAL);
                    renderValidationButton(rowPane, ID_SCHEMATIC_WARN_COUNT, levels, PackTypeSchematicRequirementSeverity.ISSUE);
                    renderValidationButton(rowPane, ID_SCHEMATIC_ERROR_COUNT, levels, PackTypeSchematicRequirementSeverity.ERROR);
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

    private void handlePackValidation()
    {
        new ValidatePackMessage(pack.id()).sendToServer();
    }

    private void handleSchematicValidation(final Button button)
    {
        final SchematicListEntry listEntry = entries.get(schematicsList.getListElementIndexByPane(button));
        if (listEntry instanceof ExistingSchematic schematic)
        {
            new ValidateSchematicMessage(pack.id(), schematic.path, schematic.name).sendToServer();
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

    private void renderValidationButton(final Pane parent, final String buttonId, final List<PackSchematic> levels, final PackTypeSchematicRequirementSeverity severity)
    {
        final List<Component> issues = levels.stream()
            .flatMap(s -> s.validationState().getIssues(severity).stream())
            .toList();

        final Button button = parent.findPaneOfTypeByID(buttonId, Button.class);
        button.setText(Component.literal(issues.isEmpty() ? "" : String.valueOf(issues.size())));

        final AbstractTextBuilder.TooltipBuilder tooltipBuilder = PaneBuilders.tooltipBuilder().hoverPane(button);
        issues.stream().limit(MAX_DISPLAY_VALIDATION_WARNINGS).forEach(tooltipBuilder::appendNL);
        if (issues.size() > MAX_DISPLAY_VALIDATION_WARNINGS)
        {
            tooltipBuilder.appendNL(Component.translatable(PACK_TYPE_VALIDATION_TOOLTIP_MORE, issues.size() - MAX_DISPLAY_VALIDATION_WARNINGS));
        }
        tooltipBuilder.build();
    }
}
