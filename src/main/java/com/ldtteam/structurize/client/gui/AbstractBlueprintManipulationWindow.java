package com.ldtteam.structurize.client.gui;

import com.google.gson.internal.LazilyParsedNumber;
import com.ldtteam.blockui.Alignment;
import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Gradient;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.ImageRepeatable;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextFieldVanilla;
import com.ldtteam.blockui.controls.TextField.Filter;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.View;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Utils;
import com.ldtteam.structurize.api.util.constant.TranslationConstants;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintTagUtils;
import com.ldtteam.structurize.client.ModKeyMappings;
import com.ldtteam.structurize.config.AbstractConfiguration;
import com.ldtteam.structurize.network.messages.BuildToolPlacementMessage;
import com.ldtteam.structurize.storage.ISurvivalBlueprintHandler;
import com.ldtteam.structurize.storage.SurvivalBlueprintHandlers;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.ValueSpec;
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
     * Placement scrolling list.
     */
    private final ScrollingList placementOptionsList;

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
        initSettings();
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
    }

    protected void initSettings()
    {
        final List<ConfigValue<?>> settings = new ArrayList<>();
        Structurize.getConfig().getClient().collectPreviewRendererSettings(settings::add);

        settingsList.setDataProvider(settings::size, (index, rowPane) -> {
            final ConfigValue<?> setting = settings.get(index);
            final Optional<ValueSpec> optional = Structurize.getConfig().getSpecFromValue(setting);
            final Text label = rowPane.findPaneOfTypeByID("label", Text.class);

            if (optional.isEmpty())
            {
                // config resolution failed (crashes in dev already, display error in production)
                label.setText(Component.translatable(TranslationConstants.NO_VALUE_SPEC));
                return;
            }

            final ValueSpec settingSpec = optional.get();
            final String nameTKey = settingSpec.getTranslationKey();

            if (label.getText() != null && label.getText().getContents() instanceof final TranslatableContents tkey && tkey.getKey().equals(nameTKey))
            {
                // dont update when same row
                return;
            }

            label.setText(Component.translatable(nameTKey));
            PaneBuilders.singleLineTooltip(Component.translatable(nameTKey + AbstractConfiguration.COMMENT_SUFFIX), rowPane);

            final ButtonImage buttonImage = rowPane.findPaneOfTypeByID("switch", ButtonImage.class);
            final TextFieldVanilla inputField = rowPane.findPaneOfTypeByID("set_input", TextFieldVanilla.class);

            // TODO: instanceof switch
            if (setting.get() instanceof final Boolean value)
            {
                final ConfigValue<Boolean> typedSetting = (ConfigValue<Boolean>) setting;

                inputField.off();
                buttonImage.on();
                buttonImage.setText(Component.translatable(value ? "options.on" : "options.off"));
                buttonImage.setHandler((button) -> {
                    final Boolean newValue = !typedSetting.get();

                    Structurize.getConfig().set(typedSetting, newValue);
                    buttonImage.setText(Component.translatable(newValue ? "options.on" : "options.off"));
                });
            }
            else if (setting.get() instanceof final Number value)
            {
                final ConfigValue<Number> typedSetting = (ConfigValue<Number>) setting;

                buttonImage.off();
                inputField.on();
                inputField.setText(typedSetting.get().toString());
                inputField.setFilter(new Filter()
                {
                    @Override
                    public String filter(final String s)
                    {
                        return s;
                    }

                    @Override
                    public boolean isAllowedCharacter(final char c)
                    {
                        return Character.isDigit(c) || c == '-' || c == '.';
                    }
                });
                inputField.setHandler(a -> {
                    if (inputField.getText().isBlank())
                    {
                        return;
                    }

                    final Number newValue = new LazilyParsedNumber(inputField.getText());
                    final boolean testResult;
                    try
                    {
                        testResult = settingSpec.test(newValue);
                    }
                    catch (final NumberFormatException e)
                    {
                        inputField.setTextColor(0xffff0000); // red
                        return;
                    }

                    if (testResult)
                    {
                        inputField.setTextColor(0xffe0e0e0); // vanilla defualt

                        final DoubleValue rendererTransparency = Structurize.getConfig().getClient().rendererTransparency;
                        if (setting == rendererTransparency && rendererTransparency.get() < 0)
                        {
                            // TODO: move to standalone ui
                            final View confirmDialog = new View();
                            confirmDialog.setPosition(70, 0);
                            confirmDialog.setSize(177, 150);
                            confirmDialog.setAlignment(Alignment.MIDDLE);

                            final Gradient hidingLayer = new Gradient();
                            hidingLayer.setGradientStart(0x10, 0x10, 0x10, 0xC0);
                            hidingLayer.setGradientEnd(0x10, 0x10, 0x10, 0xD0);
                            hidingLayer.setSize(getWindow().getWidth(), getWindow().getHeight());

                            getWindow().addChild(hidingLayer);
                            getWindow().addChild(confirmDialog);
                            View.setFocus(null);

                            final ImageRepeatable background = new ImageRepeatable();
                            background.setSize(177, 150);
                            background.setImageLoc(new ResourceLocation(MOD_ID, "textures/gui/builderhut/builder_papper.png"));
                            background.setImageSize(6, 7, 177, 231, 20, 20, 100, 100);
                            confirmDialog.addChild(background);

                            final Text text = new Text();
                            text.setPosition(10, 8);
                            text.setSize(157, 105);
                            text.setColors(Color.getByName("black"));
                            text.setTextAlignment(Alignment.TOP_MIDDLE);
                            text.setText(Component.translatable("structurize.config.transparency.warning"));
                            confirmDialog.addChild(text);

                            final ButtonImage confirm = new ButtonImage();
                            confirm.setPosition(10, 123);
                            confirm.setSize(64, 17);
                            confirm.setImage(new ResourceLocation(MOD_ID, "textures/gui/builderhut/builder_button_small.png"), false);
                            confirm.setColors(Color.getByName("black"));
                            confirm.setTextAlignment(Alignment.MIDDLE);
                            confirm.setTextRenderBox(64, 17);
                            confirm.setText(Component.translatable("gui.yes"));
                            confirm.setHandler(b -> {
                                final double newVal = newValue.doubleValue();
                                Structurize.getConfig().set(rendererTransparency, newVal < 0 ? 1 : newVal);
                                if (newVal < 0)
                                {
                                    inputField.setText("1.0");
                                }
                                getWindow().removeChild(hidingLayer);
                                getWindow().removeChild(confirmDialog);
                            });
                            confirmDialog.addChild(confirm);
                            
                            final ButtonImage cancel = new ButtonImage();
                            cancel.setPosition(103, 123);
                            cancel.setSize(64, 17);
                            cancel.setImage(new ResourceLocation(MOD_ID, "textures/gui/builderhut/builder_button_small.png"), false);
                            cancel.setColors(Color.getByName("black"));
                            cancel.setTextAlignment(Alignment.MIDDLE);
                            cancel.setTextRenderBox(64, 17);
                            cancel.setText(Component.translatable("gui.cancel"));
                            cancel.setHandler(b -> {
                                inputField.setText(Double.toString(rendererTransparency.get()));
                                getWindow().removeChild(hidingLayer);
                                getWindow().removeChild(confirmDialog);
                            });
                            confirmDialog.addChild(cancel);
                        }
                        else
                        {
                            // need properly typed thing now, but it's validated so parsing should never crash
                            // TODO: switch instanceof
                            final Object oldValue = typedSetting.get();
                            if (oldValue instanceof Integer)
                            {
                                Structurize.getConfig().set(typedSetting, newValue.intValue());
                            }
                            else if (oldValue instanceof Long)
                            {
                                Structurize.getConfig().set(typedSetting, newValue.longValue());
                            }
                            else if (oldValue instanceof Double)
                            {
                                Structurize.getConfig().set(typedSetting, newValue.doubleValue());
                            }
                        }
                    }
                    else
                    {
                        inputField.setTextColor(0xffff0000); // red
                    }
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
