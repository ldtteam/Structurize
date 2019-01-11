package com.structurize.structures.lib;

import com.google.common.base.Functions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.structurize.blockout.Log;
import com.structurize.coremod.Structurize;
import com.structurize.coremod.blocks.interfaces.IAnchorBlock;
import com.structurize.structures.client.TemplateBlockAccess;
import com.structurize.structures.client.TemplateBlockInfoTransformHandler;
import com.structurize.structures.client.TemplateEntityInfoTransformHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Utility functions for templates.
 */
public final class TemplateUtils
{
    private static final Cache<Template, Map<BlockPos, Template.BlockInfo>> templateBlockInfoCache = CacheBuilder.newBuilder().maximumSize(50).build();

    private static final Set<String> blackListedTileEntityIds = Sets.newHashSet();
    private static final Set<String> blackListedEntityIds = Sets.newHashSet();

    private TemplateUtils()
    {
        throw new IllegalArgumentException("Utils class");
    }

    /**
     * Get the tileEntity from a certain position.
     *
     * @param template the template they are in.
     * @param pos      the position they are at.
     * @param access   the world access to assign them to.
     * @return the tileEntity or null.
     */
    public static TileEntity getTileEntityFromPos(final Template template, final BlockPos pos, final TemplateBlockAccess access)
    {
        final Template.BlockInfo blockInfo = getBlockInfoFromPos(template, pos);
        if (blockInfo.tileentityData != null)
        {
            return TileEntity.create(access, blockInfo.tileentityData);
        }
        return null;
    }

    public static Template.BlockInfo getBlockInfoFromPos(final Template template, final BlockPos pos)
    {
        try
        {
            return TemplateBlockInfoTransformHandler.getInstance().Transform(Optional.ofNullable(templateBlockInfoCache
                                                                                                   .get(template,
                                                                                                     () -> template.blocks.stream()
                                                                                                             .collect(Collectors.toMap(bi -> bi.pos, Functions.identity())))
                                                                                                   .get(pos))
                                                                               .orElse(new Template.BlockInfo(pos, Blocks.AIR.getDefaultState(), null)));
        }
        catch (ExecutionException e)
        {
            Log.getLogger().warn(e);
        }

        return new Template.BlockInfo(pos, Blocks.AIR.getDefaultState(), null);
    }

    public static BlockPos getPrimaryBlockOffset(@NotNull final Template template)
    {
        return template.blocks.stream()
                 .filter(blockInfo -> blockInfo.blockState.getBlock() instanceof IAnchorBlock)
                 .findFirst()
                 .map(blockInfo -> TemplateBlockInfoTransformHandler.getInstance().Transform(blockInfo))
                 .map(blockInfo -> blockInfo.pos)
                 .orElse(new BlockPos(template.getSize().getX() / 2, 0, template.getSize().getZ() / 2));
    }

    /**
     * Creates a list of tileentities located in the template, placed inside that templates block access world.
     *
     * @param template    The template whos tileentities need to be instantiated.
     * @param blockAccess The templates world.
     * @return A list of tileentities in the template
     */
    @NotNull
    public static List<TileEntity> instantiateTileEntities(@NotNull final Template template, @NotNull final TemplateBlockAccess blockAccess)
    {
        return template.blocks.stream()
                 .map(blockInfo -> TemplateBlockInfoTransformHandler.getInstance().Transform(blockInfo))
                 .filter(blockInfo -> blockInfo.tileentityData != null)
                 .map(blockInfo -> constructTileEntity(blockInfo, blockAccess))
                 .filter(Objects::nonNull)
                 .collect(
                   Collectors.toList());
    }

    /**
     * Creates a list of entities located in the template, placed inside that templates block access world.
     *
     * @param template    The template whos entities need to be instantiated.
     * @param blockAccess The templates world.
     * @return A list of entities in the template
     */
    @NotNull
    public static List<Entity> instantiateEntities(@NotNull final Template template, @NotNull final TemplateBlockAccess blockAccess)
    {
        return template.entities.stream()
                 .map(entityInfo -> TemplateEntityInfoTransformHandler.getInstance().Transform(entityInfo))
                 .map(entityInfo -> constructEntity(entityInfo, blockAccess))
                 .filter(Objects::nonNull)
                 .collect(Collectors.toList());
    }

    @Nullable
    private static TileEntity constructTileEntity(@NotNull final Template.BlockInfo info, @NotNull final TemplateBlockAccess blockAccess)
    {
        if (info.tileentityData == null)
            return null;

        final String entityId = info.tileentityData.getString("id");

        //We know that this is going to fail.
        //Fail fast.
        if (blackListedTileEntityIds.contains(entityId))
            return null;

        try
        {
            final NBTTagCompound compound = info.tileentityData.copy();
            compound.setInteger("x", info.pos.getX());
            compound.setInteger("y", info.pos.getY());
            compound.setInteger("z", info.pos.getZ());

            return TileEntity.create(blockAccess, compound);
        }
        catch (Exception ex)
        {
            Structurize.getLogger().error("Could not create entity: " + entityId + " with nbt: " + info.tileentityData.toString());
            blackListedTileEntityIds.add(entityId);
            return null;
        }
    }

    @Nullable
    private static Entity constructEntity(@NotNull final Template.EntityInfo info, @NotNull final TemplateBlockAccess blockAccess)
    {
        if (info.entityData == null)
            return null;

        final String entityId = info.entityData.getString("id");

        //We know that this is going to fail.
        //Fail fast.
        if (blackListedEntityIds.contains(entityId))
            return null;

        try
        {
            final NBTTagCompound compound = info.entityData.copy();
            Vec3d vec3d1 = info.pos;
            NBTTagList nbttaglist = new NBTTagList();
            nbttaglist.appendTag(new NBTTagDouble(vec3d1.x));
            nbttaglist.appendTag(new NBTTagDouble(vec3d1.y));
            nbttaglist.appendTag(new NBTTagDouble(vec3d1.z));
            compound.setTag("Pos", nbttaglist);
            compound.setUniqueId("UUID", UUID.randomUUID());

            return EntityList.createEntityFromNBT(compound, blockAccess);
        }
        catch (Exception ex)
        {
            Structurize.getLogger().error("Could not create entity: " + entityId + " with nbt: " + info.entityData.toString());
            blackListedEntityIds.add(entityId);
            return null;
        }
    }
}
