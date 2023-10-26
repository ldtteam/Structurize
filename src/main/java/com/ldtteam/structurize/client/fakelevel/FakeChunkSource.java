package com.ldtteam.structurize.client.fakelevel;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

/**
 * Porting: class is relatively small, just check super class manually (all of missing methods are/were just aliases)
 */
public class FakeChunkSource extends ChunkSource
{
    private final FakeLevel fakeLevel;

    protected FakeChunkSource(final FakeLevel fakeLevel)
    {
        this.fakeLevel = fakeLevel;
    }

    @Override
    public FakeLevel getLevel()
    {
        return fakeLevel;
    }

    @Override
    @Nullable
    public ChunkAccess getChunk(final int x, final int z, final ChunkStatus chunkStatus, final boolean nonNull)
    {
        return fakeLevel.getChunk(x, z, chunkStatus, nonNull);
    }

    @Override
    public void tick(final BooleanSupplier p_202162_, final boolean p_202163_)
    {
        // noop
    }

    @Override
    public String gatherStats()
    {
        return fakeLevel.gatherChunkSourceStats();
    }

    @Override
    public int getLoadedChunksCount()
    {
        final int xCount = (fakeLevel.levelSource.getSizeX() + 15) / 16,
            zCount = (fakeLevel.levelSource.getSizeZ() + 15) / 16;
        return xCount * zCount;
    }

    @Override
    public LevelLightEngine getLightEngine()
    {
        return fakeLevel.getLightEngine();
    }
}
