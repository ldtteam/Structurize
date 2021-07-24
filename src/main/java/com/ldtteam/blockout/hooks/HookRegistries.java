package com.ldtteam.blockout.hooks;

import com.google.common.base.Predicates;
import com.ldtteam.blockout.hooks.TriggerMechanism.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

/**
 * Class holding all hook registries.
 */
public final class HookRegistries
{
    /**
     * EntityType registry
     */
    public static final EntityReg ENTITY_HOOKS = new EntityReg();
    /**
     * TileEntityType registry
     */
    public static final TileEntityReg TILE_ENTITY_HOOKS = new TileEntityReg();

    // should be in the same order as world render
    private static final HookManager<?, ?, ?>[] REGISTRIES = {TILE_ENTITY_HOOKS, ENTITY_HOOKS};

    public static void tick(final long ticks)
    {
        for (int i = 0; i < REGISTRIES.length; i++)
        {
            REGISTRIES[i].tick(ticks);
        }
    }

    public static void render(final PoseStack matrixStack, final float partialTicks)
    {
        for (int i = 0; i < REGISTRIES.length; i++)
        {
            REGISTRIES[i].render(matrixStack, partialTicks);
        }
    }

    public static class EntityReg extends HookManager<Entity, EntityType<?>, Entity>
    {
        private EntityReg()
        {
        }

        /**
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied.
         * Gui callbacks are managed by instance of targetThing.
         *
         * @param targetThing registry object of thing on which gui should be displayed on
         * @param guiLoc      location of gui xml
         * @param trigger     trigger condition
         * @see IGuiHookable
         */
        public <T extends Entity & IGuiHookable> void register(final EntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final TriggerMechanism<?> trigger)
        {
            register(targetThing, guiLoc, 0, trigger);
        }

        /**
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied + expirationTime.
         * Gui callbacks are managed by instance of targetThing.
         *
         * @param targetThing    registry object of thing on which gui should be displayed on
         * @param guiLoc         location of gui xml
         * @param expirationTime how long should gui remain opened after the condition stops being satisfied [in millis]
         * @param trigger        trigger condition
         * @see IGuiHookable
         */
        public <T extends Entity & IGuiHookable> void register(final EntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final long expirationTime,
            final TriggerMechanism<?> trigger)
        {
            register(targetThing, guiLoc, expirationTime, trigger, IGuiHookable::shouldOpen, IGuiHookable::onOpen, IGuiHookable::onClose);
        }

        /**
         * <p>
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied.
         * </p><p>
         * {@link IGuiHookable Gui callbacks} are managed by their respective parameters.
         * </p>
         *
         * @param targetThing registry object of thing on which gui should be displayed on
         * @param guiLoc      location of gui xml
         * @param trigger     trigger condition
         * @param shouldOpen  gets fired when gui is about to be opened, can deny opening
         * @param onOpen      gets fired when gui is opened
         * @param onClose     gets fired when gui is closed
         * @see IGuiHookable for gui callbacks
         */
        public <T extends Entity> void register(final EntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final TriggerMechanism<?> trigger,
            @Nullable final BiPredicate<T, Type> shouldOpen,
            @Nullable final IGuiActionCallback<T> onOpen,
            @Nullable final IGuiActionCallback<T> onClose)
        {
            register(targetThing, guiLoc, 0, trigger, shouldOpen, onOpen, onClose);
        }

        /**
         * <p>
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied + expirationTime.
         * </p><p>
         * {@link IGuiHookable Gui callbacks} are managed by their respective parameters.
         * </p>
         *
         * @param targetThing    registry object of thing on which gui should be displayed on
         * @param guiLoc         location of gui xml
         * @param expirationTime how long should gui remain opened after the condition stops being satisfied [in millis]
         * @param trigger        trigger condition
         * @param shouldOpen  gets fired when gui is about to be opened, can deny opening
         * @param onOpen         gets fired when gui is opened
         * @param onClose        gets fired when gui is closed
         * @see IGuiHookable for gui callbacks
         */
        public <T extends Entity> void register(final EntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final long expirationTime,
            final TriggerMechanism<?> trigger,
            @Nullable final BiPredicate<T, Type> shouldOpen,
            @Nullable final IGuiActionCallback<T> onOpen,
            @Nullable final IGuiActionCallback<T> onClose)
        {
            registerInternal(targetThing, guiLoc, expirationTime, trigger, shouldOpen, onOpen, onClose);
        }

