package com.ldtteam.structurize.tag;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;

public class ModTags
{
    private ModTags()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModTags. This is a utility class");
    }

    public static final TagKey<BlockEntityType<?>> SUBSTITUTION_ABSORB_WHITELIST = modTag(Registries.BLOCK_ENTITY_TYPE, "substitution_absorb_whitelist");

    public static final TagKey<Block> WEAK_SOLID_BLOCKS = modTag(Registries.BLOCK, "weak_solid_blocks");
    public static final TagKey<Block> UNSUITABLE_SOLID_FOR_PLACEHOLDER = modTag(Registries.BLOCK, "unsuitable_solid_for_placeholder");

    public static final TagKey<EntityType<?>> PREVIEW_TICKING_ENTITIES = modTag(Registries.ENTITY_TYPE, "tickable_preview_entities");

    private static <T> TagKey<T> modTag(final ResourceKey<Registry<T>> registry, @NotNull final String name)
    {
        return TagKey.create(registry, new ResourceLocation(MOD_ID, name));
    }
}
