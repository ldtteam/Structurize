package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintTagUtils;
import com.ldtteam.structurize.config.BlueprintRenderSettings;
import com.ldtteam.structurize.network.messages.SyncSettingsToServer;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Ground style of the caller.
     */
    protected int groundstyle;

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

        if (pos != null)
        {
            RenderingCache.getOrCreateBlueprintPreviewData(blueprintId).pos = pos;
            adjustToGroundOffset();
        }

        // Register all necessary buttons with the window.
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_LEFT, this::moveLeftClicked);
        registerButton(BUTTON_MIRROR, this::mirror);
        registerButton(BUTTON_RIGHT, this::moveRightClicked);
        registerButton(BUTTON_BACKWARD, this::moveBackClicked);
        registerButton(BUTTON_FORWARD, this::moveForwardClicked);
        registerButton(BUTTON_UP, this::moveUpClicked);
        registerButton(BUTTON_DOWN, this::moveDownClicked);
        registerButton(BUTTON_ROTATE_RIGHT, this::rotateRightClicked);
        registerButton(BUTTON_ROTATE_LEFT, this::rotateLeftClicked);
        registerButton(BUTTON_SETTINGS, this::settingsClicked);

        settingsList = findPaneOfTypeByID("settinglist", ScrollingList.class);
        updateRotationState();
    }


    /**
     * On clicking the red cancel button.
     */
    protected abstract void cancelClicked();

    /**
     * On clicking confirm for placement.
     */
    protected abstract void confirmClicked();

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        if (ch == '\u0000')
        {
            if (key == 265)
            {
                moveForwardClicked();
            }
            else if (key == 264)
            {
                moveBackClicked();
            }
            else if (key == 262)
            {
                if (Screen.hasShiftDown())
                {
                    rotateRightClicked();
                }
                else
                {
                    moveRightClicked();
                }
            }
            else if (key == 263)
            {
                if (Screen.hasShiftDown())
                {
                    rotateLeftClicked();
                }
                else
                {
                    moveLeftClicked();
                }
            }
            else if (key == 257)
            {
                confirmClicked();
            }
        }
        else if (ch == '+')
        {
            moveUpClicked();
        }
        else if (ch == '-')
        {
            moveDownClicked();
        }

        return super.onKeyTyped(ch, key);
    }

    /**
     * When the settings button is clicked. Open settings window.
     */
    private void settingsClicked()
    {
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
                rowPane.findPaneOfTypeByID("label", Text.class).setText(new TranslatableComponent(settings.get(index).getKey()));
                final ButtonImage buttonImage = rowPane.findPaneOfTypeByID("switch", ButtonImage.class);
                if (settings.get(index).getValue())
                {
                    buttonImage.setText(new TranslatableComponent("options.on"));
                }
                else
                {
                    buttonImage.setText(new TranslatableComponent("options.off"));
                }
                buttonImage.setTextColor(ChatFormatting.BLACK.getColor());

                buttonImage.setHandler((button) -> {
                    settings.get(index).setValue(!settings.get(index).getValue());
                    Network.getNetwork().sendToServer(new SyncSettingsToServer());
                    RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).scheduleRefresh();
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
    private void mirror()
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
        findPaneOfTypeByID(BUTTON_MIRROR, ButtonImage.class).setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_MIRROR + (RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).mirror.equals(Mirror.NONE) ? "" : GREEN_POS))), false);

        String rotation;
        switch (RenderingCache.getOrCreateBlueprintPreviewData(bluePrintId).rotation)
        {
            case CLOCKWISE_90:
                rotation = "right_green";
                break;
            case CLOCKWISE_180:
                rotation = "down_green";
                break;
            case COUNTERCLOCKWISE_90:
                rotation = "left_green";
                break;
            default:
                rotation = "up_green";
                break;
        }
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
