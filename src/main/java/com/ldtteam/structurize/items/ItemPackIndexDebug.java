// TODO: Remove before publication — debug item only
package com.ldtteam.structurize.items;

import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.index.models.Pack;
import com.ldtteam.structurize.index.models.PackSchematic;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemPackIndexDebug extends Item
{
    private static final Logger LOGGER = LogManager.getLogger();

    public ItemPackIndexDebug()
    {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(final @NotNull Level level, final @NotNull Player player, final @NotNull InteractionHand hand)
    {
        final List<Pack> packs = level.isClientSide
            ? PackManager.getClientPacks()
            : PackManager.getServerPacks(level.getServer().getLevel(Level.OVERWORLD));

        final String side = level.isClientSide ? "CLIENT" : "SERVER";

        if (packs.isEmpty())
        {
            LOGGER.info("[PackIndex][{}] No packs registered.", side);
        }
        else
        {
            LOGGER.info("[PackIndex][{}] {} pack(s):", side, packs.size());
            for (final Pack pack : packs)
            {
                LOGGER.info("[PackIndex][{}]   Pack: '{}' | type: {}",
                    side,
                    pack.getName(),
                    pack.getType().unwrapKey().map(k -> k.location().toString()).orElse("unknown"));

                for (final PackSchematic schematic : pack.getSchematics())
                {
                    LOGGER.info("[PackIndex][{}]     Schematic: '{}' | path: {} | level: {} | world: {} | pos1: {} | pos2: {} | anchor: {}",
                        side,
                        schematic.getName(),
                        schematic.getPath(),
                        schematic.getLevel(),
                        schematic.getWorld().location(),
                        schematic.getPos1(),
                        schematic.getPos2(),
                        schematic.getAnchor());
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
