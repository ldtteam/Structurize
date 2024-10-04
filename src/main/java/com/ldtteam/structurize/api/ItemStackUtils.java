package com.ldtteam.structurize.api;

import com.ldtteam.common.fakelevel.SingleBlockFakeLevel.SidedSingleBlockFakeLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility methods for the inventories.
 */
public final class ItemStackUtils
{
    private static final SidedSingleBlockFakeLevel itemHandlerFakeLevel = new SidedSingleBlockFakeLevel();

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
     * @param level real vanilla instance for fakeLevel
     * @return the list of itemstacks.
     */
    public static List<ItemStack> getItemStacksOfTileEntity(final CompoundTag compound, final BlockState state, final Level level)
    {
        final BlockPos blockpos = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
        final BlockEntity tileEntity = BlockEntity.loadStatic(blockpos, state, compound, level.registryAccess());
        if (tileEntity == null)
        {
            return Collections.emptyList();
        }

        return itemHandlerFakeLevel.get(level).useFakeLevelContext(state, tileEntity, level, fakeLevel -> {
            final List<ItemStack> items = new ArrayList<>();
            for (final IItemHandler handler : getItemHandlersFromProvider(tileEntity))
            {
                for (int slot = 0; slot < handler.getSlots(); slot++)
                {
                    final ItemStack stack = handler.getStackInSlot(slot).copy();
                    if (!ItemStackUtils.isEmpty(stack))
                    {
                        items.add(stack);
                    }
                }
            }
            return items;
        });
    }

    /**
     * Method to get sensible item handlers from blockEntity. Tries to provide whole deduplicated content. However this assumption is
     * weak. There still might be content (in returned set) that is not present at all or duplicated.
     *
     * @param provider The provider to get the IItemHandlers from.
     * @return A list with all the unique IItemHandlers a provider has.
     */
    public static Set<IItemHandler> getItemHandlersFromProvider(final BlockEntity provider)
    {
        if (provider instanceof final IItemHandler itemHandler)
        {
            // be is itemHandler itself = easy
            return Set.of(itemHandler);
        }
        if (provider instanceof final Container container)
        {
            // be is vanilla container = itemHandler cap might return SidedInvWrapper with partial inv view
            return Set.of(new InvWrapper(container));
        }

        final IItemHandler unsidedItemHandler = Capabilities.ItemHandler.BLOCK.getCapability(provider.getLevel(), provider.getBlockPos(), provider.getBlockState(), provider, null);
        if (unsidedItemHandler != null)
        {
            // weak assumption of unsided being partial view only
            return Set.of(unsidedItemHandler);
        }

        final Set<IItemHandler> handlerSet = new HashSet<>();
        for (final Direction side : Direction.values())
        {
            final IItemHandler cap = Capabilities.ItemHandler.BLOCK.getCapability(provider.getLevel(), provider.getBlockPos(), provider.getBlockState(), provider, side);
            if (cap != null)
            {
                handlerSet.add(cap);
            }
        }
        // weakest assumption of sided itemHandler having disjoint sides
        return handlerSet;
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
            if (entity instanceof final ItemFrame itemFrame)
            {
                final ItemStack stack = itemFrame.getItem();
                if (!ItemStackUtils.isEmpty(stack))
                {
                    stack.setCount(1);
                    request.add(stack);
                }
                request.add(new ItemStack(Items.ITEM_FRAME, 1));
            }
            else if (entity instanceof final ArmorStand armorStand)
            {
                request.add(entity.getPickedResult(new HitResult(Vec3.atLowerCornerOf(pos)) {
                    @Override
                    public Type getType()
                    {
                        return Type.ENTITY;
                    }
                }));
                armorStand.getArmorSlots().forEach(request::add);
                armorStand.getHandSlots().forEach(request::add);
            }
            else if (entity instanceof ContainerEntity containerEntity)
            {
                request.add(entity.getPickedResult(new HitResult(Vec3.atLowerCornerOf(pos)) {
                    @Override
                    public Type getType()
                    {
                        return Type.ENTITY;
                    }
                }));
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

            // Then sort on NBT
            if (!itemStack1.getComponents().isEmpty() && !itemStack2.getComponents().isEmpty())
            {
                final DataComponentMap nbt1 = itemStack1.getComponents();
                final DataComponentMap nbt2 = itemStack2.getComponents();

                for(final DataComponentType<?> key : nbt1.keySet())
                {
                    if(!matchDamage && key == DataComponents.DAMAGE)
                    {
                        continue;
                    }
                    if(!nbt2.has(key) || !nbt1.get(key).equals(nbt2.get(key)))
                    {
                        return false;
                    }
                }

                return nbt1.keySet().size() == nbt2.keySet().size();
            }
            else
            {
                return itemStack1.getComponents().isEmpty() == itemStack2.getComponents().isEmpty();
            }
        }
        return false;
    }
}
