package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.blockui.views.View;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Shape;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.network.messages.GenerateAndPasteMessage;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.storage.rendering.types.ShapesPreviewData;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ldtteam.structurize.api.util.constant.WindowConstants.*;

/**
 * BuildTool window.
 */
public class WindowShapeTool extends AbstractBlueprintManipulationWindow
{
    /**
     * Shape variables.
     */
    private static int     width  = 1;
    private static int     height = 1;
    private static int     length = 1;
    private static int     frequency = 1;
    private static String  equation = "";

    private static boolean hollow = false;

    /**
     * The default shape to use.
     */
    private static Shape shape = Shape.CUBE;

    /**
     * The stack used to present blocks.
     */
    private static Tuple<ItemStack, ItemStack> stack = new Tuple<>(new ItemStack(Blocks.GOLD_BLOCK), new ItemStack(Blocks.GOLD_BLOCK));

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
     * Suffix of the minus buttons.
     */
    private static final String BUTTON_MINUS = "minus";

    /**
     * Suffix of the plus buttons.
     */
    private static final String BUTTON_PLUS = "plus";

    /**
     * Id of the rotation indicator.
     */
    private static final String IMAGE_ROTATION = "rotation";

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
     * Creates a window inputShape tool.
     * This requires X, Y and Z coordinates.
     * If a structure is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used.
     *
     * @param pos coordinate.
     */
    public WindowShapeTool(@Nullable final BlockPos pos)
    {
        super(Constants.MOD_ID + SHAPE_TOOL_RESOURCE_SUFFIX, pos,0, "shapes");
        this.init(pos, false);
    }

    private void init(final BlockPos pos, final boolean shouldUpdate)
    {
        if (!hasPermission()) return;

        final BlueprintPreviewData previewData = RenderingCache.getOrCreateBlueprintPreviewData("shapes");
        @Nullable final Blueprint structure = previewData.getBlueprint();

        if (structure != null)
        {
            this.shapeWidth = width;
            this.shapeLength = length;
            this.shapeHeight = height;
            this.shapeFrequency = frequency;
            this.shapeEquation = equation;
        }

        //Register all necessary buttons with the window.
        registerButton(BUTTON_PICK_MAIN_BLOCK, this::pickMainBlock);
        registerButton(BUTTON_PICK_FILL_BLOCK, this::pickFillBlock);
        registerButton(BUTTON_UNDOREDO, b -> {
            close();
            new WindowUndoRedo().open();
        });

        registerButton(BUTTON_HOLLOW, this::hollowShapeToggle);
        registerButton(BUTTON_PASTE, this::pasteClicked);

        inputWidth = findPaneOfTypeByID(INPUT_WIDTH, TextField.class);
        inputLength = findPaneOfTypeByID(INPUT_LENGTH, TextField.class);
        inputHeight = findPaneOfTypeByID(INPUT_HEIGHT, TextField.class);
        inputFrequency = findPaneOfTypeByID(INPUT_FREQUENCY, TextField.class);
        inputShape = findPaneOfTypeByID(INPUT_SHAPE, TextField.class);

        inputWidth.setText(Integer.toString(width));
        inputLength.setText(Integer.toString(length));
        inputHeight.setText(Integer.toString(height));
        inputFrequency.setText(Integer.toString(frequency));
        inputShape.setText(equation);

        registerButton(INPUT_WIDTH + BUTTON_MINUS, () -> adjust(inputWidth, width - 1));
        registerButton(INPUT_WIDTH + BUTTON_PLUS, () -> adjust(inputWidth, width + 1));
        registerButton(INPUT_LENGTH + BUTTON_MINUS, () -> adjust(inputLength, length - 1));
        registerButton(INPUT_LENGTH + BUTTON_PLUS, () -> adjust(inputLength, length + 1));
        registerButton(INPUT_HEIGHT + BUTTON_MINUS, () -> adjust(inputHeight, height - 1));
        registerButton(INPUT_HEIGHT + BUTTON_PLUS, () -> adjust(inputHeight, height + 1));
        registerButton(INPUT_FREQUENCY + BUTTON_MINUS, () -> adjust(inputFrequency, frequency - 1));
        registerButton(INPUT_FREQUENCY + BUTTON_PLUS, () -> adjust(inputFrequency, frequency + 1));

        sections.clear();
        sections.addAll(Arrays.stream(Shape.values()).map(Enum::name).toList());

        sectionsDropDownList = findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class);
        sectionsDropDownList.setHandler(this::onDropDownListChanged);
        sectionsDropDownList.setDataProvider(new SectionDropDownList());
        sectionsDropDownList.setSelectedIndex(shape.ordinal());
        registerButton("nextShape", sectionsDropDownList::selectNext);
        registerButton("previousShape", sectionsDropDownList::selectPrevious);
        disableInputIfNecessary();

