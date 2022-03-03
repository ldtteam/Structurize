package com.ldtteam.structurize.client;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkHolder.FullChunkStatus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import net.minecraft.world.ticks.TickContainerAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;

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
        super(worldIn, new ChunkPos(x, z));
        this.access = (BlueprintBlockAccess) worldIn;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        return access.getBlockState(pos);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(final BlockPos pos, final EntityCreationType creationMode)
    {
        return access.getBlockEntity(pos);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(final BlockPos pos)
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
    public void setBlockEntityNbt(CompoundTag nbt)
    {
        // Noop
    }

    @Nullable
    @Override
    public StructureStart getStartForFeature(final ConfiguredStructureFeature<?, ?> feature)
    {
        // Noop
        return null;
    }

    @NotNull
    @Override
    public LongSet getReferencesForFeature(final ConfiguredStructureFeature<?, ?> feature)
    {
        return new LongOpenHashSet();
    }

    @Override
    public void addReferenceForFeature(final ConfiguredStructureFeature<?, ?> feature, final long p_207947_)
    {
        // Noop
    }

    @Override
    public void setStartForFeature(final ConfiguredStructureFeature<?, ?> feature, final StructureStart start)
    {
        // Noop
    }

    @Override
    public CompoundTag getBlockEntityNbt(BlockPos pos)
    {
        // Noop
        return null;
    }

    @Override
    public TickContainerAccess<Fluid> getFluidTicks()
    {
        return super.getFluidTicks();
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

    @NotNull
    @Override
    public ChunkStatus getStatus()
    {
        return ChunkStatus.FULL;
    }

    @NotNull
    @Override
    public Map<ConfiguredStructureFeature<?, ?>, LongSet> getAllReferences()
    {
        return new HashMap<>();
    }

    @NotNull
    @Override
    public Map<ConfiguredStructureFeature<?, ?>, StructureStart> getAllStarts()
    {
        return new HashMap<>();
    }

    @NotNull
    @Override
    public Set<BlockPos> getBlockEntitiesPos()
    {
        // Noop
        return new HashSet();
    }

    @NotNull
    @Override
    public Map<BlockPos, BlockEntity> getBlockEntities()
    {
        // Noop
        return new HashMap<>();
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
    public void removeBlockEntity(BlockPos pos)
    {
        // Noop
    }

    @Override
    public void unpackTicks(final long p_187986_)
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
    public void setAllReferences(final Map<ConfiguredStructureFeature<?, ?>, LongSet> p_201606_1_)
    {
        // Noop
    }

    @Override
    public void setAllStarts(final Map<ConfiguredStructureFeature<?, ?>, StructureStart> structureStartsIn)
    {
        // Noop
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
