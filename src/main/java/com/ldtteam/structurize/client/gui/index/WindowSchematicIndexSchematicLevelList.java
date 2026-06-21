package com.ldtteam.structurize.client.gui.index;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.ScrollingListContainer.RowSizeModifier;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.client.gui.AbstractWindowSkeleton;
import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.index.models.Pack;
import com.ldtteam.structurize.index.models.PackSchematic;
import com.ldtteam.structurize.index.packtypes.models.PackTypeSchematicRequirementSeverity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

import static com.ldtteam.structurize.api.constants.TranslationConstants.*;
import static com.ldtteam.structurize.client.gui.index.SchematicIndexWindowUtils.*;

/**
 * GUI window that shows the individual levels belonging to a single schematic group (path and name).
 *
 * <p>Each row represents one {@link PackSchematic} level and shows a summary label (level number,
 * positions, anchor) on the top line, with action and validation buttons on the bottom line.
 *
 * <p>Implements {@link PackManager.PackSyncListener} so the list refreshes automatically when the
 * server pushes new pack data. If the parent pack is removed, the window closes itself.
 */
public class WindowSchematicIndexSchematicLevelList extends AbstractWindowSkeleton implements PackManager.PackSyncListener
{
    private static final String ID_VALIDATE_ALL_BUTTON = "btn-validate-all";
    private static final String ID_BTN_FILTER_INFO     = "btn-filter-info";
    private static final String ID_BTN_FILTER_WARN     = "btn-filter-warn";
    private static final String ID_BTN_FILTER_ERROR    = "btn-filter-error";
    private static final String ID_SAVE_ALL_BUTTON     = "btn-save-all";
    private static final String ID_DELETE_ALL_BUTTON   = "btn-delete-all";
    private static final String ID_BACK_BUTTON         = "btn-back";
    private static final String ID_CLOSE_BUTTON        = "btn-close";
    private static final String ID_PAGE_TITLE          = "page-title";
    private static final String ID_LEVELS_LIST         = "levels";
    private static final String ID_LEVEL_LABEL         = "level-label";
    private static final String ID_LEVEL_POS1          = "level-pos1";
    private static final String ID_LEVEL_POS2          = "level-pos2";
    private static final String ID_LEVEL_ANCHOR        = "level-anchor";
    private static final String ID_LEVEL_HIGHLIGHT     = "level-highlight";
    private static final String ID_LEVEL_TELEPORT      = "level-teleport";
    private static final String ID_LEVEL_RELOCATE      = "level-relocate";
    private static final String ID_LEVEL_VALIDATE      = "level-validate";
    private static final String ID_LEVEL_SAVE          = "level-save";
    private static final String ID_LEVEL_DELETE        = "level-delete";
    private static final String ID_LEVEL_INFO_COUNT    = "level-info-count";
    private static final String ID_LEVEL_WARN_COUNT    = "level-warn-count";
    private static final String ID_LEVEL_ERROR_COUNT   = "level-error-count";

    private static final int ROW_HEIGHT_WITH_ANCHOR    = 61;
    private static final int ROW_HEIGHT_WITHOUT_ANCHOR = 48;

    @NotNull
    private Pack pack;

    @NotNull
    private final String schematicPath;

    @NotNull
    private final String schematicName;

    @NotNull
    private final ScrollingList levelsList;

    @NotNull
    private List<PackSchematic> levels;

    /**
     * Opens a level list window for the given schematic group within a pack.
     *
     * @param pack          the pack that owns this schematic
     * @param schematicPath the relative folder path of the schematic (may be empty for root-level)
     * @param schematicName the schematic file name (without extension)
     */
    WindowSchematicIndexSchematicLevelList(final @NotNull Pack pack, final @NotNull String schematicPath, final @NotNull String schematicName)
    {
        super(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "gui/windowschematicindexschematiclevellist.xml"));
        this.pack = pack;
        this.schematicPath = schematicPath;
        this.schematicName = schematicName;

