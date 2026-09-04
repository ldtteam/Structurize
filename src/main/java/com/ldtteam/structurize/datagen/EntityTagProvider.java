package com.ldtteam.structurize.datagen;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.tag.ModTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import java.util.concurrent.CompletableFuture;

/**
 * Datagen provider for Entity Tags
 */
public class EntityTagProvider extends TagsProvider<EntityType<?>>
{
    public EntityTagProvider(final PackOutput output,
        final ResourceKey<? extends Registry<EntityType<?>>> key,
        final CompletableFuture<Provider> future)
    {
        super(output, key, future, Constants.MOD_ID);
    }

    @Override
    protected void addTags(final Provider provider)
    {
        // 1.20.2 tick: armorstand, endcrystal, minecartfurnace, display

        tag(ModTags.PREVIEW_TICKING_ENTITIES).add(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse("minecraft:armor_stand")))
            .add(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse("minecraft:end_crystal")))
            .add(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse("minecraft:block_display")))
            .add(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse("minecraft:item_display")))
            .add(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse("minecraft:text_display")))
            .add(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse("minecraft:furnace_minecart")));
    }
}
