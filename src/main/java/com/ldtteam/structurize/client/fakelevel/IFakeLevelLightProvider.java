package com.ldtteam.structurize.client.fakelevel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LightEngine;
import net.neoforged.neoforge.common.NeoForgeConfigSpec.IntValue;

/**
 * Loosely based on {@link BlockAndTintGetter}
 */
public interface IFakeLevelLightProvider
{
    public static final IFakeLevelLightProvider USE_CLIENT_LEVEL = new IFakeLevelLightProvider()
    {
        @Override
        public boolean forceOwnLightLevel()
        {
            return false;
        }

        @Override
        public int getBlockLight(final BlockPos pos)
        {
            throw new UnsupportedOperationException("Noop light provider");
        }

        @Override
        public int getSkyDarken()
        {
            throw new UnsupportedOperationException("Noop light provider");
        }
    };

    /**
     * Returning false here means no other method from this iface will get called and all logic will be redirected to current client level.
     * 
     * @return false if client level should be used instead
     */
    boolean forceOwnLightLevel();

    /**
     * @return 0-15 lighting level for given pos
     */
    int getBlockLight(BlockPos pos);

    /**
     * @return sth sth vanilla daylight progress?
     */
    int getSkyDarken();

    /**
     * @return day time from 0 to 24000
     */
    default long getDayTime()
    {
        return 6000; // noon
    }

    /**
     * @return 0-15 lighting level for given pos
     */
    default int getSkyLight(final BlockPos pos)
    {
        return getBlockLight(pos);
    }

    /**
     * @return 0-15 lighting level for given pos
     */
    default int getBrightness(final LightLayer lightLayer, final BlockPos pos)
    {
        return lightLayer == LightLayer.SKY ? getSkyLight(pos) : getBlockLight(pos);
    }

    /**
     * @return 0-15 lighting level for given pos
     */
    default int getRawBrightness(final BlockPos pos, final int skyAmount)
    {
        final int sky = getSkyLight(pos) - skyAmount;
        final int block = getBlockLight(pos);
        return Math.max(block, sky);
    }

    /**
     * Simple light level config
     */
    public static class ConfigBasedLightProvider implements IFakeLevelLightProvider
    {
        private final IntValue configValue;

        public ConfigBasedLightProvider(final IntValue configValue)
        {
            this.configValue = configValue;
        }

        @Override
        public boolean forceOwnLightLevel()
        {
            final int val = configValue.get();
            return 0 <= val && val <= LightEngine.MAX_LEVEL;
        }

        @Override
        public int getBlockLight(final BlockPos pos)
        {
            return configValue.get();
        }

        @Override
        public int getSkyDarken()
        {
            return configValue.get();
        }
    }
}
