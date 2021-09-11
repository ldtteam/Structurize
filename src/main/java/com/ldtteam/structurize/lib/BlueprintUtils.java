package com.ldtteam.structurize.lib;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.BlueprintBlockAccess;
import com.ldtteam.structurize.client.BlueprintBlockInfoTransformHandler;
import com.ldtteam.structurize.client.BlueprintEntityInfoTransformHandler;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.util.BlockInfo;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility functions for blueprints.
 */
public final class BlueprintUtils
{
    private static final Function<BlockPos, BlockInfo> DEFAULT_FACTORY = pos -> new BlockInfo(pos, Blocks.AIR.defaultBlockState(), null);

    private BlueprintUtils()
    {
        throw new IllegalArgumentException("Utils class");
    }

    /**
     * Get the tileEntity from a certain position.
     *
     * @param blueprint the blueprint they are in.
     * @param pos       the position they are at.
     * @param access    the world access to assign them to.
     * @return the tileEntity or null.
     */
    public static BlockEntity getTileEntityFromPos(final Blueprint blueprint, final BlockPos pos, final BlueprintBlockAccess access)
    {
        final BlockInfo blockInfo = getBlockInfoFromPos(blueprint, pos);
        if (blockInfo.getTileEntityData() != null)
        {
            return constructTileEntity(blockInfo, access);
        }
        return null;
    }

    public static BlockInfo getBlockInfoFromPos(final Blueprint blueprint, final BlockPos pos)
    {
        final BlockInfo blockInfo = blueprint.getBlockInfoAsMap().get(pos);
        return blockInfo == null ? DEFAULT_FACTORY.apply(pos) : blockInfo;
    }

    /**
     * Creates a list of tileentities located in the blueprint, placed inside that blueprints block access world.
     *
     * @param blueprint   The blueprint whos tileentities need to be instantiated.
     * @param blockAccess The blueprint world.
     * @return A list of tileentities in the blueprint.
     */
        public static Map<BlockPos, BlockEntity> instantiateTileEntities(final Blueprint blueprint, final BlueprintBlockAccess blockAccess)
    {
        return blueprint.getBlockInfoAsList()
            .stream()
            .map(blockInfo -> BlueprintBlockInfoTransformHandler.getInstance().Transform(blockInfo))
            .filter(BlockInfo::hasTileEntityData)
            .map(blockInfo -> constructTileEntity(blockInfo, blockAccess))
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(BlockEntity::getBlockPos, e -> e));
    }

    /**
     * Creates a list of entities located in the blueprint, placed inside that blueprints block access world.
     *
     * @param blueprint   The blueprint whos entities need to be instantiated.
     * @param blockAccess The blueprints world.
     * @return A list of entities in the blueprint
     */
        public static List<Entity> instantiateEntities(final Blueprint blueprint, final BlueprintBlockAccess blockAccess)
    {
        return blueprint.getEntitiesAsList()
            .stream()
            .map(entityInfo -> BlueprintEntityInfoTransformHandler.getInstance().Transform(entityInfo))
            .map(entityInfo -> constructEntity(entityInfo, blockAccess))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Nullable
    private static BlockEntity constructTileEntity(final BlockInfo info, final BlueprintBlockAccess blockAccess)
    {
        if (info.getTileEntityData() == null) return null;

        final String entityId = info.getTileEntityData().getString("id");

        try
        {
            final CompoundTag compound = info.getTileEntityData().copy();
            compound.putInt("x", info.getPos().getX());
            compound.putInt("y", info.getPos().getY());
            compound.putInt("z", info.getPos().getZ());

            final BlockEntity entity = BlockEntity.loadStatic(info.getPos(), Objects.requireNonNull(info.getState()), compound);

            if (entity != null)
            {
                entity.setLevel(blockAccess);
            }
            return entity;
        }
        catch (final Exception ex)
        {
            Log.getLogger().error("Could not create tile entity: " + entityId + " with nbt: " + info.toString(), ex);
            return null;
        }
    }

    @Nullable
    private static Entity constructEntity(@Nullable final CompoundTag info, final BlueprintBlockAccess blockAccess)
    {
        if (info == null) return null;

        final String entityId = info.getString("id");

        try
        {
            final CompoundTag compound = info.copy();
            compound.putUUID("UUID", UUID.randomUUID());
            final Optional<EntityType<?>> type = EntityType.by(compound);
            if (type.isPresent())
            {    
                final Entity entity = type.get().create(blockAccess);
    
                if (entity != null)
                {
                    entity.deserializeNBT(compound);

                    // prevent ticking rotations
                    entity.setOldPosAndRot();
                    if (entity instanceof LivingEntity lentity)
                    {
                        lentity.yHeadRotO = lentity.yHeadRot;
                        lentity.yBodyRotO = lentity.yBodyRot;
                    }

                    return entity;
                }
            }
            return null;
        }
        catch (final Exception ex)
        {
            Log.getLogger().error("Could not create entity: " + entityId + " with nbt: " + info.toString(), ex);
            return null;
        }
    }
}
