package com.ldtteam.structurize.client.fakelevel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LayerLightSectionStorage.SectionType;
import net.minecraft.world.level.lighting.LevelLightEngine;
import javax.annotation.Nullable;

/**
 * Porting: class is relatively small, just check super class manually (all of missing methods are/were just aliases)
 */
public class FakeLevelLightEngine extends LevelLightEngine
{
    private final FakeLevel fakeLevel;
    private FakeLevelLayerLightEventListener blockLightLayer = null;
    private FakeLevelLayerLightEventListener skyLightLayer = null;

    public FakeLevelLightEngine(final FakeLevel level)
    {
        super(new LightChunkGetter()
        {
            @Override
            public LightChunk getChunkForLighting(final int p_63023_, final int p_63024_)
            {
                throw new UnsupportedOperationException("Should never happen - FakeLevel light engine ctor");
            }

            @Override
            public BlockGetter getLevel()
            {
                return level;
            }
        }, false, false);

        this.fakeLevel = level;
    }

    @Override
    public int getRawBrightness(final BlockPos p_75832_, final int p_75833_)
    {
        return fakeLevel.getRawBrightness(p_75832_, p_75833_);
    }

    @Override
    public String getDebugData(final LightLayer p_75817_, final SectionPos p_75818_)
    {
        return "FakeLevel light engine redirect - " + p_75817_;
    }

    @Override
    public LayerLightEventListener getLayerListener(final LightLayer p_75815_)
    {
        return switch (p_75815_)
        {
            case BLOCK -> {
                if (blockLightLayer == null)
                {
                    blockLightLayer = new FakeLevelLayerLightEventListener(p_75815_);
                }
                yield blockLightLayer;
            }
            case SKY -> {
                if (skyLightLayer == null)
                {
                    skyLightLayer = new FakeLevelLayerLightEventListener(p_75815_);
                }
                yield skyLightLayer;
            }
        };
    }

    @Override
    public SectionType getDebugSectionType(final LightLayer p_285008_, final SectionPos p_285336_)
    {
        // Noop, only debug rendering ?
        return SectionType.EMPTY;
    }

    @Override
    public boolean lightOnInSection(final SectionPos p_285319_)
    {
        // Noop, used only in chunk compiling?
        return false;
    }

    @Override
    public void checkBlock(final BlockPos p_75823_)
    {
        // Noop
    }

    @Override
    public boolean hasLightWork()
    {
        // Noop
        return false;
    }

    @Override
    public void propagateLightSources(final ChunkPos p_284998_)
    {
        // Noop
    }

    @Override
    public void queueSectionData(final LightLayer p_285328_, final SectionPos p_284962_, final DataLayer p_285035_)
    {
        // Noop
    }

    @Override
    public void retainData(final ChunkPos p_75829_, final boolean p_75830_)
    {
        // Noop
    }

    @Override
    public int runLightUpdates()
    {
        // Noop
        return 0;
    }

    @Override
    public void setLightEnabled(final ChunkPos p_285439_, final boolean p_285012_)
    {
        // Noop
    }

    @Override
    public void updateSectionStatus(final SectionPos p_75827_, final boolean p_75828_)
    {
        // Noop
    }

    @Override
    public void updateSectionStatus(final BlockPos p_75835_, final boolean p_75836_)
    {
        // Noop
    }

    /*
    @Override
    public int getLightSectionCount()
    {
        // super is fine
        return super.getLightSectionCount();
    }
    
    @Override
    public int getMaxLightSection()
    {
        // super is fine
        return super.getMaxLightSection();
    }
    
    @Override
    public int getMinLightSection()
    {
        // super is fine
        return super.getMinLightSection();
    }
    */

    private class FakeLevelLayerLightEventListener implements LayerLightEventListener
    {
        private final LightLayer lightLayer;

        private FakeLevelLayerLightEventListener(final LightLayer lightLayer)
        {
            this.lightLayer = lightLayer;
        }

        @Override
        public void checkBlock(final BlockPos p_164454_)
        {
            // Noop
        }

        @Override
        public boolean hasLightWork()
        {
            // Noop
            return false;
        }

        @Override
        public int runLightUpdates()
        {
            // Noop
            return 0;
        }

        @Override
        public void updateSectionStatus(final SectionPos p_75837_, final boolean p_75838_)
        {
            // Noop
        }

        @Override
        public void setLightEnabled(final ChunkPos p_164452_, final boolean p_164453_)
        {
            // Noop
        }

        @Override
        public void propagateLightSources(final ChunkPos p_285263_)
        {
            // Noop
        }

        @Override
        @Nullable
        public DataLayer getDataLayerData(final SectionPos p_75709_)
        {
            // Noop
            return null;
        }

        @Override
        public int getLightValue(final BlockPos p_75710_)
        {
            return fakeLevel.getBrightness(lightLayer, p_75710_);
        }
    }
}
