// TODO: Remove before publication — debug item only
package com.ldtteam.structurize.items;

import com.ldtteam.structurize.client.gui.index.WindowSchematicIndexPackList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemPackIndexDebug extends Item
{
    public ItemPackIndexDebug()
    {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(final @NotNull Level level, final @NotNull Player player, final @NotNull InteractionHand hand)
    {
        if (level.isClientSide)
        {
            new WindowSchematicIndexPackList().open();
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
