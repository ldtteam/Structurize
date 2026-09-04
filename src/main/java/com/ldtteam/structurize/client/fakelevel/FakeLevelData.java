package com.ldtteam.structurize.client.fakelevel;

import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;

/**
 * Minimal writable level data for client-only schematic preview levels.
 */
public class FakeLevelData implements WritableLevelData
{
    protected Supplier<ClientLevel> vanillaLevel;
    protected final IFakeLevelLightProvider lightProvider;

    protected FakeLevelData(final Supplier<ClientLevel> vanillaLevel, final IFakeLevelLightProvider lightProvider)
    {
        this.vanillaLevel = vanillaLevel;
        this.lightProvider = lightProvider;
    }

    @Override
    public LevelData.RespawnData getRespawnData()
    {
        return this.vanillaLevel.get().getLevelData().getRespawnData();
    }

    @Override
    public void setSpawn(final LevelData.RespawnData respawnData)
    {
        // Spawn state is irrelevant to an in-memory preview level.
    }

    @Override
    public long getGameTime()
    {
        return this.vanillaLevel.get().getGameTime();
    }

    public long getDayTime()
    {
        return this.lightProvider.forceOwnLightLevel()
            ? this.lightProvider.getDayTime()
            : this.vanillaLevel.get().getOverworldClockTime();
    }

    public GameRules getGameRules()
    {
        return new GameRules(this.vanillaLevel.get().enabledFeatures());
    }

    @Override
    public boolean isHardcore()
    {
        return false;
    }

    @Override
    public Difficulty getDifficulty()
    {
        // Keep entities alive if a preview is accidentally ticked.
        return Difficulty.EASY;
    }

    @Override
    public boolean isDifficultyLocked()
    {
        return true;
    }
}
