package com.ldtteam.structurize.client.fakelevel;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import java.util.function.Supplier;

/**
 * Porting: class is relatively small, just check super class manually (all of missing methods are/were just aliases)
 */
public class FakeLevelData implements WritableLevelData
{
    protected Supplier<LevelData> vanillaLevelData;
    protected final IFakeLevelLightProvider lightProvider;

    protected FakeLevelData(final Supplier<LevelData> vanillaLevelData, final IFakeLevelLightProvider lightProvider)
    {
        this.vanillaLevelData = vanillaLevelData;
        this.lightProvider = lightProvider;
    }

    @Override
    public int getXSpawn()
    {
        return 0;
    }

    @Override
    public int getYSpawn()
    {
        return 0;
    }

    @Override
    public int getZSpawn()
    {
        return 0;
    }

    @Override
    public float getSpawnAngle()
    {
        return 0;
    }

    @Override
    public long getGameTime()
    {
        return vanillaLevelData.get().getGameTime();
    }

    @Override
    public long getDayTime()
    {
        return lightProvider.forceOwnLightLevel() ? lightProvider.getDayTime() : vanillaLevelData.get().getDayTime();
    }

    @Override
    public boolean isThundering()
    {
        return false;
    }

    @Override
    public boolean isRaining()
    {
        return false;
    }

    @Override
    public void setRaining(final boolean p_78171_)
    {
        // Noop
    }

    @Override
    public boolean isHardcore()
    {
        return false;
    }

    @Override
    public GameRules getGameRules()
    {
        return vanillaLevelData.get().getGameRules();
    }

    @Override
    public Difficulty getDifficulty()
    {
        // would like peaceful but dont want to trigger entity remove in case someone actually manage to tick fake level
        return Difficulty.EASY;
    }

    @Override
    public boolean isDifficultyLocked()
    {
        return true;
    }

    @Override
    public void setXSpawn(final int p_78651_)
    {
        // Noop
    }

    @Override
    public void setYSpawn(final int p_78652_)
    {
        // Noop
    }

    @Override
    public void setZSpawn(final int p_78653_)
    {
        // Noop
    }

    @Override
    public void setSpawnAngle(final float p_78648_)
    {
        // Noop
    }
}
