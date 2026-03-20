package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.View;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.ItemStorage;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.client.gui.util.InputFilters;
import com.ldtteam.structurize.client.gui.util.ItemPositionsStorage;
import com.ldtteam.structurize.client.rendertask.RenderTaskManager;
import com.ldtteam.structurize.client.rendertask.tasks.BoxPreviewData;
import com.ldtteam.structurize.client.rendertask.tasks.BoxPreviewRenderTask;
import com.ldtteam.structurize.network.messages.*;
import com.ldtteam.structurize.placement.SimplePlacementContext;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.ScanToolData;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.ldtteam.structurize.api.util.constant.WindowConstants.*;

/**
 * Window for finishing a scan.
 */
public class WindowScan extends AbstractWindowSkeleton
{
    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowscantool.xml";

    /**
     * chest warning message
     */
    private static final String CHEST_WARNING = "chestwarning";

    /**
     * Id of clicking enter.
     */
    //private static final int ENTER_KEY = 28;

    /**
     * Current list view of resources
     */
    private final Map<ItemStorage, ItemPositionsStorage> allResources = new HashMap<>();

    /**
     * Contains all entities needed for a certain build.
     */
    private final Object2IntMap<EntityType> entities = new Object2IntOpenHashMap<>();

    /**
     * White color.
     */
    public static final int WHITE = Color.getByName("white", 0);

    /**
     * The scan tool data.
     */
    private final ScanToolData data;

    /**
     * Filter for the block and entity lists.
     */
    private String filter = "";

    /**
     * Pos 1 text fields.
     */
    private final TextField pos1x;
    private final TextField pos1y;
    private final TextField pos1z;

    /**
     * Pos 2 text fields.
     */
    private final TextField pos2x;
    private final TextField pos2y;
    private final TextField pos2z;

    private final TextField slotId;

    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * Resource scrolling list.
     */
    private final ScrollingList entityList;

    /**
     * Timer until the resources get adjusted to the filter input
     */
    private int updateFilterTimer = 0;

    /**
     * The sorted visible list of items, used by the UI
     */
    private ArrayList<ItemStorage> visibleResourcesSortedList = new ArrayList<>();

