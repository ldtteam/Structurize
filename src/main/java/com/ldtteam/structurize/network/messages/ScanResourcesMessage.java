package com.ldtteam.structurize.network.messages;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.ldtteam.blockui.BOScreen;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.ItemStorage;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.ldtteam.structurize.util.BlockUtils;
import com.sun.jna.platform.win32.Guid;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.codehaus.plexus.util.CachedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Client needs to ask server for build resources required (client doesn't have all data)
 */
public class ScanResourcesMessage
{
    /**
     * Request resources present in scan area from server
     */
    public static class Request implements IMessage
    {
        private final ResourceKey<Level> dimension;
        private final BlockPos pos1;
        private final BlockPos pos2;

        /**
         * Construction
         * @param dimension
         * @param pos1
         * @param pos2
         */
        public Request(@NotNull final ResourceKey<Level> dimension,
                       @NotNull final BlockPos pos1,
                       @NotNull final BlockPos pos2)
        {
            this.dimension = dimension;
            this.pos1 = pos1;
            this.pos2 = pos2;
        }

        /**
         * Deserialization
         * @param buf buffer
         */
        public Request(final FriendlyByteBuf buf)
        {
            this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
            this.pos1 = buf.readBlockPos();
            this.pos2 = buf.readBlockPos();
        }

        @Override
        public void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            buf.writeResourceLocation(this.dimension.location());
            buf.writeBlockPos(this.pos1);
            buf.writeBlockPos(this.pos2);
        }

        @Override
        public @Nullable LogicalSide getExecutionSide()
        {
            return LogicalSide.SERVER;
        }

        @Override
        public void onExecute(@NotNull final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final ServerLevel world = ServerLifecycleHooks.getCurrentServer().getLevel(this.dimension);
            if (world == null) return;

            final Map<ItemStorage, ItemStorage> resources = new HashMap<>();
            final List<Entity> entities = new ArrayList<>();

            for (int x = Math.min(pos1.getX(), pos2.getX()); x <= Math.max(pos1.getX(), pos2.getX()); x++)
            {
                for (int y = Math.min(pos1.getY(), pos2.getY()); y <= Math.max(pos1.getY(), pos2.getY()); y++)
                {
                    for (int z = Math.min(pos1.getZ(), pos2.getZ()); z <= Math.max(pos1.getZ(), pos2.getZ()); z++)
                    {
                        final BlockPos here = new BlockPos(x, y, z);
                        final BlockState blockState = world.getBlockState(here);
                        final BlockEntity tileEntity = world.getBlockEntity(here);
                        final List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(here));

                        for (final Entity entity : list)
                        {
                            // LEASH_KNOT, while not directly serializable, still serializes as part of the mob
                            // and drops a lead, so we should alert builders that it exists in the scan
                            if (entity.getType().canSerialize() || entity.getType().equals(EntityType.LEASH_KNOT))
                            {
                                entities.add(entity);
                            }
                        }

                        @Nullable final Block block = blockState.getBlock();
                        if (block == Blocks.AIR || block == Blocks.VOID_AIR || block == Blocks.CAVE_AIR)
                        {
                            addNeededResource(resources, new ItemStack(Blocks.AIR, 1), 1);
                        }
                        else
                        {
                            boolean handled = false;
                            for (final IPlacementHandler handler : PlacementHandlers.handlers)
                            {
                                if (handler.canHandle(world, BlockPos.ZERO, blockState))
                                {
                                    final List<ItemStack> itemList = handler.getRequiredItems(world, here, blockState, tileEntity == null ? null : tileEntity.saveWithFullMetadata(), true);
                                    for (final ItemStack stack : itemList)
                                    {
                                        addNeededResource(resources, stack, 1);
                                    }
                                    handled = true;
                                    break;
                                }
                            }

                            if (!handled)
                            {
                                addNeededResource(resources, BlockUtils.getItemStackFromBlockState(blockState), 1);
                            }
                        }
                    }
                }
            }

            Network.getNetwork().sendToPlayer(new Response(resources.values(), entities), ctxIn.getSender());
        }

        private static void addNeededResource(@NotNull final Map<ItemStorage, ItemStorage> resources,
                                              @Nullable final ItemStack stack,
                                              final int amount)
        {
            if (stack == null || amount == 0) return;

            final ItemStorage storage = new ItemStorage(stack);

            resources.merge(storage, storage, (a, b) ->
            {
                b.setAmount(a.getAmount() + b.getAmount());
                return b;
            });
        }
    }

    /**
     * Response with resources list for scan area
     */
    public static class Response implements IMessage
    {
        private static Consumer<Response> listener;

        private final Set<ItemStorage> resources;
        private final List<Entity> entities;

        /**
         * Client GUI needs to call this prior to sending the {@link Request} in
         * order to be called back when/if the response is received.  It must be
         * called each time as the callback will be discarded after being invoked.
         *
         * @param callback The callback to be called once on response.
         */
        public static void listenOnce(@NotNull final Consumer<Response> callback)
        {
            listener = callback;
        }

        /**
         * Construction
         */
        public Response(@NotNull final Collection<ItemStorage> resources,
                        @NotNull final Collection<Entity> entities)
        {
            this.resources = ImmutableSet.copyOf(resources);
            this.entities = ImmutableList.copyOf(entities);
        }

        /**
         * Deserialization
         * @param buf buffer
         */
        public Response(final FriendlyByteBuf buf)
        {
            final ImmutableSet.Builder<ItemStorage> resources = ImmutableSet.builder();
            final ImmutableList.Builder<Entity> entities = ImmutableList.builder();

            final int resourcesCount = buf.readVarInt();
            final int entitiesCount = buf.readVarInt();

            for (int i = 0; i < resourcesCount; ++i)
            {
                resources.add(new ItemStorage(buf.readItem()));
            }

            final List<Integer> entityIds = new ArrayList<>();
            for (int i = 0; i < entitiesCount; ++i)
            {
                entityIds.add(buf.readVarInt());
            }

            entities.addAll(DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> parseEntities(entityIds)));

            this.resources = resources.build();
            this.entities = entities.build();
        }

        @OnlyIn(Dist.CLIENT)
        private static Collection<Entity> parseEntities(@NotNull final Collection<Integer> entityIds)
        {
            // this relies on scan UI only being shown for regions within
            // entity loading range -- which is not necessarily true but is
            // usually the case
            final List<Entity> entities = new ArrayList<>();
            final Level world = Minecraft.getInstance().level;
            for (final int id : entityIds)
            {
                final Entity entity = world.getEntity(id);
                if (entity != null)
                {
                    entities.add(entity);
                }
            }
            return entities;
        }

        /**
         * The resources found during the scan.
         * @return resources
         */
        public Set<ItemStorage> getResources()
        {
            return this.resources;
        }

        /**
         * The entities found during the scan.
         * @return entities
         */
        public List<Entity> getEntities()
        {
            return this.entities;
        }

        @Override
        public void toBytes(@NotNull final FriendlyByteBuf buf)
        {
            buf.writeVarInt(this.resources.size());
            buf.writeVarInt(this.entities.size());

            for (final ItemStorage resource : this.resources)
            {
                buf.writeItem(resource.getItemStack());
            }

            for (final Entity entity : this.entities)
            {
                buf.writeVarInt(entity.getId());
            }
        }

        @Override
        public @Nullable LogicalSide getExecutionSide()
        {
            return LogicalSide.CLIENT;
        }

        @Override
        public void onExecute(@NotNull final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
        {
            final Consumer<Response> callback = listener;
            listener = null;

            if (callback != null)
            {
                callback.accept(this);
            }
        }
    }
}
