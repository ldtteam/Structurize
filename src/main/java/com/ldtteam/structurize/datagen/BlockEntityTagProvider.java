package com.ldtteam.structurize.datagen;

import com.ldtteam.domumornamentum.entity.block.ModBlockEntityTypes;
import com.ldtteam.domumornamentum.util.Constants;
import com.ldtteam.structurize.tag.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Datagen provider for Block Entity Tags
 */
public class BlockEntityTagProvider extends TagsProvider<BlockEntityType<?>>
{

    public BlockEntityTagProvider(
      final PackOutput output,
      final ResourceKey<? extends Registry<BlockEntityType<?>>> key,
      final CompletableFuture<HolderLookup.Provider> provider)
    {
        super(output, key, provider, Constants.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider)
    {
        this.tag(ModTags.SUBSTITUTION_ABSORB_WHITELIST)
          .add(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, Identifier.parse("minecraft:chest")))
          .add(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, Identifier.parse("minecraft:sign")))
          .add(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, Identifier.parse("minecraft:lectern")))
          .add(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, Identifier.parse("domum_ornamentum:materially_textured")));
    }
}
