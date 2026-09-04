package com.ldtteam.structurize.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import com.mojang.serialization.DynamicOps;
import com.ldtteam.structurize.api.util.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility methods for the inventories.
 */
public final class ItemStackUtils
{
    private static final HolderLookup.Provider STATIC_REGISTRIES =
        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    /**
     * Private constructor to hide the implicit one.
     */
    private ItemStackUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Get itemStack of tileEntityData. Retrieve the data from the tileEntity.
     *
     * @param compound the tileEntity stored in a compound.
     * @param state the block.
     * @return the list of itemstacks.
     */
    public static List<ItemStack> getItemStacksOfTileEntity(final CompoundTag compound, final BlockState state)
    {
        if (state.getBlock() instanceof BaseEntityBlock && compound.contains("Items"))
        {
            // because we're constructing the BlockEntity out-of-world below, chests (and perhaps a few others)
            // can't generate an IItemHandler for us, so we need to read the contents manually.
            // this could be removed if we always get a "real" BE from a world, but we're called both from a
            // real world and from a schematic non-world, and the latter still breaks.
            return getItemStacksFromNbt(compound);
        }

        BlockPos blockpos = new BlockPos(
            compound.getIntOr("x", 0),
            compound.getIntOr("y", 0),
            compound.getIntOr("z", 0)
        );
        final BlockEntity tileEntity = BlockEntity.loadStatic(blockpos, state, compound, STATIC_REGISTRIES);
        if (tileEntity == null)
        {
            return Collections.emptyList();
        }

        final List<ItemStack> items = new ArrayList<>();
        for (final IItemHandler handler : getItemHandlersFromProvider(tileEntity))
        {
            for (int slot = 0; slot < handler.getSlots(); slot++)
            {
                final ItemStack stack = handler.getStackInSlot(slot);
                if (!ItemStackUtils.isEmpty(stack))
                {
                    items.add(stack);
                }
            }
        }

        return items;
    }

    @NotNull
    private static List<ItemStack> getItemStacksFromNbt(@NotNull final CompoundTag compound)
    {
        final List<ItemStack> items = new ArrayList<>();
        final ListTag listtag = compound.getListOrEmpty("Items");

        for (int i = 0; i < listtag.size(); ++i)
        {
            final CompoundTag compoundtag = listtag.getCompoundOrEmpty(i);
            final DynamicOps<Tag> ops = STATIC_REGISTRIES.createSerializationContext(NbtOps.INSTANCE);
            final ItemStack stack = ItemStack.CODEC.parse(ops, compoundtag)
                .resultOrPartial(error -> { throw new IllegalArgumentException("Invalid item stack NBT: " + error); })
                .orElse(ItemStack.EMPTY);
            if (!stack.isEmpty())
            {
                items.add(stack);
            }
        }

        return items;
    }

    /**
     * Parses an item stack from pre-26 NBT using the built-in registries.
     */
    @NotNull
    public static ItemStack getItemStackFromNbt(@NotNull final CompoundTag compound)
    {
        if (compound.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        return ItemStack.CODEC.parse(STATIC_REGISTRIES.createSerializationContext(NbtOps.INSTANCE), compound)
            .resultOrPartial(error -> Log.getLogger().warn("Invalid item stack NBT: {}", error))
            .orElse(ItemStack.EMPTY);
    }

    /**
     * Writes an item stack using the pre-26 compound representation.
     */
    @NotNull
    public static CompoundTag writeToNbt(@NotNull final ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return new CompoundTag();
        }

        return ItemStack.CODEC.encodeStart(
                STATIC_REGISTRIES.createSerializationContext(NbtOps.INSTANCE), stack)
            .resultOrPartial(error -> Log.getLogger().warn("Failed to encode item stack: {}", error))
            .map(tag -> tag instanceof CompoundTag compound ? compound : new CompoundTag())
            .orElseGet(CompoundTag::new);
    }

    /**
     * Method to get all the IItemHandlers from a given Provider.
     *
     * @param provider The provider to get the IItemHandlers from.
     * @return A list with all the unique IItemHandlers a provider has.
     */
    public static Set<IItemHandler> getItemHandlersFromProvider(final Object provider)
    {
        final Set<IItemHandler> handlerSet = new HashSet<>();
        if (provider instanceof final BlockEntity blockEntity && blockEntity.getLevel() != null)
        {
            for (final Direction side : Direction.values())
            {
                addBlockHandler(handlerSet, blockEntity, side);
            }
            addBlockHandler(handlerSet, blockEntity, null);
        }
        else if (provider instanceof final Entity entity)
        {
            final ResourceHandler<ItemResource> handler = Capabilities.Item.ENTITY.getCapability(entity, null);
            if (handler != null)
            {
                handlerSet.add(IItemHandler.of(handler));
            }
        }
        return handlerSet;
    }

