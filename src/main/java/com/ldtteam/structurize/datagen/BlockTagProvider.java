package com.ldtteam.structurize.datagen;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.tag.ModTags;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Datagen provider for Block Tags
 */
public class BlockTagProvider extends IntrinsicHolderTagsProvider<Block>
{
    public BlockTagProvider(final PackOutput output,
        final ResourceKey<? extends Registry<Block>> key,
        final CompletableFuture<HolderLookup.Provider> provider,
        @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(output, key, provider, k -> ForgeRegistries.BLOCKS.getResourceKey(k).get(), Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider)
    {
        final IntrinsicTagAppender<Block> weakSolidTag = this.tag(ModTags.WEAK_SOLID_BLOCKS).addTag(BlockTags.LEAVES);

        provider.lookupOrThrow(Registries.BLOCK)
            .filterElements(block -> block instanceof Fallable || block instanceof FallingBlock)
            .filterElements(BlockUtils::canBlockSurviveWithoutSupport)
            .listElementIds()
            .forEach(weakSolidTag::add);

        this.tag(ModTags.UNSUITABLE_SOLID_FOR_PLACEHOLDER).addTag(BlockTags.LEAVES);

        this.tag(ModTags.BLUEPRINT_BLACKLIST);
    }
}
