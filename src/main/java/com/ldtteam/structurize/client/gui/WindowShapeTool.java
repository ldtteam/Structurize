package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.DropDownList;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.blueprints.v1.BlueprintUtil;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Shape;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.GenerateAndPasteMessage;
import com.ldtteam.structurize.network.messages.GenerateAndSaveMessage;
import com.ldtteam.structurize.network.messages.LSStructureDisplayerMessage;
import com.ldtteam.structurize.network.messages.UndoMessage;
import com.ldtteam.structurize.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.api.util.constant.Constants.*;
import static com.ldtteam.structurize.api.util.constant.WindowConstants.*;
import static com.ldtteam.structurize.client.gui.WindowBuildTool.*;

/**
 * BuildTool window.
 */
public class WindowShapeTool extends AbstractWindowSkeleton
{
    /**
     * Pre resource string.
     */
    private static final String RES_STRING = "textures/gui/buildtool/%s.png";

    /**
     * Green String for selected state.
     */
    private static final String GREEN_POS = "_green";

    /**
     * All possible rotations.
     */
    private static final int POSSIBLE_ROTATIONS = 4;

    /**
     * Id of the paste button.
     */
    private static final String BUTTON_PASTE = "paste";

    /**
     * Id of the rotation indicator.
     */
    private static final String IMAGE_ROTATION = "rotation";

    /**
     * List of section.
     */
    @NotNull
    private final List<String> sections = new ArrayList<>();

    /**
     * Drop down list for section.
     */
    private DropDownList sectionsDropDownList;

    /**
     * Input fields for length, width, height and frequency.
     */
    private TextField inputWidth;
    private TextField inputLength;
    private TextField inputHeight;
    private TextField inputFrequency;

    /**
     * The inputShape text field.
     */
    private TextField inputShape;

    /**
     * The width.
     */
    private int shapeWidth = 1;

    /**
     * The length.
     */
    private int shapeLength = 1;

    /**
     * The height
     */
    private int shapeHeight = 1;

    /**
     * The height
     */
    private int shapeFrequency = 1;

    /**
     * The equation.
     */
    private String shapeEquation = "";

    /**
     * Current rotation of the hut/decoration.
     */
    private int rotation = 0;

    /**
     * Current position the hut/decoration is rendered at.
     */
    @NotNull
    private BlockPos pos = new BlockPos(0, 0, 0);

    /**
     * Creates a window inputShape tool.
     * This requires X, Y and Z coordinates.
     * If a structure is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used.
     *
     * @param pos coordinate.
     */
    public WindowShapeTool(@Nullable final BlockPos pos)
    {
        super(Constants.MOD_ID + SHAPE_TOOL_RESOURCE_SUFFIX);
        this.init(pos, false);
    }

    private void init(final BlockPos pos, final boolean shouldUpdate)
    {
        if (!hasPermission()) return;

        @Nullable final Blueprint structure = Settings.instance.getActiveStructure();

        if (structure != null)
        {
            rotation = Settings.instance.getRotation();

            this.shapeWidth = Settings.instance.getWidth();
            this.shapeLength = Settings.instance.getLength();
            this.shapeHeight = Settings.instance.getHeight();
            this.shapeFrequency = Settings.instance.getFrequency();
            this.shapeEquation = Settings.instance.getEquation();
        }
        else if (pos != null)
        {
            this.pos = pos;
            Settings.instance.setPosition(pos);
            Settings.instance.setRotation(0);
        }

        //Register all necessary buttons with the window.
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_LEFT, this::moveLeftClicked);
        registerButton(BUTTON_MIRROR, this::mirror);
        registerButton(BUTTON_RIGHT, this::moveRightClicked);
        registerButton(BUTTON_BACKWARD, this::moveBackClicked);
        registerButton(BUTTON_FORWARD, this::moveForwardClicked);
        registerButton(BUTTON_UP, WindowShapeTool::moveUpClicked);
        registerButton(BUTTON_DOWN, WindowShapeTool::moveDownClicked);
        registerButton(BUTTON_ROTATE_RIGHT, this::rotateRightClicked);
        registerButton(BUTTON_ROTATE_LEFT, this::rotateLeftClicked);
        registerButton(BUTTON_PICK_MAIN_BLOCK, this::pickMainBlock);
        registerButton(BUTTON_PICK_FILL_BLOCK, this::pickFillBlock);