    private static void addBlockHandler(
        final Set<IItemHandler> handlers,
        final BlockEntity blockEntity,
        @Nullable final Direction side
    )
    {
        final ResourceHandler<ItemResource> handler = Capabilities.Item.BLOCK.getCapability(
            blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, side
        );
        if (handler != null)
        {
            handlers.add(IItemHandler.of(handler));
        }
    }

    /**
     * Wrapper method to check if a stack is empty.
     * Used for easy updating to 1.11.
     *
     * @param stack The stack to check.
     * @return True when the stack is empty, false when not.
     */
    public static boolean isEmpty(@Nullable final ItemStack stack)
    {
        return stack == null || stack.isEmpty() || stack == ItemStack.EMPTY || stack.getCount() <= 0;
    }

    /**
     * get the size of the stack.
     * This is for compatibility between 1.10 and 1.11
     *
     * @param stack to get the size from
     * @return the size of the stack
     */
    public static int getSize(final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return 0;
        }

        return stack.getCount();
    }

    /**
     * Get the list of required resources for entities.
     *
     * @param entity the entity object.
     * @param pos the placer pos..
     * @return a list of stacks.
     */
    public static List<ItemStack> getListOfStackForEntity(final Entity entity, final BlockPos pos)
    {
        if (entity != null)
        {
            final List<ItemStack> request = new ArrayList<>();
            if (entity instanceof ItemFrame)
            {
                final ItemStack stack = ((ItemFrame) entity).getItem();
                if (!ItemStackUtils.isEmpty(stack))
                {
                    stack.setCount(1);
                    request.add(stack);
                }
                request.add(new ItemStack(Items.ITEM_FRAME, 1));
            }
            else if (entity instanceof ArmorStand)
            {
                addIfPresent(request, entity.getPickResult());
                if (entity instanceof final LivingEntity livingEntity)
                {
                    for (final EquipmentSlot slot : EquipmentSlot.VALUES)
                    {
                        addIfPresent(request, livingEntity.getItemBySlot(slot));
                    }
                }
            }
            else if (entity instanceof ContainerEntity containerEntity)
            {
                addIfPresent(request, entity.getPickResult());
                request.addAll(containerEntity.getItemStacks());
            }

            return request.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1 The left stack to compare.
     * @param itemStack2 The right stack to compare.
     * @return True when they are equal except the stacksize, false when not.
     */
    public static boolean compareItemStacksIgnoreStackSize(final ItemStack itemStack1, final ItemStack itemStack2)
    {
        return compareItemStacksIgnoreStackSize(itemStack1, itemStack2, true, true);
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1  The left stack to compare.
     * @param itemStack2  The right stack to compare.
     * @param matchDamage Set to true to match damage data.
     * @param matchNBT    Set to true to match nbt
     * @return True when they are equal except the stacksize, false when not.
     */
    public static boolean compareItemStacksIgnoreStackSize(final ItemStack itemStack1, final ItemStack itemStack2, final boolean matchDamage, final boolean matchNBT)
    {
        return compareItemStacksIgnoreStackSize(itemStack1, itemStack2, matchDamage, matchNBT, false);
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1  The left stack to compare.
     * @param itemStack2  The right stack to compare.
     * @param matchDamage Set to true to match damage data.
     * @param matchNBT    Set to true to match nbt
     * @param min         if the count of stack2 has to be at least the same as stack1.
     * @return True when they are equal except the stacksize, false when not.
     */
    public static boolean compareItemStacksIgnoreStackSize(
      final ItemStack itemStack1,
      final ItemStack itemStack2,
      final boolean matchDamage,
      final boolean matchNBT,
      final boolean min)
    {
        if (isEmpty(itemStack1) && isEmpty(itemStack2))
        {
            return true;
        }

        if (isEmpty(itemStack1) != isEmpty(itemStack2))
        {
            return false;
        }

        if (itemStack1.getItem() == itemStack2.getItem() && (!matchDamage || itemStack1.getDamageValue() == itemStack2.getDamageValue()))
        {
            if (!matchNBT)
            {
                // Not comparing nbt
                return true;
            }

            if (min && itemStack1.getCount() > itemStack2.getCount())
            {
                return false;
            }

            // Data components replace the legacy item NBT map.
            if (matchNBT)
            {
                return ItemStack.matchesIgnoringComponents(
                    itemStack1.copyWithCount(itemStack2.getCount()),
                    itemStack2,
                    type -> !matchDamage && type == DataComponents.DAMAGE
                );
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    private static void addIfPresent(final List<ItemStack> stacks, @Nullable final ItemStack stack)
    {
        if (stack != null && !stack.isEmpty())
        {
            stacks.add(stack);
        }
    }
}
