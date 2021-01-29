package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.blocks.AbstractBlockSlab;
import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import com.ldtteam.structurize.blocks.AbstractBlockStructurizeFence;
import com.ldtteam.structurize.blocks.AbstractBlockStructurizeStairs;
import com.ldtteam.structurize.blocks.interfaces.IBlockStructurize;
import net.minecraft.block.*;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface BlockSet<E extends BlockSet<?>> extends IStringSerializable
{
    enum CutType
    {
        NORMAL("", block -> true, 4, "MM", "MM"),
        SLAB("slab", block -> block instanceof SlabBlock, 6, "MMM"),
        STAIRS("stairs", block -> block instanceof StairsBlock, 4, "M  ", "MM ", "MMM"),
        WALL("wall", block -> block instanceof WallBlock, 6, "MMM", "MMM"),
        FENCE("fence", block -> block instanceof FourWayBlock, 3, "M-M", "M-M"),
        FENCE_GATE("fence_gate", block -> block instanceof FenceGateBlock, 1, "-M-", "-M-");

        public final String suffix;
        private final Predicate<Block> instancePredicate;
        private final int recipeYield;
        private final String[] patterns;

        CutType(String suffix, Predicate<Block> isInstance, int recipeYield, String... patterns)
        {
            this.suffix = suffix;
            this.instancePredicate = isInstance;
            this.recipeYield = recipeYield;

            this.patterns = patterns.length < 4 ? patterns : new String[0];
        }

        public String withSuffix(String path)
        {
            return suffix.isEmpty() ? path : path + "_" + suffix;
        }

        public ShapedRecipeBuilder formRecipe(IItemProvider result, IItemProvider material, IItemProvider stickMaterial)
        {
            ShapedRecipeBuilder builder = new ShapedRecipeBuilder(result, recipeYield);
            for (String line : patterns) builder.patternLine(line);
            return builder.key('M', material).key('-', stickMaterial);
        }

        public boolean test(Block block)
        {
            return this.instancePredicate.test(block);
        }

        public static CutType fromSuffix(String path)
        {
            for (CutType cut : CutType.values())
            {
                if (!cut.suffix.isEmpty() && path.endsWith(cut.suffix)) return cut;
            }

            return CutType.NORMAL;
        }

        public static CutType fromSuffix(Block block)
        {
            return block.getRegistryName() != null ? fromSuffix(block.getRegistryName().getPath()) : CutType.NORMAL;
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends BlockSet<?>> T find(String contains, T[] values)
    {
        for (T e : values)
        {
            if (e.fromSearch(contains) != null) return (T) e.fromSearch(contains);
        }
        return null;
    }

    static <T extends BlockSet<?>> T find(Block block, T[] values)
    {
        return block.getRegistryName() == null ? null : find(block.getRegistryName().getPath(), values);
    }

    @Nullable
    E fromSearch(String search);

    Block setNormalBlock(Block normal);

    Block getNormalBlock();

    static <T extends BlockSet<?>> List<IBlockStructurize<?>> register(
      final T[] values,
      final boolean wallNotFence,
      final AbstractBlock.Properties props,
      final IForgeRegistry<Block> registry)
    {
        List<IBlockStructurize<?>> constructed = new ArrayList<>(4);
        for (T block : values)
        {
            constructed.add((IBlockStructurize<?>) block.setNormalBlock(new StandardBlock(block.getString(), props)));
            constructed.add(new StandardSlab(CutType.SLAB.withSuffix(block.getString()), props));
            constructed.add(new StandardStairs((Block) constructed.get(0), CutType.STAIRS.withSuffix(block.getString()), props));
            if (wallNotFence)
            {
                constructed.add(new StandardWall(CutType.WALL.withSuffix(block.getString()), props));
            }
        }

        constructed.forEach(block -> block.registerBlock(registry));
        return constructed;
    }

    static void registerItems(List<? extends IBlockStructurize<?>> items, IForgeRegistry<Item> registry, Item.Properties props)
    {
        items.forEach(block -> block.registerItemBlock(registry, props));
    }

    class StandardBlock extends AbstractBlockStructurize<StandardBlock>
    {
        public StandardBlock(final String name, final Properties properties)
        {
            super(properties);
            setRegistryName(name);
        }
    }

    class StandardSlab extends AbstractBlockSlab<StandardSlab>
    {
        public StandardSlab(final String name, final Properties properties)
        {
            super(properties);
            setRegistryName(name);
        }
    }

    class StandardStairs extends AbstractBlockStructurizeStairs<StandardStairs>
    {
        public StandardStairs(final Block model, final String name, final Properties properties)
        {
            super(model::getDefaultState, properties);
            setRegistryName(name);
        }
    }

    class StandardWall extends AbstractBlockStructurizeFence<StandardWall>
    {
        public StandardWall(final String name, final Properties properties)
        {
            super(properties);
            setRegistryName(name);
        }
    }
}
