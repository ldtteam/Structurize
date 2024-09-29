package com.ldtteam.structurize.client.fakelevel;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer.Continuation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * As much as general fake level. Features:
 * <ul>
 * <li>static access to given data</li>
 * <li>immutability - disables all external changes (but levelSource can be mutable)</li>
 * <li>most of dimension related things is delegated to current client level (class instances can travel accross dimensions)</li>
 * <li>biome info is also delegated from client level</li>
 * <li>light control - manual or delegated from client level</li>
 * <li>primitive chunk and entity management</li>
 * <li>basic heightmap support (not fully working yet)</li>
 * <li><b>Few unsafe NPEs methods :)</b></li>
 * </ul>
 * <p>
 * 
 * Porting info:
 * <ol>
 * <li>uncomment last method section</li>
 * <li>fix compile errors</li>
 * <li>add override for remaining methods and sort/implement them accordingly</li>
 * <li>comment last method section</li>
 * </ol><p>
 * 
 * TODO: extend from client level
 */
public class FakeLevel extends Level
{
    protected IFakeLevelBlockGetter levelSource;
    protected final IFakeLevelLightProvider lightProvider;
    protected Level realLevel;
    protected final Scoreboard scoreboard;
    protected final boolean overrideBeLevel;

    protected final FakeChunkSource chunkSource;
    protected final FakeLevelLightEngine lightEngine;
    protected FakeLevelEntityGetterAdapter levelEntityGetter = FakeLevelEntityGetterAdapter.EMPTY;
    // TODO: this is currently manually filled by class user - ideally if not filled yet this should get constructed from levelSource
    // manually
    protected Map<BlockPos, BlockEntity> blockEntities = Collections.emptyMap();

    /**
     * Current rendering worldPos so we can use client level real info
     */
    protected BlockPos worldPos = BlockPos.ZERO;

    // chunk cache
    int lastX, lastZ;
    ChunkAccess lastChunk = null;

    /**
     * @param levelSource     data source, also try to set block entities/entities collections
     * @param lightProvider   light source
     * @param scoreboard      if null client level is used instead
     * @param overrideBeLevel if true all block entities will have set level to this instance
     * @see #setBlockEntities(Map) for better block entity handling, if set then levelSource BE getter is not used
     * @see #setEntities(Collection) only way to add entities into fake level
     * @see #setRealLevel(Level) if you want to reuse this instance
     */
    public FakeLevel(final IFakeLevelBlockGetter levelSource,
        final IFakeLevelLightProvider lightProvider,
        @Nullable final Scoreboard scoreboard,
        final boolean overrideBeLevel)
    {
        super(new FakeLevelData(clientLevel().getLevelData(), lightProvider),
            clientLevel().dimension(),
            clientLevel().registryAccess(),
            clientLevel().dimensionTypeRegistration(),
            clientLevel().getProfilerSupplier(),
            clientLevel().isClientSide(),
            false,
            0,
            0);
        this.levelSource = levelSource;
        this.lightProvider = lightProvider;
        this.realLevel = clientLevel();
        this.scoreboard = scoreboard;
        this.overrideBeLevel = overrideBeLevel;
        this.chunkSource = new FakeChunkSource(this);
        this.lightEngine = new FakeLevelLightEngine(this);

        setRealLevel(clientLevel());
    }

    // ========================================
    // ========== FAKE LEVEL METHODS ==========
    // ========================================

    @SuppressWarnings("resource")
    protected static ClientLevel clientLevel()
    {
        return Minecraft.getInstance().level;
    }

    public void setRealLevel(final Level realLevel)
    {
        if (Objects.equals(this.realLevel, realLevel))
        {
            return;
        }

        if (realLevel != null && realLevel.isClientSide != this.isClientSide)
        {
            throw new IllegalArgumentException("Received wrong sided realLevel - fakeLevel.isClientSide = " + this.isClientSide);
        }

        this.realLevel = realLevel;
        ((FakeLevelData) this.getLevelData()).vanillaLevelData = realLevel == null ? null : realLevel.getLevelData();
    }

    public Level realLevel()
    {
        return realLevel;
    }

    /**
     * @param levelSource new data source
     */
    public void setLevelSource(final IFakeLevelBlockGetter levelSource)
    {
        this.levelSource = levelSource;
    }

    /**
     * @return current data source
     */
    public IFakeLevelBlockGetter getLevelSource()
    {
        return levelSource;
    }

    /**
     * @param worldPos where is fake level anchor when querying current client level data
     */
    public void setWorldPos(final BlockPos worldPos)
    {
        this.worldPos = worldPos;
    }

    /**
     * @return anchor in vanilla client level
     */
    public BlockPos getWorldPos()
    {
        return worldPos;
    }

    /**
     * For better block entity handling in chunk methods. If set then {@link IFakeLevelBlockGetter#getBlockEntity(BlockPos)
     * levelSource.getBlockEntity(BlockPos)} is not used. Reset with empty collection
     * 
     * @param blockEntities all block entities, should be data equivalent to levelSource
     */
    public void setBlockEntities(final Map<BlockPos, BlockEntity> blockEntities)
    {
        this.blockEntities = blockEntities;
    }

    /**
     * @param entities all entities, their level should be this fake level instance. Reset with empty collection
     */
    public void setEntities(final Collection<? extends Entity> entities)
    {
        levelEntityGetter = entities.isEmpty() ? FakeLevelEntityGetterAdapter.EMPTY : FakeLevelEntityGetterAdapter.ofEntities(entities);
    }

    // ========================================
    // ======= CTOR REAL LEVEL REDIRECTS ======
    // ========================================
    // Note: must have null check because super ctor

    @Override
    public ResourceKey<Level> dimension()
    {
        return realLevel() != null ? realLevel().dimension() : super.dimension();
    }

    @Override
    public RegistryAccess registryAccess()
    {
        return realLevel() != null ? realLevel().registryAccess() : super.registryAccess();
    }

    @Override
    public DamageSources damageSources()
    {
        return realLevel() != null ? realLevel().damageSources() : super.damageSources();
    }

    @Override
    public ProfilerFiller getProfiler()
    {
        return realLevel() != null ? realLevel().getProfiler() : super.getProfiler();
    }

