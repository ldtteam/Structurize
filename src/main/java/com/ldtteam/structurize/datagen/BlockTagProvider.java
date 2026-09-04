package com.ldtteam.structurize.datagen;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.tag.ModTags;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;

/**
 * Datagen provider for Block Tags
 */
public class BlockTagProvider extends TagsProvider<Block>
{
    public BlockTagProvider(final PackOutput output,
        final ResourceKey<? extends Registry<Block>> key,
        final CompletableFuture<HolderLookup.Provider> provider)
    {
        super(output, key, provider, Constants.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider)
    {
        final TagAppender<Block> weakSolidTag = this.tag(ModTags.WEAK_SOLID_BLOCKS);
        weakSolidTag.addTag(BlockTags.LEAVES);

        provider.lookupOrThrow(Registries.BLOCK)
            .listElements()
            .map(entry -> entry.value())
            .filter(block -> block instanceof Fallable || block instanceof FallingBlock)
            .filter(BlockUtils::canBlockSurviveWithoutSupport)
            .forEach(block -> weakSolidTag.add(ResourceKey.create(Registries.BLOCK, BuiltInRegistries.BLOCK.getKey(block))));

        this.tag(ModTags.UNSUITABLE_SOLID_FOR_PLACEHOLDER).addTag(BlockTags.LEAVES);

        this.tag(ModTags.GOOD_SOLID_FOR_PLACEHOLDER).add(ResourceKey.create(Registries.BLOCK, Identifier.parse("minecraft:farmland")));

        this.tag(ModTags.BLUEPRINT_BLACKLIST);
    }
}
