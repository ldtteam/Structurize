package com.ldtteam.structurize.client.fakelevel;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;

public class FakeLevelData implements WritableLevelData
{
    protected final LevelData vanillaLevelData;
    protected final IFakeLevelLightProvider lightProvider;

    protected FakeLevelData(final LevelData vanillaLevelData, final IFakeLevelLightProvider lightProvider)
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
        return vanillaLevelData.getGameTime();
    }

    @Override
    public long getDayTime()
    {
        return lightProvider.forceOwnLightLevel() ? lightProvider.getDayTime() : vanillaLevelData.getDayTime();
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
        return vanillaLevelData.getGameRules();
    }

    @Override
    public Difficulty getDifficulty()
    {
        // would like peaceful but dont want to trigger entity remove
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
