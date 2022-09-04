package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.View;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blocks.interfaces.IInvisibleBlueprintAnchorBlock;
import com.ldtteam.structurize.blocks.interfaces.ILeveledBlueprintAnchorBlock;
import com.ldtteam.structurize.blocks.interfaces.INamedBlueprintAnchorBlock;
import com.ldtteam.structurize.blocks.interfaces.IRequirementsBlueprintAnchorBlock;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.network.messages.BuildToolPlacementMessage;
import com.ldtteam.structurize.network.messages.SyncPreviewCacheToServer;
import com.ldtteam.structurize.storage.ISurvivalBlueprintHandler;
import com.ldtteam.structurize.storage.StructurePackMeta;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.SurvivalBlueprintHandlers;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
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
public class WindowExtendedBuildTool extends AbstractBlueprintManipulationWindow
{
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
    private ScrollingList placementOptionsList;

    /**
     * Alternatives scrolling list.
     */
    private ScrollingList alternativesList;

    /**
     * Levels scrolling list.
     */
    private ScrollingList levelsList;

    /**
     * Current selected structure pack.
     */
    private static StructurePackMeta structurePack = null;

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
    private static Future<List<StructurePacks.Category>> categoryFutures = null;

    /**
     * Next level of depth categories.
     */
    private static Map<String, Future<List<StructurePacks.Category>>> nextDepthMeta = new HashMap<>();

    /**
     * Blueprints at depth.
     */
    private static Map<String, Future<List<Blueprint>>> blueprintsAtDepth = new HashMap<>();

    /**
     * Current blueprint mapping from depth to processed blueprints.
     * Depth -> Named -> Leveled.
     */
    private static Map<String, Map<String, Map<String, List<Blueprint>>>> currentBluePrintMappingAtDepthCache = new HashMap<>();

    /**
     * Current blueprint category.
     */
    private static String currentBlueprintCat = "";

    /**
     * Type of button.
     */
    public enum ButtonType
    {
        Blueprint,
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
        super(MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX, pos, groundstyle,"blueprint");
        this.init(groundstyle, pos);
    }