        if (structure == null || shouldUpdate)
        {
            genShape();
        }
        updateRotationState();

        findPaneOfTypeByID(BUTTON_HOLLOW, ToggleButton.class)
          .setActiveState(hollow ? "hollow" : "solid");
    }

    /**
     * Generate the inputShape depending on the variables on the client.
     */
    private static void genShape()
    {
        RenderingCache.getOrCreateBlueprintPreviewData("shapes").setBlueprint(Manager.getStructureFromFormula(
          width,
          length,
          height,
          frequency,
          equation,
          shape,
          stack.getA(),
          stack.getB(),
          hollow));
    }

    private void disableInputIfNecessary()
    {
        final View height = findPaneOfTypeByID(HEIGHT_VIEW, View.class);
        final View width = findPaneOfTypeByID(WIDTH_VIEW, View.class);
        final View length = findPaneOfTypeByID(LENGTH_VIEW, View.class);
        final View frequency = findPaneOfTypeByID(FREQUENCY_VIEW, View.class);
        final View equation = findPaneOfTypeByID(SHAPE_VIEW, View.class);

        height.show();
        width.show();
        length.show();
        frequency.show();
        equation.hide();

        findPaneByID(BUTTON_HOLLOW).show();
        findPaneByID(BUTTON_PICK_FILL_BLOCK).show();
        findPaneByID(RESOURCE_ICON_FILL).show();

        if (shape == Shape.SPHERE || shape == Shape.HALF_SPHERE || shape == Shape.BOWL || shape == Shape.PYRAMID || shape == Shape.UPSIDE_DOWN_PYRAMID
                   || shape == Shape.DIAMOND)
        {
            width.hide();
            length.hide();
            frequency.hide();
        }
        else if (shape == Shape.CYLINDER || shape == Shape.CONE)
        {
            length.hide();
            frequency.hide();
        }
        else if (shape != Shape.WAVE && shape != Shape.WAVE_3D)
        {
            frequency.hide();
        }
    }

    /**
     * Opens the block picker window.
     */
    private void pickMainBlock()
    {
        new WindowReplaceBlock(stack.getA(), OldSettings.instance.getPosition(), true, this).open();
    }

    /**
     * Opens the block picker window.
     * */
    private void pickFillBlock()
    {
        new WindowReplaceBlock(stack.getB(), OldSettings.instance.getPosition(), false, this).open();
    }

    private void adjust(final TextField input, final int value)
    {
        input.setText(Integer.toString(Math.max(1, value)));

        onKeyTyped('\0', 0);
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
        OldSettings.instance.setHollow(hollowButton.isActiveState("hollow"));

        genShape();
    }

    /**
     * Confirm button clicked.
     */
    protected void confirmClicked()
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
        BlueprintUtil.writeToStream(stream, OldSettings.instance.getActiveStructure());

        // cache it locally...
        Structures.handleSaveSchematicMessage(stream.toByteArray(), true);

        if (!Minecraft.getInstance().hasSingleplayerServer())
        {
            // and also on the server if needed
            Network.getNetwork().sendToServer(new GenerateAndSaveMessage(OldSettings.instance.getPosition(),
                    OldSettings.instance.getLength(),
                    OldSettings.instance.getWidth(),
                    OldSettings.instance.getHeight(),
                    OldSettings.instance.getFrequency(),
                    OldSettings.instance.getEquation(),
                    OldSettings.instance.getShape(),
                    OldSettings.instance.getBlock(true),
                    OldSettings.instance.getBlock(false),
                    OldSettings.instance.isHollow(),
                    BlockUtils.getRotation(OldSettings.instance.getRotation()),
                    OldSettings.instance.getMirror()));
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
        Network.getNetwork().sendToServer(new GenerateAndPasteMessage(OldSettings.instance.getPosition(),
          OldSettings.instance.getLength(),
          OldSettings.instance.getWidth(),
          OldSettings.instance.getHeight(),
          OldSettings.instance.getFrequency(),
          OldSettings.instance.getEquation(),
          OldSettings.instance.getShape(),
          OldSettings.instance.getBlock(true),
          OldSettings.instance.getBlock(false),
          OldSettings.instance.isHollow(),
          BlockUtils.getRotation(OldSettings.instance.getRotation()),
          OldSettings.instance.getMirror()));
    }

    /**
     * Rotate the structure counter clockwise.
     */
    private void mirror()
    {
        OldSettings.instance.mirror();
        updateRotationState();
    }

    /**
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    @SuppressWarnings("resource")
    public void onOpened()
    {
        if (!hasPermission())
        {
            LanguageHandler.sendMessageToPlayer(Minecraft.getInstance().player, "structurize.gui.shapetool.creative_only");
            close();
        }
        // updateRotation(rotation);
        findPaneOfTypeByID(RESOURCE_ICON_MAIN, ItemIcon.class).setItem(OldSettings.instance.getBlock(true));
        findPaneOfTypeByID(RESOURCE_ICON_FILL, ItemIcon.class).setItem(OldSettings.instance.getBlock(false));
        findPaneOfTypeByID(BUTTON_UNDOREDO, Button.class).setVisible(isCreative());
        findPaneOfTypeByID(BUTTON_PASTE, Button.class).setVisible(isCreative());
    }

    public void updateBlock(final ItemStack stack, final boolean mainBlock)
    {
        OldSettings.instance.setBlock(stack, mainBlock);
        findPaneOfTypeByID(mainBlock ? RESOURCE_ICON_MAIN : RESOURCE_ICON_FILL, ItemIcon.class).setItem(stack);
        genShape();
    }

    /**
     * Defines if a player has access to the creative-only controls.
     *
     * @return true if so.
     */
    @SuppressWarnings("resource")
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
        if (list == sectionsDropDownList)
        {
            updateStyle(sections.get(sectionsDropDownList.getSelectedIndex()));
        }
    }

    /**
     * Update the style after change.
     *
     * @param s the style to use.
     */
    private void updateStyle(final String s)
    {
        if (Shape.valueOf(sections.get(sectionsDropDownList.getSelectedIndex())) != OldSettings.instance.getShape())
        {
            OldSettings.instance.setShape(s);
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
                    OldSettings.instance.setWidth(localWidth);
                    OldSettings.instance.setLength(localLength);
                    OldSettings.instance.setHeight(localHeight);
                    OldSettings.instance.setFrequency(localFrequency);
                    OldSettings.instance.setEquation(localEquation);
                    genShape();
                }
            }
            catch (NumberFormatException e)
            {
                inputWidth.setText(Integer.toString(OldSettings.instance.getWidth()));
                inputLength.setText(Integer.toString(OldSettings.instance.getLength()));
                inputHeight.setText(Integer.toString(OldSettings.instance.getHeight()));
            }
        }
        return result;
    }


    /*
     * ---------------- Miscellaneous ----------------
     */

    private void clearAndClose()
    {
        OldSettings.instance.resetBlueprint();
        close();
    }

    /**
     * Cancel the current structure.
     */
    protected void cancelClicked()
    {
        clearAndClose();
    }
}
