package com.ldtteam.structurize.client;

import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtils;
import com.ldtteam.structurize.config.BlueprintRenderSettings;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.ldtteam.structurize.api.util.constant.Constants.RENDER_PLACEHOLDERS;

/**
 * Our world/blockAccess dummy. TODO: client level
 */
public class BlueprintBlockAccess extends Level
{
    private static final Scoreboard SCOREBOARD = new Scoreboard();

    /**
     * The blueprint with the info we need.
     */
    private Blueprint blueprint;

    /**
     * Current rendering worldPos so we can use local real world info
     */
    private BlockPos worldPos;

    /**
     * Constructor to create a new world/blockAccess
     * 
     * @param blueprint the blueprint.
     */
    public BlueprintBlockAccess(final Blueprint blueprint)
    {
        super(new BlueprintLevelData(clientLevel().getLevelData()),
            clientLevel().dimension(),
            clientLevel().dimensionTypeRegistration(),
            () -> clientLevel().getProfiler(),
            true,
            false,
            0,
            0);
        this.blueprint = blueprint;
    }

    @SuppressWarnings("resource")
    private static ClientLevel clientLevel()
    {
        return Minecraft.getInstance().level;
    }

    private static Level anyLevel()
    {
        final Minecraft mc = Minecraft.getInstance();
        return SharedConstants.IS_RUNNING_IN_IDE && mc.hasSingleplayerServer() ?
            mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).level :
            mc.level;
    }

    public Blueprint getBlueprint()
    {
        return blueprint;
    }

    public void setBlueprint(final Blueprint blueprintIn)
    {
        blueprint = blueprintIn;
    }

    public void setWorldPos(final BlockPos worldPos)
    {
        this.worldPos = worldPos;
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
            return BlockUtils.getSubstitutionBlockAtWorld(anyLevel(), worldPos.offset(pos), blueprint.getRawBlockStateFunction().compose(b -> b.subtract(worldPos)));
        }
        if (state.getBlock() == ModBlocks.blockFluidSubstitution.get())
        {
            return BlockUtils.getFluidForDimension(anyLevel());
        }
        if (state.getBlock() == ModBlocks.blockSubstitution.get() && !BlueprintRenderSettings.instance.renderSettings.get(RENDER_PLACEHOLDERS))
        {
            return Blocks.AIR.defaultBlockState();
        }
        if (state.getBlock() == ModBlocks.blockTagSubstitution.get())
        {
            if (BlueprintUtils.getTileEntityFromPos(blueprint, pos, this) instanceof BlockEntityTagSubstitution tag &&
                    !tag.getReplacement().isEmpty())
            {
                return tag.getReplacement().getBlockState();
            }
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull)
    {
        return nonnull || hasChunk(x, z) ? new BlueprintChunk(this, x, z) : null;
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ)
    {
        final int posX = SectionPos.sectionToBlockCoord(chunkX);
        final int posZ = SectionPos.sectionToBlockCoord(chunkZ);
        return posX <= blueprint.getSizeX() && posZ <= blueprint.getSizeZ();
    }

    @Override
    public int getBrightness(final LightLayer lightType, final BlockPos pos)
    {
        return RenderingCache.forceLightLevel() ? RenderingCache.getOurLightLevel() :
            clientLevel().getBrightness(lightType, worldPos.offset(pos));
    }

    @Override
    public int getRawBrightness(BlockPos pos, int amount)
    {
        return RenderingCache.forceLightLevel() ? RenderingCache.getOurLightLevel() :
            clientLevel().getRawBrightness(worldPos.offset(pos), amount);
    }

    @Override
    public float getShade(Direction p_104703_, boolean p_104704_)
    {
        // VANILLA INLINE: from clientlevel
        final boolean flag = clientLevel().effects().constantAmbientLight();
        if (!p_104704_)
        {
            return flag ? 0.9F : 1.0F;
        }
        else
        {
            switch (p_104703_)
            {
                case DOWN:
                    return flag ? 0.9F : 0.5F;

                case UP:
                    return flag ? 0.9F : 1.0F;

                case NORTH:
                case SOUTH:
                    return 0.8F;

                case WEST:
                case EAST:
                    return 0.6F;

                default:
                    return 1.0F;
            }
        }
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pos)
    {
        return clientLevel().getBiome(worldPos.offset(pos));
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
        return clientLevel().getBiomeManager();
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities()
    {
        // Noop - hard to create, level internal only
        return null;
    }

    @Override
    public List<Entity> getEntities(Entity entityIn, AABB boundingBox, Predicate<? super Entity> predicate)
    {
        // Noop (ppl will ask if needed)
        return null;
    }

    @Override
    public Entity getEntity(int id)
    {
        // Noop (ppl will ask if needed)
        return null;
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
        // Noop
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
        return "Blueprint fake world for: " + blueprint.getName();
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
        return clientLevel().getRecipeManager();
    }

    @Override
    public int getSkyDarken()
    {
        return RenderingCache.forceLightLevel() ? 0 : clientLevel().getSkyDarken();
    }

    @Override
    public float getThunderLevel(float delta)
    {
        // Noop
        return 0;
    }

    @Override
    public boolean mayInteract(Player player, BlockPos pos)
    {
        // Noop
        return false;
    }

    @Override
    public boolean isDay()
    {
        return !this.dimensionType().hasFixedTime() && this.getSkyDarken() < 4;
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
    public List<VoxelShape> getEntityCollisions(@Nullable final Entity p_186447_, final AABB p_186448_)
    {
        return Collections.emptyList();
    }

    @Override
    public ChunkSource getChunkSource()
    {
        // Noop
        return null;
    }

    @NotNull
    @Override
    public RegistryAccess registryAccess()
    {
        return clientLevel().registryAccess();
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
    public List<? extends Player> players()
    {
        return Collections.emptyList();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z)
    {
        // Noop
        return null;
    }

    @Override
    public boolean canSeeSky(BlockPos blockPosIn)
    {
        // Noop
        return true;
    }

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
    public void blockEntityChanged(BlockPos p_151544_)
    {
        // Noop
    }

    @Override
    public Explosion explode(Entity p_46526_,
        DamageSource p_46527_,
        ExplosionDamageCalculator p_46528_,
        double p_46529_,
        double p_46530_,
        double p_46531_,
        float p_46532_,
        boolean p_46533_,
        BlockInteraction p_46534_)
    {
        // Noop
        return null;
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> p_151528_, AABB p_151529_, Predicate<? super T> p_151530_)
    {
        // Noop
        return null;
    }

    @Override
    public int getSeaLevel()
    {
        return 0;
    }

    @Override
    public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, GameEvent.Context p_220406_)
    {
        // Noop
    }

    @Override
    public void setBlockEntity(BlockEntity p_151524_)
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
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
    {
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

    @Override
    public void scheduleTick(BlockPos p_186461_, Block p_186462_, int p_186463_)
    {
        // Noop
    }

    @Override
    public void scheduleTick(BlockPos p_186470_, Fluid p_186471_, int p_186472_)
    {
        // Noop
    }

    @Override
    public void scheduleTick(BlockPos p_186465_, Block p_186466_, int p_186467_, TickPriority p_186468_)
    {
        // Noop
    }

    @Override
    public void scheduleTick(BlockPos p_186474_, Fluid p_186475_, int p_186476_, TickPriority p_186477_)
    {
        // Noop
    }

    @Override
    public <T extends LivingEntity> List<T> getNearbyEntities(Class<T> p_45972_,
        TargetingConditions p_45973_,
        LivingEntity p_45974_,
        AABB p_45975_)
    {
        // Noop
        return null;
    }

    @Override
    public <T extends LivingEntity> T getNearestEntity(Class<? extends T> p_45964_,
        TargetingConditions p_45965_,
        LivingEntity p_45966_,
        double p_45967_,
        double p_45968_,
        double p_45969_,
        AABB p_45970_)
    {
        // Noop
        return null;
    }

    @Override
    public int getHeight()
    {
        return blueprint.getSizeY();
    }

    @Override
    public int getMinBuildHeight()
    {
        return 0;
    }

    @Override
    public BlockHitResult clip(ClipContext p_45548_)
    {
        final Vec3 vec3 = p_45548_.getFrom().subtract(p_45548_.getTo());
        return BlockHitResult.miss(p_45548_.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), new BlockPos(p_45548_.getTo()));
    }

    @Override
    public BlockHitResult isBlockInLine(ClipBlockStateContext p_151354_)
    {
        final Vec3 vec3 = p_151354_.getFrom().subtract(p_151354_.getTo());
        return BlockHitResult.miss(p_151354_.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), new BlockPos(p_151354_.getTo()));
    }

    @Override
    @Nullable
    public BlockEntity getExistingBlockEntity(BlockPos pos)
    {
        return getBlockEntity(pos);
    }

    @Override
    public boolean collidesWithSuffocatingBlock(Entity p_186438_, AABB p_186439_)
    {
        // Noop
        return false;
    }

    @Override
    public Optional<Vec3> findFreePosition(Entity p_151419_,
        VoxelShape p_151420_,
        Vec3 p_151421_,
        double p_151422_,
        double p_151423_,
        double p_151424_)
    {
        // Noop
        return Optional.empty();
    }

    @Override
    public Iterable<VoxelShape> getBlockCollisions(Entity p_186435_, AABB p_186436_)
    {
        // Noop
        return Collections.emptyList();
    }

    @Override
    public Iterable<VoxelShape> getCollisions(Entity p_186432_, AABB p_186433_)
    {
        // Noop
        return Collections.emptyList();
    }

    @Override
    public boolean noCollision(Entity p_45757_, AABB p_45758_)
    {
        // Noop
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap)
    {
        return LazyOptional.empty();
    }

    @Override
    public void playSeededSound(Player p_220363_,
        double p_220364_,
        double p_220365_,
        double p_220366_,
        SoundEvent p_220367_,
        SoundSource p_220368_,
        float p_220369_,
        float p_220370_,
        long p_220371_)
    {
        // Noop
    }

    @Override
    public void playSeededSound(Player p_220372_,
        Entity p_220373_,
        SoundEvent p_220374_,
        SoundSource p_220375_,
        float p_220376_,
        float p_220377_,
        long p_220378_)
    {
        // Noop
    }

    private static class BlueprintLevelData implements WritableLevelData
    {
        private final LevelData vanillaLevelData;

        private BlueprintLevelData(final LevelData vanillaLevelData)
        {
            this.vanillaLevelData = vanillaLevelData;
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
            return RenderingCache.forceLightLevel() ? 6000 : clientLevel().getDayTime();
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
        public void setRaining(boolean p_78171_)
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
        public void setXSpawn(int p_78651_)
        {
            // Noop
        }

        @Override
        public void setYSpawn(int p_78652_)
        {
            // Noop
        }

        @Override
        public void setZSpawn(int p_78653_)
        {
            // Noop
        }

        @Override
        public void setSpawnAngle(float p_78648_)
        {
            // Noop
        }
    }
}
