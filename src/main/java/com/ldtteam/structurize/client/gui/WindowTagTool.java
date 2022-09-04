package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.network.messages.AddRemoveTagMessage;
import com.ldtteam.structurize.network.messages.SetTagInTool;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WindowTagTool extends AbstractWindowSkeleton
{
    private static final String WINDOW_TAG_TOOL    = ":gui/windowtagtool.xml";
    private static final String INPUT_FIELD        = "currentTag";
    private static final String LIST_LABEL         = "taglistname";
    private static final String LIST_TAG_POS       = "tagposlist";
    private static final String LIST_BLOCK         = "posblock";
    private static final String TAG_TEXT           = "tagnames";
    private static final String BUTTON_CANCEL      = "cancel";
    private static final String BUTTON_CLOSE       = "closeUI";
    private static final String BUTTON_LIST_REMOVE = "removeTag";
    /**
     * The current tag
     */
    private String currentTag = "";

    /**
     * The current world
     */
    private Level world;

    /**
     * The anchor pos
     */
    private BlockPos anchorPos = null;

    /**
     * The item
     */
    private ItemStack stack;

    /**
     * The tags list
     */
    private ScrollingList tagList;

    /**
     * BLockpos list
     */
    private List<BlockPos> positionsList = Collections.emptyList();

    /**
     * Constructor for the skeleton class of the windows.
     */
    public WindowTagTool(String currentTag, BlockPos anchorPos, final Level world, final ItemStack stack)
    {
        super(Constants.MOD_ID + WINDOW_TAG_TOOL);
        this.world = world;
        this.currentTag = currentTag;
        this.anchorPos = anchorPos;
        this.stack = stack;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        findPaneOfTypeByID(INPUT_FIELD, TextField.class).setText(currentTag);
        findPaneOfTypeByID(LIST_LABEL, Text.class).setText(new TextComponent("Existing tags in schematic:"));
        tagList = findPaneOfTypeByID(LIST_TAG_POS, ScrollingList.class);

        registerButton(BUTTON_CANCEL, this::onCancel);
        registerButton(BUTTON_CLOSE, this::onCancel);
        registerButton(BUTTON_LIST_REMOVE, this::removeTag);
        updateResourceList();
    }

    @Override
    @SuppressWarnings("resource")
    public void close()
    {
        super.close();
        currentTag = findPaneOfTypeByID(INPUT_FIELD, TextField.class).getText();
        stack.getOrCreateTag().putString(ItemTagTool.TAG_CURRENT_TAG, currentTag);
        Network.getNetwork().sendToServer(new SetTagInTool(currentTag, Minecraft.getInstance().player.getInventory().findSlotMatchingItem(stack)));
    }

    /**
     * Removes a block pos
     *
     * @param button
     */
    private void removeTag(final Button button)
    {
        int row = tagList.getListElementIndexByPane(button);
        BlockPos toRemove = positionsList.get(row);

        BlockEntity te = world.getBlockEntity(anchorPos);
        if (te instanceof IBlueprintDataProviderBE)
        {
            IBlueprintDataProviderBE dataTE = (IBlueprintDataProviderBE) te;
            Map<BlockPos, List<String>> map = dataTE.getPositionedTags();
            if (map.containsKey(toRemove) && !map.get(toRemove).isEmpty())
            {
                String tag = map.get(toRemove).get(map.get(toRemove).size() - 1);
                dataTE.removeTag(toRemove, tag);
                Network.getNetwork().sendToServer(new AddRemoveTagMessage(false, tag, toRemove, anchorPos));
            }
            updateResourceList();
        }
        else
        {
            close();
        }
    }

    /**
     * Closes current gui
     */
    private void onCancel()
    {
        close();
    }

    /**
     * Updates the current tag list
     */
    public void updateResourceList()
    {
        tagList.enable();
        tagList.show();

        BlockEntity te = world.getBlockEntity(anchorPos);
        if (te instanceof IBlueprintDataProviderBE)
        {
            IBlueprintDataProviderBE dataTE = (IBlueprintDataProviderBE) te;
            positionsList = new ArrayList<>(dataTE.getPositionedTags().keySet());
        }
        else
        {
            close();
        }

        //Creates a dataProvider for the unemployed resourceList.
        tagList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return positionsList.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                BlockEntity te = world.getBlockEntity(anchorPos);
                if (te instanceof IBlueprintDataProviderBE)
                {
                    IBlueprintDataProviderBE dataTE = (IBlueprintDataProviderBE) te;
                    positionsList = new ArrayList<>(dataTE.getPositionedTags().keySet());


                    positionsList = new ArrayList<>(dataTE.getPositionedTags().keySet());
                    final BlockPos pos = positionsList.get(index);
                    final List<String> tags = dataTE.getPositionedTags().get(pos);

                    final ItemStack displayStack = BlockUtils.getItemStackFromBlockState(world.getBlockState(dataTE.getRealWorldPos(pos)));
                    rowPane.findPaneOfTypeByID(LIST_BLOCK, ItemIcon.class).setItem(displayStack);

                    final Text tagsText = rowPane.findPaneOfTypeByID(TAG_TEXT, Text.class);
                    tagsText.setText(new TextComponent(tags.toString()));
                }
                else
                {
                    close();
                }
            }
        });
    }
}
