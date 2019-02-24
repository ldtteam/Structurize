package com.ldtteam.structurize.client.gui;

import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.api.util.LanguageHandler;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.blockout.Log;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.views.DropDownList;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.BuildToolPasteMessage;
import com.ldtteam.structurize.network.messages.LSStructureDisplayerMessage;
import com.ldtteam.structurize.network.messages.SchematicRequestMessage;
import com.ldtteam.structurize.network.messages.SchematicSaveMessage;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structures.helpers.Structure;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Array;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ldtteam.structurize.api.util.constant.Constants.MAX_MESSAGE_SIZE;
import static com.ldtteam.structurize.api.util.constant.WindowConstants.*;

/**
 * BuildTool window.
 */
public class WindowBuildTool extends AbstractWindowSkeleton
{
    /**
     * Enum of possibly free blocks for the normal player.
     */
    public enum FreeMode
    {
        SUPPLYSHIP,
        SUPPLYCAMP
    }

    /**
     * All possible rotations.
     */
    private static final int POSSIBLE_ROTATIONS = 4;

    /**
     * Rotation to rotateWithMirror right.
     */
    private static final int ROTATE_RIGHT = 1;

    /**
     * Rotation to rotateWithMirror 180 degree.
     */
    private static final int ROTATE_180 = 2;

    /**
     * Rotation to rotateWithMirror left.
     */
    private static final int ROTATE_LEFT = 3;

    /**
     * Id of the paste button.
     */
    private static final String BUTTON_PASTE = "pastecomplete";

    /**
     * Id of the paste nice button.
     */
    private static final String BUTTON_PASTE_NICE = "pastenice";

    /**
     * List of section.
     */
    @NotNull
    private final List<String> sections = new ArrayList<>();

    /**
     * List of style for the section.
     */
    @NotNull
    private List<String> styles = new ArrayList<>();

    /**
     * List of decorations or level possible to make with the style.
     */
    @NotNull
    private List<String> schematics = new ArrayList<>();

    /**
     * Current position the hut/decoration is rendered at.
     */
    @NotNull
    private BlockPos pos = new BlockPos(0, 0, 0);

    /**
     * Current rotation of the hut/decoration.
     */
    private int rotation = 0;

    /**
     * Drop down list for section.
     */
    private DropDownList sectionsDropDownList;

    /**
     * Drop down list for style.
     */
    private DropDownList stylesDropDownList;

    /**
     * Drop down list for schematic.
     */
    private DropDownList schematicsDropDownList;

    /**
     * Button to rename a scanned schematic.
     */
    private Button renameButton;

    /**
     * Button to delete a scanned schematic.
     */
    private Button deleteButton;

    /**
     * Confirmation dialog when deleting a scanned schematic.
     */
    private DialogDoneCancel confirmDeleteDialog;

    /**
     * Name of the static schematic if existent.
     */
    private String staticSchematicName = "";

    /**
     * Creates a window build tool for a specific structure.
     *
     * @param pos           the position.
     * @param structureName the structure name.
     * @param rotation      the rotation.
     * @param mode          the mode.
     */
    public WindowBuildTool(@Nullable final BlockPos pos, final String structureName, final int rotation, final WindowBuildTool.FreeMode mode)
    {
        super(Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX);

        if (!hasPermission())
        {
            return;
        }

        this.init(pos);
        if (pos != null)
        {
            Settings.instance.setupStaticMode(structureName, mode);
            staticSchematicName = structureName;
            Settings.instance.setRotation(rotation);
            this.rotation = rotation;
        }

        renameButton = findPaneOfTypeByID(BUTTON_RENAME, Button.class);
        deleteButton = findPaneOfTypeByID(BUTTON_DELETE, Button.class);
    }

