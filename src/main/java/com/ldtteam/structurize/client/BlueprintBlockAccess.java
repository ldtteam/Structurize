package com.ldtteam.structurize.client;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtils;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

/**
 * Our world/blockAccess dummy.
 * TODO: client level
 */
public class BlueprintBlockAccess extends Level
{
    private static final Scoreboard SCOREBOARD = new Scoreboard();

    /**
     * The blueprint with the info we need.
     */
    private Blueprint blueprint;

    /**
     * Constructor to create a new world/blockAccess
     * 
     * @param blueprint the blueprint.
     */
    public BlueprintBlockAccess(final Blueprint blueprint)
    {
        super((WritableLevelData) getWorld().getLevelData(),
            getWorld().dimension(),
            getWorld().dimensionTypeRegistration(),
            () -> getWorld().getProfiler(),
            true,
            true,
            0L);
        this.blueprint = blueprint;
    }

    public static Level getWorld() {
        return Minecraft.getInstance().level;
    }

    public Blueprint getBlueprint()
    {
        return blueprint;
    }

    public void setBlueprint(final Blueprint blueprintIn)
    {
        blueprint = blueprintIn;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(final BlockPos pos)
    {
        return BlueprintUtils.getTileEntityFromPos(blueprint, pos, this);
    }

        @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        final BlockState state = BlueprintUtils.getBlockInfoFromPos(blueprint, pos).getState();
        if (state.getBlock() == ModBlocks.blockSolidSubstitution.get())
        {
            return Blocks.DIRT.defaultBlockState();
        }
        if (state.getBlock() == ModBlocks.blockFluidSubstitution.get())
        {
            return Minecraft.getInstance().level != null
                    ? BlockUtils.getFluidForDimension( Minecraft.getInstance().level)
                    : Blocks.WATER.defaultBlockState();
        }
        return (state.getBlock() == ModBlocks.blockSubstitution.get() && Settings.instance.renderLightPlaceholders()) ||
                 state.getBlock() == ModBlocks.blockTagSubstitution.get() ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull)
    {
        return new BlueprintChunk(this, x, z);
    }

    @Override
    public int getMaxLocalRawBrightness(final BlockPos pos)
    {
        return 15;
    }

    @Override
    public float getBrightness(final BlockPos pos)
    {
        return 15f;
    }

    @Override
    public int getBrightness(final LightLayer lightType, final BlockPos pos)
    {
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos blockPosIn, int amount)
    {
        return 15;
    }

    @Override
    public float getShade(Direction p_230487_1_, boolean p_230487_2_)
    {
        return 0.9f;
    }

    @Override
    public Holder<Biome> getBiome(BlockPos p_226691_1_)
    {
        return getWorld().getBiome(p_226691_1_);
    }

    @Override
    public Scoreboard getScoreboard()
    {
        return SCOREBOARD;
    }

