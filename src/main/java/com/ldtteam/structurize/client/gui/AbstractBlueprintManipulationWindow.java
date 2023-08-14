package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Utils;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintTagUtils;
import com.ldtteam.structurize.client.ModKeyMappings;
import com.ldtteam.structurize.config.BlueprintRenderSettings;
import com.ldtteam.structurize.network.messages.BuildToolPlacementMessage;
import com.ldtteam.structurize.network.messages.SyncSettingsToServer;
import com.ldtteam.structurize.storage.ISurvivalBlueprintHandler;
import com.ldtteam.structurize.storage.SurvivalBlueprintHandlers;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.util.LanguageHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;

import static com.ldtteam.structurize.api.util.constant.Constants.*;
import static com.ldtteam.structurize.api.util.constant.GUIConstants.*;
import static com.ldtteam.structurize.api.util.constant.WindowConstants.*;

/**
 * BuildTool window.
 */
public abstract class AbstractBlueprintManipulationWindow extends AbstractWindowSkeleton
{
    /**
     * The id of the blueprint type in here.
     */
    private final String bluePrintId;

    /**
     * Settings scrolling list.
     */
    protected ScrollingList settingsList;

    /**
     * Placement scrolling list.
     */
    private ScrollingList placementOptionsList;

    /**
     * Ground style of the caller.
     */
    protected int groundstyle;

    /**
     * How long the UI is open.
     */
    private int openTicks = 0;

    /**
     * Creates a window build tool.
     * This requires X, Y and Z coordinates.
     * If a structure is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used.
     *
     * @param pos coordinate.
     * @param groundstyle one of the GROUNDSTYLE_ values.
     */
    public AbstractBlueprintManipulationWindow(@NotNull final String resourceId, @Nullable final BlockPos pos, final int groundstyle, final String blueprintId)
    {
        super(resourceId);
        this.groundstyle = groundstyle;
        this.bluePrintId = blueprintId;

        if (pos != null && RenderingCache.getOrCreateBlueprintPreviewData(blueprintId).getPos() == null)
        {
            RenderingCache.getOrCreateBlueprintPreviewData(blueprintId).setPos(pos);
            adjustToGroundOffset();
        }

        // Register all necessary buttons with the window.
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_LEFT, this::moveLeftClicked);
        registerButton(BUTTON_MIRROR, this::mirrorClicked);
        registerButton(BUTTON_RIGHT, this::moveRightClicked);
        registerButton(BUTTON_BACKWARD, this::moveBackClicked);
        registerButton(BUTTON_FORWARD, this::moveForwardClicked);
        registerButton(BUTTON_UP, this::moveUpClicked);
        registerButton(BUTTON_DOWN, this::moveDownClicked);
        registerButton(BUTTON_ROTATE_RIGHT, this::rotateRightClicked);
        registerButton(BUTTON_ROTATE_LEFT, this::rotateLeftClicked);
        registerButton(BUTTON_SETTINGS, this::settingsClicked);

        settingsList = findPaneOfTypeByID("settinglist", ScrollingList.class);
        placementOptionsList = findPaneOfTypeByID("placement", ScrollingList.class);
        updateRotationState();