    @Override
    public Supplier<ProfilerFiller> getProfilerSupplier()
    {
        return realLevel() != null ? realLevel().getProfilerSupplier() : super.getProfilerSupplier();
    }

    @Override
    public DimensionType dimensionType()
    {
        return realLevel() != null ? realLevel().dimensionType() : super.dimensionType();
    }

    @Override
    public Holder<DimensionType> dimensionTypeRegistration()
    {
        return realLevel() != null ? realLevel().dimensionTypeRegistration() : super.dimensionTypeRegistration();
    }

    @Override
    public WorldBorder getWorldBorder()
    {
        return realLevel() != null ? realLevel().getWorldBorder() : super.getWorldBorder();
    }

    // ========================================
    // ========== REDIRECTED METHODS ==========
    // ========================================

    @Nullable
    @Override
    public BlockEntity getBlockEntity(final BlockPos pos)
    {
        final BlockEntity blockEntity = blockEntities.isEmpty() ? levelSource.getBlockEntity(pos) : blockEntities.get(pos);
        if (blockEntity != null && blockEntity.getLevel() != this && (overrideBeLevel || !blockEntity.hasLevel()))
        {
            blockEntity.setLevel(this);
        }
        return blockEntity;
    }

    @Override
    @Nullable
    public BlockEntity getExistingBlockEntity(BlockPos pos)
    {
        return getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        return levelSource.isPosInside(pos) ? levelSource.getBlockState(pos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull)
    {
        if (lastX == x && lastZ == z && lastChunk != null)
        {
            return lastChunk;
        }
        return nonnull || hasChunk(x, z) ? new FakeChunk(this, x, z) : null;
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ)
    {
        final int posX = SectionPos.sectionToBlockCoord(chunkX);
        final int posZ = SectionPos.sectionToBlockCoord(chunkZ);
        return levelSource.getMinX() <= posX && posX < levelSource.getMaxX() &&
            levelSource.getMinZ() <= posZ &&
            posZ < levelSource.getMaxZ();
    }

    @Override
    public int getBrightness(final LightLayer lightType, final BlockPos pos)
    {
        return lightProvider.forceOwnLightLevel() ? lightProvider.getBrightness(lightType, pos) :
            realLevel().getBrightness(lightType, worldPos.offset(pos));
    }

    @Override
    public int getRawBrightness(BlockPos pos, int amount)
    {
        return lightProvider.forceOwnLightLevel() ? lightProvider.getRawBrightness(pos, amount) :
            realLevel().getRawBrightness(worldPos.offset(pos), amount);
    }

    @Override
    public int getSkyDarken()
    {
        return lightProvider.forceOwnLightLevel() ? lightProvider.getSkyDarken() : realLevel().getSkyDarken();
    }

    @Override
    public boolean isDay()
    {
        return !this.dimensionType().hasFixedTime() && this.getSkyDarken() < 4;
    }

    @Override
    public Scoreboard getScoreboard()
    {
        return scoreboard == null ? realLevel().getScoreboard() : scoreboard;
    }

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        return levelSource.getFluidState(pos);
    }

    @Override
    public int getHeight()
    {
        return levelSource.getHeight();
    }

    @Override
    public int getMinBuildHeight()
    {
        return levelSource.getMinBuildHeight();
    }

    @Override
    public boolean isInWorldBounds(final BlockPos pos)
    {
        return levelSource.isPosInside(pos);
    }

