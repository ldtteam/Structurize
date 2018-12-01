package com.structurize.structures.lib;

import com.google.common.base.Functions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.structurize.blockout.Log;
import com.structurize.coremod.blocks.interfaces.IAnchorBlock;
import com.structurize.structures.client.TemplateBlockAccess;
import com.structurize.structures.client.TemplateBlockAccessTransformHandler;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Utility functions for templates.
 */
public final class TemplateUtils
{
    private static final Cache<Template, Map<BlockPos, Template.BlockInfo>> templateBlockInfoCache = CacheBuilder.newBuilder().maximumSize(50).build();

    private TemplateUtils()
    {
        throw new IllegalArgumentException("Utils class");
    }

    /**
     * Get the tileEntity from a certain position.
     * @param template the template they are in.
     * @param pos the position they are at.
     * @param access the world access to assign them to.
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
            return TemplateBlockAccessTransformHandler.getInstance().Transform(Optional.ofNullable(templateBlockInfoCache
                                         .get(template, () -> template.blocks.stream().collect(Collectors.toMap(bi -> bi.pos, Functions.identity())))
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
                 .map(blockInfo -> TemplateBlockAccessTransformHandler.getInstance().Transform(blockInfo))
                 .map(blockInfo -> blockInfo.pos)
                 .orElse(new BlockPos(template.getSize().getX() / 2, 0, template.getSize().getZ() / 2));
    }
}
