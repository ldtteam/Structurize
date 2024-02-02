package com.ldtteam.structurize.datagen;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.tag.ModTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Datagen provider for Entity Tags
 */
public class EntityTagProvider extends IntrinsicHolderTagsProvider<EntityType<?>>
{
    public EntityTagProvider(final PackOutput output,
        final ResourceKey<? extends Registry<EntityType<?>>> key,
        final CompletableFuture<Provider> future,
        @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(output, key, future, k -> BuiltInRegistries.ENTITY_TYPE.getResourceKey(k).get(), Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(final Provider provider)
    {
        // 1.20.2 tick: armorstand, endcrystal, minecartfurnace, display

        tag(ModTags.PREVIEW_TICKING_ENTITIES).add(EntityType.ARMOR_STAND)
            .add(EntityType.END_CRYSTAL)
            .add(EntityType.BLOCK_DISPLAY)
            .add(EntityType.ITEM_DISPLAY)
            .add(EntityType.TEXT_DISPLAY)
            .add(EntityType.FURNACE_MINECART);
    }
}