        findPaneOfTypeByID("tip", Text.class).setVisible(pos != null);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        if (RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).getPos() == null)
        {
            Utils.playErrorSound(Minecraft.getInstance().player);
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("structurize.gui.missing.pos"), false);
            cancelClicked();
        }
    }

    /**
     * On clicking the red cancel button.
     */
    protected abstract void cancelClicked();

    /**
     * On clicking confirm for placement.
     */
    protected void confirmClicked()
    {
        final BlueprintPreviewData previewData = RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId);
        if (previewData.getBlueprint() != null)
        {
            if (!Minecraft.getInstance().player.isCreative())
            {
                final List<ISurvivalBlueprintHandler> handlers = SurvivalBlueprintHandlers.getMatchingHandlers(previewData.getBlueprint(), Minecraft.getInstance().level, Minecraft.getInstance().player, previewData.getPos(), previewData.getPlacementSettings());
                if (handlers.isEmpty())
                {
                    Utils.playErrorSound(Minecraft.getInstance().player);
                    if (SurvivalBlueprintHandlers.getHandlers().isEmpty())
                    {
                        Minecraft.getInstance().player.displayClientMessage(Component.translatable("structurize.gui.no.survival.handler"), false);
                    }
                    return;
                }

                if (handlers.size() == 1)
                {
                    handlePlacement(BuildToolPlacementMessage.HandlerType.Survival, handlers.get(0).getId());
                    return;
                }
            }
            updatePlacementOptions();
        }
    }

    /**
     * This is called when one of the placement options is clicked.
     * @param type the placement type
     * @param id the custom id type.
     */
    protected abstract void handlePlacement(final BuildToolPlacementMessage.HandlerType type, final String id);

    /**
     * Hides as much additional GUI as possible while showing the placement menu
     */
    protected void hideOtherGuiForPlacement()
    {
        settingsList.hide();
        settingsList.disable();
    }

    /**
     * Hides the placement menu (call from handlePlacement or similar)
     */
    protected void hidePlacementGui()
    {
        placementOptionsList.hide();
        placementOptionsList.disable();
        settingsList.hide();
        settingsList.disable();
    }

    /**
     * Update the list of placement options.
     */
    public void updatePlacementOptions()
    {
        placementOptionsList.enable();
        placementOptionsList.show();
        hideOtherGuiForPlacement();

        final List<Tuple<Component, Runnable>> categories = new ArrayList<>();
        if (Minecraft.getInstance().player.isCreative())
        {
            categories.add(new Tuple<>(Component.translatable("structurize.gui.buildtool.complete"), () -> handlePlacement(BuildToolPlacementMessage.HandlerType.Complete, "")));
            categories.add(new Tuple<>(Component.translatable("structurize.gui.buildtool.pretty"), () -> handlePlacement(BuildToolPlacementMessage.HandlerType.Pretty, "")));
        }

        final BlueprintPreviewData previewData = RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId);
        if (previewData.getBlueprint() != null)
        {
            for (final ISurvivalBlueprintHandler handler : SurvivalBlueprintHandlers.getMatchingHandlers(previewData.getBlueprint(), Minecraft.getInstance().level, Minecraft.getInstance().player, previewData.getPos(), previewData.getPlacementSettings()))
            {
                categories.add(new Tuple<>(handler.getDisplayName(), () -> handlePlacement(BuildToolPlacementMessage.HandlerType.Survival, handler.getId())));
            }
        }

        placementOptionsList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return categories.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @SuppressWarnings("resource")
            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                final ButtonImage buttonImage = rowPane.findPaneOfTypeByID("type", ButtonImage.class);
                buttonImage.setText(categories.get(index).getA());
                buttonImage.setTextColor(ChatFormatting.BLACK.getColor());
                buttonImage.setHandler(button -> categories.get(index).getB().run());
            }
        });
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (openTicks++ >= 20 * 10)
        {
            findPaneOfTypeByID("tip", Text.class).setVisible(false);
        }
    }

    @Override
    public boolean onUnhandledKeyTyped(final int ch, final int key)
    {
        if (ch != 0 || getFocus() != null) return super.onUnhandledKeyTyped(ch, key);

        final InputConstants.Key inputKey = InputConstants.Type.KEYSYM.getOrCreate(key);

        if (ModKeyMappings.MOVE_FORWARD.get().isActiveAndMatches(inputKey))
        {
            moveForwardClicked();
        }
        else if (ModKeyMappings.MOVE_BACK.get().isActiveAndMatches(inputKey))
        {
            moveBackClicked();
        }
        else if (ModKeyMappings.MOVE_LEFT.get().isActiveAndMatches(inputKey))
        {
            moveLeftClicked();
        }
        else if (ModKeyMappings.MOVE_RIGHT.get().isActiveAndMatches(inputKey))
        {
            moveRightClicked();
        }
        else if (ModKeyMappings.MOVE_UP.get().isActiveAndMatches(inputKey))
        {
            moveUpClicked();
        }
        else if (ModKeyMappings.MOVE_DOWN.get().isActiveAndMatches(inputKey))
        {
            moveDownClicked();
        }
        else if (ModKeyMappings.ROTATE_CW.get().isActiveAndMatches(inputKey))
        {
            rotateRightClicked();
        }
        else if (ModKeyMappings.ROTATE_CCW.get().isActiveAndMatches(inputKey))
        {
            rotateLeftClicked();
        }
        else if (ModKeyMappings.MIRROR.get().isActiveAndMatches(inputKey))
        {
            mirrorClicked();
        }
        else if (ModKeyMappings.PLACE.get().isActiveAndMatches(inputKey))
        {
            confirmClicked();
        }
        else
        {
            return super.onUnhandledKeyTyped(ch, key);
        }
        return true;
    }

    /**
     * When the settings button is clicked. Open settings window.
     */
    protected void settingsClicked()
    {
        if (settingsList.isVisible())
        {
            settingsList.hide();
            settingsList.disable();
            return;
        }
        settingsList.show();
        settingsList.enable();

        final List<Map.Entry<String, Boolean>> settings = new ArrayList<>(BlueprintRenderSettings.instance.renderSettings.entrySet());

        settingsList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return settings.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @SuppressWarnings("resource")
            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                rowPane.findPaneOfTypeByID("label", Text.class).setText(Component.translatable(settings.get(index).getKey()));
                final ButtonImage buttonImage = rowPane.findPaneOfTypeByID("switch", ButtonImage.class);
                if (settings.get(index).getValue())
                {
                    buttonImage.setText(Component.translatable("options.on"));
                }
                else
                {
                    buttonImage.setText(Component.translatable("options.off"));
                }
                buttonImage.setTextColor(ChatFormatting.BLACK.getColor());

                buttonImage.setHandler((button) -> {
                    settings.get(index).setValue(!settings.get(index).getValue());
                    Network.getNetwork().sendToServer(new SyncSettingsToServer());
                    RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).syncChangesToServer();
                });
            }
        });
    }

    /*
     * ---------------- Button Handling -----------------
     */

    /**
     * Rotate the structure counter clockwise.
     */
    private void mirrorClicked()
    {
        RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).mirror();
        updateRotationState();
    }

    /**
     * Move the schematic up.
     */
    private void moveUpClicked()
    {
        RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).move(new BlockPos(0, 1, 0));
    }

    /**
     * Move the structure down.
     */
    private void moveDownClicked()
    {
        RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).move(new BlockPos(0, -1, 0));
    }

    /**
     * Move the structure left.
     */
    private void moveLeftClicked()
    {
        RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).move(new BlockPos(0, 0, 0).relative(this.mc.player.getDirection().getCounterClockWise()));
    }

    /**
     * Move the structure right.
     */
    private void moveRightClicked()
    {
        RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).move(new BlockPos(0, 0, 0).relative(this.mc.player.getDirection().getClockWise()));
    }

    /**
     * Move the structure forward.
     */
    private void moveForwardClicked()
    {
        RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).move(new BlockPos(0, 0, 0).relative(this.mc.player.getDirection()));
    }

    /**
     * Move the structure back.
     */
    private void moveBackClicked()
    {
        RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).move(new BlockPos(0, 0, 0).relative(this.mc.player.getDirection().getOpposite()));
    }

    /**
     * Rotate the structure clockwise.
     */
    private void rotateRightClicked()
    {
        RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).rotate(Rotation.CLOCKWISE_90);
        updateRotationState();
    }

    /**
     * Rotate the structure counter clockwise.
     */
    private void rotateLeftClicked()
    {
        RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).rotate(Rotation.COUNTERCLOCKWISE_90);
        updateRotationState();
    }

    /*
     * ---------------- Miscellaneous ----------------
     */

    /**
     * Indicate the current orientation state
     */
    protected void updateRotationState()
    {
        findPaneOfTypeByID(BUTTON_MIRROR, ButtonImage.class).setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_MIRROR + (RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).getRotationMirror().mirror().equals(Mirror.NONE) ? "" : GREEN_POS))), false);

        final String rotation = switch (RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).getRotationMirror().rotation())
        {
            case CLOCKWISE_90 -> "right_green";
            case CLOCKWISE_180 -> "down_green";
            case COUNTERCLOCKWISE_90 -> "left_green";
            case NONE -> "up_green";
        };
        findPaneOfTypeByID(IMAGE_ROTATION, Image.class).setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, rotation)), false);
    }

    /**
     * Detects the intended ground level via tag and offsets the blueprint accordingly
     */
    protected void adjustToGroundOffset()
    {
        final Blueprint blueprint = RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).getBlueprint();
        if (blueprint != null)
        {
            int groundOffset;
            switch (groundstyle)
            {
                case GROUNDSTYLE_LEGACY_CAMP:
                    groundOffset = BlueprintTagUtils.getGroundAnchorOffsetFromGroundLevels(blueprint, BlueprintTagUtils.getNumberOfGroundLevels(blueprint, 1));
                    break;

                case GROUNDSTYLE_LEGACY_SHIP:
                    groundOffset = BlueprintTagUtils.getGroundAnchorOffsetFromGroundLevels(blueprint, BlueprintTagUtils.getNumberOfGroundLevels(blueprint, 3));
                    break;

                case GROUNDSTYLE_RELATIVE:
                default:
                    groundOffset = BlueprintTagUtils.getGroundAnchorOffset(blueprint, 1);
                    break;
            }

            --groundOffset;     // compensate for clicking the top face of the ground block
            RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).setGroundOffset(groundOffset);
        }
    }
}
