package com.ldtteam.structures.client;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Blueprint simulated chunk.
 */
public class BlueprintChunk extends Chunk
{
    /**
     * The block access it gets.
     */
    private final BlueprintBlockAccess access;

    /**
     * Construct the element.
     * @param worldIn the blockAccess.
     * @param x the chunk x.
     * @param z the chunk z.
     */
    public BlueprintChunk(final World worldIn, final int x, final int z)
    {
        super(worldIn, new ChunkPos(x, z), new Biome[0]);
        this.access = (BlueprintBlockAccess) worldIn;
    }
    
    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        return access.getBlockState(pos);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(@NotNull final BlockPos pos, final CreateEntityType creationMode)
    {
        return access.getTileEntity(pos);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(@NotNull final BlockPos pos)
    {
        return access.getTileEntity(pos);
    }
    
    @Override
    public IFluidState getFluidState(final BlockPos pos)
    {
        return access.getFluidState(pos);
    }
}
