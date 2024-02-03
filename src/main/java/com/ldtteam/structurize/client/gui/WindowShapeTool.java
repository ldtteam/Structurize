package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.blockui.views.View;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.util.Shape;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.network.messages.BuildToolPlacementMessage;
import com.ldtteam.structurize.storage.ClientFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.ldtteam.structurize.api.util.constant.Constants.BLUEPRINT_FOLDER;
import static com.ldtteam.structurize.api.util.constant.Constants.SHAPES_FOLDER;
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
     * Main and secondary blocks.
     */
    private static ItemStack mainBlock = new ItemStack(Blocks.GOLD_BLOCK);
    private static ItemStack secondaryBlock = new ItemStack(Blocks.GOLD_BLOCK);

    /**
     * Suffix of the minus buttons.
     */
    private static final String BUTTON_MINUS = "minus";

    /**
     * Suffix of the plus buttons.
     */
    private static final String BUTTON_PLUS = "plus";

    /**
     * List of section.
     */
    private final List<Tuple<Shape, MutableComponent>> sections = new ArrayList<>();

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
    private String shapeequation = "";

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
        //todo placement handler support as well
    }

    private void init(final BlockPos pos, final boolean shouldUpdate)
    {
        final BlueprintPreviewData previewData = RenderingCache.getOrCreateBlueprintPreviewData("shapes");
        @Nullable final Blueprint structure = previewData.getBlueprint();

        if (structure != null)
        {
            this.shapeWidth = width;
            this.shapeLength = length;
            this.shapeHeight = height;
            this.shapeFrequency = frequency;
            this.shapeequation = equation;
        }

        //Register all necessary buttons with the window.
        registerButton(BUTTON_PICK_MAIN_BLOCK, this::pickMainBlock);
        registerButton(BUTTON_PICK_FILL_BLOCK, this::pickFillBlock);
        registerButton(BUTTON_UNDOREDO, b -> {
            close();
            new WindowUndoRedo().open();
        });

        registerButton(BUTTON_HOLLOW, this::hollowShapeToggle);

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
        Arrays.stream(Shape.values()).map(s -> new Tuple<>(s, Component.translatable("structurize.shapetool.shape." + s.name().toLowerCase()))).forEach(sections::add);        

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
          mainBlock,
          secondaryBlock,
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
        new WindowReplaceBlock(mainBlock, RenderingCache.getOrCreateBlueprintPreviewData("shapes").getPos(), true, this).open();
    }

    /**
     * Opens the block picker window.
     * */
    private void pickFillBlock()
    {
        new WindowReplaceBlock(secondaryBlock, RenderingCache.getOrCreateBlueprintPreviewData("shapes").getPos(), false, this).open();
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
        public MutableComponent getLabel(final int index)
        {
            return sections.get(index).getB();
        }
    }

    /**
     * Toggle the hollow or solid inputShape
     */
    private void hollowShapeToggle()
    {
        final ToggleButton hollowButton = findPaneOfTypeByID(BUTTON_HOLLOW, ToggleButton.class);
        hollow = hollowButton.isActiveState("hollow");

        genShape();
    }

    @Override
    protected void handlePlacement(final BuildToolPlacementMessage.HandlerType type, final String id)
    {
        final BlueprintPreviewData previewData = RenderingCache.getOrCreateBlueprintPreviewData("shapes");
        if (previewData.getBlueprint() != null)
        {
            final String packName = Minecraft.getInstance().getUser().getName();
            final Path subpath = Path.of(
                    SHAPES_FOLDER,
                    shape.toString().toLowerCase(Locale.ROOT),
                    mainBlock.getItem().toString().replace(':', '_'),
                    secondaryBlock.getItem().toString().replace(':', '_'),
                    String.format("%dx%dx%dx%d_%c.blueprint", length, width, height, frequency, hollow ? 'h' : 'f'));
            final Path path = Minecraft.getInstance().gameDirectory.toPath()
                    .resolve(BLUEPRINT_FOLDER)
                    .resolve(packName.toLowerCase(Locale.US))
                    .resolve(subpath);

            final CompoundTag compound = BlueprintUtil.writeBlueprintToNBT(previewData.getBlueprint());
            ClientFutureProcessor.queueBlueprint(
                    new ClientFutureProcessor.BlueprintProcessingData(StructurePacks.storeBlueprint(packName, compound, path), blueprint ->
                            new BuildToolPlacementMessage(
                                    type,
                                    id,
                                    packName,
                                    subpath.toString(),
                                    previewData.getPos(),
                                    Rotation.NONE,
                                    Mirror.NONE).sendToServer()));

            if (type == BuildToolPlacementMessage.HandlerType.Survival)
            {
                clearAndClose();
            }
        }
    }

    /**
     * Cancel the current structure.
     */
    @Override
    protected void cancelClicked()
    {
        width  = 1;
        height = 1;
        length = 1;
        frequency = 1;
        equation = "";
        hollow = false;
        shape = Shape.CUBE;
        mainBlock = new ItemStack(Blocks.GOLD_BLOCK);
        secondaryBlock = new ItemStack(Blocks.GOLD_BLOCK);
        clearAndClose();
    }

    /**
     * Cleanup the variables and close this.
     */
    private void clearAndClose()
    {
        RenderingCache.removeBlueprint("shapes");
        close();
    }

    /**
     * Called when the window is opened.
     */
    @Override
    public void onOpened()
    {
        // updateRotation(rotation);
        findPaneOfTypeByID(RESOURCE_ICON_MAIN, ItemIcon.class).setItem(mainBlock);
        findPaneOfTypeByID(RESOURCE_ICON_FILL, ItemIcon.class).setItem(secondaryBlock);
        findPaneOfTypeByID(BUTTON_UNDOREDO, Button.class).setVisible(isCreative());

        super.onOpened();
    }

    /**
     * Update the block from the replace block window.
     * @param stack the stack to set.
     * @param isMain if primary or secondary.
     */
    public void updateBlock(final ItemStack stack, final boolean isMain)
    {
        if (isMain)
        {
            mainBlock = stack;
        }
        else
        {
            secondaryBlock = stack;
        }
        findPaneOfTypeByID(isMain ? RESOURCE_ICON_MAIN : RESOURCE_ICON_FILL, ItemIcon.class).setItem(stack);
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
            updateStyle(sections.get(sectionsDropDownList.getSelectedIndex()).getA());
        }
    }

    /**
     * Update the style after change.
     *
     * @param s the style to use.
     */
    private void updateStyle(final Shape newShape)
    {
        if (newShape != shape)
        {
            shape = newShape;
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
        final String localequation = inputShape.getText();

        if (!widthText.isEmpty() && !lengthText.isEmpty() && !heightText.isEmpty())
        {
            try
            {
                final int localWidth = Integer.parseInt(widthText);
                final int localHeight = Integer.parseInt(heightText);
                final int localLength = Integer.parseInt(lengthText);
                final int localFrequency = Integer.parseInt(frequencyText);

                if (shapeHeight != localHeight || shapeLength != localLength || shapeWidth != localWidth || shapeFrequency != localFrequency
                      || !shapeequation.equals(localequation))
                {
                    this.shapeWidth = localWidth;
                    this.shapeLength = localLength;
                    this.shapeHeight = localHeight;
                    this.shapeFrequency = localFrequency;
                    this.shapeequation = localequation;
                    width = localWidth;
                    length = localLength;
                    height = localHeight;
                    frequency = localFrequency;
                    equation = localequation;
                    genShape();
                }
            }
            catch (NumberFormatException e)
            {
                inputWidth.setText(Integer.toString(width));
                inputLength.setText(Integer.toString(length));
                inputHeight.setText(Integer.toString(height));
            }
        }
        return result;
    }
}