    @Override
    public CrashReportCategory fillReportDetails(CrashReport report)
    {
        CrashReportCategory crashreportcategory = report.addCategory("Structurize fake level");
        levelSource.describeSelfInCrashReport(crashreportcategory);
        return crashreportcategory;
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities()
    {
        return levelEntityGetter;
    }

    @Override
    @javax.annotation.Nullable
    public Entity getEntity(int id)
    {
        return levelEntityGetter.get(id);
    }

    @Override
    public List<AbstractClientPlayer> players()
    {
        final List<AbstractClientPlayer> result = new ArrayList<>();
        levelEntityGetter.get(EntityTypeTest.forClass(AbstractClientPlayer.class), player -> {
            result.add(player);
            return Continuation.CONTINUE;
        });
        return result;
    }

    @Override
    public int getHeight(Types heightmapType, int x, int z)
    {
        final MutableBlockPos pos = new MutableBlockPos(x, levelSource.getMinBuildHeight(), z);

        if (levelSource.isPosInside(pos))
        {
            for (int y = levelSource.getMaxBuildHeight() - 1; y >= levelSource.getMinBuildHeight(); y--)
            {
                pos.setY(y);
                if (heightmapType.isOpaque().test(levelSource.getBlockState(pos)))
                {
                    return y;
                }
            }
        }
            
        return levelSource.getMinBuildHeight();
    }

    @Override
    public ChunkSource getChunkSource()
    {
        return chunkSource;
    }

    @Override
    public LevelLightEngine getLightEngine()
    {
        return lightEngine;
    }

    @Override
    public String gatherChunkSourceStats()
    {
        return "Fake level for: " + levelSource;
    }

    @Override
    public float getShade(Direction p_104703_, boolean p_104704_)
    {
        return realLevel().getShade(p_104703_, p_104704_);
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pos)
    {
        return realLevel().getBiome(worldPos.offset(pos));
    }

    @Override
    public BiomeManager getBiomeManager()
    {
        return realLevel().getBiomeManager();
    }

    @Override
    public RecipeManager getRecipeManager()
    {
        return realLevel().getRecipeManager();
    }

    @Override
    public FeatureFlagSet enabledFeatures()
    {
        return realLevel().enabledFeatures();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z)
    {
        return realLevel().getUncachedNoiseBiome(x, y, z);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z)
    {
        return realLevel().getNoiseBiome(x, y, z);
    }

    // ========================================
    // ======= NOOP UNSAFE NULL METHODS =======
    // ========================================

    @Override
    public Explosion explode(@javax.annotation.Nullable Entity p_256233_,
        @javax.annotation.Nullable DamageSource p_255861_,
        @javax.annotation.Nullable ExplosionDamageCalculator p_255867_,
        double p_256447_,
        double p_255732_,
        double p_255717_,
        float p_256013_,
        boolean p_256228_,
        ExplosionInteraction p_255784_,
        boolean p_256377_)
    {
        throw new UnsupportedOperationException("Structurize fake immutable level - no explosions possible!");
    }

    // ========================================
    // ========== PERMANENT SETTINGS ==========
    // ========================================

    @Override
    public boolean isLoaded(BlockPos pos)
    {
        // Noop
        return true;
    }

    @Override
    public float getRainLevel(float delta)
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
    public boolean isRainingAt(BlockPos position)
    {
        return isRaining();
    }

    @Override
    public boolean noSave()
    {
        // Noop
        return true;
    }

    @Override
    public int getSeaLevel()
    {
        return 0;
    }

    // ========================================
    // ============ NOOP OVERRIDES ============
    // ========================================

    @Override
    public void destroyBlockProgress(int p_46506_, BlockPos p_46507_, int p_46508_)
    {
        // Noop
    }

    @Override
    public int getFreeMapId()
    {
        // Noop
        return 0;
    }

    @Override
    @javax.annotation.Nullable
    public MapItemSavedData getMapData(String p_46650_)
    {
        // Noop
        return null;
    }

    @Override
    public void playSeededSound(@javax.annotation.Nullable Player p_220372_,
        Entity p_220373_,
        Holder<SoundEvent> p_263500_,
        SoundSource p_220375_,
        float p_220376_,
        float p_220377_,
        long p_220378_)
    {
        // Noop
    }

    @Override
    public void playSeededSound(@javax.annotation.Nullable Player p_262953_,
        double p_263004_,
        double p_263398_,
        double p_263376_,
        Holder<SoundEvent> p_263359_,
        SoundSource p_263020_,
        float p_263055_,
        float p_262914_,
        long p_262991_)
    {
        // Noop
    }

    @Override
    public void sendBlockUpdated(BlockPos p_46612_, BlockState p_46613_, BlockState p_46614_, int p_46615_)
    {
        // Noop
    }

    @Override
    public void setMapData(String p_151533_, MapItemSavedData p_151534_)
    {
        // Noop
    }

    @Override
    public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, Context p_220406_)
    {
        // Noop
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks()
    {
        // Noop
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks()
    {
        // Noop
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public void levelEvent(@javax.annotation.Nullable Player p_46771_, int p_46772_, BlockPos p_46773_, int p_46774_)
    {
        // Noop
    }

    // ========================================
    // ============= NOOP METHODS =============
    // ========================================

    @Override
    public void addBlockEntityTicker(TickingBlockEntity p_151526_)
    {
        // Noop
    }

    @Override
    public void addFreshBlockEntities(Collection<BlockEntity> beList)
    {
        // Noop
    }

    @Override
    public void blockEvent(BlockPos p_46582_, Block p_46583_, int p_46584_, int p_46585_)
    {
        // Noop
    }

    @Override
    public void close() throws IOException
    {
        // Noop
    }

    @Override
    public boolean destroyBlock(BlockPos p_46626_, boolean p_46627_, @javax.annotation.Nullable Entity p_46628_, int p_46629_)
    {
        // Noop
        return false;
    }

    @Override
    public void markAndNotifyBlock(BlockPos p_46605_,
        @javax.annotation.Nullable LevelChunk levelchunk,
        BlockState blockstate,
        BlockState p_46606_,
        int p_46607_,
        int p_46608_)
    {
        // Noop
    }

    @Override
    public boolean mayInteract(Player p_46557_, BlockPos p_46558_)
    {
        // Noop
        return false;
    }

    @Override
    public void neighborShapeChanged(Direction p_220385_,
        BlockState p_220386_,
        BlockPos p_220387_,
        BlockPos p_220388_,
        int p_220389_,
        int p_220390_)
    {
        // Noop
    }

    @Override
    public boolean removeBlock(BlockPos p_46623_, boolean p_46624_)
    {
        return false;
    }

    @Override
    public boolean setBlock(BlockPos p_46605_, BlockState p_46606_, int p_46607_, int p_46608_)
    {
        // Noop
        return false;
    }

    @Override
    public void setRainLevel(float p_46735_)
    {
        // Noop
    }

    @Override
    public void setSpawnSettings(boolean p_46704_, boolean p_46705_)
    {
        // Noop
    }

    @Override
    public void setThunderLevel(float p_46708_)
    {
        // Noop
    }

    @Override
    public boolean shouldTickBlocksAt(long p_186456_)
    {
        // Noop
        return false;
    }

    @Override
    public boolean shouldTickDeath(Entity p_186458_)
    {
        // Noop
        return false;
    }

    @Override
    protected void tickBlockEntities()
    {
        // Noop
    }

    @Override
    public void updateNeighborsAt(BlockPos p_46673_, Block p_46674_)
    {
        // Noop
    }

    @Override
    public void updateSkyBrightness()
    {
        // Noop
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        // Noop
        return LazyOptional.empty();
    }

    @Override
    public void invalidateCaps()
    {
        // Noop
    }

    @Override
    public void reviveCaps()
    {
        // Noop
    }

    // ========================================
    // ======== SUPER IS FINE METHODS =========
    // ========================================

    /*
    @Override
    public void removeBlockEntity(BlockPos p_46748_)
    {
        super.removeBlockEntity(p_46748_);
    }

    @Override
    public void addAlwaysVisibleParticle(ParticleOptions p_46684_,
        double p_46685_,
        double p_46686_,
        double p_46687_,
        double p_46688_,
        double p_46689_,
        double p_46690_)
    {
        super.addAlwaysVisibleParticle(p_46684_, p_46685_, p_46686_, p_46687_, p_46688_, p_46689_, p_46690_);
    }

    @Override
    public void addAlwaysVisibleParticle(ParticleOptions p_46691_,
        boolean p_46692_,
        double p_46693_,
        double p_46694_,
        double p_46695_,
        double p_46696_,
        double p_46697_,
        double p_46698_)
    {
        super.addAlwaysVisibleParticle(p_46691_, p_46692_, p_46693_, p_46694_, p_46695_, p_46696_, p_46697_, p_46698_);
    }

    @Override
    public void addDestroyBlockEffect(BlockPos p_151531_, BlockState p_151532_)
    {
        super.addDestroyBlockEffect(p_151531_, p_151532_);
    }

    @Override
    public void addParticle(ParticleOptions p_46631_,
        double p_46632_,
        double p_46633_,
        double p_46634_,
        double p_46635_,
        double p_46636_,
        double p_46637_)
    {
        super.addParticle(p_46631_, p_46632_, p_46633_, p_46634_, p_46635_, p_46636_, p_46637_);
    }

    @Override
    public void addParticle(ParticleOptions p_46638_,
        boolean p_46639_,
        double p_46640_,
        double p_46641_,
        double p_46642_,
        double p_46643_,
        double p_46644_,
        double p_46645_)
    {
        super.addParticle(p_46638_, p_46639_, p_46640_, p_46641_, p_46642_, p_46643_, p_46644_, p_46645_);
    }

    @Override
    public void blockEntityChanged(BlockPos p_151544_)
    {
        super.blockEntityChanged(p_151544_);
    }

    @Override
    public void broadcastDamageEvent(Entity p_270831_, DamageSource p_270361_)
    {
        super.broadcastDamageEvent(p_270831_, p_270361_);
    }

    @Override
    public void broadcastEntityEvent(Entity p_46509_, byte p_46510_)
    {
        super.broadcastEntityEvent(p_46509_, p_46510_);
    }

    @Override
    public void createFireworks(double p_46475_,
        double p_46476_,
        double p_46477_,
        double p_46478_,
        double p_46479_,
        double p_46480_,
        @javax.annotation.Nullable CompoundTag p_46481_)
    {
        super.createFireworks(p_46475_, p_46476_, p_46477_, p_46478_, p_46479_, p_46480_, p_46481_);
    }

    @Override
    public ResourceKey<DimensionType> dimensionTypeId()
    {
        return super.dimensionTypeId();
    }

    @Override
    public void disconnect()
    {
        super.disconnect();
    }

    @Override
    public Explosion explode(@javax.annotation.Nullable Entity p_256599_,
        double p_255914_,
        double p_255684_,
        double p_255843_,
        float p_256310_,
        ExplosionInteraction p_256178_)
    {
        return super.explode(p_256599_, p_255914_, p_255684_, p_255843_, p_256310_, p_256178_);
    }

    @Override
    public Explosion explode(@javax.annotation.Nullable Entity p_255682_,
        double p_255803_,
        double p_256403_,
        double p_256538_,
        float p_255674_,
        boolean p_256634_,
        ExplosionInteraction p_256111_)
    {
        return super.explode(p_255682_, p_255803_, p_256403_, p_256538_, p_255674_, p_256634_, p_256111_);
    }

    @Override
    public Explosion explode(@javax.annotation.Nullable Entity p_255653_,
        @javax.annotation.Nullable DamageSource p_256558_,
        @javax.annotation.Nullable ExplosionDamageCalculator p_255929_,
        Vec3 p_256001_,
        float p_255963_,
        boolean p_256099_,
        ExplosionInteraction p_256371_)
    {
        return super.explode(p_255653_, p_256558_, p_255929_, p_256001_, p_255963_, p_256099_, p_256371_);
    }

    @Override
    public Explosion explode(@javax.annotation.Nullable Entity p_256145_,
        @javax.annotation.Nullable DamageSource p_256004_,
        @javax.annotation.Nullable ExplosionDamageCalculator p_255696_,
        double p_256208_,
        double p_256036_,
        double p_255746_,
        float p_256647_,
        boolean p_256098_,
        ExplosionInteraction p_256104_)
    {
        return super.explode(p_256145_, p_256004_, p_255696_, p_256208_, p_256036_, p_255746_, p_256647_, p_256098_, p_256104_);
    }

    @Override
    public BlockPos getBlockRandomPos(int p_46497_, int p_46498_, int p_46499_, int p_46500_)
    {
        return super.getBlockRandomPos(p_46497_, p_46498_, p_46499_, p_46500_);
    }

    @Override
    public LevelChunk getChunk(int p_46727_, int p_46728_)
    {
        return super.getChunk(p_46727_, p_46728_);
    }

    @Override
    public LevelChunk getChunkAt(BlockPos p_46746_)
    {
        return super.getChunkAt(p_46746_);
    }

    @Override
    @javax.annotation.Nullable
    public BlockGetter getChunkForCollisions(int p_46711_, int p_46712_)
    {
        return super.getChunkForCollisions(p_46711_, p_46712_);
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos p_46730_)
    {
        return super.getCurrentDifficultyAt(p_46730_);
    }

    @Override
    public long getDayTime()
    {
        return super.getDayTime();
    }

    @Override
    public List<Entity> getEntities(@javax.annotation.Nullable Entity p_46536_, AABB p_46537_, Predicate<? super Entity> p_46538_)
    {
        return super.getEntities(p_46536_, p_46537_, p_46538_);
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> p_151528_, AABB p_151529_, Predicate<? super T> p_151530_)
    {
        return super.getEntities(p_151528_, p_151529_, p_151530_);
    }

    @Override
    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> p_261899_,
        AABB p_261837_,
        Predicate<? super T> p_261519_,
        List<? super T> p_262046_)
    {
        super.getEntities(p_261899_, p_261837_, p_261519_, p_262046_);
    }

    @Override
    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> p_261885_,
        AABB p_262086_,
        Predicate<? super T> p_261688_,
        List<? super T> p_262071_,
        int p_261858_)
    {
        super.getEntities(p_261885_, p_262086_, p_261688_, p_262071_, p_261858_);
    }

    @Override
    public GameRules getGameRules()
    {
        return super.getGameRules();
    }

    @Override
    public long getGameTime()
    {
        return super.getGameTime();
    }

    @Override
    public LevelData getLevelData()
    {
        return super.getLevelData();
    }

    @Override
    public double getMaxEntityRadius()
    {
        return super.getMaxEntityRadius();
    }

    @Override
    public RandomSource getRandom()
    {
        return super.getRandom();
    }

    @Override
    @javax.annotation.Nullable
    public MinecraftServer getServer()
    {
        return super.getServer();
    }

    @Override
    public float getSharedSpawnAngle()
    {
        return super.getSharedSpawnAngle();
    }

    @Override
    public BlockPos getSharedSpawnPos()
    {
        return super.getSharedSpawnPos();
    }

    @Override
    public float getSunAngle(float p_46491_)
    {
        return super.getSunAngle(p_46491_);
    }

    @Override
    public void globalLevelEvent(int p_46665_, BlockPos p_46666_, int p_46667_)
    {
        super.globalLevelEvent(p_46665_, p_46666_, p_46667_);
    }

    @Override
    public <T extends Entity> void guardEntityTick(Consumer<T> p_46654_, T p_46655_)
    {
        super.guardEntityTick(p_46654_, p_46655_);
    }

    @Override
    public double increaseMaxEntityRadius(double value)
    {
        return super.increaseMaxEntityRadius(value);
    }

    @Override
    public boolean isClientSide()
    {
        return super.isClientSide();
    }

    @Override
    public boolean isFluidAtPosition(BlockPos p_151541_, Predicate<FluidState> p_151542_)
    {
        return super.isFluidAtPosition(p_151541_, p_151542_);
    }

    @Override
    public boolean isNight()
    {
        return super.isNight();
    }

    @Override
    public boolean isRaining()
    {
        return super.isRaining();
    }

    @Override
    public boolean isStateAtPosition(BlockPos p_46620_, Predicate<BlockState> p_46621_)
    {
        return super.isStateAtPosition(p_46620_, p_46621_);
    }

    @Override
    public boolean isThundering()
    {
        return super.isThundering();
    }

    @Override
    public boolean loadedAndEntityCanStandOn(BlockPos p_46576_, Entity p_46577_)
    {
        return super.loadedAndEntityCanStandOn(p_46576_, p_46577_);
    }

    @Override
    public boolean loadedAndEntityCanStandOnFace(BlockPos p_46579_, Entity p_46580_, Direction p_46581_)
    {
        return super.loadedAndEntityCanStandOnFace(p_46579_, p_46580_, p_46581_);
    }

    @Override
    public void neighborChanged(BlockPos p_46587_, Block p_46588_, BlockPos p_46589_)
    {
        super.neighborChanged(p_46587_, p_46588_, p_46589_);
    }

    @Override
    public void neighborChanged(BlockState p_220379_, BlockPos p_220380_, Block p_220381_, BlockPos p_220382_, boolean p_220383_)
    {
        super.neighborChanged(p_220379_, p_220380_, p_220381_, p_220382_, p_220383_);
    }

    @Override
    public long nextSubTickCount()
    {
        return super.nextSubTickCount();
    }

    @Override
    public void onBlockStateChange(BlockPos p_46609_, BlockState p_46610_, BlockState p_46611_)
    {
        super.onBlockStateChange(p_46609_, p_46610_, p_46611_);
    }

    @Override
    public void playLocalSound(BlockPos p_250938_,
        SoundEvent p_252209_,
        SoundSource p_249161_,
        float p_249980_,
        float p_250277_,
        boolean p_250151_)
    {
        super.playLocalSound(p_250938_, p_252209_, p_249161_, p_249980_, p_250277_, p_250151_);
    }

    @Override
    public void playLocalSound(double p_46482_,
        double p_46483_,
        double p_46484_,
        SoundEvent p_46485_,
        SoundSource p_46486_,
        float p_46487_,
        float p_46488_,
        boolean p_46489_)
    {
        super.playLocalSound(p_46482_, p_46483_, p_46484_, p_46485_, p_46486_, p_46487_, p_46488_, p_46489_);
    }

    @Override
    public void playSeededSound(@javax.annotation.Nullable Player p_220363_,
        double p_220364_,
        double p_220365_,
        double p_220366_,
        SoundEvent p_220367_,
        SoundSource p_220368_,
        float p_220369_,
        float p_220370_,
        long p_220371_)
    {
        super.playSeededSound(p_220363_, p_220364_, p_220365_, p_220366_, p_220367_, p_220368_, p_220369_, p_220370_, p_220371_);
    }

    @Override
    public void playSound(@javax.annotation.Nullable Entity p_252137_,
        BlockPos p_251749_,
        SoundEvent p_248842_,
        SoundSource p_251104_,
        float p_249531_,
        float p_250763_)
    {
        super.playSound(p_252137_, p_251749_, p_248842_, p_251104_, p_249531_, p_250763_);
    }

    @Override
    public void playSound(@javax.annotation.Nullable Player p_46560_,
        BlockPos p_46561_,
        SoundEvent p_46562_,
        SoundSource p_46563_,
        float p_46564_,
        float p_46565_)
    {
        super.playSound(p_46560_, p_46561_, p_46562_, p_46563_, p_46564_, p_46565_);
    }

    @Override
    public void playSound(@javax.annotation.Nullable Player p_46551_,
        Entity p_46552_,
        SoundEvent p_46553_,
        SoundSource p_46554_,
        float p_46555_,
        float p_46556_)
    {
        super.playSound(p_46551_, p_46552_, p_46553_, p_46554_, p_46555_, p_46556_);
    }

    @Override
    public void playSound(@javax.annotation.Nullable Player p_46543_,
        double p_46544_,
        double p_46545_,
        double p_46546_,
        SoundEvent p_46547_,
        SoundSource p_46548_,
        float p_46549_,
        float p_46550_)
    {
        super.playSound(p_46543_, p_46544_, p_46545_, p_46546_, p_46547_, p_46548_, p_46549_, p_46550_);
    }

    @Override
    protected void prepareWeather()
    {
        super.prepareWeather();
    }

    @Override
    public void sendPacketToServer(Packet<?> p_46657_)
    {
        super.sendPacketToServer(p_46657_);
    }

    @Override
    public boolean setBlock(BlockPos p_46601_, BlockState p_46602_, int p_46603_)
    {
        return super.setBlock(p_46601_, p_46602_, p_46603_);
    }

    @Override
    public boolean setBlockAndUpdate(BlockPos p_46598_, BlockState p_46599_)
    {
        return super.setBlockAndUpdate(p_46598_, p_46599_);
    }

    @Override
    public void setBlocksDirty(BlockPos p_46678_, BlockState p_46679_, BlockState p_46680_)
    {
        super.setBlocksDirty(p_46678_, p_46679_, p_46680_);
    }

    @Override
    public void setSkyFlashTime(int p_46709_)
    {
        super.setSkyFlashTime(p_46709_);
    }

    @Override
    public void setBlockEntity(BlockEntity p_151524_)
    {
        super.setBlockEntity(p_151524_);
    }

    @Override
    public boolean shouldTickBlocksAt(BlockPos p_220394_)
    {
        return super.shouldTickBlocksAt(p_220394_);
    }

    @Override
    public void updateNeighborsAtExceptFromFacing(BlockPos p_46591_, Block p_46592_, Direction p_46593_)
    {
        super.updateNeighborsAtExceptFromFacing(p_46591_, p_46592_, p_46593_);
    }

    @Override
    public void blockUpdated(BlockPos p_46781_, Block p_46782_)
    {
        super.blockUpdated(p_46781_, p_46782_);
    }

    @Override
    public long dayTime()
    {
        return super.dayTime();
    }

    @Override
    public void gameEvent(@javax.annotation.Nullable Entity p_220401_, GameEvent p_220402_, Vec3 p_220403_)
    {
        super.gameEvent(p_220401_, p_220402_, p_220403_);
    }

    @Override
    public void gameEvent(@javax.annotation.Nullable Entity p_151549_, GameEvent p_151550_, BlockPos p_151551_)
    {
        super.gameEvent(p_151549_, p_151550_, p_151551_);
    }

    @Override
    public void gameEvent(GameEvent p_220408_, BlockPos p_220409_, Context p_220410_)
    {
        super.gameEvent(p_220408_, p_220409_, p_220410_);
    }

    @Override
    public Difficulty getDifficulty()
    {
        return super.getDifficulty();
    }

    @Override
    public void levelEvent(int p_46797_, BlockPos p_46798_, int p_46799_)
    {
        super.levelEvent(p_46797_, p_46798_, p_46799_);
    }

    @Override
    public void playSound(@javax.annotation.Nullable Player p_251195_, BlockPos p_250192_, SoundEvent p_249887_, SoundSource p_250593_)
    {
        super.playSound(p_251195_, p_250192_, p_249887_, p_250593_);
    }

    @Override
    public void scheduleTick(BlockPos p_186461_, Block p_186462_, int p_186463_)
    {
        super.scheduleTick(p_186461_, p_186462_, p_186463_);
    }

    @Override
    public void scheduleTick(BlockPos p_186470_, Fluid p_186471_, int p_186472_)
    {
        super.scheduleTick(p_186470_, p_186471_, p_186472_);
    }

    @Override
    public void scheduleTick(BlockPos p_186465_, Block p_186466_, int p_186467_, TickPriority p_186468_)
    {
        super.scheduleTick(p_186465_, p_186466_, p_186467_, p_186468_);
    }

    @Override
    public void scheduleTick(BlockPos p_186474_, Fluid p_186475_, int p_186476_, TickPriority p_186477_)
    {
        super.scheduleTick(p_186474_, p_186475_, p_186476_, p_186477_);
    }

    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos p_151452_, BlockEntityType<T> p_151453_)
    {
        return super.getBlockEntity(p_151452_, p_151453_);
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@javax.annotation.Nullable Entity p_186447_, AABB p_186448_)
    {
        return super.getEntityCollisions(p_186447_, p_186448_);
    }

    @Override
    public BlockPos getHeightmapPos(Types p_45831_, BlockPos p_45832_)
    {
        return super.getHeightmapPos(p_45831_, p_45832_);
    }

    @Override
    public boolean isUnobstructed(@javax.annotation.Nullable Entity p_45828_, VoxelShape p_45829_)
    {
        return super.isUnobstructed(p_45828_, p_45829_);
    }

    @Override
    public List<Entity> getEntities(@javax.annotation.Nullable Entity p_45934_, AABB p_45935_)
    {
        return super.getEntities(p_45934_, p_45935_);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<T> p_45977_, AABB p_45978_)
    {
        return super.getEntitiesOfClass(p_45977_, p_45978_);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<T> p_45979_, AABB p_45980_, Predicate<? super T> p_45981_)
    {
        return super.getEntitiesOfClass(p_45979_, p_45980_, p_45981_);
    }

    @Override
    public <T extends LivingEntity> List<T> getNearbyEntities(Class<T> p_45972_,
        TargetingConditions p_45973_,
        LivingEntity p_45974_,
        AABB p_45975_)
    {
        return super.getNearbyEntities(p_45972_, p_45973_, p_45974_, p_45975_);
    }

    @Override
    public List<Player> getNearbyPlayers(TargetingConditions p_45956_, LivingEntity p_45957_, AABB p_45958_)
    {
        return super.getNearbyPlayers(p_45956_, p_45957_, p_45958_);
    }

    @Override
    @javax.annotation.Nullable
    public <T extends LivingEntity> T getNearestEntity(List<? extends T> p_45983_,
        TargetingConditions p_45984_,
        @javax.annotation.Nullable LivingEntity p_45985_,
        double p_45986_,
        double p_45987_,
        double p_45988_)
    {
        return super.getNearestEntity(p_45983_, p_45984_, p_45985_, p_45986_, p_45987_, p_45988_);
    }

    @Override
    @javax.annotation.Nullable
    public <T extends LivingEntity> T getNearestEntity(Class<? extends T> p_45964_,
        TargetingConditions p_45965_,
        @javax.annotation.Nullable LivingEntity p_45966_,
        double p_45967_,
        double p_45968_,
        double p_45969_,
        AABB p_45970_)
    {
        return super.getNearestEntity(p_45964_, p_45965_, p_45966_, p_45967_, p_45968_, p_45969_, p_45970_);
    }

    @Override
    @javax.annotation.Nullable
    public Player getNearestPlayer(Entity p_45931_, double p_45932_)
    {
        return super.getNearestPlayer(p_45931_, p_45932_);
    }

    @Override
    @javax.annotation.Nullable
    public Player getNearestPlayer(TargetingConditions p_45947_, LivingEntity p_45948_)
    {
        return super.getNearestPlayer(p_45947_, p_45948_);
    }

    @Override
    @javax.annotation.Nullable
    public Player getNearestPlayer(TargetingConditions p_45942_, double p_45943_, double p_45944_, double p_45945_)
    {
        return super.getNearestPlayer(p_45942_, p_45943_, p_45944_, p_45945_);
    }

    @Override
    @javax.annotation.Nullable
    public Player getNearestPlayer(double p_45919_,
        double p_45920_,
        double p_45921_,
        double p_45922_,
        @javax.annotation.Nullable Predicate<Entity> p_45923_)
    {
        return super.getNearestPlayer(p_45919_, p_45920_, p_45921_, p_45922_, p_45923_);
    }

    @Override
    @javax.annotation.Nullable
    public Player getNearestPlayer(double p_45925_, double p_45926_, double p_45927_, double p_45928_, boolean p_45929_)
    {
        return super.getNearestPlayer(p_45925_, p_45926_, p_45927_, p_45928_, p_45929_);
    }

    @Override
    @javax.annotation.Nullable
    public Player getNearestPlayer(TargetingConditions p_45950_,
        LivingEntity p_45951_,
        double p_45952_,
        double p_45953_,
        double p_45954_)
    {
        return super.getNearestPlayer(p_45950_, p_45951_, p_45952_, p_45953_, p_45954_);
    }

    @Override
    @javax.annotation.Nullable
    public Player getPlayerByUUID(UUID p_46004_)
    {
        return super.getPlayerByUUID(p_46004_);
    }

    @Override
    public boolean hasNearbyAlivePlayer(double p_45915_, double p_45916_, double p_45917_, double p_45918_)
    {
        return super.hasNearbyAlivePlayer(p_45915_, p_45916_, p_45917_, p_45918_);
    }

    @Override
    public boolean canSeeSkyFromBelowWater(BlockPos p_46862_)
    {
        return super.canSeeSkyFromBelowWater(p_46862_);
    }

    @Override
    public boolean containsAnyLiquid(AABB p_46856_)
    {
        return super.containsAnyLiquid(p_46856_);
    }

    @Override
    public Stream<BlockState> getBlockStatesIfLoaded(AABB p_46848_)
    {
        return super.getBlockStatesIfLoaded(p_46848_);
    }

    @Override
    public int getBlockTint(BlockPos p_46836_, ColorResolver p_46837_)
    {
        return super.getBlockTint(p_46836_, p_46837_);
    }

    @Override
    public ChunkAccess getChunk(BlockPos p_46866_)
    {
        return super.getChunk(p_46866_);
    }

    @Override
    public ChunkAccess getChunk(int p_46820_, int p_46821_, ChunkStatus p_46822_)
    {
        return super.getChunk(p_46820_, p_46821_, p_46822_);
    }

    @Override
    public float getLightLevelDependentMagicValue(BlockPos p_220418_)
    {
        return super.getLightLevelDependentMagicValue(p_220418_);
    }

    @Override
    public int getMaxLocalRawBrightness(BlockPos p_46804_)
    {
        return super.getMaxLocalRawBrightness(p_46804_);
    }

    @Override
    public int getMaxLocalRawBrightness(BlockPos p_46850_, int p_46851_)
    {
        return super.getMaxLocalRawBrightness(p_46850_, p_46851_);
    }

    @Override
    public void updateNeighbourForOutputSignal(BlockPos p_46718_, Block p_46719_)
    {
        super.updateNeighbourForOutputSignal(p_46718_, p_46719_);
    }

    @Override
    public float getPathfindingCostFromLightLevels(BlockPos p_220420_)
    {
        return super.getPathfindingCostFromLightLevels(p_220420_);
    }

    @Override
    public boolean hasChunkAt(BlockPos p_46806_)
    {
        return super.hasChunkAt(p_46806_);
    }

    @Override
    public boolean hasChunkAt(int p_151578_, int p_151579_)
    {
        return super.hasChunkAt(p_151578_, p_151579_);
    }

    @Override
    public boolean hasChunksAt(BlockPos p_46833_, BlockPos p_46834_)
    {
        return super.hasChunksAt(p_46833_, p_46834_);
    }

    @Override
    public boolean hasChunksAt(int p_151573_, int p_151574_, int p_151575_, int p_151576_)
    {
        return super.hasChunksAt(p_151573_, p_151574_, p_151575_, p_151576_);
    }

    @Override
    public boolean hasChunksAt(int p_46813_, int p_46814_, int p_46815_, int p_46816_, int p_46817_, int p_46818_)
    {
        return super.hasChunksAt(p_46813_, p_46814_, p_46815_, p_46816_, p_46817_, p_46818_);
    }

    @Override
    public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<? extends T>> p_249578_)
    {
        return super.holderLookup(p_249578_);
    }

    @Override
    public boolean isAreaLoaded(BlockPos center, int range)
    {
        return super.isAreaLoaded(center, range);
    }

    @Override
    public boolean isEmptyBlock(BlockPos p_46860_)
    {
        return super.isEmptyBlock(p_46860_);
    }

    @Override
    public boolean isWaterAt(BlockPos p_46802_)
    {
        return super.isWaterAt(p_46802_);
    }

    @Override
    public boolean canSeeSky(BlockPos p_45528_)
    {
        return super.canSeeSky(p_45528_);
    }

    @Override
    public BlockHitResult clip(ClipContext p_45548_)
    {
        return super.clip(p_45548_);
    }

    @Override
    @javax.annotation.Nullable
    public BlockHitResult clipWithInteractionOverride(Vec3 p_45559_,
        Vec3 p_45560_,
        BlockPos p_45561_,
        VoxelShape p_45562_,
        BlockState p_45563_)
    {
        return super.clipWithInteractionOverride(p_45559_, p_45560_, p_45561_, p_45562_, p_45563_);
    }

    @Override
    public double getBlockFloorHeight(BlockPos p_45574_)
    {
        return super.getBlockFloorHeight(p_45574_);
    }

    @Override
    public double getBlockFloorHeight(VoxelShape p_45565_, Supplier<VoxelShape> p_45566_)
    {
        return super.getBlockFloorHeight(p_45565_, p_45566_);
    }

    @Override
    public Stream<BlockState> getBlockStates(AABB p_45557_)
    {
        return super.getBlockStates(p_45557_);
    }

    @Override
    public int getLightEmission(BlockPos p_45572_)
    {
        return super.getLightEmission(p_45572_);
    }

    @Override
    public int getMaxLightLevel()
    {
        return super.getMaxLightLevel();
    }

    @Override
    public BlockHitResult isBlockInLine(ClipBlockStateContext p_151354_)
    {
        return super.isBlockInLine(p_151354_);
    }

    @Override
    public int getMaxBuildHeight()
    {
        return super.getMaxBuildHeight();
    }

    @Override
    public int getMaxSection()
    {
        return super.getMaxSection();
    }

    @Override
    public int getMinSection()
    {
        return super.getMinSection();
    }

    @Override
    public int getSectionIndex(int p_151565_)
    {
        return super.getSectionIndex(p_151565_);
    }

    @Override
    public int getSectionIndexFromSectionY(int p_151567_)
    {
        return super.getSectionIndexFromSectionY(p_151567_);
    }

    @Override
    public int getSectionYFromSectionIndex(int p_151569_)
    {
        return super.getSectionYFromSectionIndex(p_151569_);
    }

    @Override
    public int getSectionsCount()
    {
        return super.getSectionsCount();
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos p_151571_)
    {
        return super.isOutsideBuildHeight(p_151571_);
    }

    @Override
    public boolean isOutsideBuildHeight(int p_151563_)
    {
        return super.isOutsideBuildHeight(p_151563_);
    }

    @Override
    public @Nullable ModelDataManager getModelDataManager()
    {
        return super.getModelDataManager();
    }

    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade)
    {
        return super.getShade(normalX, normalY, normalZ, shade);
    }

    @Override
    public boolean collidesWithSuffocatingBlock(@javax.annotation.Nullable Entity p_186438_, AABB p_186439_)
    {
        return super.collidesWithSuffocatingBlock(p_186438_, p_186439_);
    }

    @Override
    public Optional<Vec3> findFreePosition(@javax.annotation.Nullable Entity p_151419_,
        VoxelShape p_151420_,
        Vec3 p_151421_,
        double p_151422_,
        double p_151423_,
        double p_151424_)
    {
        return super.findFreePosition(p_151419_, p_151420_, p_151421_, p_151422_, p_151423_, p_151424_);
    }

    @Override
    public Optional<BlockPos> findSupportingBlock(Entity p_286468_, AABB p_286792_)
    {
        return super.findSupportingBlock(p_286468_, p_286792_);
    }

    @Override
    public Iterable<VoxelShape> getBlockCollisions(@javax.annotation.Nullable Entity p_186435_, AABB p_186436_)
    {
        return super.getBlockCollisions(p_186435_, p_186436_);
    }

    @Override
    public Iterable<VoxelShape> getCollisions(@javax.annotation.Nullable Entity p_186432_, AABB p_186433_)
    {
        return super.getCollisions(p_186432_, p_186433_);
    }

    @Override
    public boolean isUnobstructed(Entity p_45785_)
    {
        return super.isUnobstructed(p_45785_);
    }

    @Override
    public boolean isUnobstructed(BlockState p_45753_, BlockPos p_45754_, CollisionContext p_45755_)
    {
        return super.isUnobstructed(p_45753_, p_45754_, p_45755_);
    }

    @Override
    public boolean noCollision(AABB p_45773_)
    {
        return super.noCollision(p_45773_);
    }

    @Override
    public boolean noCollision(Entity p_45787_)
    {
        return super.noCollision(p_45787_);
    }

    @Override
    public boolean noCollision(@javax.annotation.Nullable Entity p_45757_, AABB p_45758_)
    {
        return super.noCollision(p_45757_, p_45758_);
    }

    @Override
    public int getBestNeighborSignal(BlockPos p_277977_)
    {
        return super.getBestNeighborSignal(p_277977_);
    }

    @Override
    public int getControlInputSignal(BlockPos p_277757_, Direction p_278104_, boolean p_277707_)
    {
        return super.getControlInputSignal(p_277757_, p_278104_, p_277707_);
    }

    @Override
    public int getDirectSignal(BlockPos p_277954_, Direction p_277342_)
    {
        return super.getDirectSignal(p_277954_, p_277342_);
    }

    @Override
    public int getDirectSignalTo(BlockPos p_277959_)
    {
        return super.getDirectSignalTo(p_277959_);
    }

    @Override
    public int getSignal(BlockPos p_277961_, Direction p_277351_)
    {
        return super.getSignal(p_277961_, p_277351_);
    }

    @Override
    public boolean hasNeighborSignal(BlockPos p_277626_)
    {
        return super.hasNeighborSignal(p_277626_);
    }

    @Override
    public boolean hasSignal(BlockPos p_277371_, Direction p_277391_)
    {
        return super.hasSignal(p_277371_, p_277391_);
    }

    @Override
    public boolean addFreshEntity(Entity p_46964_)
    {
        return super.addFreshEntity(p_46964_);
    }

    @Override
    public boolean destroyBlock(BlockPos p_46962_, boolean p_46963_)
    {
        return super.destroyBlock(p_46962_, p_46963_);
    }

    @Override
    public boolean destroyBlock(BlockPos p_46954_, boolean p_46955_, @javax.annotation.Nullable Entity p_46956_)
    {
        return super.destroyBlock(p_46954_, p_46955_, p_46956_);
    }

    @Override
    public float getMoonBrightness()
    {
        return super.getMoonBrightness();
    }

    @Override
    public int getMoonPhase()
    {
        return super.getMoonPhase();
    }

    @Override
    public float getTimeOfDay(float p_46943_)
    {
        return super.getTimeOfDay(p_46943_);
    }

    @Override
    public Collection<PartEntity<?>> getPartEntities()
    {
        return super.getPartEntities();
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap)
    {
        return super.getCapability(cap);
    }
    */
}
