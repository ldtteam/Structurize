package com.ldtteam.structurize.tag;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;

public class ModTags
{
    private ModTags()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModTags. This is a utility class");
    }

    public static final TagKey<BlockEntityType<?>> SUBSTITUTION_ABSORB_WHITELIST
            = blockEntityTag("substitution_absorb_whitelist");

    private static TagKey<BlockEntityType<?>> blockEntityTag(@NotNull final String name)
    {
        return TagKey.create(Registry.BLOCK_ENTITY_TYPE_REGISTRY,
                new ResourceLocation(MOD_ID, name));
    }
}