        registerButton(BUTTON_HOLLOW, this::hollowShapeToggle);

        registerButton(UNDO_BUTTON, this::undoClicked);
        registerButton(BUTTON_PASTE, this::pasteClicked);

        inputWidth = findPaneOfTypeByID(INPUT_WIDTH, TextField.class);
        inputLength = findPaneOfTypeByID(INPUT_LENGTH, TextField.class);
        inputHeight = findPaneOfTypeByID(INPUT_HEIGHT, TextField.class);
        inputFrequency = findPaneOfTypeByID(INPUT_FREQUENCY, TextField.class);
        inputShape = findPaneOfTypeByID(INPUT_SHAPE, TextField.class);

        inputWidth.setText(Integer.toString(Settings.instance.getWidth()));
        inputLength.setText(Integer.toString(Settings.instance.getLength()));
        inputHeight.setText(Integer.toString(Settings.instance.getHeight()));
        inputFrequency.setText(Integer.toString(Settings.instance.getFrequency()));
        inputShape.setText(Settings.instance.getEquation());

        sections.clear();
        sections.addAll(Arrays.stream(Shape.values()).map(Enum::name).collect(Collectors.toList()));

        sectionsDropDownList = findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class);
        sectionsDropDownList.setHandler(this::onDropDownListChanged);
        sectionsDropDownList.setDataProvider(new SectionDropDownList());
        sectionsDropDownList.setSelectedIndex(Settings.instance.getShape().ordinal());
        disableInputIfNecessary();
        if (structure == null || shouldUpdate)
        {
            genShape();
        }
        updateRotationState();

        findPaneOfTypeByID(BUTTON_HOLLOW, ToggleButton.class)
          .setActiveState(Settings.instance.isHollow() ? "hollow" : "solid");
    }

    /**
     * Generate the inputShape depending on the variables on the client.
     */
    private static void genShape()
    {
        Settings.instance.setActiveSchematic(Manager.getStructureFromFormula(
          Settings.instance.getWidth(),
          Settings.instance.getLength(),
          Settings.instance.getHeight(),
          Settings.instance.getFrequency(),
          Settings.instance.getEquation(),
          Settings.instance.getShape(),
          Settings.instance.getBlock(true),
          Settings.instance.getBlock(false),
          Settings.instance.isHollow()));
    }

    /**
     * Generate the inputShape depending on the variables on the client.
     */
    public static void commonStructureUpdate()
    {
        genShape();
        updateRotation(Settings.instance.getRotation());
    }

    private void disableInputIfNecessary()
    {
        final Shape shape = Settings.instance.getShape();
        final Text heightLabel = findPaneOfTypeByID(HEIGHT_LABEL, Text.class);
        final Text widthLabel = findPaneOfTypeByID(WIDTH_LABEL, Text.class);
        final Text lengthLabel = findPaneOfTypeByID(LENGTH_LABEL, Text.class);
        final Text frequencyLabel = findPaneOfTypeByID(FREQUENCY_LABEL, Text.class);
        final Text shapeLabel = findPaneOfTypeByID(SHAPE_LABEL, Text.class);

        inputHeight.show();
        inputWidth.show();
        inputLength.show();
        inputFrequency.show();
        inputShape.hide();
        heightLabel.show();
        widthLabel.show();
        lengthLabel.show();
        frequencyLabel.show();
        shapeLabel.hide();

        findPaneByID(BUTTON_HOLLOW).show();
        findPaneByID(BUTTON_PICK_FILL_BLOCK).show();
        findPaneByID(RESOURCE_ICON_FILL).show();


        if (shape == Shape.RANDOM)
        {
            inputShape.show();
            shapeLabel.show();
            inputFrequency.hide();
            frequencyLabel.hide();
            findPaneByID(BUTTON_HOLLOW).hide();
            findPaneByID(BUTTON_PICK_FILL_BLOCK).hide();
            findPaneByID(RESOURCE_ICON_FILL).hide();
        }
        else if (shape == Shape.SPHERE || shape == Shape.HALF_SPHERE || shape == Shape.BOWL || shape == Shape.PYRAMID || shape == Shape.UPSIDE_DOWN_PYRAMID
                   || shape == Shape.DIAMOND)
        {
            inputWidth.hide();
            inputLength.hide();
            inputFrequency.hide();
            widthLabel.hide();
            lengthLabel.hide();
            frequencyLabel.hide();
        }
        else if (shape == Shape.CYLINDER)
        {
            inputLength.hide();
            lengthLabel.hide();
            inputFrequency.hide();
            frequencyLabel.hide();
        }
        else if (shape != Shape.WAVE && shape != Shape.WAVE_3D)
        {
            inputFrequency.hide();
            frequencyLabel.hide();
        }
    }

    /**
     * Opens the block picker window.
     */
    private void pickMainBlock()
    {
        new WindowReplaceBlock(Settings.instance.getBlock(true), Settings.instance.getPosition(), true, this).open();
    }

    /**
     * Opens the block picker window.
     */
    private void pickFillBlock()
    {
        new WindowReplaceBlock(Settings.instance.getBlock(false), Settings.instance.getPosition(), false, this).open();
    }

    /**
     * Drop down class for sections.
     */
    private class SectionDropDownList implements DropDownList.DataProvider
    {
        @Override
        public int getElementCount()
        {
            return sections.size();
        }

        @Override
        public String getLabel(final int index)
        {
            return sections.get(index);
        }
    }

    /**
     * Toggle the hollow or solid inputShape
     */
    private void hollowShapeToggle()
    {
        final ToggleButton hollowButton = findPaneOfTypeByID(BUTTON_HOLLOW, ToggleButton.class);
        Settings.instance.setHollow(hollowButton.isActiveState("hollow"));

        genShape();
    }

    /**
     * Undo the last change.
     */
    private void undoClicked()
    {
        Network.getNetwork().sendToServer(new UndoMessage());
    }

    /**
     * Confirm button clicked.
     */
    private void confirmClicked()
    {
        place();
        clearAndClose();
    }

    /**
     * Paste button clicked.
     */
    private void pasteClicked()
    {
        if (isCreative())
        {
            paste();
        }

        clearAndClose();
    }

    /**
     * Override if place without paste is required.
     */
    protected void place()
    {
        if (isCreative())
        {
            paste();
        }
    }

    /**
     * Saves the current shape to the server.
     * @return A name that can be used to place it.
     */
    protected StructureName save()
    {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BlueprintUtil.writeToStream(stream, Settings.instance.getActiveStructure());

        // cache it locally...
        Structures.handleSaveSchematicMessage(stream.toByteArray(), true);

        if (!Minecraft.getInstance().hasSingleplayerServer())
        {
            // and also on the server if needed
            Network.getNetwork().sendToServer(new GenerateAndSaveMessage(Settings.instance.getPosition(),
                    Settings.instance.getLength(),
                    Settings.instance.getWidth(),
                    Settings.instance.getHeight(),
                    Settings.instance.getFrequency(),
                    Settings.instance.getEquation(),
                    Settings.instance.getShape(),
                    Settings.instance.getBlock(true),
                    Settings.instance.getBlock(false),
                    Settings.instance.isHollow(),
                    BlockUtils.getRotation(Settings.instance.getRotation()),
                    Settings.instance.getMirror()));
        }

        // this assumes that the server will generate exactly the same blueprint data as the client did;
        // hopefully that is true, since they should be using the same algorithm to do it...
        return new StructureName(Structures.SCHEMATICS_CACHE + Structures.SCHEMATICS_SEPARATOR + StructureUtils.calculateMD5(stream.toByteArray()));
    }

    /**
     * Paste a schematic in the world.
     */
    private void paste()
    {
        Network.getNetwork().sendToServer(new GenerateAndPasteMessage(Settings.instance.getPosition(),
          Settings.instance.getLength(),
          Settings.instance.getWidth(),
          Settings.instance.getHeight(),
          Settings.instance.getFrequency(),
          Settings.instance.getEquation(),
          Settings.instance.getShape(),
          Settings.instance.getBlock(true),
          Settings.instance.getBlock(false),
          Settings.instance.isHollow(),
          BlockUtils.getRotation(Settings.instance.getRotation()),
          Settings.instance.getMirror()));
    }

    /**
     * Rotate the structure counter clockwise.
     */
    private void mirror()
    {
        Settings.instance.mirror();
        updateRotationState();
    }

    /**
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    public void onOpened()
    {
        if (!hasPermission())
        {
            LanguageHandler.sendMessageToPlayer(Minecraft.getInstance().player, "structurize.gui.shapetool.creative_only");
            close();
        }
        // updateRotation(rotation);
        findPaneOfTypeByID(RESOURCE_ICON_MAIN, ItemIcon.class).setItem(Settings.instance.getBlock(true));
        findPaneOfTypeByID(RESOURCE_ICON_FILL, ItemIcon.class).setItem(Settings.instance.getBlock(false));
        findPaneOfTypeByID(UNDO_BUTTON, Button.class).setVisible(isCreative());
        findPaneOfTypeByID(BUTTON_PASTE, Button.class).setVisible(isCreative());
    }

    public void updateBlock(final ItemStack stack, final boolean mainBlock)
    {
        Settings.instance.setBlock(stack, mainBlock);
        findPaneOfTypeByID(mainBlock ? RESOURCE_ICON_MAIN : RESOURCE_ICON_FILL, ItemIcon.class).setItem(stack);
        genShape();
    }

    /**
     * Defines if a player has access to the creative-only controls.
     *
     * @return true if so.
     */
    public boolean isCreative()
    {
        return Minecraft.getInstance().player.isCreative();
    }

    /**
     * Defines if a player has permission to use this.
     *
     * @return true if so.
     */
    public boolean hasPermission()
    {
        return isCreative();
    }

    /*
     * ---------------- Input Handling -----------------
     */

    /**
     * called every time one of the dropdownlist changed.
     *
     * @param list the dropdown list which change
     */
    private void onDropDownListChanged(final DropDownList list)
    {
        if (list.isEnabled())
        {
            if (list == sectionsDropDownList)
            {
                updateStyle(sections.get(sectionsDropDownList.getSelectedIndex()));
            }
        }
    }

    /**
     * Update the style after change.
     *
     * @param s the style to use.
     */
    private void updateStyle(final String s)
    {
        if (Shape.valueOf(sections.get(sectionsDropDownList.getSelectedIndex())) != Settings.instance.getShape())
        {
            Settings.instance.setShape(s);
            genShape();
        }
        disableInputIfNecessary();
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean result = super.onKeyTyped(ch, key);
        final String widthText = inputWidth.getText();
        final String lengthText = inputLength.getText();
        final String heightText = inputHeight.getText();
        final String frequencyText = inputFrequency.getText();
        final String localEquation = inputShape.getText();

        if (!widthText.isEmpty() && !lengthText.isEmpty() && !heightText.isEmpty())
        {
            try
            {
                final int localWidth = Integer.parseInt(widthText);
                final int localHeight = Integer.parseInt(heightText);
                final int localLength = Integer.parseInt(lengthText);
                final int localFrequency = Integer.parseInt(frequencyText);

                if (shapeHeight != localHeight || shapeLength != localLength || shapeWidth != localWidth || shapeFrequency != localFrequency
                      || !shapeEquation.equals(localEquation))
                {
                    this.shapeWidth = localWidth;
                    this.shapeLength = localLength;
                    this.shapeHeight = localHeight;
                    this.shapeFrequency = localFrequency;
                    this.shapeEquation = localEquation;
                    Settings.instance.setWidth(localWidth);
                    Settings.instance.setLength(localLength);
                    Settings.instance.setHeight(localHeight);
                    Settings.instance.setFrequency(localFrequency);
                    Settings.instance.setEquation(localEquation);
                    genShape();
                }
            }
            catch (NumberFormatException e)
            {
                inputWidth.setText(Integer.toString(Settings.instance.getWidth()));
                inputLength.setText(Integer.toString(Settings.instance.getLength()));
                inputHeight.setText(Integer.toString(Settings.instance.getHeight()));
            }
        }
        return result;
    }

    /*
     * ---------------- Button Handling -----------------
     */

    /**
     * Move the schematic up.
     */
    private static void moveUpClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 1, 0));
    }

    /**
     * Move the structure down.
     */
    private static void moveDownClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, -1, 0));
    }

    /**
     * Move the structure left.
     */
    private void moveLeftClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).relative(this.mc.player.getDirection().getCounterClockWise()));
    }

    /**
     * Move the structure right.
     */
    private void moveRightClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).relative(this.mc.player.getDirection().getClockWise()));
    }

    /**
     * Move the structure forward.
     */
    private void moveForwardClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).relative(this.mc.player.getDirection()));
    }

    /**
     * Move the structure back.
     */
    private void moveBackClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).relative(this.mc.player.getDirection().getOpposite()));
    }

    /**
     * Rotate the structure clockwise.
     */
    private void rotateRightClicked()
    {
        rotation = (rotation + ROTATE_ONCE) % POSSIBLE_ROTATIONS;
        updateRotation(rotation);
        updateRotationState();
    }

    /**
     * Rotate the structure counter clockwise.
     */
    private void rotateLeftClicked()
    {
        rotation = (rotation + ROTATE_THREE_TIMES) % POSSIBLE_ROTATIONS;
        updateRotation(rotation);
        updateRotationState();
    }


    /*
     * ---------------- Miscellaneous ----------------
     */

    private void clearAndClose()
    {
        Settings.instance.resetBlueprint();
        Network.getNetwork().sendToServer(new LSStructureDisplayerMessage(null, false));
        close();
    }

    /**
     * Cancel the current structure.
     */
    private void cancelClicked()
    {
        clearAndClose();
    }

    /**
     * Updates the rotation of the structure depending on the input.
     *
     * @param rotation the rotation to be set.
     */
    private static void updateRotation(final int rotation)
    {
        final PlacementSettings settings = new PlacementSettings();
        switch (rotation)
        {
            case ROTATE_ONCE:
                settings.setRotation(Rotation.CLOCKWISE_90);
                break;
            case ROTATE_TWICE:
                settings.setRotation(Rotation.CLOCKWISE_180);
                break;
            case ROTATE_THREE_TIMES:
                settings.setRotation(Rotation.COUNTERCLOCKWISE_90);
                break;
            default:
                settings.setRotation(Rotation.NONE);
        }
        Settings.instance.setRotation(rotation);
        settings.setMirror(Settings.instance.getMirror());
    }

    private void updateRotationState()
    {
        findPaneOfTypeByID(BUTTON_MIRROR, ButtonImage.class)
                .setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_MIRROR +
                        (Settings.instance.getMirror().equals(Mirror.NONE) ? "" : GREEN_POS))));

        String rotation;
        switch (Settings.instance.getRotation())
        {
            case ROTATE_ONCE:
                rotation = "right_green";
                break;
            case ROTATE_TWICE:
                rotation = "down_green";
                break;
            case ROTATE_THREE_TIMES:
                rotation = "left_green";
                break;
            default:
                rotation = "up_green";
                break;
        }
        findPaneOfTypeByID(IMAGE_ROTATION, Image.class)
                .setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, rotation)));
    }

    /**
     * Called when the window is closed.
     * Updates state via {@link LSStructureDisplayerMessage}
     */
    @Override
    public void onClosed()
    {
        if (Settings.instance.getActiveStructure() != null)
        {
            Network.getNetwork().sendToServer(new LSStructureDisplayerMessage(Settings.instance.serializeNBT(), true));
        }
    }
}
