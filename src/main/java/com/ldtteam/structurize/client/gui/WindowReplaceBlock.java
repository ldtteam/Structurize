package com.ldtteam.structurize.client.gui;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.network.messages.ReplaceBlockMessage;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.ldtteam.structurize.api.util.constant.WindowConstants.*;

/**
 * Window for the replace block GUI.
 */
public class WindowReplaceBlock extends Window implements ButtonHandler
{
    private static final String BUTTON_DONE          = "done";
    private static final String BUTTON_CANCEL        = "cancel";
    private static final String INPUT_NAME           = "name";
    private static final String WINDOW_REPLACE_BLOCK = ":gui/windowreplaceblock.xml";

    /**
     * The stack to replace.
     */
    private final ItemStack from;

    /**
     * The start position.
     */
    private final BlockPos pos1;

    /**
     * White color.
     */
    private static final int WHITE     = Color.getByName("white", 0);

    /**
     * The end position.
     */
    private final BlockPos pos2;

    /**
     * List of all item stacks in the game.
     */
    private final List<ItemStack> allItems = new ArrayList<>();

    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * The filter for the resource list.
     */
    private String filter = "";

    /**
     * If this is to choose the main or the replace block.
     */
    private final boolean mainBlock;

    /**
     * The origin window.
     */
    private final Window origin;

    /**
     * Create the replacement GUI.
     * @param initialStack the initial stack.
     * @param pos1 the start pos.
     * @param pos2 the end pos.
     * @param origin the origin view.
     */
    public WindowReplaceBlock(@NotNull final ItemStack initialStack, final BlockPos pos1, final BlockPos pos2, final Window origin)
    {
        super(Constants.MOD_ID + WINDOW_REPLACE_BLOCK);
        this.from = initialStack;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.mainBlock = false;
        resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        this.origin = origin;
    }

    /**
     * Create the replacement GUI.
     * @param initialStack the initial stack.
     * @param pos the central pos.
     * @param main main block or fill block.
     * @param origin the origin view.
     */
    public WindowReplaceBlock(@NotNull final ItemStack initialStack, final BlockPos pos, final boolean main, final Window origin)
    {
        super(Constants.MOD_ID + WINDOW_REPLACE_BLOCK);
        this.from = initialStack;
        this.pos1 = pos;
        this.pos2 = BlockPos.ZERO;
        this.mainBlock = main;
        resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        this.origin = origin;
    }

    @Override
    public void onOpened()
    {
        findPaneOfTypeByID("resourceIconFrom", ItemIcon.class).setItem(from);
        findPaneOfTypeByID("resourceNameFrom", Label.class).setLabelText((IFormattableTextComponent) from.getDisplayName());
        findPaneOfTypeByID("resourceIconTo", ItemIcon.class).setItem(new ItemStack(Blocks.AIR));
        findPaneOfTypeByID("resourceNameTo", Label.class).setLabelText((IFormattableTextComponent) new ItemStack(Blocks.AIR).getDisplayName());
        updateResources();
    }

    private void updateResources()
    {
        allItems.clear();
        allItems.addAll(ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(ForgeRegistries.ITEMS.iterator(), Spliterator.ORDERED), false)
            .map(ItemStack::new)
            .filter(stack -> (stack.getItem() instanceof BlockItem) && (filter.isEmpty() || stack.getTranslationKey().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US)))).collect(Collectors.toList())));

        final List<ItemStack> specialBlockList = new ArrayList<>();
        specialBlockList.add(new ItemStack(Items.WATER_BUCKET));
        specialBlockList.add(new ItemStack(Items.LAVA_BUCKET));
        specialBlockList.add(new ItemStack(Items.MILK_BUCKET));

        allItems.addAll(specialBlockList.stream().filter(
                stack -> filter.isEmpty()
                        || stack.getTranslationKey().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                        || stack.getDisplayName().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US)))
                .collect(Collectors.toList()));
        updateResourceList();
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean result = super.onKeyTyped(ch, key);
        final String name = findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();
        if (!name.isEmpty())
        {
            filter = name;
        }
        updateResources();
        return result;
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            final ItemStack to = findPaneOfTypeByID("resourceIconTo", ItemIcon.class).getItem();
            if (!ItemStackUtils.isEmpty(to))
            {
                if (origin instanceof WindowScan)
                {
                    Network.getNetwork().sendToServer(new ReplaceBlockMessage(pos1, pos2, from, to));
                }
                else
                {
                    new WindowShapeTool(pos1, to, mainBlock).open();
                }
                origin.open();
            }
        }
        else if (button.getID().equals(BUTTON_CANCEL))
        {
            origin.open();
        }
        else if(button.getID().equals(BUTTON_SELECT))
        {
            final int row = resourceList.getListElementIndexByPane(button);
            final ItemStack to = allItems.get(row);
            findPaneOfTypeByID("resourceIconTo", ItemIcon.class).setItem(to);
            findPaneOfTypeByID("resourceNameTo", Label.class).setLabelText((IFormattableTextComponent) to.getDisplayName());
        }
    }

    public void updateResourceList()
    {
        resourceList.enable();
        resourceList.show();
        final List<ItemStack> tempRes = new ArrayList<>(allItems);

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
                final ItemStack resource = tempRes.get(index);
                final Label resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Label.class);
                resourceLabel.setLabelText((IFormattableTextComponent) resource.getDisplayName());
                resourceLabel.setColor(WHITE, WHITE);
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
            }
        });
    }
}
