package com.ldtteam.structurize.api;

import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.index.packtypes.models.PackType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class Registries
{
    public static final ResourceKey<Registry<PackType>> SCHEMATIC_INDEX_PACK_TYPES =
        ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, ("schematic_index_pack_types")));
}