    @SuppressWarnings("resource")
    private void init(final int groundstyle, final BlockPos pos)
    {
        this.groundstyle = groundstyle;

        if (structurePack != null && !structurePack.getName().equals(StructurePacks.selectedPack.getName()))
        {
            depth = "";
            currentBluePrintMappingAtDepthCache.clear();
            blueprintsAtDepth.clear();
            nextDepthMeta.clear();
            categoryFutures = null;
            nextDepth = "";
            currentBlueprintCat = "";
            RenderingCache.removeBlueprint("blueprint");
            if (pos != null && RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getPos() == null)
            {
                RenderingCache.getOrCreateBlueprintPreviewData("blueprint").setPos(pos);
                adjustToGroundOffset();
            }
        }

        structurePack = StructurePacks.selectedPack;

        registerButton(BUTTON_SWITCH_STYLE, this::switchPackClicked);

        folderList = findPaneOfTypeByID("subcategories", ScrollingList.class);
        blueprintList = findPaneOfTypeByID("blueprints", ScrollingList.class);
        placementOptionsList = findPaneOfTypeByID("placement", ScrollingList.class);
        alternativesList = findPaneOfTypeByID("alternatives", ScrollingList.class);
        levelsList = findPaneOfTypeByID("levels", ScrollingList.class);

        if (depth.isEmpty())
        {
            findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(structurePack.getName()).setStyle(Style.EMPTY.withBold(true)));
        }
        else
        {
            findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(
              structurePack.getName()
                + "/"
                + depth
                + (RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint() == null ? "" : ("/" + RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint().getFileName())))
              .setStyle(Style.EMPTY.withBold(true)));
        }
        categoryFutures = StructurePacks.getCategoriesFuture(structurePack.getName(), "");
        findPaneOfTypeByID("manipulator", View.class).setVisible(RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint() != null);

        if (!currentBlueprintCat.isEmpty())
        {
            final String up = currentBlueprintCat.substring(0, currentBlueprintCat.lastIndexOf(":"));
            handleBlueprintCategory(up.contains(":") ? up : currentBlueprintCat, true);
        }
        updateRotationState();
    }

    /**
     * Opens the switch style window.
     */
    private void switchPackClicked()
    {
        new WindowSwitchPack(() -> new WindowExtendedBuildTool(RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getPos(), groundstyle)).open();
    }

    @Override
    protected void cancelClicked()
    {
        BlueprintPreviewData previewData = RenderingCache.removeBlueprint("blueprint");
        previewData.setBlueprint(null);
        previewData.setPos(BlockPos.ZERO);
        Network.getNetwork().sendToServer(new SyncPreviewCacheToServer(previewData));


        close();
        currentBlueprintCat = "";
        depth = "";
    }

    @Override
    protected void confirmClicked()
    {
        final BlueprintPreviewData previewData = RenderingCache.getOrCreateBlueprintPreviewData("blueprint");
        if (previewData.getBlueprint() != null)
        {
            if (!Minecraft.getInstance().player.isCreative())
            {
                final List<ISurvivalBlueprintHandler> handlers = SurvivalBlueprintHandlers.getMatchingHandlers(previewData.getBlueprint(), Minecraft.getInstance().level, Minecraft.getInstance().player, previewData.getPos(), previewData.getPlacementSettings());
                if (handlers.isEmpty())
                {
                    if (SurvivalBlueprintHandlers.getHandlers().isEmpty())
                    {
                    Minecraft.getInstance().player.sendMessage(new TranslatableComponent("structurize.gui.no.survival.handler"), Minecraft.getInstance().player.getUUID());
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

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (categoryFutures != null && categoryFutures.isDone())
        {
            final View categoryView = findPaneOfTypeByID("categories", View.class);
            if (!categoryView.getChildren().isEmpty())
            {
                categoryView.getChildren().clear();
            }
            try
            {
                int index = 0;
                for (final StructurePacks.Category category : categoryFutures.get())
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
                    PaneBuilders.tooltipBuilder().hoverPane(img).build().setText(new TextComponent(id.substring(0, 1).toUpperCase(Locale.US) + id.substring(1)));

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
            categoryFutures = null;
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

    /**
     * Update the list of placement options.
     */
    public void updatePlacementOptions()
    {
        placementOptionsList.enable();
        placementOptionsList.show();
        blueprintList.disable();
        blueprintList.hide();
        folderList.hide();
        folderList.disable();
        alternativesList.hide();
        alternativesList.disable();
        levelsList.hide();
        levelsList.disable();
        settingsList.hide();
        settingsList.disable();

        final List<Tuple<Component, Runnable>> categories = new ArrayList<>();
        if (Minecraft.getInstance().player.isCreative())
        {
            categories.add(new Tuple<>(new TranslatableComponent("structurize.gui.buildtool.complete"), () -> handlePlacement(BuildToolPlacementMessage.HandlerType.Complete, "")));
            categories.add(new Tuple<>(new TranslatableComponent("structurize.gui.buildtool.pretty"), () -> handlePlacement(BuildToolPlacementMessage.HandlerType.Pretty, "")));
        }

        final BlueprintPreviewData previewData = RenderingCache.getOrCreateBlueprintPreviewData("blueprint");
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

    /**
     * This is called when one of the placement options is clicked.
     * @param type the placement type
     * @param id the custom id type.
     */
    private void handlePlacement(final BuildToolPlacementMessage.HandlerType type, final String id)
    {
        final BlueprintPreviewData previewData = RenderingCache.getOrCreateBlueprintPreviewData("blueprint");
        if (previewData.getBlueprint() != null)
        {
            Network.getNetwork()
              .sendToServer(new BuildToolPlacementMessage(type,
                id,
                structurePack.getName(),
                structurePack.getSubPath(previewData.getBlueprint().getFilePath().resolve(previewData.getBlueprint().getFileName() + ".blueprint")),
                previewData.getPos(),
                previewData.getRotation(),
                previewData.getMirror()));
            if (type == BuildToolPlacementMessage.HandlerType.Survival)
            {
                cancelClicked();
            }
        }
    }

    @Override
    public void settingsClicked()
    {
        super.settingsClicked();
        folderList.disable();
        folderList.hide();
        blueprintList.disable();
        blueprintList.hide();
    }

    /**
     * Update the sub category structure.
     * @param inputCategories the categories to render now.
     */
    public void updateFolders(final List<StructurePacks.Category> inputCategories)
    {
        folderList.enable();
        folderList.show();
        blueprintList.disable();
        blueprintList.hide();
        placementOptionsList.hide();
        placementOptionsList.disable();
        settingsList.hide();
        settingsList.disable();

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

    /**
     * Update the displayed blueprints.
     * @param inputBluePrints the blueprints to display.
     * @param depth the depth they're at.
     */
    public void updateBlueprints(final List<Blueprint> inputBluePrints, final String depth)
    {
        blueprintList.enable();
        blueprintList.show();
        folderList.disable();
        folderList.hide();
        placementOptionsList.hide();
        placementOptionsList.disable();
        settingsList.hide();
        settingsList.disable();

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

        final Map<String, List<Blueprint>> blueprintMapping = new HashMap<>();

        for (final Blueprint blueprint : inputBluePrints)
        {
            final BlockState anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
            if (!Minecraft.getInstance().player.isCreative()
                  && anchor.getBlock() instanceof IInvisibleBlueprintAnchorBlock
                  && !((IInvisibleBlueprintAnchorBlock) anchor.getBlock()).isVisible(blueprint.getTileEntityData(RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getPos(), blueprint.getPrimaryBlockOffset())))
            {
               continue;
            }

            if (anchor.getBlock() instanceof ILeveledBlueprintAnchorBlock)
            {
                final int level = ((ILeveledBlueprintAnchorBlock) anchor.getBlock()).getLevel(blueprint.getTileEntityData(RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getPos(), blueprint.getPrimaryBlockOffset()));
                final String name = blueprint.getFileName().replace(Integer.toString(level), "");
                final List<Blueprint> blueprintList = blueprintMapping.getOrDefault(name, new ArrayList<>());
                blueprintList.add(blueprint);
                blueprintMapping.put(name, blueprintList);
            }
            else
            {
                final String name = blueprint.getFileName();
                final List<Blueprint> blueprintList = blueprintMapping.getOrDefault(name, new ArrayList<>());
                blueprintList.add(blueprint);
                blueprintMapping.put(name, blueprintList);
            }
        }

        final Map<String, Map<String, List<Blueprint>>> altBlueprintMapping = new HashMap<>();

        for (final Map.Entry<String, List<Blueprint>> entry : blueprintMapping.entrySet())
        {
            final Blueprint blueprint = entry.getValue().get(0);
            final BlockState anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
            if (anchor.getBlock() instanceof INamedBlueprintAnchorBlock)
            {
                final String name = anchor.getBlock().getDescriptionId();
                final Map<String, List<Blueprint>> tempLeveledBlueprints = altBlueprintMapping.getOrDefault(name, new HashMap<>());
                tempLeveledBlueprints.put(entry.getKey(), entry.getValue());
                altBlueprintMapping.put(name, tempLeveledBlueprints);
            }
            else
            {
                final String name = blueprint.getFileName();
                final Map<String, List<Blueprint>> tempLeveledBlueprints = altBlueprintMapping.getOrDefault(name, new HashMap<>());
                tempLeveledBlueprints.put(entry.getKey(), entry.getValue());
                altBlueprintMapping.put(name, tempLeveledBlueprints);
            }
        }

        currentBluePrintMappingAtDepthCache.put(depth, altBlueprintMapping);

        for (final Map.Entry<String, Map<String, List<Blueprint>>> entry : altBlueprintMapping.entrySet())
        {
            blueprints.add(new ButtonData(ButtonType.Blueprint, entry.getKey()));
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

    /**
     * Update the alternative blueprint list.
     * @param bluePrintMapping the mapping of blueprint name to leveled blueprints.
     */
    public void updateAlternatives(final Map<String, List<Blueprint>> bluePrintMapping, final String depth)
    {
        alternativesList.enable();
        alternativesList.show();
        levelsList.hide();
        levelsList.disable();
        settingsList.hide();
        settingsList.disable();

        final List<Map.Entry<String, List<Blueprint>>> list = new ArrayList<>(bluePrintMapping.entrySet());

        alternativesList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return list.size();
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
                final ButtonImage button = rowPane.findPaneOfTypeByID("alternative", ButtonImage.class);
                rowPane.findPaneOfTypeByID("id", Text.class).setText(new TextComponent(depth + ":" + list.get(index).getKey()));
                button.setText(new TextComponent("Alternative: " + (index+1)));
                button.setTextColor(ChatFormatting.BLACK.getColor());
            }
        });
    }

    /**
     * Update the alternative blueprint list.
     * @param blueprints the different blueprint levels.
     */
    public void updateLevels(final List<Blueprint> blueprints, final String depth, final boolean hasAlternatives)
    {
        levelsList.enable();
        levelsList.show();
        alternativesList.hide();
        alternativesList.disable();
        settingsList.hide();
        settingsList.disable();

        if (hasAlternatives)
        {
            blueprints.add(0, null);
        }

        levelsList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return blueprints.size();
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
                if (blueprints.get(index) == null )
                {
                    final String buttonId = depth.substring(0, depth.lastIndexOf(":")) + ":back";
                    final ButtonImage button = rowPane.findPaneOfTypeByID("level", ButtonImage.class);
                    rowPane.findPaneOfTypeByID("id", Text.class).setText(new TextComponent(buttonId));
                    button.setText(new TextComponent(""));
                    button.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/back_medium.png"), false);
                }
                else
                {
                    final String buttonId = depth + ":" + (hasAlternatives ? index - 1 : index);
                    final ButtonImage button = rowPane.findPaneOfTypeByID("level", ButtonImage.class);
                    rowPane.findPaneOfTypeByID("id", Text.class).setText(new TextComponent(buttonId));
                    button.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/button_medium.png"), false);
                    button.setText(new TextComponent("Level: " + (index + (hasAlternatives ? 0 : 1))));
                    button.setTextColor(ChatFormatting.BLACK.getColor());
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
        else if (buttonData.type == ButtonType.Blueprint)
        {
            final String id = (String) buttonData.data;
            if (img == null)
            {
                img = rowPane.findPaneOfTypeByID(depth + ":" + id, ButtonImage.class);
            }

            if (img == null)
            {
                return;
            }
            img.setID(depth + ":" + id);

            final Map<String, List<Blueprint>> blueprintMap = currentBluePrintMappingAtDepthCache.get(depth).get(id);
            final Blueprint firstBlueprint = blueprintMap.values().iterator().next().get(0);

            final BlockState anchor = firstBlueprint.getBlockState(firstBlueprint.getPrimaryBlockOffset());
            final List<MutableComponent> toolTip = new ArrayList<>();
            if (anchor.getBlock() instanceof INamedBlueprintAnchorBlock)
            {
                img.setText(((INamedBlueprintAnchorBlock) anchor.getBlock()).getBlueprintDisplayName());
                toolTip.addAll(((INamedBlueprintAnchorBlock) anchor.getBlock()).getDesc());
            }
            else
            {
                img.setText(new TextComponent(id.split("/")[id.split("/").length - 1]));
            }
            img.setVisible(true);

            boolean hasMatch = false;
            for (final List<Blueprint> blueprints : blueprintMap.values())
            {
                for (final Blueprint blueprint : blueprints)
                {
                    if (blueprint.equals(RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint()))
                    {
                        hasMatch = true;
                        break;
                    }
                }
            }

            boolean hasAlts = blueprintMap.values().size() > 1;
            if (anchor.getBlock() instanceof IRequirementsBlueprintAnchorBlock)
            {
                toolTip.addAll(((IRequirementsBlueprintAnchorBlock) anchor.getBlock()).getRequirements(Minecraft.getInstance().level, RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getPos(), Minecraft.getInstance().player));
                if (!((IRequirementsBlueprintAnchorBlock) anchor.getBlock()).areRequirementsMet(Minecraft.getInstance().level, RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getPos(), Minecraft.getInstance().player))
                {
                    PaneBuilders.tooltipBuilder().hoverPane(img).build().setText(toolTip);
                    img.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/button_blueprint_disabled" + (hasAlts ? "_variant" : "") + ".png"), false);
                    img.disable();
                    return;
                }
            }

            boolean isInvis = anchor.getBlock() instanceof IInvisibleBlueprintAnchorBlock
                  && !((IInvisibleBlueprintAnchorBlock) anchor.getBlock()).isVisible(firstBlueprint.getTileEntityData(RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getPos(), firstBlueprint.getPrimaryBlockOffset()));

            PaneBuilders.tooltipBuilder().hoverPane(img).build().setText(toolTip);

            if (hasMatch)
            {
                img.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/button_blueprint_selected" + (isInvis ? "_creative" : "") + (hasAlts ? "_variant" : "") + ".png"), false);
            }
            else
            {
                img.setImage(new ResourceLocation(MOD_ID, "textures/gui/buildtool/button_blueprint"  + (isInvis ? "_creative" : "") + (hasAlts ? "_variant" : "") + ".png"), false);
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
        boolean handled = false;
        if (button.getID().contains("back:"))
        {
            nextDepth = button.getID().split(":").length == 1 ? "" : button.getID().split(":")[1];
            updateFolders(Collections.emptyList());
            updateBlueprints(Collections.emptyList(), "");
            depth = nextDepth;
            if (depth.isEmpty())
            {
                for (final Pane pane : findPaneOfTypeByID("categories", View.class).getChildren())
                {
                    pane.enable();
                }
            }
            findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(structurePack.getName() + "/" + nextDepth).setStyle(Style.EMPTY.withBold(true)));
            button.setHoverPane(null);
            handled = true;
        }
        else if (nextDepthMeta.containsKey(button.getID()))
        {
            nextDepth = button.getID();
            updateFolders(Collections.emptyList());
            updateBlueprints(Collections.emptyList(), "");
            depth = nextDepth;
            findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(structurePack.getName() + "/" + nextDepth).setStyle(Style.EMPTY.withBold(true)));
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
            handled = true;
        }
        else if (blueprintsAtDepth.containsKey(button.getID()))
        {
            nextDepth = button.getID();
            updateFolders(Collections.emptyList());
            updateBlueprints(Collections.emptyList(), "");
            depth = nextDepth;
            findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(structurePack.getName() + "/" + nextDepth).setStyle(Style.EMPTY.withBold(true)));
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
            handled = true;
        }
        else if (button.getID().contains(":"))
        {
            for (final Pane pane : findPaneOfTypeByID("categories", View.class).getChildren())
            {
                pane.enable();
            }

            currentBlueprintCat = button.getID().replace(":back", "");
            handleBlueprintCategory(currentBlueprintCat, false);
            button.setHoverPane(null);
            handled = true;
        }
        else if (button.getID().equals("alternative") || button.getID().equals("level"))
        {
            for (final Pane pane : findPaneOfTypeByID("categories", View.class).getChildren())
            {
                pane.enable();
            }

            currentBlueprintCat = button.getParent().findPaneOfTypeByID("id", Text.class).getText().getString().replace(":back", "");
            handleBlueprintCategory(currentBlueprintCat, false);
            button.setHoverPane(null);
            handled = true;
        }

        findPaneOfTypeByID("manipulator", View.class).setVisible(RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint() != null);

        if (!handled)
        {
            super.onButtonClicked(button);
        }
    }

    private void handleBlueprintCategory(final String categoryId, final boolean onOpen)
    {
        final String[] split = categoryId.split(":");
        final String id = split[1];
        final Map<String, List<Blueprint>> mapping = currentBluePrintMappingAtDepthCache.get(split[0]).get(id);
        if (mapping == null)
        {
            Log.getLogger().error("Invalid blueprint name at depth: " + categoryId);
            return;
        }

        if (split.length == 2)
        {
            if (mapping.size() == 1 && mapping.values().iterator().next().size() == 1)
            {
                alternativesList.hide();
                alternativesList.disable();
                levelsList.hide();
                levelsList.disable();

                final Blueprint blueprint = mapping.values().iterator().next().get(0);
                findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(structurePack.getName() + "/" + depth + "/" + blueprint.getFileName()).setStyle(Style.EMPTY.withBold(true)));
                RenderingCache.getOrCreateBlueprintPreviewData("blueprint").setBlueprint(blueprint);
                adjustToGroundOffset();
                return;
            }

            if (mapping.size() > 1)
            {
                updateLevels(Collections.emptyList(), "", false);
                updateAlternatives(mapping, categoryId);
            }
            else
            {
                updateAlternatives(Collections.emptyMap(), categoryId);
                final List<Blueprint> leveled = mapping.values().iterator().next();

                if (RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint() == null || !onOpen)
                {
                    final Blueprint blueprint = leveled.get(0);
                    findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(
                      structurePack.getName() + "/" + depth + "/" + blueprint.getFileName()).setStyle(Style.EMPTY.withBold(true)));
                    RenderingCache.getOrCreateBlueprintPreviewData("blueprint").setBlueprint(blueprint);
                    adjustToGroundOffset();
                }
                updateLevels(leveled, categoryId + ":" + mapping.keySet().iterator().next(), false);
            }
        }
        else if (split.length == 3)
        {
            final List<Blueprint> list = mapping.get(split[2]);
            if (list == null || list.isEmpty())
            {
                Log.getLogger().error("Invalid blueprint name at depth: " + categoryId);
                return;
            }

            if (RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint() == null || list.size() == 1 || !onOpen)
            {
                final Blueprint blueprint = list.get(0);
                findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(
                  structurePack.getName() + "/" + depth + "/" + blueprint.getFileName()).setStyle(Style.EMPTY.withBold(true)));
                RenderingCache.getOrCreateBlueprintPreviewData("blueprint").setBlueprint(blueprint);
                adjustToGroundOffset();
            }

            if (list.size() == 1)
            {
                return;
            }

            updateAlternatives(Collections.emptyMap(), categoryId);
            updateLevels(new ArrayList<>(list), categoryId, mapping.size() > 1);
        }
        else if (split.length == 4)
        {
            final List<Blueprint> list = mapping.get(split[2]);
            if (list == null || list.isEmpty())
            {
                Log.getLogger().error("Invalid blueprint name at depth: " + categoryId);
                return;
            }

            try
            {
                int level = Integer.parseInt(split[3]);
                final Blueprint blueprint = list.get(level);
                findPaneOfTypeByID("tree", Text.class).setText(new TextComponent(structurePack.getName() + "/" + depth + "/" + blueprint.getFileName()).setStyle(Style.EMPTY.withBold(true)));
                RenderingCache.getOrCreateBlueprintPreviewData("blueprint").setBlueprint(blueprint);
                adjustToGroundOffset();
                return;
            }
            catch (final NumberFormatException exception)
            {
                Log.getLogger().error("Invalid blueprint name at depth: " + categoryId);
            }
        }
        else
        {
            Log.getLogger().error("Invalid blueprint name at depth: " + categoryId);
        }
        updateFolders(Collections.emptyList());
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
