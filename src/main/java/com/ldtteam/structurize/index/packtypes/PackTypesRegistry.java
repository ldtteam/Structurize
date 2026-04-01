package com.ldtteam.structurize.index.packtypes;

import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.index.packtypes.models.PackType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

import static com.ldtteam.structurize.api.Registries.SCHEMATIC_INDEX_PACK_TYPES;

public class PackTypesRegistry
{
    public static final DeferredRegister<PackType> DEFERRED_REGISTER = DeferredRegister.create(SCHEMATIC_INDEX_PACK_TYPES, Constants.MOD_ID);

    public static final DeferredHolder<PackType, PackType> DEFAULT_PACK_TYPE =
        register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "default"), builder -> builder.withName(Component.literal("Default")));

    public static DeferredHolder<PackType, PackType> register(
        final ResourceLocation packId,
        final Function<PackType.Builder, PackType.Builder> configure)
    {
        final PackType.Builder builder = configure.apply(new PackType.Builder(packId));
        return DEFERRED_REGISTER.register(packId.getPath(), builder::build);
    }
}
