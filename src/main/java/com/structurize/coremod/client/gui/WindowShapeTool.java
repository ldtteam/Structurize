package com.structurize.coremod.client.gui;

import com.structurize.api.util.BlockUtils;
import com.structurize.api.util.LanguageHandler;
import com.structurize.api.util.Log;
import com.structurize.api.util.constant.Constants;
import com.structurize.blockout.controls.Button;
import com.structurize.blockout.views.DropDownList;
import com.structurize.coremod.Structurize;
import com.structurize.coremod.management.Manager;
import com.structurize.coremod.management.StructureName;
import com.structurize.coremod.management.Structures;
import com.structurize.coremod.network.messages.BuildToolPasteMessage;
import com.structurize.coremod.network.messages.SchematicRequestMessage;
import com.structurize.coremod.network.messages.SchematicSaveMessage;
import com.structurize.coremod.network.messages.UndoMessage;
import com.structurize.structures.helpers.Settings;
import com.structurize.structures.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Array;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
     * Current rotation of the hut/decoration.
     */
    private int rotation = 0;

    /**
     * Current position the hut/decoration is rendered at.
     */
    @NotNull
    private BlockPos pos = new BlockPos(0, 0, 0);

    /**
     * Creates a window build tool.
     * This requires X, Y and Z coordinates.
     * If a structure is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used.
     *
     * @param pos coordinate.
     */
    public WindowShapeTool(@Nullable final BlockPos pos)
    {
        super(Constants.MOD_ID + SHAPE_TOOL_RESOURCE_SUFFIX);
        this.init(pos);
    }

    private void init(final BlockPos pos)
    {
        @Nullable final Structure structure = Settings.instance.getActiveStructure();

        if (structure != null)
        {
            rotation = Settings.instance.getRotation();
        }
        else if (pos != null)
        {
            this.pos = pos;
            Settings.instance.setPosition(pos);
            Settings.instance.setRotation(0);
        }

        //Register all necessary buttons with the window.
        registerButton(BUTTON_CONFIRM, this::pasteNice);
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
        registerButton(BUTTON_PASTE, this::pasteComplete);

        registerButton(UNDO_BUTTON, this::undoClicked);
    }

    /**
     * Undo the last change.
     */
    private void undoClicked()
    {
        Structurize.getNetwork().sendToServer(new UndoMessage());
    }

    private void pasteNice()
    {
        paste(false);
    }

    /**
     * Paste a schematic in the world.
     */
    private void pasteComplete()
    {
        paste(true);
    }

    /**
     * Paste a schematic in the world.
     *
     * @param complete if complete paste or partial.
     */
    private void paste(final boolean complete)
    {
        //** TODO: Ray adds stuff here **//
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

        sections.clear();
        final List<String> allSections = Structures.getSections();
        for (final String section : allSections)
        {
            if (section.equals(Structures.SCHEMATICS_PREFIX) || section.equals(Structures.SCHEMATICS_SCAN))
            {
                sections.add(section);
            }
        }

        findPaneOfTypeByID(BUTTON_PASTE, Button.class).setVisible(true);
        findPaneOfTypeByID(UNDO_BUTTON, Button.class).setVisible(true);
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
