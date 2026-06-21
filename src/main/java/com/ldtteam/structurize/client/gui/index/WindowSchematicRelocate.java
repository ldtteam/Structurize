package com.ldtteam.structurize.client.gui.index;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.client.gui.AbstractBlueprintManipulationWindow;
import com.ldtteam.structurize.network.messages.BuildToolPlacementMessage;
import com.ldtteam.structurize.network.messages.index.RelocateSchematicLevelMessage;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.structurize.api.constants.Constants.GROUNDSTYLE_RELATIVE;
import static com.ldtteam.structurize.api.constants.GUIConstants.BUTTON_SWITCH_STYLE;
import static com.ldtteam.structurize.api.constants.WindowConstants.BUILD_TOOL_RESOURCE_SUFFIX;

/**
 * Placement GUI opened when relocating a schematic index entry.
 *
 * <p>The blueprint is loaded in memory from the schematic's stored bounding box (by the server)
 * and placed into the {@link #PREVIEW_KEY} rendering slot before this window opens. Pack selection
 * and all other build-tool navigation are not shown — the user only positions and confirms.
 *
 * <p>On confirmation the blocks are placed at the chosen location and the {@link
 * com.ldtteam.structurize.index.models.PackSchematic} bounding box is updated to match.
 * On cancel nothing happens.
 */
public class WindowSchematicRelocate extends AbstractBlueprintManipulationWindow
{
    public static final String PREVIEW_KEY = "relocate";

    @NotNull
    private final String packId;
    @NotNull
    private final String schematicPath;
    @NotNull
    private final String schematicName;
    private final int    level;

    /**
     * Opens the relocation window for the given schematic level.
     *
     * @param packId        the pack identifier
     * @param schematicPath the relative folder path of the schematic (may be empty)
     * @param schematicName the schematic file name (without extension)
     * @param level         the specific level to relocate (1-based)
     * @param startPos      initial placement position (typically the player's position)
     */
    public WindowSchematicRelocate(
        @NotNull final String packId,
        @NotNull final String schematicPath,
        @NotNull final String schematicName,
        final int level,
        @NotNull final BlockPos startPos)
    {
        super(Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX, startPos, GROUNDSTYLE_RELATIVE, PREVIEW_KEY);
        this.packId = packId;
        this.schematicPath = schematicPath;
        this.schematicName = schematicName;
        this.level = level;
        findPaneOfTypeByID(BUTTON_SWITCH_STYLE, Button.class).hide();
    }

    @Override
    public void onClosed()
    {
        RenderingCache.removeBlueprint(PREVIEW_KEY);
        super.onClosed();
    }

    @Override
    protected void cancelClicked()
    {
        close();
    }

    @Override
    protected void handlePlacement(final BuildToolPlacementMessage.HandlerType type, final String id)
    {
        final BlueprintPreviewData previewData = RenderingCache.getOrCreateBlueprintPreviewData(PREVIEW_KEY);
        final BlockPos anchorPos = previewData.getPos();
        final RotationMirror rotationMirror = previewData.getRotationMirror();

        if (anchorPos == null)
        {
            return;
        }

        new RelocateSchematicLevelMessage(packId, schematicPath, schematicName, level, anchorPos, rotationMirror).sendToServer();
        close();
    }

    @Override
    protected void confirmClicked()
    {
        // Skip the survival-handler selection flow — relocation always uses Complete placement.
        handlePlacement(BuildToolPlacementMessage.HandlerType.Complete, "");
    }
}
