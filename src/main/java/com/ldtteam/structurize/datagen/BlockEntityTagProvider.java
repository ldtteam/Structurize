package com.ldtteam.structurize.datagen;

import com.ldtteam.domumornamentum.entity.block.ModBlockEntityTypes;
import com.ldtteam.domumornamentum.util.Constants;
import com.ldtteam.structurize.tag.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Datagen provider for Block Entity Tags
 */
public class BlockEntityTagProvider extends IntrinsicHolderTagsProvider<BlockEntityType<?>>
{

    public BlockEntityTagProvider(
      final PackOutput output,
      final ResourceKey<? extends Registry<BlockEntityType<?>>> key,
      final CompletableFuture<HolderLookup.Provider> provider,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(output, key, provider, k -> ForgeRegistries.BLOCK_ENTITY_TYPES.getResourceKey(k).get(), Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider)
    {
        this.tag(ModTags.SUBSTITUTION_ABSORB_WHITELIST)
          .add(BlockEntityType.CHEST)
          .add(BlockEntityType.SIGN)
          .add(BlockEntityType.LECTERN)
          .add(ModBlockEntityTypes.MATERIALLY_TEXTURED.get());
    }
}
