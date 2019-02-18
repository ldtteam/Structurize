package com.structurize.structures.client;

import com.structurize.structures.blueprints.v1.Blueprint;
import com.structurize.structures.lib.BlueprintUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Biomes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Our world/blockAccess dummy.
 */
public class BluePrintBlockAccess extends World implements IBlockAccess
{
    /**
     * The blueprint with the info we need.
     */
    private final Blueprint blueprint;

    /**
     * Constructor to create a new world/blockAccess
     * @param blueprint the blueprint.
     */
    public BluePrintBlockAccess(final Blueprint blueprint)
    {
        super(Minecraft.getMinecraft().world.getSaveHandler(), Minecraft.getMinecraft().world.getWorldInfo(), Minecraft.getMinecraft().world.provider, Minecraft.getMinecraft().world.profiler, true);
        this.blueprint = blueprint;
    }

    public Blueprint getBlueprint()
    {
        return blueprint;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(@NotNull final BlockPos pos)
    {
        return BlueprintUtils.getTileEntityFromPos(blueprint, pos, this);
    }

    @Override
    public int getCombinedLight(@NotNull final BlockPos pos, final int lightValue)
    {
        return 15 << 20 | 15 << 4;
    }

    @Override
    public int getLight(final BlockPos pos)
    {
        return 15;
    }

    @Override
    public float getLightBrightness(@NotNull final BlockPos pos)
    {
        return 1f;
    }

    @NotNull
    @Override
    public IBlockState getBlockState(@NotNull final BlockPos pos)
    {
        return BlueprintUtils.getBlockInfoFromPos(blueprint, pos).getState();
    }

    @Override
    public boolean isAirBlock(@NotNull final BlockPos pos)
    {
        return getBlockState(pos).getBlock() instanceof BlockAir;
    }

    @Override
    protected boolean isChunkLoaded(final int x, final int z, final boolean allowEmpty)
    {
        return true;
    }

    @NotNull
    @Override
    public Biome getBiome(@NotNull final BlockPos pos)
    {
        return Biomes.PLAINS;
    }

    @NotNull
    @Override
    protected IChunkProvider createChunkProvider()
    {
        return Minecraft.getMinecraft().world.getChunkProvider();
    }

    @Override
    public int getStrongPower(@NotNull final BlockPos pos, @NotNull final EnumFacing direction)
    {
        return 0;
    }

    @NotNull
    @Override
    public WorldType getWorldType()
    {
        return WorldType.DEFAULT;
    }

    @Override
    public boolean isSideSolid(@NotNull final BlockPos pos, @NotNull final EnumFacing side, final boolean def)
    {
        return getBlockState(pos).isSideSolid(this, pos, side);
    }
}
