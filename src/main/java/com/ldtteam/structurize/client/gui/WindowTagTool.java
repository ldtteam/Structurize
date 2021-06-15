package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.network.messages.AddRemoveTagMessage;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

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
    private World world;

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
    public WindowTagTool(String currentTag, BlockPos anchorPos, final World world, final ItemStack stack)
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
        findPaneOfTypeByID(LIST_LABEL, Text.class).setText("Existing tags in schematic:");
        tagList = findPaneOfTypeByID(LIST_TAG_POS, ScrollingList.class);

        registerButton(BUTTON_CANCEL, this::onCancel);
        registerButton(BUTTON_CLOSE, this::onCancel);
        registerButton(BUTTON_LIST_REMOVE, this::removeTag);
        updateResourceList();
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean result = super.onKeyTyped(ch, key);
        currentTag = findPaneOfTypeByID(INPUT_FIELD, TextField.class).getText();
        stack.getOrCreateTag().putString(ItemTagTool.TAG_CURRENT_TAG, currentTag);
        return result;
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

        TileEntity te = world.getBlockEntity(anchorPos);
        if (te instanceof IBlueprintDataProvider)
        {
            IBlueprintDataProvider dataTE = (IBlueprintDataProvider) te;
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

        TileEntity te = world.getBlockEntity(anchorPos);
        if (te instanceof IBlueprintDataProvider)
        {
            IBlueprintDataProvider dataTE = (IBlueprintDataProvider) te;
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
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                TileEntity te = world.getBlockEntity(anchorPos);
                if (te instanceof IBlueprintDataProvider)
                {
                    IBlueprintDataProvider dataTE = (IBlueprintDataProvider) te;
                    positionsList = new ArrayList<>(dataTE.getPositionedTags().keySet());


                    positionsList = new ArrayList<>(dataTE.getPositionedTags().keySet());
                    final BlockPos pos = positionsList.get(index);
                    final List<String> tags = dataTE.getPositionedTags().get(pos);

                    final ItemStack displayStack = BlockUtils.getItemStackFromBlockState(world.getBlockState(dataTE.getRealWorldPos(pos)));
                    rowPane.findPaneOfTypeByID(LIST_BLOCK, ItemIcon.class).setItem(displayStack);

                    final Text tagsText = rowPane.findPaneOfTypeByID(TAG_TEXT, Text.class);
                    tagsText.setText(tags.toString());
                }
                else
                {
                    close();
                }
            }
        });
    }
}
