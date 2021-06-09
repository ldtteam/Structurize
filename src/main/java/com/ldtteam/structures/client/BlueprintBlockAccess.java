package com.ldtteam.structures.client;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.util.BlockUtils;
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
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.*;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraft.world.storage.MapData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
        super((ISpawnWorldInfo) getWorld().getLevelData(),
            getWorld().dimension(),
            getWorld().dimensionType(),
            () -> getWorld().getProfiler(),
            true,
            true,
            0L);
        this.blueprint = blueprint;
    }

    public static World getWorld() {
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
    public TileEntity getBlockEntity(@NotNull final BlockPos pos)
    {
        return BlueprintUtils.getTileEntityFromPos(blueprint, pos, this);
    }

    @NotNull
    @Override
    public BlockState getBlockState(@NotNull final BlockPos pos)
    {
        final BlockState state = BlueprintUtils.getBlockInfoFromPos(blueprint, pos).getState().getBlockState();
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
        return state.getBlock() == ModBlocks.blockSubstitution.get() ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull)
    {
        return new BlueprintChunk(this, x, z);
    }

    @Override
    public int getMaxLocalRawBrightness(@NotNull final BlockPos pos)
    {
        return 15;
    }

    @Override
    public float getBrightness(@NotNull final BlockPos pos)
    {
        return 15f;
    }

    @Override
    public int getBrightness(@NotNull final LightType lightType, @NotNull final BlockPos pos)
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
            return Fluids.EMPTY.defaultFluidState();
        }
        else
        {
            return getBlockState(pos).getFluidState();
        }
    }

    @Override
    public boolean loadedAndEntityCanStandOnFace(BlockPos p_234929_1_, @NotNull Entity p_234929_2_, @NotNull Direction p_234929_3_)
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
    public void addAllPendingBlockEntities(Collection<TileEntity> tileEntityCollection)
    {
        // Noop
    }

    @Override
    public boolean addBlockEntity(TileEntity tile)
    {
        // Noop
        return false;
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
    public Explosion explode(Entity entityIn, double xIn, double yIn, double zIn, float explosionRadius, Mode modeIn)
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
        Mode modeIn)
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
    public List<Entity> getEntities(Entity entityIn, AxisAlignedBB boundingBox, Predicate<? super Entity> predicate)
    {
        // Noop
        return null;
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityType<T> type, AxisAlignedBB boundingBox, Predicate<? super T> predicate)
    {
        // Noop
        return null;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> clazz, AxisAlignedBB aabb, Predicate<? super T> filter)
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
    public int getHeight(Type heightmapType, int x, int z)
    {
        // Noop
        return 0;
    }

    @Override
    public WorldLightManager getLightEngine()
    {
        // Noop
        return null;
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> p_225316_1_,
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
    public ITagCollectionSupplier getTagManager()
    {
        return getWorld().getTagManager();
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
    public IWorldInfo getLevelData()
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
    public boolean mayInteract(PlayerEntity player, BlockPos pos)
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
        Chunk chunk,
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
    public void blockEntityChanged(BlockPos pos, TileEntity unusedTileEntity)
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
    public void playSound(PlayerEntity playerIn,
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
    public void setMapData(MapData mapDataIn)
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
    public void setBlockEntity(BlockPos pos, TileEntity tileEntityIn)
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
    public Stream<VoxelShape> getEntityCollisions(Entity p_230318_1_, AxisAlignedBB p_230318_2_, Predicate<Entity> p_230318_3_)
    {
        // Noop
        return null;
    }

    @Override
    public int getHeight()
    {
        // Noop
        return 256;
    }

    @Override
    public AbstractChunkProvider getChunkSource()
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
    public BlockPos getHeightmapPos(Type heightmapType, BlockPos pos)
    {
        // Noop
        return null;
    }

    @Override
    public DynamicRegistries registryAccess()
    {
        return null;
    }

    @Override
    public ITickList<Block> getBlockTicks()
    {
        // Noop
        return null;
    }

    @Override
    public ITickList<Fluid> getLiquidTicks()
    {
        // Noop
        return null;
    }

    @Override
    public void levelEvent(PlayerEntity player, int type, BlockPos pos, int data)
    {
        // Noop
    }

    @Override
    public <T extends LivingEntity> T getNearestEntity(List<? extends T> entities,
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
    public PlayerEntity getNearestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate)
    {
        // Noop
        return null;
    }

    @Override
    public PlayerEntity getPlayerByUUID(UUID uniqueIdIn)
    {
        // Noop
        return null;
    }

    @Override
    public List<? extends PlayerEntity> players()
    {
        // Noop
        return null;
    }

    @Override
    public <T extends LivingEntity> List<T> getNearbyEntities(Class<? extends T> p_217374_1_,
        EntityPredicate p_217374_2_,
        LivingEntity p_217374_3_,
        AxisAlignedBB p_217374_4_)
    {
        // Noop
        return null;
    }

    @Override
    public List<PlayerEntity> getNearbyPlayers(EntityPredicate predicate, LivingEntity target, AxisAlignedBB box)
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
    public Biome getNoiseBiome(int x, int y, int z)
    {
        // Noop
        return null;
    }

    @Override
    public Biome getUncachedNoiseBiome(int x, int y, int z)
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
