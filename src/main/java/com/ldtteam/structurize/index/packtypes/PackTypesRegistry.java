package com.ldtteam.structurize.index.packtypes;

import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.index.packtypes.models.PackType;
import com.ldtteam.structurize.index.packtypes.models.PackTypeRequirement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

import static com.ldtteam.structurize.api.Registries.SCHEMATIC_INDEX_PACK_TYPES;

/**
 * Holds the deferred register and built-in {@link PackType} registrations for the schematic index system.
 *
 * <p>Third-party mods can register additional pack types by calling
 * {@link #register(ResourceLocation, Function)} after obtaining a reference to this class,
 * provided they do so before the registry freezes.
 */
public class PackTypesRegistry
{
    public static final DeferredRegister<PackType> DEFERRED_REGISTER = DeferredRegister.create(SCHEMATIC_INDEX_PACK_TYPES, Constants.MOD_ID);

    /**
     * The built-in default pack type, used as a fallback and for testing.
     * Requires a schematic named {@code "test"} at levels 1 and 2, and optionally one named {@code "flex"} at level 1.
     */
    public static final DeferredHolder<PackType, PackType> DEFAULT_PACK_TYPE =
        register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "default"),
            builder -> builder.withName(Component.literal("Default"))
                .addPackRequirement(new PackTypeRequirement.Builder().requiresName("test").requiresLevelCount(2))
                .addPackRequirement(new PackTypeRequirement.Builder().requiresName("flex").requiresLevelCount(1).optional()));

    /**
     * Registers a new {@link PackType} with the given ID, configured via the provided function.
     *
     * @param packId    the resource location used as the registry key
     * @param configure a function that receives a pre-initialized {@link PackType.Builder} and returns the configured builder
     * @return the deferred holder for the registered pack type
     */
    public static DeferredHolder<PackType, PackType> register(
        final ResourceLocation packId,
        final Function<PackType.Builder, PackType.Builder> configure)
    {
        final PackType.Builder builder = configure.apply(new PackType.Builder(packId));
        return DEFERRED_REGISTER.register(packId.getPath(), builder::build);
    }
}
