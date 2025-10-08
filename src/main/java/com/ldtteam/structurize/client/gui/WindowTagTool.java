package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.common.util.BlockToItemHelper;
import com.ldtteam.structurize.api.TagManager;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.network.messages.AddRemoveTagMessage;
import com.ldtteam.structurize.network.messages.SetTagInTool;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WindowTagTool extends AbstractWindowSkeleton
{
    private static final String WINDOW_TAG_TOOL    = ":gui/windowtagtool.xml";
    private static final String INPUT_FIELD        = "currentTag";
    private static final String LIST_TAG_POS       = "tagposlist";
    private static final String LIST_BLOCK         = "posblock";
    private static final String TAG_TEXT           = "tagnames";
    private static final String BUTTON_CANCEL      = "cancel";
    private static final String BUTTON_CLOSE       = "closeUI";
    private static final String BUTTON_LIST_REMOVE = "removeTag";
    private static final String LIST_TAG_OPTION    = "tagoptionlist";
    private static final String TAG_SELECT         = "select";

    /**
     * The current world
     */
    @NotNull
    private final Level world;

    /**
     * The anchor pos
     */
    @NotNull
    private final BlockPos anchorPos;

    /**
     * The item
     */
    @NotNull
    private final ItemStack stack;

    /**
     * The tags list
     */
    @NotNull
    private final ScrollingList tagList;

    /**
     * The tags list
     */
    @NotNull
    private final ScrollingList tagOptionList;

    /**
     * The input field.
     */
    @NotNull
    private final TextField inputField;

    /**
     * Positions list with tags
     */
    @NotNull
    private List<PositionWithTags> positionsList = new ArrayList<>();

    /**
     * Tag options.
     */
    @NotNull
    private final List<String> tagOptions = new ArrayList<>();

    /**
     * Constructor for the skeleton class of the windows.
     */
    public WindowTagTool(final @NotNull String currentTag, final @NotNull BlockPos anchorPos, final @NotNull Level world, final @NotNull ItemStack stack)
    {
        super(Constants.MOD_ID + WINDOW_TAG_TOOL);
        this.world = world;
        this.anchorPos = anchorPos;
        this.stack = stack;
        this.tagList = findPaneOfTypeByID(LIST_TAG_POS, ScrollingList.class);
        this.tagOptionList = findPaneOfTypeByID(LIST_TAG_OPTION, ScrollingList.class);
        this.inputField = findPaneOfTypeByID(INPUT_FIELD, TextField.class);
        this.inputField.setText(currentTag);

        tagOptions.addAll(TagManager.getGlobalTagOptions());
        final Block block = world.getBlockState(anchorPos).getBlock();
        if (block instanceof IAnchorBlock anchorBlock)
        {
            tagOptions.addAll(TagManager.getMatchingTagOptions(anchorBlock));
        }

        registerButton(TAG_SELECT, this::tagOptionSelected);
        registerButton(BUTTON_CANCEL, this::close);
        registerButton(BUTTON_CLOSE, this::close);
        registerButton(BUTTON_LIST_REMOVE, this::removeTag);

        findPaneOfTypeByID(LIST_TAG_POS, ScrollingList.class).setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return positionsList.size();
            }

            @Override
            public boolean shouldUpdate()
            {
                return false;
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                final PositionWithTags positionWithTags = positionsList.get(index);

                final BlockPos blockPosition = anchorPos.offset(positionWithTags.position);
                final ItemStack displayStack =
                    BlockToItemHelper.getItemStack(world.getBlockState(blockPosition), world.getBlockEntity(blockPosition), Minecraft.getInstance().player);
                rowPane.findPaneOfTypeByID(LIST_BLOCK, ItemIcon.class).setItem(displayStack);
                rowPane.findPaneOfTypeByID(TAG_TEXT, Text.class).setText(Component.literal(String.join(", ", positionWithTags.tags.toString())));
            }
        });

        tagOptionList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return tagOptions.size();
            }

            @Override
            public boolean shouldUpdate()
            {
                return false;
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                final String tag = tagOptions.get(index);

                final Text tagsText = rowPane.findPaneOfTypeByID(TAG_TEXT, Text.class);
                tagsText.setText(Component.literal(tag));
                PaneBuilders.tooltipBuilder().hoverPane(tagsText).build()
                    .setText(Component.translatable("com.ldtteam.tag.tooltip." + tag));

                rowPane.findPaneOfTypeByID(TAG_SELECT, Button.class).setEnabled(!tag.equals(inputField.getText()));
            }
        });
    }

    private void tagOptionSelected(final Button button)
    {
        final int row = tagOptionList.getListElementIndexByPane(button);
        inputField.setText(tagOptions.get(row));
        tagOptionList.refreshElementPanes(true);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        updateResourceList();
        tagOptionList.refreshElementPanes(true);
    }

    @Override
    public void close()
    {
        super.close();
        stack.getOrCreateTag().putString(ItemTagTool.TAG_CURRENT_TAG, inputField.getText());
        Network.getNetwork().sendToServer(new SetTagInTool(inputField.getText(), Minecraft.getInstance().player.getInventory().findSlotMatchingItem(stack)));
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean returnValue = super.onKeyTyped(ch, key);
        tagOptionList.refreshElementPanes(true);
        return returnValue;
    }

    /**
     * Removes a block pos
     *
     * @param button
     */
    private void removeTag(final Button button)
    {
        final int row = tagList.getListElementIndexByPane(button);
        final BlockPos toRemove = positionsList.get(row).position;

        final BlockEntity te = world.getBlockEntity(anchorPos);
        if (te instanceof final IBlueprintDataProviderBE dataTE)
        {
            final Map<BlockPos, List<String>> map = dataTE.getPositionedTags();
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
     * Updates the current tag list
     */
    public void updateResourceList()
    {
        BlockEntity te = world.getBlockEntity(anchorPos);
        if (te instanceof final IBlueprintDataProviderBE dataTE)
        {
            final ArrayList<PositionWithTags> positions = new ArrayList<>();
            for (final Map.Entry<BlockPos, List<String>> entry : dataTE.getPositionedTags().entrySet())
            {
                positions.add(new PositionWithTags(entry.getKey(), entry.getValue()));
            }
            positionsList = positions;
            tagList.refreshElementPanes(true);
        }
        else
        {
            close();
        }
    }

    private record PositionWithTags(
        BlockPos position,
        List<String> tags)
    {
    }
}
