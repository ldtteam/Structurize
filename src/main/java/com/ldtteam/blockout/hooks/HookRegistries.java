package com.ldtteam.blockout.hooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import com.google.common.base.Predicates;
import com.mojang.blaze3d.matrix.MatrixStack;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.chunk.Chunk;

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

    private static final HookManager<?, ?, ?>[] REGISTRIES = {TILE_ENTITY_HOOKS, ENTITY_HOOKS};

    public static void tick(final long ticks)
    {
        for (int i = 0; i < REGISTRIES.length; i++)
        {
            REGISTRIES[i].tick(ticks);
        }
    }

    public static void render(final MatrixStack matrixStack, final float partialTicks)
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
         * will get closed once the condition stops being satisfied.
         * Both of opened and closed actions are listened by targetThing.
         *
         * @param targetThing registry object of thing on which gui should be displayed on
         * @param guiLoc      location of gui xml
         * @param trigger     opening condition
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
         * Both of opened and closed actions are listened by targetThing.
         *
         * @param targetThing    registry object of thing on which gui should be displayed on
         * @param guiLoc         location of gui xml
         * @param expirationTime how long should gui remain opened after the condition stops being satisfied [in millis]
         * @param trigger        opening condition
         */
        public <T extends Entity & IGuiHookable> void register(final EntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final long expirationTime,
            final TriggerMechanism<?> trigger)
        {
            register(targetThing, guiLoc, expirationTime, trigger, IGuiHookable::onOpen, IGuiHookable::onClose);
        }

        /**
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied.
         * Both of opened and closed actions can be listened using respective window callbacks.
         *
         * @param targetThing registry object of thing on which gui should be displayed on
         * @param guiLoc      location of gui xml
         * @param trigger     opening condition
         * @param onOpen      gets fired when gui is opened
         * @param onClose     gets fired when gui is closed
         */
        public <T extends Entity> void register(final EntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final TriggerMechanism<?> trigger,
            @Nullable final IGuiActionCallback<T> onOpen,
            @Nullable final IGuiActionCallback<T> onClose)
        {
            register(targetThing, guiLoc, 0, trigger, onOpen, onClose);
        }

        /**
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied + expirationTime.
         * Both of opened and closed actions can be listened using respective window callbacks.
         *
         * @param targetThing    registry object of thing on which gui should be displayed on
         * @param guiLoc         location of gui xml
         * @param expirationTime how long should gui remain opened after the condition stops being satisfied [in millis]
         * @param trigger        opening condition
         * @param onOpen         gets fired when gui is opened
         * @param onClose        gets fired when gui is closed
         */
        public <T extends Entity> void register(final EntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final long expirationTime,
            final TriggerMechanism<?> trigger,
            @Nullable final IGuiActionCallback<T> onOpen,
            @Nullable final IGuiActionCallback<T> onClose)
        {
            registerInternal(targetThing, guiLoc, expirationTime, trigger, onOpen, onClose);
        }

        @Override
        protected List<Entity> findTriggered(final EntityType<?> entityType, final TriggerMechanism<?> trigger)
        {
            final Minecraft mc = Minecraft.getInstance();
            final List<Entity> targets;

            switch (trigger.getType())
            {
                case DISTANCE:
                    targets = mc.world.getEntitiesWithinAABB((EntityType<Entity>) entityType,
                        mc.player.getBoundingBox().grow((double) trigger.getConfig()),
                        Predicates.alwaysTrue());
                    break;

                case RAY_TRACE:
                    if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == Type.ENTITY)
                    {
                        final Entity entity = ((EntityRayTraceResult) mc.objectMouseOver).getEntity();
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
        protected void translateToGuiBottomCenter(final MatrixStack ms, final Entity entity, final float partialTicks)
        {
            final double x = MathHelper.lerp(partialTicks, entity.lastTickPosX, entity.getPosX());
            final double y = MathHelper.lerp(partialTicks, entity.lastTickPosY, entity.getPosY());
            final double z = MathHelper.lerp(partialTicks, entity.lastTickPosZ, entity.getPosZ());
            ms.translate(x, y + entity.getHeight() + 0.3d, z);
        }
    }

    public static class TileEntityReg extends HookManager<TileEntity, TileEntityType<?>, BlockPos>
    {
        private TileEntityReg()
        {
        }

        /**
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied.
         * Both of opened and closed actions are listened by targetThing.
         *
         * @param targetThing registry object of thing on which gui should be displayed on
         * @param guiLoc      location of gui xml
         * @param trigger     opening condition
         */
        public <T extends TileEntity & IGuiHookable> void register(final TileEntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final TriggerMechanism<?> trigger)
        {
            register(targetThing, guiLoc, 0, trigger);
        }

        /**
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied + expirationTime.
         * Both of opened and closed actions are listened by targetThing.
         *
         * @param targetThing    registry object of thing on which gui should be displayed on
         * @param guiLoc         location of gui xml
         * @param expirationTime how long should gui remain opened after the condition stops being satisfied [in millis]
         * @param trigger        opening condition
         */
        public <T extends TileEntity & IGuiHookable> void register(final TileEntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final long expirationTime,
            final TriggerMechanism<?> trigger)
        {
            register(targetThing, guiLoc, expirationTime, trigger, IGuiHookable::onOpen, IGuiHookable::onClose);
        }

        /**
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied.
         * Both of opened and closed actions can be listened using respective window callbacks.
         *
         * @param targetThing registry object of thing on which gui should be displayed on
         * @param guiLoc      location of gui xml
         * @param trigger     opening condition
         * @param onOpen      gets fired when gui is opened
         * @param onClose     gets fired when gui is closed
         */
        public <T extends TileEntity> void register(final TileEntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final TriggerMechanism<?> trigger,
            @Nullable final IGuiActionCallback<T> onOpen,
            @Nullable final IGuiActionCallback<T> onClose)
        {
            register(targetThing, guiLoc, 0, trigger, onOpen, onClose);
        }

        /**
         * Register a gui (located at guiLoc) to targetThing. This gui will be opened everytime trigger condition is satisfied and
         * will get closed once the condition is no longer satisfied + expirationTime.
         * Both of opened and closed actions can be listened using respective window callbacks.
         *
         * @param targetThing    registry object of thing on which gui should be displayed on
         * @param guiLoc         location of gui xml
         * @param expirationTime how long should gui remain opened after the condition stops being satisfied [in millis]
         * @param trigger        opening condition
         * @param onOpen         gets fired when gui is opened
         * @param onClose        gets fired when gui is closed
         */
        public <T extends TileEntity> void register(final TileEntityType<T> targetThing,
            final ResourceLocation guiLoc,
            final long expirationTime,
            final TriggerMechanism<?> trigger,
            @Nullable final IGuiActionCallback<T> onOpen,
            @Nullable final IGuiActionCallback<T> onClose)
        {
            registerInternal(targetThing, guiLoc, expirationTime, trigger, onOpen, onClose);
        }

        @Override
        protected List<TileEntity> findTriggered(final TileEntityType<?> teType, final TriggerMechanism<?> trigger)
        {
            final Minecraft mc = Minecraft.getInstance();
            final List<TileEntity> targets;

            switch (trigger.getType())
            {
                case DISTANCE:
                    final AxisAlignedBB aabb = mc.player.getBoundingBox().grow((double) trigger.getConfig());
                    final int xStart = MathHelper.floor(aabb.minX / 16.0D);
                    final int xEnd = MathHelper.ceil(aabb.maxX / 16.0D);
                    final int zStart = MathHelper.floor(aabb.minZ / 16.0D);
                    final int zEnd = MathHelper.ceil(aabb.maxZ / 16.0D);

                    targets = new ArrayList<>();
                    for (int chunkX = xStart; chunkX < xEnd; ++chunkX)
                    {
                        for (int chunkZ = zStart; chunkZ < zEnd; ++chunkZ)
                        {
                            final Chunk chunk = mc.world.getChunkProvider().getChunk(chunkX, chunkZ, false);
                            if (chunk != null)
                            {
                                for (final Entry<BlockPos, TileEntity> entry : chunk.getTileEntityMap().entrySet())
                                {
                                    final BlockPos bp = entry.getKey();
                                    final TileEntity te = entry.getValue();
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
                    if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == Type.BLOCK)
                    {
                        final TileEntity te = mc.world.getTileEntity(((BlockRayTraceResult) mc.objectMouseOver).getPos());
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
        protected BlockPos keyMapper(final TileEntity thing)
        {
            return thing.getPos();
        }

        @Override
        protected void translateToGuiBottomCenter(final MatrixStack ms, final TileEntity thing, final float partialTicks)
        {
            ms.translate(thing.getPos().getX() + 0.5d, thing.getPos().getY() + 1.1d, thing.getPos().getZ() + 0.5d);
        }
    }
}
