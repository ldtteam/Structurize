package com.ldtteam.structurize.util;

import com.google.common.collect.ImmutableMap;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.AttachedStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Block to Item wrapper for blocks which should not be in vanilla blockToItem map
 */
public class BlockToItemMaps
{
    /**
     * Blocks which are aquireable in different ways
     */
    private static final Map<Block, IItemProvider> SURVIVAL_HELPERS;

    /**
     * Creative map
     */
    private static final Map<Block, IItemProvider> CREATIVE_HELPERS;

    /**
     * Blocks which should be treated as something else during build requirements
     */
    private static final Map<Block, IItemProvider> BUILDING_HELPERS;

    /**
     * Blocks which should be treated as something else during build requirements
     */
    private static final Map<Block, IItemProvider> VANILLA_MAP;

    static
    {
        final ImmutableMap.Builder<Block, IItemProvider> survival = ImmutableMap.builder();
        final ImmutableMap.Builder<Block, IItemProvider> creative = ImmutableMap.builder();
        final ImmutableMap.Builder<Block, IItemProvider> building = ImmutableMap.builder();

        // TODO missing items: END_GATEWAY, END_PORTAL, NETHER_PORTAL
        // MOVING_PISTON and PISTON_HEAD are airs everytime

        // air blocks
        register(creative, Blocks.CAVE_AIR, ModItems.vanillaCaveAir.get());
        register(creative, Blocks.VOID_AIR, ModItems.vanillaVoidAir.get());

        // frostwalker enchantment
        register(building, Blocks.FROSTED_ICE, ((FlowingFluidBlock) Blocks.WATER).getFluid().getFilledBucket());

        // missing growables
        register(survival, Blocks.BAMBOO_SAPLING, Items.BAMBOO);
        register(survival, Blocks.KELP_PLANT, Items.KELP);
        register(survival, Blocks.TALL_SEAGRASS, Items.SEAGRASS);
        register(survival, Blocks.TWISTING_VINES_PLANT, Items.TWISTING_VINES);
        register(survival, Blocks.WEEPING_VINES_PLANT, Items.WEEPING_VINES);

        // farmland and path can be dirt and grass block
        register(building, Blocks.FARMLAND, Items.DIRT);
        register(building, Blocks.GRASS_PATH, Items.DIRT);
        register(building, Blocks.GRASS_BLOCK, Items.DIRT);

        ForgeRegistries.BLOCKS.forEach(block -> {
            // all flower pots are pots and not air
            if (block instanceof FlowerPotBlock)
            {
                register(survival, block, Items.FLOWER_POT);
            }

            // fire blocks
            else if (block instanceof AbstractFireBlock)
            {
                register(survival, block, Items.FLINT_AND_STEEL);
            }

            // fluid to bucket helper
            else if (block instanceof FlowingFluidBlock)
            {
                register(survival, block, ((FlowingFluidBlock) block).getFluid().getFilledBucket());
            }

            // thanks Mojang
            else if (block instanceof AttachedStemBlock)
            {
                register(survival, block, ((AttachedStemBlock) block).grownFruit.getStem().asItem());
            }
        });

        SURVIVAL_HELPERS = survival.build();
        CREATIVE_HELPERS = creative.build();
        BUILDING_HELPERS = building.build();
        VANILLA_MAP = (Map) Item.BLOCK_TO_ITEM;
    }

    private static void register(final ImmutableMap.Builder<Block, IItemProvider> category,
        final Block block,
        final Item item)
    {
        category.put(block, () -> item);
    }

    private BlockToItemMaps()
    {
    }

    @Nullable
    public static IItemProvider getCreativeItem(final Block block)
    {
        return CREATIVE_HELPERS.get(block);
    }

    @Nullable
    public static IItemProvider getSurvivalItem(final Block block)
    {
        return SURVIVAL_HELPERS.get(block);
    }

    @Nullable
    public static IItemProvider getBuildingItem(final Block block)
    {
        return BUILDING_HELPERS.get(block);
    }

    @Nullable
    public static IItemProvider getVanillaItem(final Block block)
    {
        return VANILLA_MAP.get(block);
    }

    /**
     * Iterates over given maps in order they were supplied
     *
     * @param block block to find item for
     * @param mapEnum search enum {@link MapEnum}
     * @return item or null
     */
    @Nullable
    public static IItemProvider getFrom(final Block block, final MapEnum mapEnum)
    {
        IItemProvider result = null;
        for (int i = 0; i < mapEnum.maps.size(); i++)
        {
            result = mapEnum.maps.get(i).get(block);
            if (result != null)
            {
                break;
            }
        }

        return result;
    }

    public static enum MapEnum
    {
        ALL(CREATIVE_HELPERS, BUILDING_HELPERS, SURVIVAL_HELPERS, VANILLA_MAP),
        CREATIVE_NOT_BUILDING(CREATIVE_HELPERS, SURVIVAL_HELPERS, VANILLA_MAP),
        SURVIVAL_NOT_BUILDING(SURVIVAL_HELPERS, VANILLA_MAP),
        SURVIVAL_PLUS_BUILDING(BUILDING_HELPERS, SURVIVAL_HELPERS, VANILLA_MAP);

        private final List<Map<Block, IItemProvider>> maps;

        private MapEnum(final Map<Block, IItemProvider>... maps)
        {
            this.maps = Arrays.asList(maps);
        }
    }
}