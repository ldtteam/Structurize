package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.View;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.client.gui.util.InputFilters;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;

/**
 * Window to select a resource from a given list of items
 */
public class WindowSelectRes extends AbstractWindowSkeleton
{
    /**
     * Static vars.
     */
    private static final String BUTTON_DONE                 = "done";
    private static final String BUTTON_CANCEL               = "cancel";
    private static final String INPUT_RESOURCES             = "input";
    public static final  String RESOURCE_ICON_FROM          = "resourceIconFrom";
    public static final  String RESOURCE_NAME_FROM          = "resourceNameFrom";
    public static final  String MIDDLE_LABEL                = "to";
    public static final  String RESOURCE_ICON_TO            = "resourceIconTo";
    public static final  String RESOURCE_NAME_TO            = "resourceNameTo";
    public static final  String COUNT                       = "count";
    public static final  String COUNT_TEXT                  = "countText";
    public static final  String RESOURCE_LIST_VIEW          = "pickres";
    public static final  String RESOURCE_LIST               = "resources";
    public static final  String RESOURCE_LIST_SELECT        = "select";
    public static final  String DESC                        = "desc";
    public static final  String RESOURCE_LIST_RESOURCE_NAME = "resourceName";
    public static final  String RESOURCE_LIST_RESOURCE_ICON = "resourceIcon";
    private static final int    WHITE                       = Color.getByName("white", 0);

    /**
     * All items in the list.
     */
    private final List<ItemStack> allItems;

    /**
     * All items in the list.
     */
    private final List<ItemStack> displayedItems = new ArrayList<>();

    /**
     * The consumer that receives the block quantity.
     */
    private final BiConsumer<ItemStack, Integer> resultAction;

    /**
     * Secondary confirm and count settings
     */
    private final boolean secondaryConfirm;

    /**
     * The text to set for count selection
     */
    private Component selectCountText = null;

    /**
     * The description displayed, can be empty
     */
    private Component desc = Component.empty();

    /**
     * The filter string.
     */
    private String filter = "";

    /**
     * Update timer for refreshing resources
     */
    private int updateResourcesTimer = 0;

    /**
     * Resource list to render.
     */
    private final ScrollingList resourceList;

    /**
     * The original window to return to
     */
    public final BOWindow origin;

    public WindowSelectRes(
        @Nullable final BOWindow origin,
        final Component description,
        @Nullable final ItemStack previousItem,
        final List<ItemStack> allItems,
        final BiConsumer<ItemStack, Integer> resultAction)
    {
        this(new ResourceLocation(MOD_ID, "gui/windowselectres.xml"), origin, description, previousItem, allItems, resultAction, false, null);
    }

    public WindowSelectRes(
        @Nullable final BOWindow origin,
        final Component description,
        @Nullable final ItemStack previousItem,
        final List<ItemStack> allItems,
        final BiConsumer<ItemStack, Integer> resultAction,
        final boolean secondaryConfirm,
        @Nullable Component selectCountText)
    {
        this(new ResourceLocation(MOD_ID, "gui/windowselectres.xml"), origin, description, previousItem, allItems, resultAction, secondaryConfirm, selectCountText);
    }

    public WindowSelectRes(
        final ResourceLocation xml,
        @Nullable final BOWindow origin,
        final Component description,
        @Nullable final ItemStack previousItem,
        final List<ItemStack> allItems,
        final BiConsumer<ItemStack, Integer> resultAction,
        final boolean secondaryConfirm,
        @Nullable Component selectCountText)
    {
        super(xml);
        this.allItems = allItems;
        if (selectCountText != null)
        {
            this.secondaryConfirm = true;
        }
        else
        {
            this.secondaryConfirm = secondaryConfirm;
        }
        this.selectCountText = selectCountText;
        this.desc = description;
        this.origin = origin;
        this.resultAction = resultAction;

        final Text descText = this.findPaneOfTypeByID(DESC, Text.class);

        final ItemIcon fromStackIcon = this.findPaneOfTypeByID(RESOURCE_ICON_FROM, ItemIcon.class);
        final Text fromStackDesc = this.findPaneOfTypeByID(RESOURCE_NAME_FROM, Text.class);
        final Text fromToLabel = this.findPaneOfTypeByID(MIDDLE_LABEL, Text.class);
        final ItemIcon toStackIcon = this.findPaneOfTypeByID(RESOURCE_ICON_TO, ItemIcon.class);
        final Text toStackDesc = this.findPaneOfTypeByID(RESOURCE_NAME_TO, Text.class);
        final TextField countField = this.findPaneOfTypeByID(COUNT, TextField.class);
        final Text countText = this.findPaneOfTypeByID(COUNT_TEXT, Text.class);

        final ButtonImage buttonSecondaryConfirm = this.findPaneOfTypeByID(BUTTON_DONE, ButtonImage.class);
        final ButtonImage buttonCancel = this.findPaneOfTypeByID(BUTTON_CANCEL, ButtonImage.class);

        final View resPicker = this.findPaneOfTypeByID(RESOURCE_LIST_VIEW, View.class);
        this.resourceList = this.findPaneOfTypeByID(RESOURCE_LIST, ScrollingList.class);
        final TextField selectResFilter = this.findPaneOfTypeByID(INPUT_RESOURCES, TextField.class);

        descText.setText(desc);
        if (previousItem != null)
        {
            fromStackIcon.setItem(previousItem);
            fromStackIcon.setVisible(true);
            fromStackDesc.setText(previousItem.getHoverName());
            fromStackDesc.setVisible(true);
            fromToLabel.setVisible(true);
        }

        if (secondaryConfirm)
        {
            toStackIcon.setItem(Items.AIR.getDefaultInstance());
            toStackIcon.setVisible(true);
            toStackDesc.setText(Items.AIR.getDefaultInstance().getHoverName());
            toStackDesc.setVisible(true);
            buttonSecondaryConfirm.setVisible(true);
        }

        countField.setText("1");
        if (this.selectCountText != null)
        {
            countField.setVisible(true);
            countField.setFilter(InputFilters.ONLY_POSITIVE_NUMBERS_MAX1k);
            countText.setVisible(true);
            countText.setText(selectCountText);
        }

        resPicker.setVisible(true);

        registerButton(buttonSecondaryConfirm.getID(), this::secondaryConfirm);
        registerButton(buttonCancel.getID(), this::cancelClicked);
        registerButton(RESOURCE_LIST_SELECT, this::selectClicked);
        selectResFilter.setHandler(input -> {
            final String newFilter = input.getText();
            if (!newFilter.equals(filter))
            {
                filter = newFilter;
                this.updateResourcesTimer = 10;
            }
        });
    }

