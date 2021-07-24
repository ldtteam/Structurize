package com.ldtteam.structures.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.core.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.server.level.ChunkHolder.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.level.chunk.LevelChunk.EntityCreationType;

/**
 * Blueprint simulated chunk.
 */
public class BlueprintChunk extends LevelChunk
{
    /**
     * The block access it gets.
     */
    private final BlueprintBlockAccess access;

    /**
     * Construct the element.
     * 
     * @param worldIn the blockAccess.
     * @param x       the chunk x.
     * @param z       the chunk z.
     */
    public BlueprintChunk(final Level worldIn, final int x, final int z)
    {
        super(worldIn, new ChunkPos(x, z), new ChunkBiomeContainer(worldIn.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), new Biome[0]));
        this.access = (BlueprintBlockAccess) worldIn;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        return access.getBlockState(pos);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(@NotNull final BlockPos pos, final EntityCreationType creationMode)
    {
        return access.getBlockEntity(pos);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(@NotNull final BlockPos pos)
    {
        return access.getBlockEntity(pos);
    }

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        return access.getFluidState(pos);
    }

    @Override
    public FluidState getFluidState(int bx, int by, int bz)
    {
        return getFluidState(new BlockPos(bx, by, bz));
    }

    @Override
    public Level getLevel()
    {
        return access;
    }

    @Override
    public void addEntity(Entity entityIn)
    {
        // Noop
    }

    @Override
    public void addBlockEntity(BlockEntity tileEntityIn)
    {
        // Noop
    }

    @Override
    public void setBlockEntityNbt(CompoundTag nbt)
    {
        // Noop
    }

    @Override
    public void setBlockEntity(BlockPos pos, BlockEntity tileEntityIn)
    {
        // Noop
    }

    @Override
    public StructureStart<?> getStartForFeature(StructureFeature<?> p_230342_1_)
    {
        // Noop
        return null;
    }

    @Override
    public void addReferenceForFeature(StructureFeature<?> p_230343_1_, long p_230343_2_)
    {
        // Noop
    }

    @Override
    public void setStartForFeature(StructureFeature<?> p_230344_1_, StructureStart<?> p_230344_2_)
    {
        // Noop
    }

    @Override
    public LongSet getReferencesForFeature(StructureFeature<?> p_230346_1_)
    {
        // Noop
        return null;
    }

    @Override
    public ChunkBiomeContainer getBiomes()
    {
        // Noop
        return null;
    }

    @Override
    public TickList<Block> getBlockTicks()
    {
        // Noop
        return null;
    }

    @Override
    public CompoundTag getBlockEntityNbt(BlockPos pos)
    {
        // Noop
        return null;
    }

    @Override
    public <T extends Entity> void getEntitiesOfClass(Class<? extends T> entityClass,
        AABB aabb,
        List<T> listToFill,
        Predicate<? super T> filter)
    {
        // Noop
    }

    @Override
    public void getEntities(Entity entityIn,
        AABB aabb,
        List<Entity> listToFill,
        Predicate<? super Entity> filter)
    {
        // Noop
    }

    @Override
    public <T extends Entity> void getEntities(EntityType<?> entitytypeIn,
        AABB aabb,
        List<? super T> list,
        Predicate<? super T> filter)
    {
        // Noop
    }

    @Override
    public ClassInstanceMultiMap<Entity>[] getEntitySections()
    {
        // Noop
        return null;
    }

    @Override
    public TickList<Fluid> getLiquidTicks()
    {
        // Noop
        return null;
    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Types typeIn)
    {
        // Noop
        return null;
    }

    @Override
    public Collection<Entry<Types, Heightmap>> getHeightmaps()
    {
        // Noop
        return null;
    }

    @Override
    public long getInhabitedTime()
    {
        // Noop
        return 0;
    }

    @Override
    public Stream<BlockPos> getLights()
    {
        // Noop
        return null;
    }

    @Override
    public FullChunkStatus getFullStatus()
    {
        // Noop
        return null;
    }

    @Override
    public ShortList[] getPostProcessing()
    {
        // Noop
        return null;
    }

