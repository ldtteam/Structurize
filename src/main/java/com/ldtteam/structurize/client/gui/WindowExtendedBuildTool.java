package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.View;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blockentities.interfaces.ILeveledBlueprintAnchorBlock;
import com.ldtteam.structurize.blockentities.interfaces.INamedBlueprintAnchorBlock;
import com.ldtteam.structurize.blockentities.interfaces.IRequirementsBlueprintAnchorBlock;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintTagUtils;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.storage.ISurvivalBlueprintHandler;
import com.ldtteam.structurize.storage.StructurePackMeta;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.SurvivalBlueprintHandlers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.ldtteam.structurize.api.util.constant.Constants.*;
import static com.ldtteam.structurize.api.util.constant.GUIConstants.*;
import static com.ldtteam.structurize.api.util.constant.WindowConstants.*;

/**
 * BuildTool window.
 */
public class WindowExtendedBuildTool extends AbstractWindowSkeleton
{
    /**
     * Ground style of the caller.
     */
    private int groundstyle;

    /**
     * Folder scrolling list.
     */
    private ScrollingList folderList;

    /**
     * Blueprint scrolling list.
     */
    private ScrollingList blueprintList;

    /**
     * Placement scrolling list.
     */
    private ScrollingList placementList;

    /**
     * Current selected structure pack.
     */
    private static StructurePackMeta structurePack;

    /**
     * Next depth to open.
     */
    private static String nextDepth = "";

    /**
     * Current depth to display.
     */
    private static String depth = "";

    /**
     * List of categories at the current depth.
     */
    private static Future<List<StructurePacks.Category>>  categoryList = null;

    /**
     * Next level of depth categories.
     */
    private static Map<String, Future<List<StructurePacks.Category>>> nextDepthMeta = new HashMap<>();

    /**
     * Blueprints at depth.
     */
    private static Map<String, Future<List<Blueprint>>> blueprintsAtDepth = new HashMap<>();

    /**
     * Type of button.
     */
    public enum ButtonType
    {
        Blueprint,
        Leveled_Blueprint,
        SubCategory,
        Back
    }

    /**
     * Creates a window build tool.
     * This requires X, Y and Z coordinates.
     * If a structure is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used.
     *
     * @param pos coordinate.
     * @param groundstyle one of the GROUNDSTYLE_ values.
     */
    public WindowExtendedBuildTool(@Nullable final BlockPos pos, final int groundstyle)
    {
        super(MOD_ID + BUILD_TOOL_PLUS_PLUS_RESOURCE_SUFFIX);
        this.init(pos, groundstyle);
    }

    @SuppressWarnings("resource")
    private void init(final BlockPos pos, final int groundstyle)
    {
        this.groundstyle = groundstyle;
        if (pos != null)
        {
            Settings.instance.setPosition(pos);
            adjustToGroundOffset();
        }

        //if (structurePack == null)
        {
            //todo get it from the "StructurePacks" static
            //todo we need a way to get it from the colony?
            structurePack = StructurePacks.packMetas.get("Moroccan");
        }

        // Register all necessary buttons with the window.
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_LEFT, this::moveLeftClicked);
        registerButton(BUTTON_MIRROR, this::mirror);
        registerButton(BUTTON_RIGHT, this::moveRightClicked);
        registerButton(BUTTON_BACKWARD, this::moveBackClicked);
        registerButton(BUTTON_FORWARD, this::moveForwardClicked);
        registerButton(BUTTON_UP, WindowExtendedBuildTool::moveUpClicked);
        registerButton(BUTTON_DOWN, WindowExtendedBuildTool::moveDownClicked);
        registerButton(BUTTON_ROTATE_RIGHT, this::rotateRightClicked);
        registerButton(BUTTON_ROTATE_LEFT, this::rotateLeftClicked);

        folderList = findPaneOfTypeByID("subcategories", ScrollingList.class);
        blueprintList = findPaneOfTypeByID("blueprints", ScrollingList.class);
        placementList = findPaneOfTypeByID("placement", ScrollingList.class);

        findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(structurePack.getName() + "/" + depth + (Settings.instance.getActiveStructure() == null ? "" : ("/" + Settings.instance.getActiveStructure().getName()))));
        //todo level selector simple list
        categoryList = StructurePacks.getCategoriesFuture(structurePack.getName(), "");

        if (Settings.instance.getActiveStructure() == null)
        {
            findPaneOfTypeByID("manipulator", View.class).setVisible(false);
        }
        else
        {
            findPaneOfTypeByID("manipulator", View.class).setVisible(true);
        }
    }

    private void cancelClicked()
    {

    }

    private void confirmClicked()
    {
        if (Settings.instance.getActiveStructure() != null)
        {
            if (!Minecraft.getInstance().player.isCreative())
            {
                final List<ISurvivalBlueprintHandler> handlers = SurvivalBlueprintHandlers.getMatchingHandlers(Settings.instance.getActiveStructure(), Minecraft.getInstance().level, Minecraft.getInstance().player, Settings.instance.getPosition(), Settings.instance.getPlacementSettings());
                if (handlers.isEmpty())
                {
                    // todo notify player that there is no survival handler registered.
                    return;
                }

                if (handlers.size() == 1)
                {
                    // Handle placement directly with handler id.
                    return;
                }
            }
            updatePlacement();
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (categoryList != null && categoryList.isDone())
        {
            //todo only display the "manipulation menu" when an actual schematic is selected
            final View categoryView = findPaneOfTypeByID("categories", View.class);
            if (!categoryView.getChildren().isEmpty())
            {
                categoryView.getChildren().clear();
            }
            try
            {
                int index = 0;
                for (final StructurePacks.Category category : categoryList.get())
                {
                    final ButtonImage img = new ButtonImage();
                    if (category.hasIcon)
                    {
                        try
                        {
                            img.setImage(OutOfJarResourceLocation.of(MOD_ID, category.packMeta.getPath().resolve(category.subPath).resolve("icon.png")), false);
                            img.setImageDisabled(OutOfJarResourceLocation.of(MOD_ID, category.packMeta.getPath().resolve(category.subPath).resolve("icon_disabled.png")), false);
                        }
                        catch (final Exception ex)
                        {
                            img.setImage(new ResourceLocation(DEFAULT_ICON), false);
                        }
                    }
                    else
                    {
                        img.setImage(new ResourceLocation(DEFAULT_ICON), false);
                    }

                    final String id = category.subPath;
                    img.setSize(19, 19);
                    img.setPosition(index * 20, 0);
                    img.setID(id);

                    categoryView.addChild(img);
                    PaneBuilders.tooltipBuilder().hoverPane(img).build().setText(new TextComponent(id));

                    if (category.isTerminal)
                    {
                        blueprintsAtDepth.put(id, StructurePacks.getBlueprintsFuture(structurePack.getName(), id));
                    }
                    else
                    {
                        nextDepthMeta.put(id, StructurePacks.getCategoriesFuture(structurePack.getName(), id));
                    }

                    index++;
                }
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            categoryList = null;
        }

        if (!nextDepth.isEmpty() && nextDepthMeta.containsKey(nextDepth))
        {
            final Future<List<StructurePacks.Category>> subCategories = nextDepthMeta.get(nextDepth);
            if (subCategories.isDone())
            {
                try
                {
                    final List<StructurePacks.Category> subCats = subCategories.get();
                    if (subCats.isEmpty())
                    {
                        nextDepthMeta.remove(nextDepth);
                        blueprintsAtDepth.put(nextDepth, StructurePacks.getBlueprintsFuture(id, nextDepth));
                    }
                    else
                    {
                        subCats.sort(Comparator.comparing((a) -> a.subPath));
                        for (final StructurePacks.Category subCat : subCats)
                        {
                            String id = subCat.subPath.endsWith("/") ? subCat.subPath.substring(0, subCat.subPath.length() - 1) : subCat.subPath;
                            if (id.startsWith("/"))
                            {
                                id = id.substring(1);
                            }

                            if (subCat.isTerminal)
                            {
                                blueprintsAtDepth.put(id, StructurePacks.getBlueprintsFuture(structurePack.getName(), id));
                            }
                            else
                            {
                                nextDepthMeta.put(id, StructurePacks.getCategoriesFuture(structurePack.getName(), id));
                            }
                        }
                        updateFolders(subCats);
                        nextDepth = "";
                    }
                }
                catch (final Exception ex)
                {
                    Log.getLogger().error("Something happened when loading subcategories", ex);
                }
            }
        }
        else if (!nextDepth.isEmpty() && blueprintsAtDepth.containsKey(nextDepth))
        {
            final Future<List<Blueprint>> blueprints = blueprintsAtDepth.get(nextDepth);
            if (blueprints.isDone())
            {
                try
                {
                    updateBlueprints(blueprints.get(), nextDepth);
                    nextDepth = "";
                }
                catch (InterruptedException | ExecutionException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updatePlacement()
    {
        placementList.enable();
        placementList.show();
        blueprintList.disable();
        blueprintList.hide();
        folderList.hide();
        folderList.disable();

        final List<Component> categories = new ArrayList<>();
        if (Minecraft.getInstance().player.isCreative())
        {
            categories.add(new TranslatableComponent("structurize.gui.buildtool.complete"));
            categories.add(new TranslatableComponent("structurize.gui.buildtool.pretty"));
        }

        if (Settings.instance.getActiveStructure() != null)
        {
            for (final ISurvivalBlueprintHandler handler : SurvivalBlueprintHandlers.getMatchingHandlers(Settings.instance.getActiveStructure(), Minecraft.getInstance().level, Minecraft.getInstance().player, Settings.instance.getPosition(), Settings.instance.getPlacementSettings()))
            {
                categories.add(handler.getDisplayName());
            }
        }

        //Creates a dataProvider for the unemployed resourceList.
        placementList.setDataProvider(new ScrollingList.DataProvider()
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
                buttonImage.setText(categories.get(index));
                buttonImage.setTextColor(ChatFormatting.BLACK.getColor());
                //todo handle button click.
                //todo redirect potential to custom handler
            }
        });
    }

    public void updateFolders(final List<StructurePacks.Category> inputCategories)
    {
        folderList.enable();
        folderList.show();
        blueprintList.disable();
        blueprintList.hide();
        placementList.hide();
        placementList.disable();

        final List<ButtonData> categories = new ArrayList<>();
        if (!inputCategories.isEmpty())
        {
            String parentCat = "";
            if (nextDepth.contains("/"))
            {
                final String[] split = nextDepth.split("/");
                final String currentCat = split[split.length - 1].equals("") ? split[split.length - 2] : split[split.length - 1];
                parentCat = nextDepth.replace("/" + currentCat, "");
            }
            categories.add(new ButtonData(ButtonType.Back, parentCat));
        }

        for (final StructurePacks.Category category :inputCategories)
        {
            categories.add(new ButtonData(ButtonType.SubCategory, category));
        }

        if (categories.size() <= 3)
        {
            folderList.setSize(270,20);
            folderList.setPosition(100, 180);
        }
        else if (categories.size() > 6)
        {
            folderList.setSize(270,60);
            folderList.setPosition(100, 140);
        }
        else
        {
            folderList.setSize(270,40);
            folderList.setPosition(100, 160);
        }

        //Creates a dataProvider for the unemployed resourceList.
        folderList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return (int) Math.ceil(categories.size() / 3.0);
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
                for (int i = 1; i <= 3; i++)
                {
                    int catIndex = index * 3 + i - 1;
                    if (categories.size() > catIndex)
                    {
                        handleSubCat(categories.get(catIndex), rowPane, i);
                    }
                }
            }
        });
    }

    public void updateBlueprints(final List<Blueprint> inputBluePrints, final String depth)
    {
        blueprintList.enable();
        blueprintList.show();
        folderList.disable();
        folderList.hide();
        placementList.hide();
        placementList.disable();

        final List<ButtonData> blueprints = new ArrayList<>();
        if (!inputBluePrints.isEmpty())
        {
            String parentCat = "";
            if (depth.contains("/"))
            {
                final String[] split = depth.split("/");
                final String currentCat = split[split.length - 1].equals("") ? split[split.length - 2] : split[split.length - 1];
                parentCat = depth.replace("/" + currentCat, "");
            }
            blueprints.add(new ButtonData(ButtonType.Back, parentCat));
        }

        final Map<String, List<Blueprint>> leveledBlueprints = new HashMap<>();

        for (final Blueprint blueprint : inputBluePrints)
        {
            final BlockState anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
            if (anchor.getBlock() instanceof ILeveledBlueprintAnchorBlock)
            {
                final int level = ((ILeveledBlueprintAnchorBlock) anchor.getBlock()).getLevel(blueprint.getTileEntityData(Settings.instance.getPosition().offset(blueprint.getPrimaryBlockOffset()), blueprint.getPrimaryBlockOffset()));
                final String name = blueprint.getName().replace(Integer.toString(level), "");
                final List<Blueprint> blueprintList = leveledBlueprints.getOrDefault(name, new ArrayList<>());
                blueprintList.add(blueprint);
                leveledBlueprints.put(name, blueprintList);
            }
            else
            {
                blueprints.add(new ButtonData(ButtonType.Blueprint, blueprint));
            }
        }

        for (final Map.Entry<String, List<Blueprint>> entry : leveledBlueprints.entrySet())
        {
            blueprints.add(new ButtonData(ButtonType.Leveled_Blueprint, entry.getValue()));
        }

        if (blueprints.size() <= 3)
        {
            blueprintList.setSize(270,20);
            blueprintList.setPosition(100, 180);
        }
        else if (blueprints.size() > 6)
        {
            blueprintList.setSize(270,60);
            blueprintList.setPosition(100, 140);
        }
        else
        {
            blueprintList.setSize(270,40);
            blueprintList.setPosition(100, 160);
        }

        //Creates a dataProvider for the unemployed resourceList.
        blueprintList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return (int) Math.ceil(blueprints.size() / 3.0);
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
                for (int i = 1; i <= 3; i++)
                {
                    int catIndex = index * 3 + i - 1;
                    if (blueprints.size() > catIndex)
                    {
                        handleBlueprint(blueprints.get(catIndex), rowPane, i, depth);
                    }
                }
            }
        });
    }

    private void handleBlueprint(final ButtonData buttonData, final Pane rowPane, final int index, final String depth)
    {
        ButtonImage img = rowPane.findPaneOfTypeByID(Integer.toString(index), ButtonImage.class);
        if (buttonData.type == ButtonType.Back)
        {
            if (img == null)
            {
                img = rowPane.findPaneOfTypeByID("back:" + buttonData.data, ButtonImage.class);
            }
            img.setID("back:" + buttonData.data);
            img.setVisible(true);
            img.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/back_medium.png"), false);
            PaneBuilders.tooltipBuilder().hoverPane(img).build().setText(new TextComponent("back"));
        }
        else if (buttonData.type == ButtonType.Leveled_Blueprint)
        {
            final List<Blueprint> blueprints = (List<Blueprint>) buttonData.data;
            final Blueprint firstBlueprint = blueprints.get(0);
            final BlockState anchor = firstBlueprint.getBlockState(firstBlueprint.getPrimaryBlockOffset());
            final int level = ((ILeveledBlueprintAnchorBlock) anchor.getBlock()).getLevel(firstBlueprint.getTileEntityData(Settings.instance.getPosition().offset(firstBlueprint.getPrimaryBlockOffset()), firstBlueprint.getPrimaryBlockOffset()));

            final String id = firstBlueprint.getName().replace(Integer.toString(level), "");
            if (img == null)
            {
                img = rowPane.findPaneOfTypeByID(depth + ":" + id, ButtonImage.class);
            }

            //todo, now, on click we don't want to render! we want to render a number switcher for the levels)

            img.setID(depth + ":" + id);
            final List<MutableComponent> toolTip = new ArrayList<>();
            final TextComponent desc = new TextComponent(id.split("/")[id.split("/").length - 1]);
            if (anchor.getBlock() instanceof INamedBlueprintAnchorBlock)
            {
                img.setText(((INamedBlueprintAnchorBlock) anchor.getBlock()).getBlueprintDisplayName());
                toolTip.addAll(((INamedBlueprintAnchorBlock) anchor.getBlock()).getDesc());
            }
            else
            {
                img.setText(desc);
            }
            img.setVisible(true);

            boolean hasMatch = false;
            for (final Blueprint blueprint : blueprints)
            {
                if (blueprint.equals(Settings.instance.getActiveStructure()))
                {
                    hasMatch = true;
                    break;
                }
            }

            if (anchor.getBlock() instanceof IRequirementsBlueprintAnchorBlock)
            {
                toolTip.addAll(((IRequirementsBlueprintAnchorBlock) anchor.getBlock()).getRequirements(Minecraft.getInstance().level, Settings.instance.getPosition(), Minecraft.getInstance().player));
                if (!((IRequirementsBlueprintAnchorBlock) anchor.getBlock()).areRequirementsMet(Minecraft.getInstance().level, Settings.instance.getPosition(), Minecraft.getInstance().player))
                {
                    PaneBuilders.tooltipBuilder().hoverPane(img).build().setText(toolTip);
                    img.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/disabled_blueprint_medium.png"), false);
                    return;
                }
            }
            PaneBuilders.tooltipBuilder().hoverPane(img).build().setText(toolTip);

            if (hasMatch)
            {
                img.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/selected_blueprint_medium.png"), false);
            }
            else
            {
                img.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/blueprint_medium.png"), false);
            }
        }
        else if (buttonData.type == ButtonType.Blueprint)
        {
            final Blueprint blueprint = (Blueprint) buttonData.data;
            final String id = blueprint.getName();
            if (img == null)
            {
                img = rowPane.findPaneOfTypeByID(depth + ":" + id, ButtonImage.class);
            }

            img.setID(depth + ":" + id);
            final Component desc;
            final Block anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset()).getBlock();
            if (anchor instanceof INamedBlueprintAnchorBlock)
            {
                desc = ((INamedBlueprintAnchorBlock) anchor).getBlueprintDisplayName();
            }
            else
            {
                desc = new TextComponent(id.split("/")[id.split("/").length - 1]);
            }
            img.setText(desc);
            img.setVisible(true);

            if (anchor instanceof IRequirementsBlueprintAnchorBlock)
            {
                PaneBuilders.tooltipBuilder().hoverPane(img).build().setText(((IRequirementsBlueprintAnchorBlock) anchor).getRequirements(Minecraft.getInstance().level, Settings.instance.getPosition(), Minecraft.getInstance().player));
                if (!((IRequirementsBlueprintAnchorBlock) anchor).areRequirementsMet(Minecraft.getInstance().level, Settings.instance.getPosition(), Minecraft.getInstance().player))
                {
                    img.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/disabled_blueprint_medium.png"), false);
                    return;
                }
            }

            if (blueprint.equals(Settings.instance.getActiveStructure()))
            {
                img.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/selected_blueprint_medium.png"), false);
            }
            else
            {
                img.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/blueprint_medium.png"), false);
            }
        }
    }

    private void handleSubCat(final ButtonData buttonData, final Pane rowPane, final int index)
    {
        ButtonImage img = rowPane.findPaneOfTypeByID(Integer.toString(index), ButtonImage.class);
        if (buttonData.type == ButtonType.Back)
        {
            if (img == null)
            {
                img = rowPane.findPaneOfTypeByID("back:" + buttonData.data, ButtonImage.class);
            }
            img.setID("back:" + buttonData.data);
            img.setVisible(true);
            img.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/back_medium.png"), false);
            PaneBuilders.tooltipBuilder().hoverPane(img).build().setText(new TextComponent("back"));
            return;
        }

        final StructurePacks.Category subCat = (StructurePacks.Category) buttonData.data;
        String id = subCat.subPath.endsWith("/") ? subCat.subPath.substring(0, subCat.subPath.length() - 1) : subCat.subPath;
        if (id.startsWith("/"))
        {
            id = id.substring(1);
        }

        if (img == null)
        {
            img = rowPane.findPaneOfTypeByID(id, ButtonImage.class);
        }

        img.setID(id);
        String descString = id.split("/")[id.split("/").length-1];
        descString = descString.substring(0, 1).toUpperCase(Locale.US) + descString.substring(1);
        final TextComponent desc = new TextComponent(descString);
        img.setText(desc);
        img.setVisible(true);
        img.setTextColor(ChatFormatting.BLACK.getColor());
    }

    @Override
    public void onButtonClicked(final Button button)
    {
        if (button.getID().contains("back:"))
        {
            nextDepth = button.getID().split(":").length == 1 ? "" : button.getID().split(":")[1];
            updateFolders(Collections.emptyList());
            updateBlueprints(Collections.emptyList(), "");
            depth = nextDepth;
            findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(structurePack.getName() + "/" + nextDepth));
            button.setHoverPane(null);
        }
        else if (nextDepthMeta.containsKey(button.getID()))
        {
            nextDepth = button.getID();
            updateFolders(Collections.emptyList());
            updateBlueprints(Collections.emptyList(), "");
            depth = nextDepth;
            findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(structurePack.getName() + "/" + nextDepth));
            if (nextDepth.contains("/"))
            {
                button.setHoverPane(null);
            }
            else
            {
                for (final Pane pane : findPaneOfTypeByID("categories", View.class).getChildren())
                {
                    pane.enable();
                }
                button.disable();
            }
        }
        else if (blueprintsAtDepth.containsKey(button.getID()))
        {
            nextDepth = button.getID();
            updateFolders(Collections.emptyList());
            updateBlueprints(Collections.emptyList(), "");
            depth = nextDepth;
            findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(structurePack.getName() + "/" + nextDepth));
            if (nextDepth.contains("/"))
            {
                button.setHoverPane(null);
            }
            else
            {
                for (final Pane pane : findPaneOfTypeByID("categories", View.class).getChildren())
                {
                    pane.enable();
                }
                button.disable();
            }
        }

        else if (button.getID().contains(":"))
        {
            final String[] split = button.getID().split(":");
            if (split.length == 2)
            {
                if (blueprintsAtDepth.containsKey(split[0]))
                {
                    try
                    {
                        for (final Blueprint blueprint: blueprintsAtDepth.get(split[0]).get())
                        {
                            if (blueprint.getName().equals(split[1]))
                            {
                                findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(structurePack.getName() + "/" + depth + "/" + blueprint.getName()));
                                Settings.instance.setActiveSchematic(blueprint);
                                return;
                            }
                        }
                    }
                    catch (InterruptedException | ExecutionException e)
                    {
                        Log.getLogger().error("Error retrieving blueprint from future.", e);
                    }
                }
            }
            else
            {
                Log.getLogger().error("Invalid blueprint name at depth: " + button.getID());
            }
            button.setHoverPane(null);
            updateFolders(Collections.emptyList());
        }

        if (Settings.instance.getActiveStructure() == null)
        {
            findPaneOfTypeByID("manipulator", View.class).setVisible(false);
        }
        else
        {
            findPaneOfTypeByID("manipulator", View.class).setVisible(true);
        }

        super.onButtonClicked(button);
    }

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
                moveRightClicked();
            }
            else if (key == 263)
            {
                moveLeftClicked();
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

    /*
     * ---------------- Button Handling -----------------
     */

    /**
     * Rotate the structure counter clockwise.
     */
    private void mirror()
    {
        Settings.instance.mirror();
        updateRotationState();
    }

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
        Settings.instance.setRotation((Settings.instance.getRotation() + ROTATE_RIGHT_INDEX) % POSSIBLE_ROTATIONS);
        updateRotationState();
    }

    /**
     * Rotate the structure counter clockwise.
     */
    private void rotateLeftClicked()
    {
        Settings.instance.setRotation((Settings.instance.getRotation() + ROTATE_LEFT_INDEX) % POSSIBLE_ROTATIONS);
        updateRotationState();
    }

    /*
     * ---------------- Miscellaneous ----------------
     */

    /**
     * Indicate the current orientation state
     */
    private void updateRotationState()
    {
        findPaneOfTypeByID(BUTTON_MIRROR, ButtonImage.class).setImage(new ResourceLocation(MOD_ID, String.format(RES_STRING, BUTTON_MIRROR + (Settings.instance.getMirror().equals(Mirror.NONE) ? "" : GREEN_POS))), false);

        String rotation;
        switch (Settings.instance.getRotation())
        {
            case ROTATE_RIGHT_INDEX:
                rotation = "right_green";
                break;
            case ROTATE_180_INDEX:
                rotation = "down_green";
                break;
            case ROTATE_LEFT_INDEX:
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
    private void adjustToGroundOffset()
    {
        final Blueprint blueprint = Settings.instance.getActiveStructure();
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
            Settings.instance.setGroundOffset(groundOffset);
        }
    }

    /**
     * Button Data.
     */
    public static class ButtonData
    {
        public ButtonType type;
        public Object data;

        public ButtonData(ButtonType type, Object data)
        {
            this.type = type;
            this.data = data;
        }
    }
}