    /**
     * Constructor for when the player wants to scan something.
     *
     * @param data the scan tool data
     */
    public WindowScan(@NotNull final ScanToolData data)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.data = data;
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::discardClicked);
        registerButton(BUTTON_SHOW_RES, this::showResClicked);
        registerButton(VISIBLE_CHECKBOX, this::updateResources);
        registerButton(HIDDEN_CHECKBOX, this::updateResources);
        registerButton(BUTTON_REMOVE_ENTITY, this::removeEntity);
        registerButton(BUTTON_REMOVE_BLOCK, this::removeBlock);
        registerButton(BUTTON_REPLACE_BLOCK, this::replaceBlock);
        registerButton(BUTTON_FILL_PLACEHOLDERS, this::showFillplaceholderUI);
        registerButton(BUTTON_CANCEL_FILL, this::cancelFill);
        registerButton(BUTTON_DO_FILL, this::fillPlaceholders);
        registerButton(BUTTON_UNDOREDO, b -> {
            close();
            new WindowUndoRedo().open();
        });
        registerButton(REMOVE_FILTERED, this::removeFilteredBlock);


        pos1x = findPaneOfTypeByID(POS1X_LABEL, TextField.class);
        pos1y = findPaneOfTypeByID(POS1Y_LABEL, TextField.class);
        pos1z = findPaneOfTypeByID(POS1Z_LABEL, TextField.class);

        pos2x = findPaneOfTypeByID(POS2X_LABEL, TextField.class);
        pos2y = findPaneOfTypeByID(POS2Y_LABEL, TextField.class);
        pos2z = findPaneOfTypeByID(POS2Z_LABEL, TextField.class);

        slotId = findPaneOfTypeByID("slot", TextField.class);

        pos1x.setFilter(InputFilters.ONLY_NUMBERS);
        pos1y.setFilter(InputFilters.ONLY_NUMBERS);
        pos1z.setFilter(InputFilters.ONLY_NUMBERS);
        pos2x.setFilter(InputFilters.ONLY_NUMBERS);
        pos2y.setFilter(InputFilters.ONLY_NUMBERS);
        pos2z.setFilter(InputFilters.ONLY_NUMBERS);
        slotId.setFilter(InputFilters.ONLY_NUMBERS);

        resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        entityList = findPaneOfTypeByID(LIST_ENTITIES, ScrollingList.class);
    }

    /**
     * Shows fill placeholder UI
     */
    private void showFillplaceholderUI()
    {
        findPaneOfTypeByID(FILL_PLACEHOLDERS_UI, View.class).setVisible(true);
        findPaneOfTypeByID(LIST_ENTITIES, ScrollingList.class).setVisible(false);
        findPaneOfTypeByID(BUTTON_FILL_PLACEHOLDERS, ButtonImage.class).setVisible(false);
    }

    /**
     * Cancel fill UI
     */
    private void cancelFill()
    {
        findPaneOfTypeByID(FILL_PLACEHOLDERS_UI, View.class).setVisible(false);
        findPaneOfTypeByID(LIST_ENTITIES, ScrollingList.class).setVisible(true);
        findPaneOfTypeByID(BUTTON_FILL_PLACEHOLDERS, ButtonImage.class).setVisible(true);
    }

    /**
     * Sends a fill request to the server
     */
    private void fillPlaceholders()
    {
        try
        {
            double yStretch = Double.parseDouble(findPaneOfTypeByID(INPUT_YSTRETCH, TextField.class).getText());
            double circleRadiusMult = Double.parseDouble(findPaneOfTypeByID(INPUT_RADIUS, TextField.class).getText());
            int heightOffset = Integer.parseInt(findPaneOfTypeByID(INPUT_HEIGHT_OFFSET, TextField.class).getText());
            int minDistToBlocks = Integer.parseInt(findPaneOfTypeByID(INPUT_BLOCKDIST, TextField.class).getText());
            Network.getNetwork()
                .sendToServer(new FillTopPlaceholderMessage(data.getCurrentSlotData().getBox().getPos1(),
                    data.getCurrentSlotData().getBox().getPos2(),
                    yStretch,
                    circleRadiusMult,
                    heightOffset,
                    minDistToBlocks));
        }
        catch (Exception e)
        {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Invalid Number"), false);
        }
        close();
    }

    /**
     * Method called when show resources has been clicked.
     */
    private void showResClicked()
    {
        findPaneOfTypeByID(FILTER_NAME, TextField.class).show();
        findPaneOfTypeByID(BUTTON_SHOW_RES, Button.class).hide();
        findPaneOfTypeByID(REMOVE_FILTERED, Button.class).show();
        findPaneOfTypeByID(VISIBLE_CHECKBOX, CheckBox.class).show();
        findPaneOfTypeByID(VISIBLE_CHECKBOX, CheckBox.class).setChecked(true);
        findPaneOfTypeByID(HIDDEN_CHECKBOX, CheckBox.class).show();
        findPaneOfTypeByID(HIDDEN_CHECKBOX, CheckBox.class).setChecked(true);
        updateResources();
    }

    private void removeEntity(final Button button)
    {
        final int x1 = Integer.parseInt(pos1x.getText());
        final int y1 = Integer.parseInt(pos1y.getText());
        final int z1 = Integer.parseInt(pos1z.getText());

        final int x2 = Integer.parseInt(pos2x.getText());
        final int y2 = Integer.parseInt(pos2y.getText());
        final int z2 = Integer.parseInt(pos2z.getText());

        final int row = entityList.getListElementIndexByPane(button);
        final EntityType entity = new ArrayList<>(entities.keySet()).get(row);
        Network.getNetwork().sendToServer(new RemoveEntityMessage(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), EntityType.getKey(entity)));
        entities.removeInt(entity);
        updateEntitylist();
    }

    private void removeBlock(final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);
        final ItemPositionsStorage toRemove = allResources.get(visibleResourcesSortedList.get(row));
        Network.getNetwork().sendToServer(new RemoveBlockMessage(toRemove));
        removeAllNeededResource(toRemove.itemStorage.getItemStack());
        updateResourceList();
    }

    /**
     * Helper to get the current set of resources to display
     *
     * @return
     */
    private Set<ItemStorage> getResources()
    {
        return allResources.keySet();
    }

    private void removeFilteredBlock()
    {
        Network.getNetwork().sendToServer(new RemoveBlockMessage(allResources.values().stream().toList()));
        allResources.clear();
        updateResourceList();
    }

    private void replaceBlock(final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);
        new WindowReplaceBlock(this, allResources.get(visibleResourcesSortedList.get(row))).open();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        if (!Minecraft.getInstance().player.isCreative())
        {
            pos1x.disable();
            pos1y.disable();
            pos1z.disable();

            pos2x.disable();
            pos2y.disable();
            pos2z.disable();
        }

        loadSlot();

        findPaneOfTypeByID(FILTER_NAME, TextField.class).setHandler(input -> {
            filter = findPaneOfTypeByID(FILTER_NAME, TextField.class).getText();


            updateFilterTimer = 10;
        });

        updateFilterTimer = 30;
    }

    @Override
    public void onClosed()
    {
        if (RenderTaskManager.getTasksByGroup("scan") != null)   // not confirmed/cancelled
        {
            updateBounds();
        }

        super.onClosed();
    }

    @Override
    public void onUpdate()
    {
        if (updateFilterTimer > 0)
        {
            updateFilterTimer--;
            if (updateFilterTimer == 0)
            {
                updateResources();
            }
        }

        super.onUpdate();
    }

    /**
     * On cancel button.
     */
    private void discardClicked()
    {
        RenderTaskManager.removeTaskGroup("scan");
        RenderTaskManager.removeTaskGroup("clickedResource");
        close();
    }

    /**
     * On confirm button.
     */
    private void confirmClicked()
    {
        updateBounds();

        final ScanToolData.Slot slot = data.getCurrentSlotData();
        Network.getNetwork().sendToServer(new ScanOnServerMessage(slot, true));
        RenderTaskManager.removeTaskGroup("scan");
        close();
    }

    @Override
    public boolean onUnhandledKeyTyped(final int ch, final int key)
    {
        if (ch >= '0' && ch <= '9')
        {
            updateBounds();
            data.moveTo(ch - '0');
            loadSlot();
            updateResources();
            return true;
        }

        return super.onUnhandledKeyTyped(ch, key);
    }

    private void loadSlot()
    {
        slotId.setText(String.valueOf(data.getCurrentSlotId()));
        final ScanToolData.Slot slot = data.getCurrentSlotData();

        pos1x.setText(String.valueOf(slot.getBox().getPos1().getX()));
        pos1y.setText(String.valueOf(slot.getBox().getPos1().getY()));
        pos1z.setText(String.valueOf(slot.getBox().getPos1().getZ()));

        pos2x.setText(String.valueOf(slot.getBox().getPos2().getX()));
        pos2y.setText(String.valueOf(slot.getBox().getPos2().getY()));
        pos2z.setText(String.valueOf(slot.getBox().getPos2().getZ()));

        RenderTaskManager.addRenderTask("scan", new BoxPreviewRenderTask("scan", slot.getBox(), 60 * 10));

        findPaneOfTypeByID(NAME_LABEL, TextField.class).setText("");
        if (!slot.getName().isEmpty())
        {
            findPaneOfTypeByID(NAME_LABEL, TextField.class).setText(slot.getName());
        }
        else if (slot.getBox().getAnchor().isPresent())
        {
            final BlockEntity tile = Minecraft.getInstance().player.level().getBlockEntity(slot.getBox().getAnchor().get());
            if (tile instanceof IBlueprintDataProviderBE && !((IBlueprintDataProviderBE) tile).getSchematicName().isEmpty())
            {
                findPaneOfTypeByID(NAME_LABEL, TextField.class).setText(((IBlueprintDataProviderBE) tile).getSchematicName());
            }
        }
    }

    private void updateBounds()
    {
        BlockPos pos1, pos2;

        final BlockPos def = Minecraft.getInstance().player.blockPosition();
        try
        {
            final int x1 = pos1x.getText().isEmpty() ? def.getX() : Integer.parseInt(pos1x.getText());
            final int y1 = pos1y.getText().isEmpty() ? def.getY() : Integer.parseInt(pos1y.getText());
            final int z1 = pos1z.getText().isEmpty() ? def.getZ() : Integer.parseInt(pos1z.getText());
            pos1 = new BlockPos(x1, y1, z1);

            final int x2 = pos2x.getText().isEmpty() ? def.getX() : Integer.parseInt(pos2x.getText());
            final int y2 = pos2y.getText().isEmpty() ? def.getY() : Integer.parseInt(pos2y.getText());
            final int z2 = pos2z.getText().isEmpty() ? def.getZ() : Integer.parseInt(pos2z.getText());
            pos2 = new BlockPos(x2, y2, z2);
        }
        catch (final NumberFormatException e)
        {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Invalid Number"), false);
            return;
        }

        final String name = findPaneOfTypeByID(NAME_LABEL, TextField.class).getText();
        final ScanToolData.Slot slot = data.getCurrentSlotData();
        data.setCurrentSlotData(new ScanToolData.Slot(name, new BoxPreviewData(pos1, pos2, slot.getBox().getAnchor())));
        RenderTaskManager.addRenderTask("scan", new BoxPreviewRenderTask("scan", data.getCurrentSlotData().getBox(), 60 * 10));
        Network.getNetwork().sendToServer(new UpdateScanToolMessage(data));
    }

    /**
     * Clears and resets/updates all resources.
     */
    private void updateResources()
    {
        updateFilterTimer = 0;
        updateBounds();

        final Level world = Minecraft.getInstance().level;
        allResources.clear();
        entities.clear();

        if (findPaneByID(BUTTON_SHOW_RES).isVisible())
        {
            return;
        }

        final ScanToolData.Slot slot = data.getCurrentSlotData();

        final List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(slot.getBox().getPos1(), slot.getBox().getPos2()));

        for (final Entity entity : list)
        {
            // LEASH_KNOT, while not directly serializable, still serializes as part of the mob
            // and drops a lead, so we should alert builders that it exists in the scan
            if (!entities.containsKey(entity.getName().getString())
                && (entity.getType().canSerialize() || entity.getType().equals(EntityType.LEASH_KNOT))
                && (filter.isEmpty() || (entity.getName().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                || (entity.toString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))))))
            {
                entities.mergeInt(entity.getType(), 1, Integer::sum);
            }
        }

        final BlockPos.MutableBlockPos here = new BlockPos.MutableBlockPos();
        final int minX = Math.min(slot.getBox().getPos1().getX(), slot.getBox().getPos2().getX());
        final int minY = Math.min(slot.getBox().getPos1().getY(), slot.getBox().getPos2().getY());
        final int minZ = Math.min(slot.getBox().getPos1().getZ(), slot.getBox().getPos2().getZ());
        final int maxX = Math.max(slot.getBox().getPos1().getX(), slot.getBox().getPos2().getX());
        final int maxY = Math.max(slot.getBox().getPos1().getY(), slot.getBox().getPos2().getY());
        final int maxZ = Math.max(slot.getBox().getPos1().getZ(), slot.getBox().getPos2().getZ());

        for (int x = minX; x <= maxX; x++)
        {
            for (int z = minZ; z <= maxZ; z++)
            {
                for (int y = minY; y <= maxY; y++)
                {
                    here.set(x, y, z);
                    final BlockState blockState = world.getBlockState(here);
                    final BlockEntity tileEntity = world.getBlockEntity(here);

                    boolean visible = false;
                    for (final Direction dir : Direction.values())
                    {
                        BlockPos offsetPos = here.relative(dir);
                        if (!(offsetPos.getX() >= minX && offsetPos.getX() <= maxX && offsetPos.getY() >= minY && offsetPos.getY() <= maxY && offsetPos.getZ() >= minZ
                            && offsetPos.getZ() <= maxZ)
                            || !world.getBlockState(offsetPos).canOcclude())
                        {
                            visible = true;
                            break;
                        }
                    }

                    @Nullable final Block block = blockState.getBlock();
                    if (block == Blocks.AIR || block == Blocks.VOID_AIR || block == Blocks.CAVE_AIR)
                    {
                        addNeededResource(new ItemStack(Blocks.AIR, 1), visible, here);
                    }
                    else
                    {
                        final IPlacementHandler handler = PlacementHandlers.getHandler(world, BlockPos.ZERO, blockState);
                        final List<ItemStack> itemList = handler.getRequiredItems(world, here, blockState, tileEntity == null ? null : tileEntity.saveWithFullMetadata(), new SimplePlacementContext(false, new PlacementSettings()));
                        for (final ItemStack stack : itemList)
                        {
                            addNeededResource(stack, visible, here);
                        }
                    }
                }
            }
        }

        window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).refreshElementPanes();
        updateResourceList();
        updateEntitylist();
    }

    /**
     * Add a new resource to the needed list.
     *
     * @param res    the resource.
     * @param pos    the blockpos the resource is for
     */
    public void addNeededResource(@Nullable final ItemStack res, final boolean visible, final BlockPos pos)
    {
        if (res == null)
        {
            return;
        }

        final var visibleCheckBox = findPaneOfTypeByID(VISIBLE_CHECKBOX, CheckBox.class);
        final var hiddenCheckBox = findPaneOfTypeByID(HIDDEN_CHECKBOX, CheckBox.class);
        if (visible && !visibleCheckBox.isChecked())
        {
            return;
        }

        if (!visible && !hiddenCheckBox.isChecked())
        {
            return;
        }

        if (filter.isEmpty()
            || res.getDescriptionId().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
            || res.getHoverName().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US)))
        {
            final ItemStorage stackToStore = new ItemStorage(res, 1, true, false);
            final ItemPositionsStorage existing = allResources.computeIfAbsent(stackToStore, ItemPositionsStorage::new);
            existing.addItemAndPos(stackToStore, pos.immutable());
        }
    }

    public void removeAllNeededResource(final ItemStack res)
    {
        final ItemStorage storage = new ItemStorage(res, 1, true, false);
        allResources.remove(storage);
    }

    public void updateEntitylist()
    {
        entityList.enable();
        entityList.show();
        final List<EntityType> tempEntities = new ArrayList<>(entities.keySet());

        //Creates a dataProvider for the unemployed resourceList.
        entityList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return tempEntities.size();
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
                final EntityType entity = tempEntities.get(index);
                ItemStack entityIcon = entity.create(Minecraft.getInstance().level).getPickResult();
                if (entity == EntityType.GLOW_ITEM_FRAME)
                {
                    entityIcon = new ItemStack(Items.GLOW_ITEM_FRAME);
                }
                else if (entity == EntityType.ITEM_FRAME)
                {
                    entityIcon = new ItemStack(Items.ITEM_FRAME);
                }
                else if (entity == EntityType.MINECART)
                {
                    entityIcon = new ItemStack(Items.MINECART);
                }
                rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class).setText(Component.literal(Integer.toString(entities.getInt(entity))));
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(entityIcon);
                rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class).setText(entity.getDescription());
                if (!Minecraft.getInstance().player.isCreative())
                {
                    rowPane.findPaneOfTypeByID(BUTTON_REMOVE_ENTITY, Button.class).hide();
                }
            }
        });
    }

    public void updateResourceList()
    {
        resourceList.enable();
        resourceList.show();
        window.findPaneOfTypeByID(CHEST_WARNING, Text.class).show();
        visibleResourcesSortedList = new ArrayList<>(getResources());
        visibleResourcesSortedList.sort(Comparator.comparing(s1 -> s1.getItemStack().getHoverName().getString()));

        //Creates a dataProvider for the unemployed resourceList.
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return visibleResourcesSortedList.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            @SuppressWarnings("resource")
            public void updateElement(final int index, final Pane rowPane)
            {
                final ItemStorage resource = visibleResourcesSortedList.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
                final Text quantityLabel = rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class);
                resourceLabel.setText(resource.getItemStack().getHoverName());
                quantityLabel.setText(Component.literal(Integer.toString(resource.getAmount())));
                resourceLabel.setColors(WHITE);
                quantityLabel.setColors(WHITE);

                final ItemStack copy = resource.getItemStack().copy();
                copy.setCount(1);
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(copy);
                if (!Minecraft.getInstance().player.isCreative())
                {
                    rowPane.findPaneOfTypeByID(BUTTON_REMOVE_BLOCK, Button.class).hide();
                    rowPane.findPaneOfTypeByID(BUTTON_REPLACE_BLOCK, Button.class).hide();
                }

                List<Component> tooltip = new ArrayList<>(rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).getModifiedItemStackTooltip());
                tooltip.add(Component.translatable("com.ldtteam.structurize.gui.scantool.item.tooltipclick"));

                (new AbstractTextBuilder.AutomaticTooltipBuilder()).hoverPane(rowPane.findPaneOfTypeByID(BUTTON_SHOWBLOCK, Button.class))
                    .build()
                    .setTextOld(tooltip);
                rowPane.findPaneOfTypeByID(BUTTON_SHOWBLOCK, Button.class).setHandler(b -> doHighLightBlocks(b, resource));
            }

            private void doHighLightBlocks(Button button, final ItemStorage block)
            {
                final ItemPositionsStorage itemPositionsStorage = allResources.get(block);
                for (final BlockPos position : itemPositionsStorage.positions)
                {
                    BoxPreviewRenderTask previewData =
                        new BoxPreviewRenderTask("clickedResource" + position.toShortString(), new BoxPreviewData(position, position, Optional.empty()), 30);
                    RenderTaskManager.addRenderTask("clickedResource", previewData);
                }
                window.close();
            }
        });
    }
}