    /**
     * Creates a window build tool.
     * This requires X, Y and Z coordinates.
     * If a structure is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used.
     *
     * @param pos coordinate.
     */
    public WindowBuildTool(@Nullable final BlockPos pos)
    {
        super(Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX);

        if (!hasPermission())
        {
            return;
        }

        this.init(pos);
        renameButton = findPaneOfTypeByID(BUTTON_RENAME, Button.class);
        deleteButton = findPaneOfTypeByID(BUTTON_DELETE, Button.class);
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

        initBuildingTypeNavigation();
        initStyleNavigation();
        initSchematicNavigation();

        //Register all necessary buttons with the window.
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_LEFT, this::moveLeftClicked);
        registerButton(BUTTON_MIRROR, WindowBuildTool::mirror);
        registerButton(BUTTON_RIGHT, this::moveRightClicked);
        registerButton(BUTTON_BACKWARD, this::moveBackClicked);
        registerButton(BUTTON_FORWARD, this::moveForwardClicked);
        registerButton(BUTTON_UP, WindowBuildTool::moveUpClicked);
        registerButton(BUTTON_DOWN, WindowBuildTool::moveDownClicked);
        registerButton(BUTTON_ROTATE_RIGHT, this::rotateRightClicked);
        registerButton(BUTTON_ROTATE_LEFT, this::rotateLeftClicked);
        registerButton(BUTTON_PASTE, this::pasteComplete);
        registerButton(BUTTON_PASTE_NICE, this::pasteNice);

