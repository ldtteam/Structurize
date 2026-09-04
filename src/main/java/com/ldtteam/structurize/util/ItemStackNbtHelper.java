package com.ldtteam.structurize.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

/**
 * Compatibility boundary between Structurize's persisted item tags and Minecraft's data components.
 */
public final class ItemStackNbtHelper
{
    private ItemStackNbtHelper()
    {
    }

    public static boolean hasCustomTag(final ItemStack stack)
    {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    @Nullable
    public static CompoundTag getCustomTag(final ItemStack stack)
    {
        final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData == null ? null : customData.copyTag();
    }

    public static CompoundTag getOrCreateCustomTag(final ItemStack stack)
    {
        final CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        return tag;
    }

    public static void setCustomTag(final ItemStack stack, final CompoundTag tag)
    {
        if (tag.isEmpty())
        {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
        else
        {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public static ItemStack readNetworkStack(final FriendlyByteBuf buf)
    {
        final RegistryFriendlyByteBuf registryBuf = new RegistryFriendlyByteBuf(
            buf,
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        return ItemStack.OPTIONAL_STREAM_CODEC.decode(registryBuf);
    }

    public static void writeNetworkStack(final FriendlyByteBuf buf, final ItemStack stack)
    {
        final RegistryFriendlyByteBuf registryBuf = new RegistryFriendlyByteBuf(
            buf,
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuf, stack);
    }
}