    @Override
    public ChunkPos getPos()
    {
        // Noop
        return null;
    }

    @Override
    public LevelChunkSection[] getSections()
    {
        // Noop
        return null;
    }

    @Override
    public ChunkStatus getStatus()
    {
        return ChunkStatus.FULL;
    }

    @Override
    public Map<StructureFeature<?>, LongSet> getAllReferences()
    {
        // Noop
        return null;
    }

    @Override
    public Map<StructureFeature<?>, StructureStart<?>> getAllStarts()
    {
        // Noop
        return null;
    }

    @Override
    public Set<BlockPos> getBlockEntitiesPos()
    {
        // Noop
        return null;
    }

    @Override
    public Map<BlockPos, BlockEntity> getBlockEntities()
    {
        // Noop
        return null;
    }

    @Override
    public int getHeight(Types heightmapType, int x, int z)
    {
        // Noop
        return 0;
    }

    @Override
    public UpgradeData getUpgradeData()
    {
        // Noop
        return null;
    }

    @Override
    public Level getWorldForge()
    {
        return access;
    }

    @Override
    public LevelLightEngine getLightEngine()
    {
        // Noop
        return null;
    }

    @Override
    public boolean isLightCorrect()
    {
        return true;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public boolean isUnsaved()
    {
        return false;
    }

    @Override
    public void markUnsaved()
    {
        // Noop
    }

    @Override
    public void runPostLoad()
    {
        // Noop
    }

    @Override
    public void postProcessGeneration()
    {
        // Noop
    }

    @Override
    public void replaceWithPacketData(ChunkBiomeContainer biomeContainerIn, FriendlyByteBuf packetBufferIn, CompoundTag nbtIn, int availableSections)
    {
        // Noop
    }

    @Override
    public void removeEntity(Entity entityIn)
    {
        // Noop
    }

    @Override
    public void removeEntity(Entity entityIn, int index)
    {
        // Noop
    }

    @Override
    public void removeBlockEntity(BlockPos pos)
    {
        // Noop
    }

    @Override
    public void unpackTicks()
    {
        // Noop
    }

    @Override
    public void packTicks(ServerLevel serverWorldIn)
    {
        // Noop
    }

    @Override
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving)
    {
        // Noop
        return null;
    }

    @Override
    public void setLastSaveHadEntities(boolean hasEntitiesIn)
    {
        // Noop
    }

    @Override
    public void setHeightmap(Types type, long[] data)
    {
        // Noop
    }

    @Override
    public void setInhabitedTime(long newInhabitedTime)
    {
        // Noop
    }

    @Override
    public void setLastSaveTime(long saveTime)
    {
        // Noop
    }

    @Override
    public void setLightCorrect(boolean lightCorrectIn)
    {
        // Noop
    }

    @Override
    public void setLoaded(boolean loaded)
    {
        // Noop
    }

    @Override
    public void setFullStatus(Supplier<FullChunkStatus> locationTypeIn)
    {
        // Noop
    }

    @Override
    public void setUnsaved(boolean modified)
    {
        // Noop
    }

    @Override
    public void setAllReferences(Map<StructureFeature<?>, LongSet> p_201606_1_)
    {
        // Noop
    }

    @Override
    public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> structureStartsIn)
    {
        // Noop
    }

    @Override
    protected void invalidateCaps()
    {
        // Noop
    }

    @Override
    protected void reviveCaps()
    {
        // Noop
    }

    @Override
    public void addPackedPostProcess(short packedPosition, int index)
    {
        // Noop
    }

    @Override
    public LevelChunkSection getHighestSection()
    {
        // Noop
        return null;
    }

    @Override
    public int getHighestSectionPosition()
    {
        // Noop
        return 255;
    }

    @Override
    public boolean isYSpaceEmpty(int startY, int endY)
    {
        return false;
    }

    @Override
    public void markPosForPostprocessing(BlockPos pos)
    {
        // Noop
    }

    @Override
    public int getLightEmission(BlockPos pos)
    {
        return 15;
    }
}
