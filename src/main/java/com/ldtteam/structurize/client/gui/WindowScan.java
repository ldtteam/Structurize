package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.controls.TextField;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.api.util.ItemStorage;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.network.messages.*;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
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
     * Id of clicking enter.
     */
    //private static final int ENTER_KEY = 28;

    /**
     * Contains all resources needed for a certain build.
     */
    private Map<String, ItemStorage> resources = new HashMap<>();

    /**
     * Contains all entities needed for a certain build.
     */
    private Map<String, Entity> entities = new HashMap<>();

    /**
     * White color.
     */
    private static final int WHITE     = Color.getByName("white", 0);

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

        pos1x = findPaneOfTypeByID(POS1X_LABEL, TextField.class);
        pos1y = findPaneOfTypeByID(POS1Y_LABEL, TextField.class);
        pos1z = findPaneOfTypeByID(POS1Z_LABEL, TextField.class);

        pos2x = findPaneOfTypeByID(POS2X_LABEL, TextField.class);
        pos2y = findPaneOfTypeByID(POS2Y_LABEL, TextField.class);
        pos2z = findPaneOfTypeByID(POS2Z_LABEL, TextField.class);

        resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        entityList = findPaneOfTypeByID(LIST_ENTITIES, ScrollingList.class);
        registerButton(UNDO_BUTTON, this::undoClicked);
    }

    /**
     * Undo the last change.
     */
    private void undoClicked()
    {
        Network.getNetwork().sendToServer(new UndoMessage());
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
        resources.remove(stack.getTranslationKey() + ":" + stack.getDamage() + "-" + hashCode);
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

        Settings.instance.setAnchorPos(this.anchorPos);
        Settings.instance.setBox(new Tuple<>(pos1, pos2));
        findPaneOfTypeByID(UNDO_BUTTON, Button.class).setVisible(true);
    }

    /**
     * On cancel button.
     */
    private void discardClicked()
    {
        Settings.instance.setAnchorPos(Optional.empty());
        Settings.instance.setBox(null);
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

        Network.getNetwork().sendToServer(new ScanOnServerMessage(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), name, true, this.anchorPos));
        Settings.instance.setAnchorPos(Optional.empty());
        Settings.instance.setBox(null);
        close();
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean result = super.onKeyTyped(ch, key);
        final String name = findPaneOfTypeByID(FILTER_NAME, TextField.class).getText().toLowerCase(Locale.US);
        if (!filter.equals(name))
        {
            filter = name;
            updateResources();
        }

        return result;
    }

    /**
     * Clears and resets/updates all resources.
     */
    public void updateResources()
    {
        final BlockPos def = Minecraft.getInstance().player.getPosition();
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
            Minecraft.getInstance().player.sendMessage(new StringTextComponent("Invalid number in scal tool positions!"), Minecraft.getInstance().player.getUniqueID());
            return;
        }

        Settings.instance.setAnchorPos(this.anchorPos);
        Settings.instance.setBox(new Tuple<>(pos1, pos2));
        Network.getNetwork().sendToServer(new UpdateScanToolMessage(pos1, pos2));
        
        final World world = Minecraft.getInstance().world;
        resources = new HashMap<>(300);
        entities = new HashMap<>(30);

        if (findPaneByID(BUTTON_SHOW_RES).isVisible())
        {
            return;
        }

        BlockPos.getAllInBoxMutable(pos1, pos2).forEach(here -> {
            final BlockState blockState = world.getBlockState(here);
            final TileEntity tileEntity = world.getTileEntity(here);

            @Nullable final Block block = blockState.getBlock();
            if (block != null)
            {
                if (tileEntity != null)
                {
                    for (final ItemStack stack : ItemStackUtils.getItemStacksOfTileEntity(tileEntity))
                    {
                        addNeededResource(stack.copy(), 1);
                    }
                }

                if ((block instanceof BedBlock && blockState.get(BedBlock.PART) == BedPart.HEAD)
                || block instanceof DoorBlock && blockState.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
                {
                    // noop
                }
                else
                {
                    addNeededResource(BlockUtils.getItemStackFromBlockStateStrict(blockState), 1);
                }
            }
        });

        for (final Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos1, pos2)))
        {
            if (!entities.containsKey(entity.getName().getString())
                    && (filter.isEmpty() || entity.getName().getString().toLowerCase(Locale.US).contains(filter)))
            {
                entities.put(entity.getName().getString(), entity);
            }
        }

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

        final String key = res.getTranslationKey() + ":" + res.getDamage() + "-" + (res.hasTag() ? res.getTag().hashCode() : 0);
        ItemStorage resource = resources.get(key);
        if (resource == null)
        {
            resource = new ItemStorage(res);
            resource.setAmount(amount);

            if (filter.isEmpty() || res.getDisplayName().getString().toLowerCase(Locale.US).contains(filter))
            {
                resources.put(key, resource);
            }
        }
        else
        {
            resource.setAmount(resource.getAmount() + amount);
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
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class).setText((IFormattableTextComponent) tempEntities.get(index).getName());
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
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStorage resource = tempRes.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
                final Text quantityLabel = rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class);
                resourceLabel.setText((IFormattableTextComponent) resource.getItemStack().getDisplayName());
                quantityLabel.setText(Integer.toString(resource.getAmount()));
                resourceLabel.setColors(WHITE);
                quantityLabel.setColors(WHITE);
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(new ItemStack(resource.getItem(), 1, resource.getItemStack().getTag()));
                if (!Minecraft.getInstance().player.isCreative())
                {
                    rowPane.findPaneOfTypeByID(BUTTON_REMOVE_BLOCK, Button.class).hide();
                    rowPane.findPaneOfTypeByID(BUTTON_REPLACE_BLOCK, Button.class).hide();
                }
            }
        });
    }
}
