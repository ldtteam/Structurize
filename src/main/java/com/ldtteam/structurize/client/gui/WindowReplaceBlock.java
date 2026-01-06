package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.client.gui.util.InputFilters;
import com.ldtteam.structurize.client.gui.util.ItemPositionsStorage;
import com.ldtteam.structurize.client.gui.util.ItemUtil;
import com.ldtteam.structurize.network.messages.ReplaceBlockMessage;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Window for the replace block GUI.
 */
public class WindowReplaceBlock extends WindowSelectRes
{
    /**
     * The block and its positions to replace
     */
    private final ItemPositionsStorage toReplace;

    /**
     * Create a selection window with the origin window as input.
     *
     * @param origin    the origin.
     * @param toReplace the block to replace
     */
    public WindowReplaceBlock(
        final @Nullable BOWindow origin,
        final @NotNull ItemPositionsStorage toReplace)
    {
        super(origin,
            Component.translatable("com.ldtteam.structurize.gui.replaceblock.info"),
            toReplace.itemStorage.getItemStack(),
            ItemUtil.getAllItemsInlcudingInventory(),
            ((stack, integer) -> {}),
            true,
            Component.translatable("com.ldtteam.structurize.gui.scan.replace.pct"));
        this.toReplace = toReplace;
        findPaneOfTypeByID("count", TextField.class).setText("100");
        findPaneOfTypeByID(COUNT, TextField.class).setFilter(InputFilters.PERCENT);
    }

    @Override
    public void onSelectResource(final ItemStack to, final Integer count)
    {
        if (!ItemStackUtils.isEmpty(to) || to.getItem() instanceof AirItem)
        {
            if (origin instanceof WindowScan)
            {
                final BlockState fromBS = BlockUtils.getBlockStateFromStack(toReplace.itemStorage.getItemStack());
                final BlockState toBS = BlockUtils.getBlockStateFromStack(to);

                final List<Property<?>> missingProperties = new ArrayList<>(toBS.getProperties());
                missingProperties.removeAll(fromBS.getProperties());
                if (!missingProperties.isEmpty())
                {
                    Minecraft.getInstance().player.displayClientMessage(Component.translatable("structurize.gui.replaceblock.ambiguous_properties",
                        fromBS.getBlock().getName(),
                        toBS.getBlock().getName(),
                        missingProperties.stream()
                            .map(prop -> getPropertyName(prop) + " - " + prop.getName())
                            .collect(Collectors.joining(", ", "[", "]"))), false);
                }
                if (toBS.is(ModBlocks.NULL_PLACEMENT))
                {
                    Minecraft.getInstance().player.displayClientMessage(Component.translatable("structurize.gui.replaceblock.null_placement",
                        toBS.getBlock().getName()), false);
                }

                final String pct = findPaneOfTypeByID("count", TextField.class).getText();
                int pctNum;
                try
                {
                    pctNum = Integer.parseInt(pct);
                }
                catch (NumberFormatException ex)
                {
                    pctNum = 100;
                    Minecraft.getInstance().player.displayClientMessage(Component.translatable("structurize.gui.replaceblock.badpct"), false);
                }

                Network.getNetwork().sendToServer(new ReplaceBlockMessage(toReplace, to, pctNum));
            }
        }

        super.onSelectResource(to, count);
    }

    private String getPropertyName(final Property<?> clazz)
    {
        return clazz instanceof BooleanProperty ? "Boolean"
            : clazz instanceof IntegerProperty ? "Integer"
                : clazz instanceof EnumProperty ? "Enum"
                    : clazz instanceof DirectionProperty ? "Direction"
                        : clazz.getClass().getSimpleName();
    }
}