        // Header
        final String label = schematicPath.isEmpty() ? schematicName : schematicPath + "/" + schematicName;
        findPaneOfTypeByID(ID_PAGE_TITLE, Text.class).setText(Component.translatable(SCHEMATIC_INDEX_LEVEL_OVERVIEW_PAGE_TITLE, pack.name(), label));
        registerButton(ID_BACK_BUTTON, this::close);
        registerButton(ID_CLOSE_BUTTON, this::closeAll);

        // Subheader
        registerButton(ID_VALIDATE_ALL_BUTTON, () -> SchematicIndexActions.validateSchematic(pack.id(), schematicPath, schematicName));
        registerButton(ID_SAVE_ALL_BUTTON, () -> SchematicIndexActions.saveSchematic(pack.id(), schematicPath, schematicName));
        registerButton(ID_DELETE_ALL_BUTTON, () -> SchematicIndexActions.deleteSchematic(pack.id(), schematicPath, schematicName));
        simpleTooltip(findPaneOfTypeByID(ID_VALIDATE_ALL_BUTTON, Button.class), Component.translatable(SCHEMATIC_INDEX_LEVEL_BUTTON_VALIDATE_ALL));
        simpleTooltip(findPaneOfTypeByID(ID_SAVE_ALL_BUTTON, Button.class), Component.translatable(SCHEMATIC_INDEX_LEVEL_BUTTON_SAVE_ALL));
        simpleTooltip(findPaneOfTypeByID(ID_DELETE_ALL_BUTTON, Button.class), Component.translatable(SCHEMATIC_INDEX_LEVEL_BUTTON_DELETE_ALL));
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
        registerButton(ID_LEVEL_HIGHLIGHT, this::handleLevelHighlight);
        registerButton(ID_LEVEL_TELEPORT, this::handleLevelTeleport);
        registerButton(ID_LEVEL_RELOCATE, this::handleLevelRelocate);
        registerButton(ID_LEVEL_VALIDATE, this::handleLevelValidation);
        registerButton(ID_LEVEL_SAVE, this::handleLevelSave);
        registerButton(ID_LEVEL_DELETE, this::handleLevelDelete);

        this.levels = buildLevels();

