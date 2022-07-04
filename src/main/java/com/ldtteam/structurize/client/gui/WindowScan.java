package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.ItemStorage;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.network.messages.*;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.ScanPreviewData;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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

    /** chest warning message */
    private static final String CHEST_WARNING = "chestwarning";

    /**
     * Id of clicking enter.
     */
    //private static final int ENTER_KEY = 28;

    /**
     * Contains all resources needed for a certain build.
     */
    private final Map<String, ItemStorage> resources = new HashMap<>();

    /**
     * Contains all entities needed for a certain build.
     */
    private final Map<String, Entity> entities = new HashMap<>();

    /**
     * White color.
     */
    public static final int WHITE = Color.getByName("white", 0);

    /**
     * The first pos.
     */
    private BlockPos pos1;

    /**
     * The second pos.
     */
    private BlockPos pos2;

    /**
     * The anchor pos.
     */
    private Optional<BlockPos> anchorPos;

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

    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * Resource scrolling list.
     */
    private final ScrollingList entityList;

    /**
     * Constructor for when the player wants to scan something.
     * @param pos1 the first pos.
     * @param pos2 the second pos.
     */
    public WindowScan(final BlockPos pos1, final BlockPos pos2, final Optional<BlockPos> anchorPos)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.anchorPos = anchorPos;
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::discardClicked);
        registerButton(BUTTON_SHOW_RES, this::showResClicked);
        registerButton(BUTTON_REMOVE_ENTITY, this::removeEntity);
        registerButton(BUTTON_REMOVE_BLOCK, this::removeBlock);
        registerButton(BUTTON_REPLACE_BLOCK, this::replaceBlock);
        registerButton(BUTTON_UNDOREDO, b -> {
            close();
            new WindowUndoRedo().open();
        });

        pos1x = findPaneOfTypeByID(POS1X_LABEL, TextField.class);
        pos1y = findPaneOfTypeByID(POS1Y_LABEL, TextField.class);
        pos1z = findPaneOfTypeByID(POS1Z_LABEL, TextField.class);

        pos2x = findPaneOfTypeByID(POS2X_LABEL, TextField.class);
        pos2y = findPaneOfTypeByID(POS2Y_LABEL, TextField.class);
        pos2z = findPaneOfTypeByID(POS2Z_LABEL, TextField.class);

        resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        entityList = findPaneOfTypeByID(LIST_ENTITIES, ScrollingList.class);
    }

    /**
     * Method called when show resources has been clicked.
     */
    private void showResClicked()
    {
        findPaneOfTypeByID(FILTER_NAME, TextField.class).show();
        findPaneOfTypeByID(BUTTON_SHOW_RES, Button.class).hide();
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
        final Entity entity = new ArrayList<>(entities.values()).get(row);
        Network.getNetwork().sendToServer(new RemoveEntityMessage(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), entity.getName().getString()));
        entities.remove(entity.getName().getString());
        updateEntitylist();
    }

    private void removeBlock(final Button button)
    {
        final int x1 = Integer.parseInt(pos1x.getText());
        final int y1 = Integer.parseInt(pos1y.getText());
        final int z1 = Integer.parseInt(pos1z.getText());

        final int x2 = Integer.parseInt(pos2x.getText());
        final int y2 = Integer.parseInt(pos2y.getText());
        final int z2 = Integer.parseInt(pos2z.getText());

        final int row = resourceList.getListElementIndexByPane(button);
        final List<ItemStorage> tempRes = new ArrayList<>(resources.values());
        final ItemStack stack = tempRes.get(row).getItemStack();
        Network.getNetwork().sendToServer(new RemoveBlockMessage(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), stack));
        final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
        resources.remove(stack.getDescriptionId() + ":" + stack.getDamageValue() + "-" + hashCode);
        updateResourceList();
    }

    private void replaceBlock(final Button button)
    {
        final int x1 = Integer.parseInt(pos1x.getText());
        final int y1 = Integer.parseInt(pos1y.getText());
        final int z1 = Integer.parseInt(pos1z.getText());

        final int x2 = Integer.parseInt(pos2x.getText());
        final int y2 = Integer.parseInt(pos2y.getText());
        final int z2 = Integer.parseInt(pos2z.getText());

        final int row = resourceList.getListElementIndexByPane(button);
        final List<ItemStorage> tempRes = new ArrayList<>(resources.values());

        new WindowReplaceBlock(tempRes.get(row).getItemStack(), new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), this).open();
    }

    @Override
    @SuppressWarnings("resource")
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

        pos1x.setText(String.valueOf(pos1.getX()));
        pos1y.setText(String.valueOf(pos1.getY()));
        pos1z.setText(String.valueOf(pos1.getZ()));

        pos2x.setText(String.valueOf(pos2.getX()));
        pos2y.setText(String.valueOf(pos2.getY()));
        pos2z.setText(String.valueOf(pos2.getZ()));

        RenderingCache.boxRenderingCache.put("scan", new ScanPreviewData(pos1, pos2, anchorPos));
        if (anchorPos.isPresent())
        {
            final BlockEntity tile = Minecraft.getInstance().player.level.getBlockEntity(anchorPos.get());
            if (tile instanceof IBlueprintDataProvider && !((IBlueprintDataProvider) tile).getSchematicName().isEmpty())
            {
                findPaneOfTypeByID(NAME_LABEL, TextField.class).setText(((IBlueprintDataProvider) tile).getSchematicName());
            }
        }

        findPaneOfTypeByID(FILTER_NAME, TextField.class).setHandler(input -> {
            filter = findPaneOfTypeByID(FILTER_NAME, TextField.class).getText();

            updateResources();
        });
    }

    /**
     * On cancel button.
     */
    private void discardClicked()
    {
        RenderingCache.boxRenderingCache.remove("scan");
        close();
    }

    /**
     * On confirm button.
     */
    private void confirmClicked()
    {
        final String name = findPaneOfTypeByID(NAME_LABEL, TextField.class).getText();

        final int x1 = Integer.parseInt(pos1x.getText());
        final int y1 = Integer.parseInt(pos1y.getText());
        final int z1 = Integer.parseInt(pos1z.getText());

        final int x2 = Integer.parseInt(pos2x.getText());
        final int y2 = Integer.parseInt(pos2y.getText());
        final int z2 = Integer.parseInt(pos2z.getText());

        Network.getNetwork().sendToServer(new ScanOnServerMessage(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), name, true, RenderingCache.boxRenderingCache.get("scan").anchor ));
        RenderingCache.boxRenderingCache.remove("scan");
        close();
    }

    /**
     * Clears and resets/updates all resources.
     */
    @SuppressWarnings("resource")
    private void updateResources()
    {
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
        catch(final NumberFormatException e)
        {
            Minecraft.getInstance().player.sendMessage(new TextComponent("Invalid Number - Closing!"), Minecraft.getInstance().player.getUUID());
            close();
            return;
        }

        RenderingCache.boxRenderingCache.put("scan", new ScanPreviewData(pos1, pos2, this.anchorPos));
        Network.getNetwork().sendToServer(new UpdateScanToolMessage(pos1, pos2));
        
        final Level world = Minecraft.getInstance().level;
        resources.clear();
        entities.clear();

        if (findPaneByID(BUTTON_SHOW_RES).isVisible())
        {
            return;
        }

        for(int x = Math.min(pos1.getX(), pos2.getX()); x <= Math.max(pos1.getX(), pos2.getX()); x++)
        {
            for(int y = Math.min(pos1.getY(), pos2.getY()); y <= Math.max(pos1.getY(), pos2.getY()); y++)
            {
                for(int z = Math.min(pos1.getZ(), pos2.getZ()); z <= Math.max(pos1.getZ(), pos2.getZ()); z++)
                {
                    final BlockPos here = new BlockPos(x, y, z);
                    final BlockState blockState = world.getBlockState(here);
                    final BlockEntity tileEntity = world.getBlockEntity(here);
                    final List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(here));

                    for (final Entity entity : list)
                    {
                        // LEASH_KNOT, while not directly serializable, still serializes as part of the mob
                        // and drops a lead, so we should alert builders that it exists in the scan
                        if (!entities.containsKey(entity.getName().getString())
                                && (entity.getType().canSerialize() || entity.getType().equals(EntityType.LEASH_KNOT))
                                && (filter.isEmpty() || (entity.getName().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                                    || (entity.toString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))))))
                        {
                            entities.put(entity.getName().getString(), entity);
                        }
                    }

                    @Nullable final Block block = blockState.getBlock();
                    if (block == Blocks.AIR || block == Blocks.VOID_AIR || block == Blocks.CAVE_AIR)
                    {
                        addNeededResource(new ItemStack(Blocks.AIR, 1), 1);
                    }
                    else
                    {
                        boolean handled = false;
                        for (final IPlacementHandler handler : PlacementHandlers.handlers)
                        {
                            if (handler.canHandle(world, BlockPos.ZERO, blockState))
                            {
                                final List<ItemStack> itemList = handler.getRequiredItems(world, here, blockState, tileEntity == null ? null : tileEntity.saveWithFullMetadata(), true);
                                for (final ItemStack stack : itemList)
                                {
                                    addNeededResource(stack, 1);
                                }
                                handled = true;
                                break;
                            }
                        }

                        if (!handled)
                        {
                            addNeededResource(BlockUtils.getItemStackFromBlockState(blockState), 1);
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
     * @param amount the amount.
     */
    public void addNeededResource(@Nullable final ItemStack res, final int amount)
    {
        if (res == null || amount == 0)
        {
            return;
        }

        final int hashCode = res.hasTag() ? res.getTag().hashCode() : 0;
        ItemStorage resource = resources.get(res.getDescriptionId() + ":" + res.getDamageValue() + "-" + hashCode);
        if (resource == null)
        {
            resource = new ItemStorage(res);
            resource.setAmount(amount);
        }
        else
        {
            resource.setAmount(resource.getAmount() + amount);
        }

        if (filter.isEmpty()
                || res.getDescriptionId().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                || res.getHoverName().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US)))
        {
            resources.put(res.getDescriptionId() + ":" + res.getDamageValue() + "-" + hashCode, resource);
        }
    }

    public void updateEntitylist()
    {
        entityList.enable();
        entityList.show();
        final List<Entity> tempEntities = new ArrayList<>(entities.values());

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
                final Entity entity = tempEntities.get(index);
                ItemStack entityIcon = entity.getPickResult();
                if (entity instanceof GlowItemFrame)
                {
                    entityIcon = new ItemStack(Items.GLOW_ITEM_FRAME);
                }
                else if (entity instanceof ItemFrame)
                {
                    entityIcon = new ItemStack(Items.ITEM_FRAME);
                }
                else if (entity instanceof AbstractMinecart)
                {
                    entityIcon = new ItemStack(Items.MINECART);
                }
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(entityIcon);
                rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class).setText(entity.getName());
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
        final List<ItemStorage> tempRes = new ArrayList<>(resources.values());

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
                return tempRes.size();
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
                final ItemStorage resource = tempRes.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
                final Text quantityLabel = rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class);
                resourceLabel.setText(resource.getItemStack().getHoverName());
                quantityLabel.setText(new TextComponent(Integer.toString(resource.getAmount())));
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
            }
        });
    }
}
