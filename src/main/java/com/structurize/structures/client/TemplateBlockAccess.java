package com.structurize.structures.client;

import com.structurize.structures.lib.TemplateUtils;
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
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.storage.WorldInfo;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Our world/blockAccess dummy.
 */
public class TemplateBlockAccess extends World implements IBlockAccess
{
    /**
     * The template with the info we need.
     */
    private final Template template;

    /**
     * Constructor to create a new world/blockAccess
     * @param template the template to create it from.
     */
    public TemplateBlockAccess(final Template template)
    {
        super(Minecraft.getMinecraft().world.getSaveHandler(), Minecraft.getMinecraft().world.getWorldInfo(), Minecraft.getMinecraft().world.provider, Minecraft.getMinecraft().world.profiler, true);
        this.template = template;
    }

    public Template getTemplate()
    {
        return template;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(@NotNull final BlockPos pos)
    {
        return TemplateUtils.getTileEntityFromPos(template, pos, this);
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
    public float getLightBrightness(final BlockPos pos)
    {
        return 1f;
    }

    @NotNull
    @Override
    public IBlockState getBlockState(@NotNull final BlockPos pos)
    {
        return TemplateUtils.getBlockInfoFromPos(template, pos).blockState;
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
