package com.ldtteam.structures.client;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

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
        super(worldIn, x, z);
        this.access = (BlueprintBlockAccess) worldIn;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(final BlockPos pos, final EnumCreateEntityType creationMode)
    {
        return access.getTileEntity(pos);
    }
}
