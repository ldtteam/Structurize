package com.ldtteam.structurize.datagen;

import com.ldtteam.domumornamentum.util.Constants;
import com.ldtteam.structurize.tag.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
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
    public BlockTagProvider(
      final PackOutput output,
      final ResourceKey<? extends Registry<Block>> key,
      final CompletableFuture<HolderLookup.Provider> provider,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(output, key, provider, k -> ForgeRegistries.BLOCKS.getResourceKey(k).get(), Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider)
    {
        this.tag(ModTags.NON_SOLID_BLOCKS)
          .addTag(BlockTags.LEAVES);
    }
}
