package com.ldtteam.structurize.client.fakelevel;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData.BlockEntityTagOutput;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.TickContainerAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Blueprint simulated chunk.
 */
public class FakeChunk extends LevelChunk
{
    /**
     * The block access it gets.
     */
    private final FakeLevel fakeLevel;

    /**
     * Construct the element.
     * 
     * @param worldIn the blockAccess.
     * @param x       the chunk x.
     * @param z       the chunk z.
     */
    public FakeChunk(final FakeLevel worldIn, final int x, final int z)
    {
        super(worldIn, new ChunkPos(x, z));
        this.fakeLevel = worldIn;
    }

    // ========================================
    // ========== REDIRECTED METHODS ==========
    // ========================================

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        return fakeLevel.getBlockState(pos);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(final BlockPos pos, final EntityCreationType creationMode)
    {
        return fakeLevel.getBlockEntity(pos);
    }

    @Override
    @Nullable
    public BlockEntity getExistingBlockEntity(BlockPos pos)
    {
        return fakeLevel.getBlockEntity(pos);
    }

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        return fakeLevel.getFluidState(pos);
    }

    @Override
    public FluidState getFluidState(final int bx, final int by, final int bz)
    {
        return getFluidState(new BlockPos(bx, by, bz));
    }

    @Override
    public FakeLevel getLevel()
    {
        return fakeLevel;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z)
    {
        return fakeLevel.getNoiseBiome(x, y, z);
    }

    @Override
    public Map<BlockPos, BlockEntity> getBlockEntities()
    {
        return fakeLevel.blockEntities;
    }

    @Override
    public Set<BlockPos> getBlockEntitiesPos()
    {
        return getBlockEntities().keySet();
    }

    // ========================================
    // ======= NOOP UNSAFE NULL METHODS =======
    // ========================================

    // ========================================
    // ========== PERMANENT SETTINGS ==========
    // ========================================

    @Override
    public FullChunkStatus getFullStatus()
    {
        return FullChunkStatus.FULL;
    }

    @Override
    public ChunkStatus getStatus()
    {
        return ChunkStatus.FULL;
    }

    @Override
    public boolean isUnsaved()
    {
        return false;
    }

    @Override
    public boolean isUpgrading()
    {
        return false;
    }

    @Override
    public boolean isLightCorrect()
    {
        return true;
    }

    // ========================================
    // ========== HEIGHTMAP RELATED ===========
    // ========================================

    @Override
    public int getHeight(Types type, int x, int z)
    {
        return fakeLevel.getHeight(type, chunkPos.getBlockX(x), chunkPos.getBlockZ(z));
    }

    @Override
    public Collection<Entry<Types, Heightmap>> getHeightmaps()
    {
        // TODO: investigate..
        return Collections.emptyList();
    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Types p_62079_)
    {
        return null;
    }

    @Override
    public boolean hasPrimedHeightmap(Types p_187659_)
    {
        return false;
    }

    // ========================================
    // =========== SECTION RELATED ============
    // ========================================

    @Override
    public void findBlocks(BiPredicate<BlockState, BlockPos> p_285343_, BiConsumer<BlockPos, BlockState> p_285030_)
    {
        // TODO Auto-generated method stub
        super.findBlocks(p_285343_, p_285030_);
    }

    @Override
    public boolean isYSpaceEmpty(int p_62075_, int p_62076_)
    {
        return false;
    }

    @Override
    public LevelChunkSection[] getSections()
    {
        return new LevelChunkSection[0];
    }

    // ========================================
    // ============= NOOP METHODS =============
    // ========================================

    @Override
    public void addAndRegisterBlockEntity(BlockEntity p_156391_)
    {
        // Noop
    }

    @Override
    public boolean areCapsCompatible(CapabilityProvider<LevelChunk> other)
    {
        // Noop
        return false;
    }

    @Override
    public boolean areCapsCompatible(@Nullable CapabilityDispatcher other)
    {
        // Noop
        return false;
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
    @javax.annotation.Nullable
    public CompoundTag getBlockEntityNbtForSaving(BlockPos p_62932_)
    {
        // Noop
        return null;
    }

    @Override
    public TickContainerAccess<Block> getBlockTicks()
    {
        // Noop
        return BlackholeTickAccess.emptyContainer();
    }

    @Override
    public TickContainerAccess<Fluid> getFluidTicks()
    {
        // Noop
        return BlackholeTickAccess.emptyContainer();
    }

    @Override
    public void postProcessGeneration()
    {
        // Noop
    }

    @Override
    public void registerAllBlockEntitiesAfterLevelLoad()
    {
        // Noop
    }

    @Override
    public void removeBlockEntity(BlockPos p_62919_)
    {
        // Noop
    }

    @Override
    public void replaceBiomes(FriendlyByteBuf p_275574_)
    {
        // Noop
    }

    @Override
    public void replaceWithPacketData(FriendlyByteBuf p_187972_, CompoundTag p_187973_, Consumer<BlockEntityTagOutput> p_187974_)
    {
        // Noop
    }

    @Override
    public void reviveCaps()
    {
        // Noop
    }

    @Override
    public void setBlockEntity(BlockEntity p_156374_)
    {
        // Noop
    }

    @Override
    @javax.annotation.Nullable
    public BlockState setBlockState(BlockPos p_62865_, BlockState p_62866_, boolean p_62867_)
    {
        // Noop
        return null;
    }

    @Override
    public void setFullStatus(Supplier<FullChunkStatus> p_62880_)
    {
        // Noop
    }

    @Override
    public void unpackTicks(long p_187986_)
    {
        // Noop
    }

    @Override
    public void addPackedPostProcess(short p_62092_, int p_62093_)
    {
        // Noop
    }

    @Override
    public void addReferenceForStructure(Structure p_223007_, long p_223008_)
    {
        // Noop
    }

    @Override
    public void fillBiomesFromNoise(BiomeResolver p_187638_, Sampler p_187639_)
    {
        // Noop
    }

    @Override
    @javax.annotation.Nullable
    public CompoundTag getBlockEntityNbt(BlockPos p_62103_)
    {
        // Noop, for pending BEs only
        return null;
    }

    @Override
    public void setAllReferences(Map<Structure, LongSet> p_187663_)
    {
        // Noop
    }

    @Override
    public void setAllStarts(Map<Structure, StructureStart> p_62090_)
    {
        // Noop
    }

    @Override
    public void setBlendingData(BlendingData p_187646_)
    {
        // Noop
    }

    @Override
    public void setBlockEntityNbt(CompoundTag p_62091_)
    {
        // Noop
    }

    @Override
    public void setLightCorrect(boolean p_62100_)
    {
        // Noop
    }

    @Override
    public void setStartForStructure(Structure p_223010_, StructureStart p_223011_)
    {
        // Noop
    }

    @Override
    public void setUnsaved(boolean p_62094_)
    {
        // Noop
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap)
    {
        // Noop
        return LazyOptional.empty();
    }

    @Override
    public void setHeightmap(Types p_62083_, long[] p_62084_)
    {
        // Noop
    }

    // ========================================
    // ======== SUPER IS FINE METHODS =========
    // ========================================

    /*
    @Override
    public void addEntity(Entity p_62826_)
    {
        super.addEntity(p_62826_);
    }
    
    @Override
    public void clearAllBlockEntities()
    {
        super.clearAllBlockEntities();
    }
    
    @Override
    @javax.annotation.Nullable
    public BlockEntity getBlockEntity(BlockPos p_62912_)
    {
        return super.getBlockEntity(p_62912_);
    }
    
    @Override
    public GameEventListenerRegistry getListenerRegistry(int p_251193_)
    {
        return super.getListenerRegistry(p_251193_);
    }
    
    @Override
    public TicksToSave getTicksForSerialization()
    {
        return super.getTicksForSerialization();
    }
    
    @Override
    public Level getWorldForge()
    {
        return super.getWorldForge();
    }
    
    @Override
    public boolean isEmpty()
    {
        return super.isEmpty();
    }
    
    @Override
    public void registerTickContainerInLevel(ServerLevel p_187959_)
    {
        super.registerTickContainerInLevel(p_187959_);
    }
    
    @Override
    public void runPostLoad()
    {
        super.runPostLoad();
    }
    
    @Override
    public void setLoaded(boolean p_62914_)
    {
        super.setLoaded(p_62914_);
    }
    
    @Override
    public void unregisterTickContainerFromLevel(ServerLevel p_187980_)
    {
        super.unregisterTickContainerFromLevel(p_187980_);
    }
    
    @Override
    public BiomeGenerationSettings carverBiome(Supplier<BiomeGenerationSettings> p_223015_)
    {
        return super.carverBiome(p_223015_);
    }
    
    @Override
    public void findBlocks(Predicate<BlockState> p_285343_, BiConsumer<BlockPos, BlockState> p_285030_)
    {
        super.findBlocks(p_285343_, p_285030_);
    }
    
    @Override
    public Map<Structure, LongSet> getAllReferences()
    {
        return super.getAllReferences();
    }
    
    @Override
    public Map<Structure, StructureStart> getAllStarts()
    {
        return super.getAllStarts();
    }
    
    @Override
    @javax.annotation.Nullable
    public BelowZeroRetrogen getBelowZeroRetrogen()
    {
        return super.getBelowZeroRetrogen();
    }
    
    @Override
    @javax.annotation.Nullable
    public BlendingData getBlendingData()
    {
        return super.getBlendingData();
    }
    
    @Override
    public int getHeight()
    {
        return super.getHeight();
    }
    
    @Override
    public LevelHeightAccessor getHeightAccessorForGeneration()
    {
        return super.getHeightAccessorForGeneration();
    }
    
    @Override
    public int getHighestFilledSectionIndex()
    {
        return super.getHighestFilledSectionIndex();
    }
    
    @Override
    public ChunkStatus getHighestGeneratedStatus()
    {
        return super.getHighestGeneratedStatus();
    }
    
    @Override
    public int getHighestSectionPosition()
    {
        return super.getHighestSectionPosition();
    }
    
    @Override
    public long getInhabitedTime()
    {
        return super.getInhabitedTime();
    }
    
    @Override
    public int getMinBuildHeight()
    {
        return super.getMinBuildHeight();
    }
    
    @Override
    public NoiseChunk getOrCreateNoiseChunk(Function<ChunkAccess, NoiseChunk> p_223013_)
    {
        return super.getOrCreateNoiseChunk(p_223013_);
    }
    
    @Override
    public ChunkPos getPos()
    {
        return super.getPos();
    }
    
    @Override
    public ShortList[] getPostProcessing()
    {
        return super.getPostProcessing();
    }
    
    @Override
    public LongSet getReferencesForStructure(Structure p_223017_)
    {
        return super.getReferencesForStructure(p_223017_);
    }
    
    @Override
    public LevelChunkSection getSection(int p_187657_)
    {
        return super.getSection(p_187657_);
    }
    
    @Override
    public ChunkSkyLightSources getSkyLightSources()
    {
        return super.getSkyLightSources();
    }
    
    @Override
    @javax.annotation.Nullable
    public StructureStart getStartForStructure(Structure p_223005_)
    {
        return super.getStartForStructure(p_223005_);
    }
    
    @Override
    public UpgradeData getUpgradeData()
    {
        return super.getUpgradeData();
    }
    
    @Override
    public boolean hasAnyStructureReferences()
    {
        return super.hasAnyStructureReferences();
    }
    
    @Override
    public void incrementInhabitedTime(long p_187633_)
    {
        super.incrementInhabitedTime(p_187633_);
    }
    
    @Override
    public void initializeLightSources()
    {
        super.initializeLightSources();
    }
    
    @Override
    public boolean isOldNoiseGeneration()
    {
        return super.isOldNoiseGeneration();
    }
    
    @Override
    public void markPosForPostprocessing(BlockPos p_62102_)
    {
        super.markPosForPostprocessing(p_62102_);
    }
    
    @Override
    public void setInhabitedTime(long p_62099_)
    {
        super.setInhabitedTime(p_62099_);
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
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos p_151367_, BlockEntityType<T> p_151368_)
    {
        return super.getBlockEntity(p_151367_, p_151368_);
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
    */
}
