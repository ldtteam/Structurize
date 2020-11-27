package com.ldtteam.structures.client;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.storage.ISpawnWorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Explosion;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.GameRules;
import net.minecraft.world.ITickList;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraft.world.storage.MapData;

/**
 * Our world/blockAccess dummy.
 */
public class BlueprintBlockAccess extends World
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
        super((ISpawnWorldInfo) getWorld().getWorldInfo(),
            getWorld().getDimensionKey(),
            getWorld().func_230315_m_(),
            () -> getWorld().getProfiler(),
            true,
            true,
            0L);
        this.blueprint = blueprint;
    }

    public static World getWorld() {
        return Minecraft.getInstance().world;
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
    public TileEntity getTileEntity(@NotNull final BlockPos pos)
    {
        return BlueprintUtils.getTileEntityFromPos(blueprint, pos, this);
    }

    @NotNull
    @Override
    public BlockState getBlockState(@NotNull final BlockPos pos)
    {
        final BlockState state = BlueprintUtils.getBlockInfoFromPos(blueprint, pos).getState().getBlockState();
        if (state.getBlock() == ModBlocks.blockSolidSubstitution)
        {
            return Blocks.DIRT.getDefaultState();
        }
        if (state.getBlock() == ModBlocks.blockFluidSubstitution)
        {
            return Blocks.WATER.getDefaultState();
        }
        return state.getBlock() == ModBlocks.blockSubstitution ? Blocks.AIR.getDefaultState() : state;
    }

    @Override
    public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull)
    {
        return new BlueprintChunk(this, x, z);
    }

    @Override
    public int getLight(@NotNull final BlockPos pos)
    {
        return 15;
    }

    @Override
    public float getBrightness(@NotNull final BlockPos pos)
    {
        return 15f;
    }

    @Override
    public int getLightFor(@NotNull final LightType lightType, @NotNull final BlockPos pos)
    {
        return 15;
    }

    @Override
    public int getLightSubtracted(BlockPos blockPosIn, int amount)
    {
        return 15;
    }

    @Override
    public float func_230487_a_(Direction p_230487_1_, boolean p_230487_2_)
    {
        return 0.9f;
    }

    @Override
    public Biome getBiome(BlockPos p_226691_1_)
    {
        return getWorld().getBiome(p_226691_1_);
    }

    @Override
    public Scoreboard getScoreboard()
    {
        return SCOREBOARD;
    }

    @NotNull
    @Override
    public FluidState getFluidState(@NotNull final BlockPos pos)
    {
        if (isOutsideBuildHeight(pos))
        {
            return Fluids.EMPTY.getDefaultState();
        }
        else
        {
            return getBlockState(pos).getFluidState();
        }
    }

    @Override
    public boolean isBlockPresent(BlockPos pos)
    {
        // Noop
        return true;
    }

    @Override
    public void addTileEntities(Collection<TileEntity> tileEntityCollection)
    {
        // Noop
    }

    @Override
    public boolean addTileEntity(TileEntity tile)
    {
        // Noop
        return false;
    }

    @Override
    public void calculateInitialSkylight()
    {
        // Noop
    }

    @Override
    protected void calculateInitialWeather()
    {
        // Noop
    }

    @Override
    public void close() throws IOException
    {
        // Noop
    }

    @Override
    public Explosion createExplosion(Entity entityIn, double xIn, double yIn, double zIn, float explosionRadius, Mode modeIn)
    {
        // Noop
        return null;
    }

    @Override
    public Explosion createExplosion(Entity entityIn,
        double xIn,
        double yIn,
        double zIn,
        float explosionRadius,
        boolean causesFire,
        Mode modeIn)
    {
        // Noop
        return null;
    }

    @Override
    public CrashReportCategory fillCrashReport(CrashReport report)
    {
        CrashReportCategory crashreportcategory = report.makeCategory("Structurize rendering engine");
        crashreportcategory.addDetail("Blueprint",
            () -> blueprint.getName() + " of size: " + blueprint.getSizeX() + "|" + blueprint.getSizeY() + "|" + blueprint.getSizeZ());
        return crashreportcategory;
    }

    @Override
    public boolean setBlockState(BlockPos p_241211_1_, BlockState p_241211_2_, int p_241211_3_, int p_241211_4_)
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
    public float getCelestialAngleRadians(float partialTicks)
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
    public DifficultyInstance getDifficultyForLocation(BlockPos pos)
    {
        // Noop
        return null;
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(Entity entityIn, AxisAlignedBB boundingBox, Predicate<? super Entity> predicate)
    {
        // Noop
        return null;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(EntityType<T> type, AxisAlignedBB boundingBox, Predicate<? super T> predicate)
    {
        // Noop
        return null;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, Predicate<? super T> filter)
    {
        // Noop
        return null;
    }

    @Override
    public Entity getEntityByID(int id)
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
    public int getHeight(Type heightmapType, int x, int z)
    {
        // Noop
        return 0;
    }

    @Override
    public WorldLightManager getLightManager()
    {
        // Noop
        return null;
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesWithinAABB(Class<? extends T> p_225316_1_,
        AxisAlignedBB p_225316_2_,
        Predicate<? super T> p_225316_3_)
    {
        // Noop
        return null;
    }

    @Override
    public MapData getMapData(String mapName)
    {
        // Noop
        return null;
    }

    @Override
    public int getNextMapId()
    {
        // Noop
        return 0;
    }

    @Override
    public String getProviderName()
    {
        // Noop
        return null;
    }

    @Override
    public float getRainStrength(float delta)
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
    public ITagCollectionSupplier getTags()
    {
        return getWorld().getTags();
    }

    @Override
    public int getSkylightSubtracted()
    {
        // Noop
        return 0;
    }

    @Override
    public float getThunderStrength(float delta)
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
    public IWorldInfo getWorldInfo()
    {
        // Noop
        return null;
    }

    @Override
    public void guardEntityTick(Consumer<Entity> consumerEntity, Entity entityIn)
    {
        // Noop
    }

    @Override
    public boolean isBlockModifiable(PlayerEntity player, BlockPos pos)
    {
        // Noop
        return false;
    }

    @Override
    public boolean isBlockinHighHumidity(BlockPos pos)
    {
        // Noop
        return false;
    }

    @Override
    public boolean isDaytime()
    {
        // Noop
        return true;
    }

    @Override
    public boolean isNightTime()
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
    public boolean isSaveDisabled()
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
        Chunk chunk,
        BlockState blockstate,
        BlockState p_241211_2_,
        int p_241211_3_,
        int p_241211_4_)
    {
        // Noop
    }

    @Override
    public void markBlockRangeForRenderUpdate(BlockPos blockPosIn, BlockState oldState, BlockState newState)
    {
        // Noop
    }

    @Override
    public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity)
    {
        // Noop
    }

    @Override
    public void neighborChanged(BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        // Noop
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags)
    {
        // Noop
    }

    @Override
    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockIn)
    {
        // Noop
    }

    @Override
    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, Direction skipSide)
    {
        // Noop
    }

    @Override
    public void onBlockStateChange(BlockPos pos, BlockState blockStateIn, BlockState newState)
    {
        // Noop
    }

    @Override
    public void playMovingSound(PlayerEntity playerIn,
        Entity entityIn,
        SoundEvent eventIn,
        SoundCategory categoryIn,
        float volume,
        float pitch)
    {
        // Noop
    }

    @Override
    public void playSound(PlayerEntity player,
        double x,
        double y,
        double z,
        SoundEvent soundIn,
        SoundCategory category,
        float volume,
        float pitch)
    {
        // Noop
    }

    @Override
    public void registerMapData(MapData mapDataIn)
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
    public void removeTileEntity(BlockPos pos)
    {
        // Noop
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress)
    {
        // Noop
    }

    @Override
    public void setAllowedSpawnTypes(boolean hostile, boolean peaceful)
    {
        // Noop
    }

    @Override
    public void setRainStrength(float strength)
    {
        // Noop
    }

    @Override
    public void setThunderStrength(float strength)
    {
        // Noop
    }

    @Override
    public void setTileEntity(BlockPos pos, TileEntity tileEntityIn)
    {
        // Noop
    }

    @Override
    public void tickBlockEntities()
    {
        // Noop
    }

    @Override
    public void updateComparatorOutputLevel(BlockPos pos, Block blockIn)
    {
        // Noop
    }

    @Override
    public boolean checkNoEntityCollision(Entity entityIn, VoxelShape shape)
    {
        // Noop
        return true;
    }

    @Override
    public boolean chunkExists(int chunkX, int chunkZ)
    {
        // Noop
        return true;
    }

    @Override
    public Stream<VoxelShape> func_230318_c_(Entity p_230318_1_, AxisAlignedBB p_230318_2_, Predicate<Entity> p_230318_3_)
    {
        // Noop
        return null;
    }

    @Override
    public int func_234938_ad_()
    {
        // Noop
        return 256;
    }

    @Override
    public AbstractChunkProvider getChunkProvider()
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
    public BlockPos getHeight(Type heightmapType, BlockPos pos)
    {
        // Noop
        return null;
    }

    @Override
    public DynamicRegistries func_241828_r()
    {
        return null;
    }

    @Override
    public ITickList<Block> getPendingBlockTicks()
    {
        // Noop
        return null;
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks()
    {
        // Noop
        return null;
    }

    @Override
    public void playEvent(PlayerEntity player, int type, BlockPos pos, int data)
    {
        // Noop
    }

    @Override
    public <T extends LivingEntity> T getClosestEntity(List<? extends T> entities,
        EntityPredicate predicate,
        LivingEntity target,
        double x,
        double y,
        double z)
    {
        // Noop
        return null;
    }

    @Override
    public PlayerEntity getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate)
    {
        // Noop
        return null;
    }

    @Override
    public PlayerEntity getPlayerByUuid(UUID uniqueIdIn)
    {
        // Noop
        return null;
    }

    @Override
    public List<? extends PlayerEntity> getPlayers()
    {
        // Noop
        return null;
    }

    @Override
    public <T extends LivingEntity> List<T> getTargettableEntitiesWithinAABB(Class<? extends T> p_217374_1_,
        EntityPredicate p_217374_2_,
        LivingEntity p_217374_3_,
        AxisAlignedBB p_217374_4_)
    {
        // Noop
        return null;
    }

    @Override
    public List<PlayerEntity> getTargettablePlayersWithinAABB(EntityPredicate predicate, LivingEntity target, AxisAlignedBB box)
    {
        // Noop
        return null;
    }

    @Override
    public boolean isPlayerWithin(double x, double y, double z, double distance)
    {
        // Noop
        return false;
    }

    @Override
    public boolean canBlockSeeSky(BlockPos pos)
    {
        // Noop
        return true;
    }

    @Override
    public int getBlockColor(BlockPos blockPosIn, ColorResolver colorResolverIn)
    {
        return super.getBlockColor(blockPosIn, colorResolverIn);
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z)
    {
        // Noop
        return null;
    }

    @Override
    public Biome getNoiseBiomeRaw(int x, int y, int z)
    {
        // Noop
        return null;
    }

    @Override
    public boolean isAreaLoaded(int fromX, int fromY, int fromZ, int toX, int toY, int toZ)
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
    public boolean addEntity(Entity entityIn)
    {
        // Noop
        return false;
    }
}
