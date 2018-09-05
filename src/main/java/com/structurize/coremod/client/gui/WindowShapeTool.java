package com.structurize.coremod.client.gui;

import com.structurize.api.util.LanguageHandler;
import com.structurize.api.util.Shape;
import com.structurize.api.util.constant.Constants;
import com.structurize.blockout.controls.Button;
import com.structurize.blockout.controls.ItemIcon;
import com.structurize.blockout.controls.Label;
import com.structurize.blockout.controls.TextField;
import com.structurize.blockout.views.DropDownList;
import com.structurize.coremod.Structurize;
import com.structurize.coremod.management.Manager;
import com.structurize.coremod.network.messages.*;
import com.structurize.structures.helpers.Settings;
import com.structurize.structures.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.structurize.api.util.constant.Constants.*;
import static com.structurize.api.util.constant.WindowConstants.*;

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
     * Current rotation of the hut/decoration.
     */
    private int rotation = 0;

    /**
     * Current position the hut/decoration is rendered at.
     */
    @NotNull
    private BlockPos pos = new BlockPos(0, 0, 0);

    /**
     * Creates a window shape tool.
     * This requires X, Y and Z coordinates.
     * If a structure is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used.
     *
     * @param pos coordinate.
     */
    public WindowShapeTool(@Nullable final BlockPos pos)
    {
        super(Constants.MOD_ID + SHAPE_TOOL_RESOURCE_SUFFIX);
        if (Minecraft.getMinecraft().player.capabilities.isCreativeMode)
        {
            this.init(pos, false);
        }
    }

    /**
     * Creates a window shape tool.
     * This requires X, Y and Z coordinates.
     * If a structure is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used.
     *
     * @param pos coordinate.
     * @param mainBlock if main or fill block.
     */
    public WindowShapeTool(@Nullable final BlockPos pos, final ItemStack stack, final boolean mainBlock)
    {
        super(Constants.MOD_ID + SHAPE_TOOL_RESOURCE_SUFFIX);
        if (Minecraft.getMinecraft().player.capabilities.isCreativeMode)
        {
            Settings.instance.setBlock(stack, mainBlock);
            this.init(pos, true);
        }
    }

    private void init(final BlockPos pos, final boolean shouldUpdate)
    {
        @Nullable final Structure structure = Settings.instance.getActiveStructure();

        if (structure != null)
        {
            rotation = Settings.instance.getRotation();

            this.shapeWidth = Settings.instance.getWidth();
            this.shapeLength = Settings.instance.getLength();
            this.shapeHeight = Settings.instance.getHeight();
            this.shapeFrequency = Settings.instance.getFrequency();
        }
        else if (pos != null)
        {
            this.pos = pos;
            Settings.instance.setPosition(pos);
            Settings.instance.setRotation(0);
        }

        findPaneOfTypeByID(RESOURCE_ICON_MAIN, ItemIcon.class).setItem(Settings.instance.getBlock(true));
        findPaneOfTypeByID(RESOURCE_ICON_FILL, ItemIcon.class).setItem(Settings.instance.getBlock(false));

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

        inputWidth.setText(Integer.toString(Settings.instance.getWidth()));
        inputLength.setText(Integer.toString(Settings.instance.getLength()));
        inputHeight.setText(Integer.toString(Settings.instance.getHeight()));
        inputFrequency.setText(Integer.toString(Settings.instance.getFrequency()));

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

        final Button replaceButton = findPaneOfTypeByID(BUTTON_HOLLOW, Button.class);
        findPaneOfTypeByID(BUTTON_HOLLOW, Button.class).setLabel(Settings.instance.isHollow() ? LanguageHandler.format("com.structurize.coremod.gui.shapeTool.hollow") : LanguageHandler.format("com.structurize.coremod.gui.shapeTool.solid"));
    }

    /**
     * Generate the shape depending on the variables on the client.
     */
    private void genShape()
    {
        final Structure structure = new Structure(Minecraft.getMinecraft().world);
        structure.setTemplate(Manager.getStructureFromFormula(
          Settings.instance.getWidth(),
          Settings.instance.getLength(),
          Settings.instance.getHeight(),
          Settings.instance.getFrequency(),
          Settings.instance.getShape(),
          Settings.instance.getBlock(true),
          Settings.instance.getBlock(false),
          Settings.instance.isHollow()));
        structure.setPlacementSettings(new PlacementSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE));
        Settings.instance.setActiveSchematic(structure);
    }

    private void disableInputIfNecessary()
    {
        final Shape shape = Settings.instance.getShape();
        final Label heightLabel = findPaneOfTypeByID(HEIGHT_LABEL, Label.class);
        final Label widthLabel = findPaneOfTypeByID(WIDTH_LABEL, Label.class);
        final Label lengthLabel = findPaneOfTypeByID(LENGTH_LABEL, Label.class);
        final Label frequencyLabel = findPaneOfTypeByID(FREQUENCY_LABEL, Label.class);

        inputHeight.show();
        inputWidth.show();
        inputLength.show();
        inputFrequency.show();
        heightLabel.show();
        widthLabel.show();
        lengthLabel.show();
        frequencyLabel.show();

        if (shape == Shape.SPHERE || shape == Shape.HALF_SPHERE || shape == Shape.BOWL || shape == Shape.PYRAMID || shape == Shape.UPSIDE_DOWN_PYRAMID || shape == Shape.DIAMOND)
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
        new WindowReplaceBlock(Settings.instance.getBlock(true), Settings.instance.getPosition(), true).open();
    }

    /**
     * Opens the block picker window.
     */
    private void pickFillBlock()
    {
        new WindowReplaceBlock(Settings.instance.getBlock(false), Settings.instance.getPosition(), false).open();
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
     * Ignore the blocks already in the world
     */
    private void replaceBlocksToggle()
    {
        final Button replaceButton = findPaneOfTypeByID(BUTTON_REPLACE, Button.class);
        if (replaceButton.getLabel().equalsIgnoreCase(LanguageHandler.format("com.structurize.coremod.gui.shapeTool.replace")))
        {
            replaceButton.setLabel(LanguageHandler.format("com.structurize.coremod.gui.shapeTool.ignore"));
        }
        else if (replaceButton.getLabel().equalsIgnoreCase(LanguageHandler.format("com.structurize.coremod.gui.shapeTool.ignore")))
        {
            replaceButton.setLabel(LanguageHandler.format("com.structurize.coremod.gui.shapeTool.replace"));
        }
    }

    /**
     * Toggle the hollow or solid shape
     */
    private void hollowShapeToggle()
    {
        final Button replaceButton = findPaneOfTypeByID(BUTTON_HOLLOW, Button.class);
        if (replaceButton.getLabel().equalsIgnoreCase(LanguageHandler.format("com.structurize.coremod.gui.shapeTool.hollow")))
        {
            replaceButton.setLabel(LanguageHandler.format("com.structurize.coremod.gui.shapeTool.solid"));
            Settings.instance.setHollow(false);
        }
        else if (replaceButton.getLabel().equalsIgnoreCase(LanguageHandler.format("com.structurize.coremod.gui.shapeTool.solid")))
        {
            replaceButton.setLabel(LanguageHandler.format("com.structurize.coremod.gui.shapeTool.hollow"));
            Settings.instance.setHollow(true);
        }

        genShape();
    }

    /**
     * Undo the last change.
     */
    private void undoClicked()
    {
        Structurize.getNetwork().sendToServer(new UndoMessage());
    }

    /**
     * Paste a schematic in the world.
     */
    private void paste()
    {
        Structurize.getNetwork().sendToServer(new GenerateAndPasteMessage(Settings.instance.getPosition(),
          Settings.instance.getLength(),
          Settings.instance.getWidth(),
          Settings.instance.getHeight(),
          Settings.instance.getFrequency(),
          Settings.instance.getShape(),
          Settings.instance.getBlock(true),
          Settings.instance.getBlock(false),
          Settings.instance.isHollow(),
          Settings.instance.getRotation(),
          Settings.instance.getMirror()));
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
        if (!Minecraft.getMinecraft().player.capabilities.isCreativeMode)
        {
            close();
        }
        updateRotation(rotation);
        findPaneOfTypeByID(UNDO_BUTTON, Button.class).setVisible(true);
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

        if (!widthText.isEmpty() && !lengthText.isEmpty() && !heightText.isEmpty())
        {
            try
            {
                final int localWidth = Integer.parseInt(widthText);
                final int localHeight = Integer.parseInt(heightText);
                final int localLength = Integer.parseInt(lengthText);
                final int localFrequency = Integer.parseInt(frequencyText);

                if (shapeHeight != localHeight || shapeLength != localLength || shapeWidth != localWidth || shapeFrequency != localFrequency)
                {
                    this.shapeWidth = localWidth;
                    this.shapeLength = localLength;
                    this.shapeHeight = localHeight;
                    this.shapeFrequency = localFrequency;
                    Settings.instance.setWidth(localWidth);
                    Settings.instance.setLength(localLength);
                    Settings.instance.setHeight(localHeight);
                    Settings.instance.setFrequency(localFrequency);
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
        Settings.instance.moveTo(new BlockPos(0, 0, 0).offset(this.mc.player.getHorizontalFacing().rotateYCCW()));
    }

    /**
     * Move the structure right.
     */
    private void moveRightClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).offset(this.mc.player.getHorizontalFacing().rotateY()));
    }

    /**
     * Move the structure forward.
     */
    private void moveForwardClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).offset(this.mc.player.getHorizontalFacing()));
    }

    /**
     * Move the structure back.
     */
    private void moveBackClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).offset(this.mc.player.getHorizontalFacing().getOpposite()));
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

        if (Settings.instance.getActiveStructure() != null)
        {
            Settings.instance.getActiveStructure().setPlacementSettings(settings.setMirror(Settings.instance.getMirror()));
        }
    }
}
