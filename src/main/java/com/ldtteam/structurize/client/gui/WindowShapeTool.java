package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.DropDownList;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Shape;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.network.messages.GenerateAndPasteMessage;
import com.ldtteam.structurize.network.messages.LSStructureDisplayerMessage;
import com.ldtteam.structurize.network.messages.UndoMessage;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.api.util.constant.Constants.*;
import static com.ldtteam.structurize.api.util.constant.WindowConstants.*;

/**
 * BuildTool window.
 */
public class WindowShapeTool extends AbstractWindowSkeleton
{
    /**
     * All possible rotations.
     */
    private static final int POSSIBLE_ROTATIONS = 4;

    /**
     * List of section.
     */
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
        if (Minecraft.getInstance().player.isCreative())
        {
            this.init(pos, false);
        }
    }

    private void init(final BlockPos pos, final boolean shouldUpdate)
    {
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
        registerButton(BUTTON_CONFIRM, this::paste);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_LEFT, this::moveLeftClicked);
        registerButton(BUTTON_MIRROR, WindowShapeTool::mirror);
        registerButton(BUTTON_RIGHT, this::moveRightClicked);
        registerButton(BUTTON_BACKWARD, this::moveBackClicked);
        registerButton(BUTTON_FORWARD, this::moveForwardClicked);
        registerButton(BUTTON_UP, WindowShapeTool::moveUpClicked);
        registerButton(BUTTON_DOWN, WindowShapeTool::moveDownClicked);
        registerButton(BUTTON_ROTATE_RIGHT, this::rotateRightClicked);
        registerButton(BUTTON_ROTATE_LEFT, this::rotateLeftClicked);
        registerButton(BUTTON_PICK_MAIN_BLOCK, this::pickMainBlock);
        registerButton(BUTTON_PICK_FILL_BLOCK, this::pickFillBlock);

        registerButton(BUTTON_REPLACE, this::replaceBlocksToggle);
        registerButton(BUTTON_HOLLOW, this::hollowShapeToggle);

        registerButton(UNDO_BUTTON, this::undoClicked);

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

    // TODO: confirm whether this button actually exists. This function may be useless.
    /**
     * Ignore the blocks already in the world
     */
    private void replaceBlocksToggle()
    {
        final Button replaceButton = findPaneOfTypeByID(BUTTON_REPLACE, Button.class);
        if (replaceButton.getTextAsString().equalsIgnoreCase(LanguageHandler.format("com.ldtteam.structurize.gui.shapetool.replace")))
        {
            replaceButton.setText(LanguageHandler.format("com.ldtteam.structurize.gui.shapetool.ignore"));
        }
        else if (replaceButton.getTextAsString().equalsIgnoreCase(LanguageHandler.format("com.ldtteam.structurize.gui.shapetool.ignore")))
        {
            replaceButton.setText(LanguageHandler.format("com.ldtteam.structurize.gui.shapetool.replace"));
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
        close();
    }

    /**
     * Rotate the structure counter clockwise.
     */
    private static void mirror()
    {
        Settings.instance.mirror();
    }

    /**
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    public void onOpened()
    {
        if (!Minecraft.getInstance().player.isCreative())
        {
            close();
        }
        // updateRotation(rotation);
        findPaneOfTypeByID(RESOURCE_ICON_MAIN, ItemIcon.class).setItem(Settings.instance.getBlock(true));
        findPaneOfTypeByID(RESOURCE_ICON_FILL, ItemIcon.class).setItem(Settings.instance.getBlock(false));
        findPaneOfTypeByID(UNDO_BUTTON, Button.class).setVisible(true);
    }

    public void updateBlock(final ItemStack stack, final boolean mainBlock)
    {
        Settings.instance.setBlock(stack, mainBlock);
        findPaneOfTypeByID(mainBlock ? RESOURCE_ICON_MAIN : RESOURCE_ICON_FILL, ItemIcon.class).setItem(stack);
        genShape();
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
    }

    /**
     * Rotate the structure counter clockwise.
     */
    private void rotateLeftClicked()
    {
        rotation = (rotation + ROTATE_THREE_TIMES) % POSSIBLE_ROTATIONS;
        updateRotation(rotation);
    }


    /*
     * ---------------- Miscellaneous ----------------
     */

    /**
     * Cancel the current structure.
     */
    private void cancelClicked()
    {
        Settings.instance.reset();
        Network.getNetwork().sendToServer(new LSStructureDisplayerMessage(null, false));
        close();
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
