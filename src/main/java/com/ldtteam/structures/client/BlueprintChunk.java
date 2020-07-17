package com.ldtteam.structures.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ChunkHolder.LocationType;
import net.minecraft.world.server.ServerWorld;

/**
 * Blueprint simulated chunk.
 */
public class BlueprintChunk extends Chunk
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
    public BlueprintChunk(final World worldIn, final int x, final int z)
    {
        super(worldIn, new ChunkPos(x, z), new BiomeContainer(new Biome[0]));
        this.access = (BlueprintBlockAccess) worldIn;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        return access.getBlockState(pos);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(@NotNull final BlockPos pos, final CreateEntityType creationMode)
    {
        return access.getTileEntity(pos);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(@NotNull final BlockPos pos)
    {
        return access.getTileEntity(pos);
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
    public World getWorld()
    {
        return access;
    }

    @Override
    public void addEntity(Entity entityIn)
    {
        // Noop
    }

    @Override
    public void addTileEntity(TileEntity tileEntityIn)
    {
        // Noop
    }

    @Override
    public void addTileEntity(CompoundNBT nbt)
    {
        // Noop
    }

    @Override
    public void addTileEntity(BlockPos pos, TileEntity tileEntityIn)
    {
        // Noop
    }

    @Override
    public StructureStart<?> func_230342_a_(Structure<?> p_230342_1_)
    {
        // Noop
        return null;
    }

    @Override
    public void func_230343_a_(Structure<?> p_230343_1_, long p_230343_2_)
    {
        // Noop
    }

    @Override
    public void func_230344_a_(Structure<?> p_230344_1_, StructureStart<?> p_230344_2_)
    {
        // Noop
    }

    @Override
    public LongSet func_230346_b_(Structure<?> p_230346_1_)
    {
        // Noop
        return null;
    }

    @Override
    public BiomeContainer getBiomes()
    {
        // Noop
        return null;
    }

    @Override
    public ITickList<Block> getBlocksToBeTicked()
    {
        // Noop
        return null;
    }

    @Override
    public CompoundNBT getDeferredTileEntity(BlockPos pos)
    {
        // Noop
        return null;
    }

    @Override
    public <T extends Entity> void getEntitiesOfTypeWithinAABB(Class<? extends T> entityClass,
        AxisAlignedBB aabb,
        List<T> listToFill,
        Predicate<? super T> filter)
    {
        // Noop
    }

    @Override
    public void getEntitiesWithinAABBForEntity(Entity entityIn,
        AxisAlignedBB aabb,
        List<Entity> listToFill,
        Predicate<? super Entity> filter)
    {
        // Noop
    }

    @Override
    public <T extends Entity> void getEntitiesWithinAABBForList(EntityType<?> entitytypeIn,
        AxisAlignedBB aabb,
        List<? super T> list,
        Predicate<? super T> filter)
    {
        // Noop
    }

    @Override
    public ClassInheritanceMultiMap<Entity>[] getEntityLists()
    {
        // Noop
        return null;
    }

    @Override
    public ITickList<Fluid> getFluidsToBeTicked()
    {
        // Noop
        return null;
    }

    @Override
    public Heightmap getHeightmap(Type typeIn)
    {
        // Noop
        return null;
    }

    @Override
    public Collection<Entry<Type, Heightmap>> getHeightmaps()
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
    public Stream<BlockPos> getLightSources()
    {
        // Noop
        return null;
    }

    @Override
    public LocationType getLocationType()
    {
        // Noop
        return null;
    }

    @Override
    public ShortList[] getPackedPositions()
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
    public ChunkSection[] getSections()
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
    public Map<Structure<?>, LongSet> getStructureReferences()
    {
        // Noop
        return null;
    }

    @Override
    public Map<Structure<?>, StructureStart<?>> getStructureStarts()
    {
        // Noop
        return null;
    }

    @Override
    public Set<BlockPos> getTileEntitiesPos()
    {
        // Noop
        return null;
    }

    @Override
    public Map<BlockPos, TileEntity> getTileEntityMap()
    {
        // Noop
        return null;
    }

    @Override
    public int getTopBlockY(Type heightmapType, int x, int z)
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
    public World getWorldForge()
    {
        return access;
    }

    @Override
    public WorldLightManager getWorldLightManager()
    {
        // Noop
        return null;
    }

    @Override
    public boolean hasLight()
    {
        return true;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public boolean isModified()
    {
        return false;
    }

    @Override
    public void markDirty()
    {
        // Noop
    }

    @Override
    public void postLoad()
    {
        // Noop
    }

    @Override
    public void postProcess()
    {
        // Noop
    }

    @Override
    public void read(BiomeContainer biomeContainerIn, PacketBuffer packetBufferIn, CompoundNBT nbtIn, int availableSections)
    {
        // Noop
    }

    @Override
    public void removeEntity(Entity entityIn)
    {
        // Noop
    }

    @Override
    public void removeEntityAtIndex(Entity entityIn, int index)
    {
        // Noop
    }

    @Override
    public void removeTileEntity(BlockPos pos)
    {
        // Noop
    }

    @Override
    public void rescheduleTicks()
    {
        // Noop
    }

    @Override
    public void saveScheduledTicks(ServerWorld serverWorldIn)
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
    public void setHasEntities(boolean hasEntitiesIn)
    {
        // Noop
    }

    @Override
    public void setHeightmap(Type type, long[] data)
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
    public void setLight(boolean lightCorrectIn)
    {
        // Noop
    }

    @Override
    public void setLoaded(boolean loaded)
    {
        // Noop
    }

    @Override
    public void setLocationType(Supplier<LocationType> locationTypeIn)
    {
        // Noop
    }

    @Override
    public void setModified(boolean modified)
    {
        // Noop
    }

    @Override
    public void setStructureReferences(Map<Structure<?>, LongSet> p_201606_1_)
    {
        // Noop
    }

    @Override
    public void setStructureStarts(Map<Structure<?>, StructureStart<?>> structureStartsIn)
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
    public void addPackedPosition(short packedPosition, int index)
    {
        // Noop
    }

    @Override
    public ChunkSection getLastExtendedBlockStorage()
    {
        // Noop
        return null;
    }

    @Override
    public int getTopFilledSegment()
    {
        // Noop
        return 255;
    }

    @Override
    public boolean isEmptyBetween(int startY, int endY)
    {
        return false;
    }

    @Override
    public void markBlockForPostprocessing(BlockPos pos)
    {
        // Noop
    }

    @Override
    public int getLightValue(BlockPos pos)
    {
        return 15;
    }
}