        @Override
        protected List<Entity> findTriggered(final EntityType<?> entityType, final TriggerMechanism<?> trigger)
        {
            final Minecraft mc = Minecraft.getInstance();
            final List<Entity> targets;

            switch (trigger.getType())
            {
                case DISTANCE:
                    targets = mc.level.getEntities((EntityType<Entity>) entityType,
                        mc.player.getBoundingBox().inflate(((TriggerMechanism<Double>) trigger).getConfig()),
                        Predicates.alwaysTrue());
                    break;

                case RAY_TRACE:
                    if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY)
                    {
                        final Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
                        targets = entity.getType() == entityType ? Arrays.asList(entity) : Collections.emptyList();
                    }
                    else
                    {
                        targets = Collections.emptyList();
                    }
                    break;

                default:
                    throw new IllegalArgumentException("No trigger mechanism for Entity and " + trigger.getName() + " trigger.");
            }

            return targets;
        }

        @Override
        protected Entity keyMapper(final Entity thing)
        {
            return thing;
        }

        @Override
        protected void translateToGuiBottomCenter(final PoseStack ms, final Entity entity, final float partialTicks)
        {
            final double x = Mth.lerp(partialTicks, entity.xOld, entity.getX());
            final double y = Mth.lerp(partialTicks, entity.yOld, entity.getY());
            final double z = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
            ms.translate(x, y + entity.getBbHeight() + 0.3d, z);
        }
    }

    public static class TileEntityReg extends HookManager<BlockEntity, BlockEntityType<?>, BlockPos>
    {
        private TileEntityReg()
        {
        }

        /**
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied.
         * Gui callbacks are managed by instance of targetThing.
         *
         * @param targetThing registry object of thing on which gui should be displayed on
         * @param guiLoc      location of gui xml
         * @param trigger     trigger condition
         * @see IGuiHookable
         */
        public <T extends BlockEntity & IGuiHookable> void register(final BlockEntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final TriggerMechanism<?> trigger)
        {
            register(targetThing, guiLoc, 0, trigger);
        }

        /**
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied + expirationTime.
         * Gui callbacks are managed by instance of targetThing.
         *
         * @param targetThing    registry object of thing on which gui should be displayed on
         * @param guiLoc         location of gui xml
         * @param expirationTime how long should gui remain opened after the condition stops being satisfied [in millis]
         * @param trigger        trigger condition
         * @see IGuiHookable
         */
        public <T extends BlockEntity & IGuiHookable> void register(final BlockEntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final long expirationTime,
            final TriggerMechanism<?> trigger)
        {
            register(targetThing, guiLoc, expirationTime, trigger, IGuiHookable::shouldOpen, IGuiHookable::onOpen, IGuiHookable::onClose);
        }

        /**
         * <p>
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied.
         * </p><p>
         * {@link IGuiHookable Gui callbacks} are managed by their respective parameters.
         * </p>
         *
         * @param targetThing registry object of thing on which gui should be displayed on
         * @param guiLoc      location of gui xml
         * @param trigger     trigger condition
         * @param shouldOpen  gets fired when gui is about to be opened, can deny opening
         * @param onOpen      gets fired when gui is opened
         * @param onClose     gets fired when gui is closed
         * @see IGuiHookable for gui callbacks
         */
        public <T extends BlockEntity> void register(final BlockEntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final TriggerMechanism<?> trigger,
            @Nullable final BiPredicate<T, Type> shouldOpen,
            @Nullable final IGuiActionCallback<T> onOpen,
            @Nullable final IGuiActionCallback<T> onClose)
        {
            register(targetThing, guiLoc, 0, trigger, shouldOpen, onOpen, onClose);
        }

        /**
         * <p>
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied + expirationTime.
         * </p><p>
         * {@link IGuiHookable Gui callbacks} are managed by their respective parameters.
         * </p>
         *
         * @param targetThing    registry object of thing on which gui should be displayed on
         * @param guiLoc         location of gui xml
         * @param expirationTime how long should gui remain opened after the condition stops being satisfied [in millis]
         * @param trigger        trigger condition
         * @param shouldOpen  gets fired when gui is about to be opened, can deny opening
         * @param onOpen         gets fired when gui is opened
         * @param onClose        gets fired when gui is closed
         * @see IGuiHookable for gui callbacks
         */
        public <T extends BlockEntity> void register(final BlockEntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final long expirationTime,
            final TriggerMechanism<?> trigger,
            @Nullable final BiPredicate<T, Type> shouldOpen,
            @Nullable final IGuiActionCallback<T> onOpen,
            @Nullable final IGuiActionCallback<T> onClose)
        {
            registerInternal(targetThing, guiLoc, expirationTime, trigger, shouldOpen, onOpen, onClose);
        }

        @Override
        protected List<BlockEntity> findTriggered(final BlockEntityType<?> teType, final TriggerMechanism<?> trigger)
        {
            final Minecraft mc = Minecraft.getInstance();
            final List<BlockEntity> targets;

            switch (trigger.getType())
            {
                case DISTANCE:
                    final AABB aabb = mc.player.getBoundingBox().inflate(((TriggerMechanism<Double>) trigger).getConfig());
                    final int xStart = Mth.floor(aabb.minX / 16.0D);
                    final int xEnd = Mth.ceil(aabb.maxX / 16.0D);
                    final int zStart = Mth.floor(aabb.minZ / 16.0D);
                    final int zEnd = Mth.ceil(aabb.maxZ / 16.0D);

                    targets = new ArrayList<>();
                    for (int chunkX = xStart; chunkX < xEnd; ++chunkX)
                    {
                        for (int chunkZ = zStart; chunkZ < zEnd; ++chunkZ)
                        {
                            final LevelChunk chunk = mc.level.getChunkSource().getChunk(chunkX, chunkZ, false);
                            if (chunk != null)
                            {
                                for (final Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet())
                                {
                                    final BlockPos bp = entry.getKey();
                                    final BlockEntity te = entry.getValue();
                                    if (te.getType() == teType && bp.getX() > aabb.minX && bp.getX() < aabb.maxX
                                        && bp.getY() > aabb.minY && bp.getY() < aabb.maxY && bp.getZ() > aabb.minZ && bp.getZ() < aabb.maxZ)
                                    {
                                        targets.add(te);
                                    }
                                }
                            }
                        }
                    }
                    break;

                case RAY_TRACE:
                    if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK)
                    {
                        final BlockEntity te = mc.level.getBlockEntity(((BlockHitResult) mc.hitResult).getBlockPos());
                        targets = te == null || te.getType() != teType ? Collections.emptyList() : Arrays.asList(te);
                    }
                    else
                    {
                        targets = Collections.emptyList();
                    }
                    break;

                default:
                    throw new IllegalArgumentException("No trigger mechanism for TileEntity and " + trigger.getName() + " trigger.");
            }

            return targets;
        }

        @Override
        protected BlockPos keyMapper(final BlockEntity thing)
        {
            return thing.getBlockPos();
        }

        @Override
        protected void translateToGuiBottomCenter(final PoseStack ms, final BlockEntity thing, final float partialTicks)
        {
            ms.translate(thing.getBlockPos().getX() + 0.5d, thing.getBlockPos().getY() + 1.1d, thing.getBlockPos().getZ() + 0.5d);
        }
    }
}