        @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        if (isOutsideBuildHeight(pos))
        {
            return Fluids.EMPTY.defaultFluidState();
        }
        else
        {
            return getBlockState(pos).getFluidState();
        }
    }

    @Override
    public boolean loadedAndEntityCanStandOnFace(BlockPos p_234929_1_, Entity p_234929_2_, Direction p_234929_3_)
    {
        return !isOutsideBuildHeight(p_234929_1_) && getBlockState(p_234929_1_).entityCanStandOnFace(this, p_234929_1_, p_234929_2_, p_234929_3_);
    }

    @Override
    public boolean isLoaded(BlockPos pos)
    {
        // Noop
        return true;
    }

    @Override
    public void updateSkyBrightness()
    {
        // Noop
    }

    @Override
    protected void prepareWeather()
    {
        // Noop
    }

    @Override
    public void close() throws IOException
    {
        // Noop
    }

    @Override
    public Explosion explode(Entity entityIn, double xIn, double yIn, double zIn, float explosionRadius, BlockInteraction modeIn)
    {
        // Noop
        return null;
    }

    @Override
    public Explosion explode(Entity entityIn,
        double xIn,
        double yIn,
        double zIn,
        float explosionRadius,
        boolean causesFire,
        BlockInteraction modeIn)
    {
        // Noop
        return null;
    }

    @Override
    public CrashReportCategory fillReportDetails(CrashReport report)
    {
        CrashReportCategory crashreportcategory = report.addCategory("Structurize rendering engine");
        crashreportcategory.setDetail("Blueprint",
            () -> blueprint.getName() + " of size: " + blueprint.getSizeX() + "|" + blueprint.getSizeY() + "|" + blueprint.getSizeZ());
        return crashreportcategory;
    }

    @Override
    public boolean setBlock(BlockPos p_241211_1_, BlockState p_241211_2_, int p_241211_3_, int p_241211_4_)
    {
        // Noop
        return false;
    }

    @Override
    public boolean destroyBlock(BlockPos p_241212_1_, boolean p_241212_2_, Entity p_241212_3_, int p_241212_4_)
    {
        // Noop
        return false;
    }

    @Override
    public BiomeManager getBiomeManager()
    {
        // Noop
        return null;
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities()
    {
        return null;
    }

    @Override
    public float getSunAngle(float partialTicks)
    {
        // Noop
        return 0;
    }

    @Override
    public long getDayTime()
    {
        // Noop
        return 6000;
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pos)
    {
        // Noop
        return null;
    }

    @Override
    public List<Entity> getEntities(Entity entityIn, AABB boundingBox, Predicate<? super Entity> predicate)
    {
        // Noop
        return null;
    }

    @Override
    public Entity getEntity(int id)
    {
        // Noop
        return null;
    }

    @Override
    public GameRules getGameRules()
    {
        // Noop
        return null;
    }

    @Override
    public long getGameTime()
    {
        // Noop
        return 6000;
    }

    @Override
    public int getHeight(Types heightmapType, int x, int z)
    {
        // Noop
        return 0;
    }

    @Override
    public LevelLightEngine getLightEngine()
    {
        // Noop
        return null;
    }

    @Override
    public MapItemSavedData getMapData(String mapName)
    {
        // Noop
        return null;
    }

    @Override
    public void setMapData(final String p_151533_, final MapItemSavedData p_151534_)
    {

    }

    @Override
    public int getFreeMapId()
    {
        // Noop
        return 0;
    }

    @Override
    public String gatherChunkSourceStats()
    {
        // Noop
        return null;
    }

    @Override
    public float getRainLevel(float delta)
    {
        // Noop
        return 0;
    }

    @Override
    public RecipeManager getRecipeManager()
    {
        // Noop
        return null;
    }

    @Override
    public int getSkyDarken()
    {
        // Noop
        return 0;
    }

    @Override
    public float getThunderLevel(float delta)
    {
        // Noop
        return 0;
    }

    @Override
    public WorldBorder getWorldBorder()
    {
        // Noop
        return null;
    }

    @Override
    public LevelData getLevelData()
    {
        // Noop
        return null;
    }

    @Override
    public boolean mayInteract(Player player, BlockPos pos)
    {
        // Noop
        return false;
    }

    @Override
    public boolean isHumidAt(BlockPos pos)
    {
        // Noop
        return false;
    }

    @Override
    public boolean isDay()
    {
        // Noop
        return true;
    }

    @Override
    public boolean isNight()
    {
        // Noop
        return false;
    }

    @Override
    public boolean isRaining()
    {
        // Noop
        return false;
    }

    @Override
    public boolean isRainingAt(BlockPos position)
    {
        // Noop
        return false;
    }

    @Override
    public boolean noSave()
    {
        // Noop
        return true;
    }

    @Override
    public boolean isThundering()
    {
        // Noop
        return false;
    }

    @Override
    public void markAndNotifyBlock(BlockPos p_241211_1_,
        LevelChunk chunk,
        BlockState blockstate,
        BlockState p_241211_2_,
        int p_241211_3_,
        int p_241211_4_)
    {
        // Noop
    }

    @Override
    public void setBlocksDirty(BlockPos blockPosIn, BlockState oldState, BlockState newState)
    {
        // Noop
    }

    @Override
    public void neighborChanged(BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        // Noop
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags)
    {
        // Noop
    }

    @Override
    public void updateNeighborsAt(BlockPos pos, Block blockIn)
    {
        // Noop
    }

    @Override
    public void updateNeighborsAtExceptFromFacing(BlockPos pos, Block blockType, Direction skipSide)
    {
        // Noop
    }

    @Override
    public void onBlockStateChange(BlockPos pos, BlockState blockStateIn, BlockState newState)
    {
        // Noop
    }

    @Override
    public void playSound(Player playerIn,
        Entity entityIn,
        SoundEvent eventIn,
        SoundSource categoryIn,
        float volume,
        float pitch)
    {
        // Noop
    }

    @Override
    public void playSound(Player player,
        double x,
        double y,
        double z,
        SoundEvent soundIn,
        SoundSource category,
        float volume,
        float pitch)
    {
        // Noop
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving)
    {
        // Noop
        return false;
    }

    @Override
    public void removeBlockEntity(BlockPos pos)
    {
        // Noop
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress)
    {
        // Noop
    }

    @Override
    public void setSpawnSettings(boolean hostile, boolean peaceful)
    {
        // Noop
    }

    @Override
    public void setRainLevel(float strength)
    {
        // Noop
    }

    @Override
    public void setThunderLevel(float strength)
    {
        // Noop
    }

    @Override
    public void tickBlockEntities()
    {
        // Noop
    }

    @Override
    public void updateNeighbourForOutputSignal(BlockPos pos, Block blockIn)
    {
        // Noop
    }

    @Override
    public boolean isUnobstructed(Entity entityIn, VoxelShape shape)
    {
        // Noop
        return true;
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ)
    {
        // Noop
        return true;
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable final Entity p_186447_, final AABB p_186448_)
    {
        return new ArrayList<>();
    }

    @Override
    public int getMaxBuildHeight()
    {
        // Noop
        return 256;
    }

    @Override
    public ChunkSource getChunkSource()
    {
        // Noop
        return null;
    }

    @Override
    public Difficulty getDifficulty()
    {
        // Noop
        return Difficulty.PEACEFUL;
    }

    @Override
    public BlockPos getHeightmapPos(Types heightmapType, BlockPos pos)
    {
        // Noop
        return null;
    }

    @NotNull
    @Override
    public RegistryAccess registryAccess()
    {
        return Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.registryAccess();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks()
    {
        return null;
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks()
    {
        return null;
    }

    @Override
    public void levelEvent(Player player, int type, BlockPos pos, int data)
    {
        // Noop
    }

    @Override
    public void gameEvent(@Nullable final Entity p_151549_, final GameEvent p_151550_, final BlockPos p_151551_)
    {

    }

    @Override
    public <T extends LivingEntity> T getNearestEntity(List<? extends T> entities,
        TargetingConditions predicate,
        LivingEntity target,
        double x,
        double y,
        double z)
    {
        // Noop
        return null;
    }

    @Override
    public Player getNearestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate)
    {
        // Noop
        return null;
    }

    @Override
    public Player getPlayerByUUID(UUID uniqueIdIn)
    {
        // Noop
        return null;
    }

    @Override
    public List<? extends Player> players()
    {
        // Noop
        return null;
    }

    @Override
    public List<Player> getNearbyPlayers(TargetingConditions predicate, LivingEntity target, AABB box)
    {
        // Noop
        return null;
    }

    @Override
    public boolean hasNearbyAlivePlayer(double x, double y, double z, double distance)
    {
        // Noop
        return false;
    }

    @Override
    public boolean canSeeSkyFromBelowWater(BlockPos pos)
    {
        // Noop
        return true;
    }

    @Override
    public int getBlockTint(BlockPos blockPosIn, ColorResolver colorResolverIn)
    {
        return super.getBlockTint(blockPosIn, colorResolverIn);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z)
    {
        // Noop
        return null;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z)
    {
        // Noop
        return null;
    }

    @Override
    public boolean hasChunksAt(int fromX, int fromY, int fromZ, int toX, int toY, int toZ)
    {
        // Noop
        return true;
    }

    @Override
    public boolean canSeeSky(BlockPos blockPosIn)
    {
        // Noop
        return true;
    }

    @Override
    public boolean addFreshEntity(Entity entityIn)
    {
        // Noop
        return false;
    }
}