        registerButton(BUTTON_RENAME, this::renameClicked);
        registerButton(BUTTON_DELETE, this::deleteClicked);
    }

    public void pasteNice()
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
        final String sname;
        if (Settings.instance.isStaticSchematicMode())
        {
            sname = Settings.instance.getStaticSchematicName();
        }
        else
        {
            sname = schematics.get(schematicsDropDownList.getSelectedIndex());
        }
        final StructureName structureName = new StructureName(sname);
        if (structureName.getPrefix().equals(Structures.SCHEMATICS_SCAN) && FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            //We need to check that the server have it too using the md5
            requestAndPlaceScannedSchematic(structureName, true, complete);
        }
        else
        {
            paste(structureName, complete);
        }
        Settings.instance.reset();
        close();
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
            final String name = sections.get(index);
            if (Structures.SCHEMATICS_SCAN.equals(name))
            {
                return LanguageHandler.format("com.ldtteam.structurize.gui.buildtool.scans");
            }
            else if (Structures.SCHEMATICS_PREFIX.equals(name))
            {
                return LanguageHandler.format("com.ldtteam.structurize.gui.buildtool.decorations");
            }
            //should be a something else.
            return getSectionName(name);
        }
    }

    /**
     * Get the correct name for the section.
     * Mods which need this should override this.
     * @param name the initial name.
     * @return the formatted name.
     */
    public String getSectionName(final String name)
    {
        return name;
    }

    /**
     * Initialise the previous/next and drop down list for section.
     */
    private void initBuildingTypeNavigation()
    {
        registerButton(BUTTON_PREVIOUS_TYPE_ID, this::previousSection);
        registerButton(BUTTON_NEXT_TYPE_ID, this::nextSection);
        sectionsDropDownList = findPaneOfTypeByID(DROPDOWN_TYPE_ID, DropDownList.class);
        sectionsDropDownList.setHandler(this::onDropDownListChanged);
        sectionsDropDownList.setDataProvider(new SectionDropDownList());
    }

    /**
     * Initialise the previous/next and drop down list for style.
     */
    private void initStyleNavigation()
    {
        registerButton(BUTTON_PREVIOUS_STYLE_ID, this::previousStyle);
        registerButton(BUTTON_NEXT_STYLE_ID, this::nextStyle);
        stylesDropDownList = findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class);
        stylesDropDownList.setHandler(this::onDropDownListChanged);
        stylesDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return styles.size();
            }

            @Override
            public String getLabel(final int index)
            {
                if (index >= 0 && index < styles.size())
                {
                    return styles.get(index);
                }
                return "";
            }
        });
    }

    /**
     * Initialise the previous/next and drop down list for schematic.
     */
    private void initSchematicNavigation()
    {
        registerButton(BUTTON_PREVIOUS_SCHEMATIC_ID, this::previousSchematic);
        registerButton(BUTTON_NEXT_SCHEMATIC_ID, this::nextSchematic);
        schematicsDropDownList = findPaneOfTypeByID(DROPDOWN_SCHEMATIC_ID, DropDownList.class);
        schematicsDropDownList.setHandler(this::onDropDownListChanged);
        schematicsDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return schematics.size();
            }

            @Override
            public String getLabel(final int index)
            {
                final StructureName sn = new StructureName(schematics.get(index));
                return sn.getLocalizedName();
            }
        });
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
        super.onOpened();
        if (!hasPermission())
        {
            close();
            return;
        }

        if (Settings.instance.isStaticSchematicMode())
        {
            sections.add(Structures.SCHEMATICS_PREFIX);
            setStructureName(staticSchematicName);
        }
        else
        {
            Structures.loadScannedStyleMaps();

            sections.clear();
            final InventoryPlayer inventory = this.mc.player.inventory;
            final List<String> allSections = Structures.getSections();
            for (final String section : allSections)
            {
                if (section.equals(Structures.SCHEMATICS_PREFIX) || section.equals(Structures.SCHEMATICS_SCAN) || inventoryHasHut(inventory, section))
                {
                    sections.add(section);
                }
            }

            if (Minecraft.getMinecraft().player.capabilities.isCreativeMode)
            {
                findPaneOfTypeByID(BUTTON_PASTE, Button.class).setVisible(true);
                findPaneOfTypeByID(BUTTON_PASTE_NICE, Button.class).setVisible(true);
            }
            else
            {
                findPaneOfTypeByID(BUTTON_PASTE, Button.class).setVisible(false);
                findPaneOfTypeByID(BUTTON_PASTE_NICE, Button.class).setVisible(false);
            }

            setStructureName(Settings.instance.getStructureName());
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (Manager.isSchematicDownloaded())
        {
            Manager.setSchematicDownloaded(false);
            changeSchematic();
        }
    }

    /**
     * Called when the window is closed.
     * If there is a current structure, its information is stored in {@link Settings}.
     * Also updates state via {@link LSStructureDisplayerMessage}
     */
    @Override
    public void onClosed()
    {
        if (Settings.instance.getActiveStructure() != null)
        {
            final ByteBuf buffer = Unpooled.buffer();
            
            Settings.instance.setSchematicInfo(schematics.get(schematicsDropDownList.getSelectedIndex()), rotation);
            Settings.instance.toBytes(buffer);
            Structurize.getNetwork().sendToServer(new LSStructureDisplayerMessage(buffer, true));
        }
    }

    /*
     * ---------------- Schematic Navigation Handling -----------------
     */

    /**
     * Change to the next section, Builder, Citizen ... Decorations and Scan.
     */
    private void nextSection()
    {
        sectionsDropDownList.selectNext();
    }

    /**
     * Change to the previous section, Builder, Citizen ... Decorations and Scan.
     */
    private void previousSection()
    {
        sectionsDropDownList.selectPrevious();
    }

    /**
     * Change to the next style.
     */
    private void nextStyle()
    {
        stylesDropDownList.selectNext();
    }

    /**
     * Change to the previous style.
     */
    private void previousStyle()
    {
        stylesDropDownList.selectPrevious();
    }

    /**
     * Update the styles list but try to keep the same one.
     */
    private void updateStyles()
    {
        String currentStyle = "";
        if (stylesDropDownList.getSelectedIndex() > -1 && stylesDropDownList.getSelectedIndex() < styles.size())
        {
            currentStyle = styles.get(stylesDropDownList.getSelectedIndex());
        }
        styles = Structures.getStylesFor(sections.get(sectionsDropDownList.getSelectedIndex()));
        int newIndex = styles.indexOf(currentStyle);
        if (newIndex == -1)
        {
            newIndex = 0;
        }

        final boolean enabled;
        if (Settings.instance.isStaticSchematicMode())
        {
            enabled = false;
        }
        else
        {
            enabled = styles.size() > 1;
        }

        findPaneOfTypeByID(BUTTON_PREVIOUS_STYLE_ID, Button.class).setEnabled(enabled);
        findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class).setEnabled(enabled);
        findPaneOfTypeByID(BUTTON_NEXT_STYLE_ID, Button.class).setEnabled(enabled);
        stylesDropDownList.setSelectedIndex(newIndex);
    }

    /*
     * ---------------- Button Handling -----------------
     */

    /**
     * Go to the next schematic.
     */
    private void nextSchematic()
    {
        schematicsDropDownList.selectNext();
    }

    /**
     * Go to the previous schematic.
     */
    private void previousSchematic()
    {
        schematicsDropDownList.selectPrevious();
    }

    /**
     * Update the list a available schematics.
     */
    private void updateSchematics()
    {
        String schematic = "";
        if (schematicsDropDownList.getSelectedIndex() > -1 && schematicsDropDownList.getSelectedIndex() < schematics.size())
        {
            schematic = schematics.get(schematicsDropDownList.getSelectedIndex());
        }
        final String currentSchematic = schematic.isEmpty() ? "" : (new StructureName(schematic)).getSchematic();
        final String section = sections.get(sectionsDropDownList.getSelectedIndex());
        final String style = styles.get(stylesDropDownList.getSelectedIndex());

        if (Settings.instance.isStaticSchematicMode())
        {
            schematics = new ArrayList<>();
            schematics.add(staticSchematicName);
        }
        else
        {
            schematics = Structures.getSchematicsFor(section, style);
        }
        int newIndex = -1;
        for (int i = 0; i < schematics.size(); i++)
        {
            final StructureName sn = new StructureName(schematics.get(i));
            if (sn.getSchematic().equals(currentSchematic))
            {
                newIndex = i;
                break;
            }
        }

        if (newIndex == -1)
        {
            newIndex = 0;
        }

        final boolean enabled;
        if (Settings.instance.isStaticSchematicMode())
        {
            enabled = false;
        }
        else
        {
            enabled = schematics.size() > 1;
        }

        findPaneOfTypeByID(BUTTON_PREVIOUS_SCHEMATIC_ID, Button.class).setEnabled(enabled);
        findPaneOfTypeByID(DROPDOWN_SCHEMATIC_ID, DropDownList.class).setEnabled(enabled);
        findPaneOfTypeByID(BUTTON_NEXT_SCHEMATIC_ID, Button.class).setEnabled(enabled);
        schematicsDropDownList.setSelectedIndex(newIndex);
    }

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
                final String name = sections.get(sectionsDropDownList.getSelectedIndex());
                if (Structures.SCHEMATICS_SCAN.equals(name))
                {
                    renameButton.setVisible(true);
                    deleteButton.setVisible(true);
                }
                else
                {
                    renameButton.setVisible(false);
                    deleteButton.setVisible(false);
                }
                updateStyles();
            }
            else if (list == stylesDropDownList)
            {
                updateSchematics();
            }
            else if (list == schematicsDropDownList)
            {
                changeSchematic();
            }
        }
    }

    /**
     * Set the structure name.
     *
     * @param structureName name of the structure name
     *                      Ex: schematics/wooden/Builder2
     */
    private void setStructureName(final String structureName)
    {
        if (structureName != null)
        {
            final StructureName sn = new StructureName(structureName);
            final int sectionIndex = sections.indexOf(sn.getSection());
            if (sectionIndex != -1)
            {
                sectionsDropDownList.setSelectedIndex(sectionIndex);
                final int styleIndex = styles.indexOf(sn.getStyle());
                if (styleIndex != -1)
                {
                    stylesDropDownList.setSelectedIndex(styleIndex);
                    final int schematicIndex = schematics.indexOf(sn.toString());
                    if (schematicIndex != -1)
                    {
                        schematicsDropDownList.setSelectedIndex(schematicIndex);
                        return;
                    }
                }
            }
        }

        //We did not find the structure, select the first of each
        sectionsDropDownList.setSelectedIndex(0);
        stylesDropDownList.setSelectedIndex(0);
        schematicsDropDownList.setSelectedIndex(0);
    }

    /**
     * Check if the player inventory has a certain hut.
     *
     * @param inventory the player inventory.
     * @param hut       the hut.
     * @return true if so.
     */
    private static boolean inventoryHasHut(@NotNull final InventoryPlayer inventory, final String hut)
    {
        return inventory.hasItemStack(new ItemStack(Block.getBlockFromName(Constants.MINECOLONIES_MOD_ID + HUT_PREFIX + hut)));
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
        rotation = (rotation + ROTATE_RIGHT) % POSSIBLE_ROTATIONS;
        updateRotation(rotation);
    }

    /**
     * Rotate the structure counter clockwise.
     */
    private void rotateLeftClicked()
    {
        rotation = (rotation + ROTATE_LEFT) % POSSIBLE_ROTATIONS;
        updateRotation(rotation);
    }


    /*
     * ---------------- Miscellaneous ----------------
     */

    /**
     * Changes the current structure.
     * Set to button position at that time
     */
    private void changeSchematic()
    {
        if (!Settings.instance.isStaticSchematicMode())
        {
            Settings.instance.setStructureName(schematics.get(schematicsDropDownList.getSelectedIndex()));
        }

        commonStructureUpdate();

        if (Settings.instance.getPosition() == null)
        {
            Settings.instance.setPosition(this.pos);
        }
    }

    /**
     * Changes the current structure.
     */
    public static void commonStructureUpdate()
    {
        final String sname;

        if (Settings.instance.isStaticSchematicMode())
        {
            sname = Settings.instance.getStaticSchematicName();
        }
        else
        {
            sname = Settings.instance.getStructureName();
        }
        
        if (sname == null)
        {
            return;
        }

        final StructureName structureName = new StructureName(sname);
        final String md5 = Structures.getMD5(structureName.toString());
        final Structure structure = new Structure(Minecraft.getMinecraft().world, structureName.toString(),
            new PlacementSettings(Settings.instance.getMirror(), BlockUtils.getRotation(Settings.instance.getRotation())));

        if (structure.isBluePrintMissing() || !structure.isCorrectMD5(md5))
        {
            if (structure.isBluePrintMissing())
            {
                Log.getLogger().info("Blueprint structure " + structureName + " missing");
            }
            else
            {
                Log.getLogger().info("structure " + structureName + " md5 error");
            }

            Log.getLogger().info("Request To Server for structure " + structureName);
            if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
            {
                Structurize.getNetwork().sendToServer(new SchematicRequestMessage(structureName.toString()));
                return;
            }
            else
            {
                Log.getLogger().error("WindowBuildTool: Need to download schematic on a standalone client/server. This should never happen");
            }
        }
        Settings.instance.setStructureName(structureName.toString());
        Settings.instance.setActiveSchematic(structure);
    }

    /**
     * Request to build a player scan.
     *
     * @param paste         if it should be pasted.
     * @param complete      if pasted, should it be complete.
     * @param structureName of the scan to be built.
     */
    public void requestAndPlaceScannedSchematic(@NotNull final StructureName structureName, final boolean paste, final boolean complete)
    {
        if (!Structures.isPlayerSchematicsAllowed())
        {
            return;
        }

        if (Structures.hasMD5(structureName))
        {
            final String md5 = Structures.getMD5(structureName.toString());
            final String serverSideName = Structures.SCHEMATICS_CACHE + '/' + md5;
            if (!Structures.hasMD5(new StructureName(serverSideName)))
            {
                final InputStream stream = StructureLoadingUtils.getStream(structureName.toString());
                if (stream != null)
                {
                    final UUID id = UUID.randomUUID();
                    final byte[] structureAsByteArray = StructureLoadingUtils.getStreamAsByteArray(stream);

                    if (structureAsByteArray.length <= MAX_MESSAGE_SIZE)
                    {
                        Structurize.getNetwork().sendToServer(new SchematicSaveMessage(structureAsByteArray, id, 1, 1));
                    }
                    else
                    {
                        final int pieces = structureAsByteArray.length / MAX_MESSAGE_SIZE;

                        Log.getLogger().info("BuilderTool: sending: " + pieces + " pieces with the schematic " + structureName + "(md5:" + md5 + ") to the server");
                        for (int i = 1; i <= pieces; i++)
                        {
                            final int start = (i - 1) * MAX_MESSAGE_SIZE;
                            final int size;
                            if (i == pieces)
                            {
                                size = structureAsByteArray.length - (start);
                            }
                            else
                            {
                                size = MAX_MESSAGE_SIZE;
                            }
                            final byte[] bytes = new byte[size];
                            Array.copy(structureAsByteArray, start, bytes, 0, size);
                            Structurize.getNetwork().sendToServer(new SchematicSaveMessage(bytes, id, pieces, i));
                        }
                    }
                }
                else
                {
                    Log.getLogger().warn("BuildTool: Can not load " + structureName);
                }
            }
            else
            {
                Log.getLogger().warn("BuildTool: server does not have " + serverSideName);
            }

            if (paste || pasteDirectly())
            {
                Structurize.getNetwork().sendToServer(new BuildToolPasteMessage(
                  serverSideName,
                  structureName.toString(),
                  Settings.instance.getPosition(),
                  BlockUtils.getRotation(Settings.instance.getRotation()),
                  false,
                  Settings.instance.getMirror(),
                  complete, null));
            }
            else
            {
                place(new StructureName(serverSideName));
            }
        }
        else
        {
            if (pasteDirectly())
            {
                paste(structureName, complete);
            }
            else
            {
                place(structureName);
            }
            Log.getLogger().warn("BuilderTool: Can not send schematic without md5: " + structureName);
        }
    }

    /**
     * Request to send a scanned schematic to the server.
     *
     * @param structureName of the scan to be built.
     */
    public static void requestScannedSchematic(@NotNull final StructureName structureName)
    {
        if (!Structures.isPlayerSchematicsAllowed())
        {
            return;
        }

        if (Structures.hasMD5(structureName))
        {
            final String md5 = Structures.getMD5(structureName.toString());
            final String serverSideName = Structures.SCHEMATICS_CACHE + '/' + md5;
            if (!Structures.hasMD5(new StructureName(serverSideName)))
            {
                final InputStream stream = StructureLoadingUtils.getStream(structureName.toString());
                if (stream != null)
                {
                    final UUID id = UUID.randomUUID();
                    final byte[] structureAsByteArray = StructureLoadingUtils.getStreamAsByteArray(stream);

                    if (structureAsByteArray.length <= MAX_MESSAGE_SIZE)
                    {
                        Structurize.getNetwork().sendToServer(new SchematicSaveMessage(structureAsByteArray, id, 1, 1));
                    }
                    else
                    {
                        final int pieces = structureAsByteArray.length / MAX_MESSAGE_SIZE;

                        Log.getLogger().info("BuilderTool: sending: " + pieces + " pieces with the schematic " + structureName + "(md5:" + md5 + ") to the server");
                        for (int i = 1; i <= pieces; i++)
                        {
                            final int start = (i - 1) * MAX_MESSAGE_SIZE;
                            final int size;
                            if (i == pieces)
                            {
                                size = structureAsByteArray.length - (start);
                            }
                            else
                            {
                                size = MAX_MESSAGE_SIZE;
                            }
                            final byte[] bytes = new byte[size];
                            Array.copy(structureAsByteArray, start, bytes, 0, size);
                            Structurize.getNetwork().sendToServer(new SchematicSaveMessage(bytes, id, pieces, i));
                        }
                    }
                }
                else
                {
                    Log.getLogger().warn("BuilderTool: Can not load " + structureName);
                }
            }
            else
            {
                Log.getLogger().warn("BuilderTool: server does not have " + serverSideName);
            }
        }
        else
        {
            Log.getLogger().warn("BuilderTool: Can not send schematic without md5: " + structureName);
        }
    }


    /**
     * Override if place without paste is required.
     * @param structureName
     */
    public void place(final StructureName structureName)
    {

    }

    /**
     * Override if check and place without paste is required.
     */
    public void checkAndPlace()
    {

    }

    /**
     * Defines if a player has permission to use this.
     *
     * @return true if so.
     */
    public boolean hasPermission()
    {
        return Minecraft.getMinecraft().player.capabilities.isCreativeMode;
    }

    /**
     * If a schematic should be pasted instantly.
     * @return true if so.
     */
    public boolean pasteDirectly()
    {
        return true;
    }

    /**
     * Method to directly paste a structure.
     */
    public void paste(final StructureName name, final boolean complete)
    {
        Structurize.getNetwork().sendToServer(new BuildToolPasteMessage(
          name.toString(),
          name.toString(),
          Settings.instance.getPosition(),
          BlockUtils.getRotation(Settings.instance.getRotation()),
          false,
          Settings.instance.getMirror(),
          complete, Settings.instance.getFreeMode()));
    }

    /**
     * Send a packet telling the server to place the current structure.
     */
    private void confirmClicked()
    {
        if (Settings.instance.isStaticSchematicMode() && Settings.instance.getActiveStructure() != null)
        {
            checkAndPlace();
        }
        else
        {
            final StructureName structureName = new StructureName(schematics.get(schematicsDropDownList.getSelectedIndex()));
            if (structureName.getPrefix().equals(Structures.SCHEMATICS_SCAN) && FMLCommonHandler.instance().getMinecraftServerInstance() == null)
            {
                //We need to check that the server have it too using the md5
                requestAndPlaceScannedSchematic(structureName, false, false);
            }
            else
            {
                if (pasteDirectly())
                {
                    paste(structureName, false);
                }
                else
                {
                    place(structureName);
                }
            }

            if (!GuiScreen.isShiftKeyDown())
            {
                cancelClicked();
            }
        }
    }

    /**
     * Cancel the current structure.
     */
    public void cancelClicked()
    {
        Settings.instance.reset();
        Structurize.getNetwork().sendToServer(new LSStructureDisplayerMessage(Unpooled.buffer(), false));
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
            case ROTATE_RIGHT:
                settings.setRotation(Rotation.CLOCKWISE_90);
                break;
            case ROTATE_180:
                settings.setRotation(Rotation.CLOCKWISE_180);
                break;
            case ROTATE_LEFT:
                settings.setRotation(Rotation.COUNTERCLOCKWISE_90);
                break;
            default:
                settings.setRotation(Rotation.NONE);
        }
        Settings.instance.setRotation(rotation);
        settings.setMirror(Settings.instance.getMirror());
        if (Settings.instance.getActiveStructure() != null)
        {
            Settings.instance.getActiveStructure().setPlacementSettings(settings);
        }
    }

    /**
     * Action performed when rename button is clicked.
     */
    private void renameClicked()
    {
        final StructureName structureName = new StructureName(schematics.get(schematicsDropDownList.getSelectedIndex()));
        @NotNull final WindowStructureNameEntry window = new WindowStructureNameEntry(structureName);
        window.open();
    }

    /**
     * Action performed when rename button is clicked.
     */
    private void deleteClicked()
    {
        confirmDeleteDialog = new DialogDoneCancel(getWindow());
        confirmDeleteDialog.setHandler(this::onDialogClosed);
        final StructureName structureName = new StructureName(schematics.get(schematicsDropDownList.getSelectedIndex()));
        confirmDeleteDialog.setTitle(LanguageHandler.format("com.minecolonies.gui.structure.delete.title"));
        confirmDeleteDialog.setTextContent(LanguageHandler.format("com.minecolonies.gui.structure.delete.body", structureName.toString()));
        confirmDeleteDialog.open();
    }

    /**
     * handle when a dialog is closed.
     *
     * @param dialog   which is being closed.
     * @param buttonId is the id of the button used to close the dialog.
     */
    public void onDialogClosed(final DialogDoneCancel dialog, final int buttonId)
    {
        if (dialog == confirmDeleteDialog && buttonId == DialogDoneCancel.DONE)
        {
            final StructureName structureName = new StructureName(schematics.get(schematicsDropDownList.getSelectedIndex()));
            if (Structures.SCHEMATICS_SCAN.equals(structureName.getPrefix())
                  && Structures.deleteScannedStructure(structureName))
            {
                Structures.loadScannedStyleMaps();
                if (schematics.size() > 1)
                {
                    schematicsDropDownList.selectNext();
                    stylesDropDownList.setSelectedIndex(stylesDropDownList.getSelectedIndex());
                }
                else if (styles.size() > 1)
                {
                    stylesDropDownList.selectNext();
                }
                else
                {
                    sectionsDropDownList.selectNext();
                }
            }
        }
    }
}
