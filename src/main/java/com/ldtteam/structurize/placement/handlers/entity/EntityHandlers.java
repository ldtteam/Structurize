package com.ldtteam.structurize.placement.handlers.entity;

import com.ldtteam.structurize.placement.structure.IStructureHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeSpawnEggItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing all Entity Handler implementations.
 */
public final class EntityHandlers
{
    public static final List<IEntityHandler> handlers = new ArrayList<>();
    static
    {
        handlers.add(new MobEntityHandler());
        handlers.add(new TextDisplayHandler());
        handlers.add(new ItemFrameHandler());
        handlers.add(new HangingEntityHandler());
        handlers.add(new ArmorStandHandler());
        handlers.add(new ContainerEntityHandler());

        handlers.add(new DefaultEntityHandler());   // always last
    }

    /**
     * Allows for adding new handlers without having to clear the list
     * in other mods just to override one
     * @param handler the new handler to add
     * @param override the class to override if it can be found
     */
    public static void add(final IEntityHandler handler, final Class<?> override)
    {
        for (int i = 0; i < handlers.size(); i++)
        {
            if (override.isInstance(handlers.get(i)))
            {
                handlers.set(i, handler);
                return;
            }
        }
        add(handler);
    }

    /**
     * Adds a handler to the start of the handlers list,
     * effectively overriding existing ones with similar
     * 'canHandle' functions because this one will evaluate before them
     * @param handler the new handler to add
     */
    public static void add(final IEntityHandler handler)
    {
        handlers.add(1, handler);
    }

    /**
     * Private constructor to hide implicit one.
     */
    private EntityHandlers()
    {
        // Intentionally left empty.
    }

    /**
     * Handler for all kinds of mob
     */
    public static class MobEntityHandler implements IEntityHandler
    {
        @Override
        public boolean canHandle(final Entity entity)
        {
            return entity instanceof Mob;
        }

        @Override
        public boolean canPlace(final Entity entity, final boolean isCreative, final boolean isFancy)
        {
            return isCreative;
        }

        @Override
        public List<ItemStack> getRequiredItems(final Entity entity)
        {
            final SpawnEggItem egg = ForgeSpawnEggItem.fromEntityType(entity.getType());
            return egg == null ? List.of() : List.of(new ItemStack(egg));
        }
    }

    /**
     * Handler for text displays.
     */
    public static class TextDisplayHandler implements IEntityHandler
    {
        @Override
        public boolean canHandle(final Entity entity)
        {
            return entity instanceof Display.TextDisplay;
        }

        @Override
        public boolean canPlace(final Entity entity, final boolean isCreative, final boolean isFancy)
        {
            return isCreative && !isFancy;
        }

        @Override
        public List<ItemStack> getRequiredItems(final Entity entity)
        {
            return List.of();
        }
    }

    /**
     * Handler for item frames.
     */
    public static class ItemFrameHandler extends HangingEntityHandler
    {
        @Override
        public boolean canHandle(final Entity entity)
        {
            return entity instanceof ItemFrame;
        }

        @Override
        public List<ItemStack> getRequiredItems(final Entity entity)
        {
            final List<ItemStack> request = new ArrayList<>();
            request.add(((ItemFrame) entity).getItem().copyWithCount(1));
            request.add(new ItemStack(Items.ITEM_FRAME));
            return request;
        }
    }

    /**
     * Handler for hanging entities (e.g. paintings).
     */
    public static class HangingEntityHandler implements IEntityHandler
    {
        @Override
        public boolean canHandle(final Entity entity)
        {
            return entity instanceof HangingEntity;
        }

        @Override
        public Vec3 adjustPosition(final Entity entity, final BlockPos pos)
        {
            final HangingEntity hang = (HangingEntity) entity;
            return IEntityHandler.super.adjustPosition(entity, pos)
                    .subtract(Vec3.atLowerCornerOf(hang.blockPosition().subtract(hang.getPos())));
        }
    }

    /**
     * Handler for armor stands.
     */
    public static class ArmorStandHandler implements IEntityHandler
    {
        @Override
        public boolean canHandle(final Entity entity)
        {
            return entity instanceof ArmorStand;
        }

        @Override
        public List<ItemStack> getRequiredItems(final Entity entity)
        {
            final List<ItemStack> request = new ArrayList<>(IEntityHandler.super.getRequiredItems(entity));
            entity.getArmorSlots().forEach(request::add);
            entity.getHandSlots().forEach(request::add);
            return request;
        }
    }

    /**
     * Handler for container entities.
     */
    public static class ContainerEntityHandler implements IEntityHandler
    {
        @Override
        public boolean canHandle(final Entity entity)
        {
            return entity instanceof ContainerEntity;
        }

        @Override
        public List<ItemStack> getRequiredItems(final Entity entity)
        {
            final List<ItemStack> request = new ArrayList<>(IEntityHandler.super.getRequiredItems(entity));
            request.addAll(((ContainerEntity) entity).getItemStacks());
            return request;
        }
    }

    /**
     * Handler for any other entities.
     */
    public static class DefaultEntityHandler implements IEntityHandler
    {
        @Override
        public boolean canHandle(final Entity entity)
        {
            return true;
        }

        @Override
        public boolean canPlace(final Entity entity, final boolean isCreative, final boolean isFancy)
        {
            return isCreative;
        }
    }
}