        this.levelsList = findPaneOfTypeByID(ID_LEVELS_LIST, ScrollingList.class);
        this.levelsList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return levels.size();
            }

            @Override
            public void modifyRowSize(final int index, final RowSizeModifier modifier)
            {
                modifier.setHeight(levels.get(index).anchor().isPresent() ? ROW_HEIGHT_WITH_ANCHOR : ROW_HEIGHT_WITHOUT_ANCHOR);
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                final PackSchematic schematic = levels.get(index);

                rowPane.findPaneOfTypeByID(ID_LEVEL_LABEL, Text.class).setText(Component.translatable(SCHEMATIC_INDEX_LEVEL_LABEL, schematic.level()));

                simpleTooltip(rowPane.findPaneOfTypeByID(ID_LEVEL_HIGHLIGHT, Button.class), Component.translatable(SCHEMATIC_INDEX_LEVEL_BUTTON_HIGHLIGHT));
                simpleTooltip(rowPane.findPaneOfTypeByID(ID_LEVEL_TELEPORT, Button.class), Component.translatable(SCHEMATIC_INDEX_LEVEL_BUTTON_TELEPORT));
                simpleTooltip(rowPane.findPaneOfTypeByID(ID_LEVEL_RELOCATE, Button.class), Component.translatable(SCHEMATIC_INDEX_LEVEL_BUTTON_RELOCATE));
                simpleTooltip(rowPane.findPaneOfTypeByID(ID_LEVEL_VALIDATE, Button.class), Component.translatable(SCHEMATIC_INDEX_LEVEL_BUTTON_VALIDATE));
                simpleTooltip(rowPane.findPaneOfTypeByID(ID_LEVEL_SAVE, Button.class), Component.translatable(SCHEMATIC_INDEX_LEVEL_BUTTON_SAVE));
                simpleTooltip(rowPane.findPaneOfTypeByID(ID_LEVEL_DELETE, Button.class), Component.translatable(SCHEMATIC_INDEX_LEVEL_BUTTON_DELETE));

                final boolean hasErrors = !schematic.validationState().getIssues(PackTypeSchematicRequirementSeverity.ERROR).isEmpty();
                rowPane.findPaneOfTypeByID(ID_LEVEL_SAVE, Button.class).setEnabled(!hasErrors);

                renderValidationCountButton(rowPane.findPaneOfTypeByID(ID_LEVEL_INFO_COUNT, Button.class),
                    PackTypeSchematicRequirementSeverity.INFORMATIONAL,
                    schematic.validationState().getIssues(PackTypeSchematicRequirementSeverity.INFORMATIONAL),
                    Integer.MAX_VALUE);
                renderValidationCountButton(rowPane.findPaneOfTypeByID(ID_LEVEL_WARN_COUNT, Button.class),
                    PackTypeSchematicRequirementSeverity.ISSUE,
                    schematic.validationState().getIssues(PackTypeSchematicRequirementSeverity.ISSUE),
                    Integer.MAX_VALUE);
                renderValidationCountButton(rowPane.findPaneOfTypeByID(ID_LEVEL_ERROR_COUNT, Button.class),
                    PackTypeSchematicRequirementSeverity.ERROR,
                    schematic.validationState().getIssues(PackTypeSchematicRequirementSeverity.ERROR),
                    Integer.MAX_VALUE);

                rowPane.findPaneOfTypeByID(ID_LEVEL_POS1, Text.class).setText(Component.translatable(SCHEMATIC_INDEX_LEVEL_POS1, formatPosString(schematic.pos1())));
                rowPane.findPaneOfTypeByID(ID_LEVEL_POS2, Text.class).setText(Component.translatable(SCHEMATIC_INDEX_LEVEL_POS2, formatPosString(schematic.pos2())));

                final Text anchorText = rowPane.findPaneOfTypeByID(ID_LEVEL_ANCHOR, Text.class);
                schematic.anchor().ifPresentOrElse(a -> {
                    anchorText.on();
                    anchorText.setText(Component.translatable(SCHEMATIC_INDEX_LEVEL_ANCHOR, formatPosString(a)));
                }, anchorText::off);
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
        this.levels = buildLevels();
    }

    private void handleLevelHighlight(final Button button)
    {
        final PackSchematic schematic = levels.get(levelsList.getListElementIndexByPane(button));
        SchematicIndexActions.highlightSchematicLevel(schematic);
        closeAll();
    }

    private void handleLevelTeleport(final Button button)
    {
        final PackSchematic schematic = levels.get(levelsList.getListElementIndexByPane(button));
        SchematicIndexActions.teleportToSchematicLevel(schematic);
        closeAll();
    }

    private void handleLevelRelocate(final Button button)
    {
        final PackSchematic schematic = levels.get(levelsList.getListElementIndexByPane(button));
        SchematicIndexActions.relocateSchematicLevel(pack.id(), schematic.path(), schematic.name(), schematic.level());
    }

    private void handleLevelValidation(final Button button)
    {
        final PackSchematic schematic = levels.get(levelsList.getListElementIndexByPane(button));
        SchematicIndexActions.validateSchematic(pack.id(), schematic.path(), schematic.name(), schematic.level());
    }

    private void handleLevelSave(final Button button)
    {
        final PackSchematic schematic = levels.get(levelsList.getListElementIndexByPane(button));
        SchematicIndexActions.saveSchematic(pack.id(), schematic.path(), schematic.name(), schematic.level());
    }

    private void handleLevelDelete(final Button button)
    {
        final PackSchematic schematic = levels.get(levelsList.getListElementIndexByPane(button));
        SchematicIndexActions.deleteSchematic(pack.id(), schematic.path(), schematic.name(), schematic.level());
    }

    private List<PackSchematic> buildLevels()
    {
        return pack.schematics()
            .stream()
            .filter(s -> s.path().equals(schematicPath) && s.name().equals(schematicName))
            .sorted(Comparator.comparingInt(PackSchematic::level))
            .toList();
    }

    private static String formatPosString(final BlockPos pos)
    {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }
}