    @Override
    public void onOpened()
    {
        Pane.setFocus(this.findPaneOfTypeByID(INPUT_RESOURCES, TextField.class));
        this.updateResources();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (updateResourcesTimer > 0 && --updateResourcesTimer == 0)
        {
            updateResources();
        }
    }

    /**
     * Closes and restores the original window
     */
    private void restoreOrigin()
    {
        this.close();
        if (origin != null)
        {
            origin.open();
            origin.onOpened();
        }
    }

    /**
     * Done clicked to reopen the origin window.
     */
    protected void secondaryConfirm(final Button button)
    {
        final ItemStack to = this.findPaneOfTypeByID(RESOURCE_ICON_TO, ItemIcon.class).getItem();
        onSelectResource(to, getCount());
    }

    /**
     * Finish on selecting a resource
     *
     * @param stack
     * @param count
     */
    public void onSelectResource(final ItemStack stack, final Integer count)
    {
        this.resultAction.accept(stack, count);
        restoreOrigin();
    }

    /**
     * Cancel clicked to close this window.
     */
    protected void cancelClicked(final Button button)
    {
        restoreOrigin();
    }

    /**
     * Select button clicked.
     *
     * @param button the clicked button.
     */
    protected void selectClicked(final Button button)
    {
        final int row = this.resourceList.getListElementIndexByPane(button);
        final ItemStack to = this.displayedItems.get(row);
        this.findPaneOfTypeByID(RESOURCE_ICON_TO, ItemIcon.class).setItem(to);
        this.findPaneOfTypeByID(RESOURCE_NAME_TO, Text.class).setText(to.getHoverName());

        if (!secondaryConfirm)
        {
            onSelectResource(to, getCount());
        }
    }

    /**
     * Parses and returns the count of the items selected. If not selecting counts it returns 1
     *
     * @return
     */
    private int getCount()
    {
        int count = 1;
        if (selectCountText != null)
        {
            try
            {
                count = Integer.parseInt(this.findPaneOfTypeByID(COUNT, TextField.class).getText());
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Invalid input in Selection BOWindow for Quantity, defaulting to 1!");
            }
        }
        return count;
    }

    /**
     * Update the list of resources.
     */
    private void updateResources()
    {
        this.displayedItems.clear();

        for (final ItemStack stack : allItems)
        {
            if ((this.filter.isEmpty()
                || stack.getDescriptionId().toLowerCase(Locale.US).contains(this.filter.toLowerCase(Locale.US))
                || stack.getHoverName().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))))
            {
                this.displayedItems.add(stack);
            }
        }

        if (filter.isEmpty())
        {
            displayedItems.sort(Comparator.comparing(s -> mc.player.getInventory().contains((ItemStack) s))
                .reversed()
                .thenComparing((s1 -> ((ItemStack) s1).getHoverName().getString())));
        }
        else
        {
            displayedItems.sort(Comparator.comparing(s -> mc.player.getInventory().contains((ItemStack) s))
                .reversed()
                .thenComparingInt(s1 -> StringUtils.getLevenshteinDistance(((ItemStack) s1).getHoverName().getString(), filter)));
        }
        this.updateResourceList();
    }

    /**
     * Fill the resource list.
     */
    private void updateResourceList()
    {
        this.resourceList.enable();
        this.resourceList.show();
        final List<ItemStack> tempRes = new ArrayList<>(this.displayedItems);
        this.resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            public int getElementCount()
            {
                return tempRes.size();
            }

            public void updateElement(int index, @NotNull Pane rowPane)
            {
                final ItemStack resource = tempRes.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_LIST_RESOURCE_NAME, Text.class);
                resourceLabel.setText(resource.getHoverName());
                resourceLabel.setColors(WHITE);
                rowPane.findPaneOfTypeByID(RESOURCE_LIST_RESOURCE_ICON, ItemIcon.class).setItem(resource);
            }
        });
    }
}
