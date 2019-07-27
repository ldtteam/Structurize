package com.ldtteam.structures.client;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.MapData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Our world/blockAccess dummy.
 */
public class BlueprintBlockAccess extends World implements IBlockReader
{
    /**
     * The blueprint with the info we need.
     */
    private final Blueprint blueprint;

    /**
     * Constructor to create a new world/blockAccess
     * @param blueprint the blueprint.
     */
    public BlueprintBlockAccess(final Blueprint blueprint)
    {
        super(Minecraft.getInstance().world.getWorldInfo(), Minecraft.getInstance().world.dimension.getType(),
          new BiFunction<World, Dimension, AbstractChunkProvider>() {
              @Override
              public AbstractChunkProvider apply(final World world, final Dimension dimension)
              {
                  return Minecraft.getInstance().world.getChunkProvider();
              }
          }, Minecraft.getInstance().world.getProfiler(), true);
        this.blueprint = blueprint;
    }

    public Blueprint getBlueprint()
    {
        return blueprint;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(@NotNull final BlockPos pos)
    {
        return BlueprintUtils.getTileEntityFromPos(blueprint, pos, this);
    }

    @Nullable
    @Override
    public Entity getEntityByID(final int id)
    {
        return null;
    }

    @Override
    public int getCombinedLight(@NotNull final BlockPos pos, final int lightValue)
    {
        return 15 << 20 | 15 << 4;
    }

    @Override
    public int getLight(@NotNull final BlockPos pos)
    {
        return 15;
    }

    @Override
    public float getBrightness(@NotNull final BlockPos pos)
    {
        return 1f;
    }

    @NotNull
    @Override
    public BlockState getBlockState(@NotNull final BlockPos pos)
    {
        final BlockState state = BlueprintUtils.getBlockInfoFromPos(blueprint, pos).getState().getBlockState();
        return state.getBlock() == ModBlocks.blockSubstitution ? Blocks.AIR.getDefaultState() : state;
    }

    @Override
    public void playSound(
      @Nullable final PlayerEntity player,
      final double x,
      final double y,
      final double z,
      final SoundEvent soundIn,
      final SoundCategory category,
      final float volume,
      final float pitch)
    {

    }

    @Override
    public void playMovingSound(
      @Nullable final PlayerEntity p_217384_1_,
      @NotNull final Entity entity,
      @NotNull final SoundEvent event,
      @NotNull final SoundCategory category,
      final float p_217384_5_,
      final float p_217384_6_)
    {

    }

    @Override
    public boolean isAirBlock(@NotNull final BlockPos pos)
    {
        return getBlockState(pos).getBlockState().getBlock() instanceof AirBlock;
    }

    @Override
    public boolean isAreaLoaded(final BlockPos p_isAreaLoaded_1_, final int p_isAreaLoaded_2_)
    {
        return true;
    }

    @NotNull
    @Override
    public Biome getBiome(@NotNull final BlockPos pos)
    {
        return Biomes.PLAINS;
    }

    @NotNull
    @Override
    public Chunk getChunk(final BlockPos pos)
    {
        return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    @NotNull
    @Override
    public Chunk getChunk(int chunkX, int chunkZ)
    {
        return new BlueprintChunk(this, chunkX, chunkZ);
    }

    @Override
    public void notifyBlockUpdate(@NotNull final BlockPos pos, @NotNull final BlockState oldState, @NotNull final BlockState newState, final int flags)
    {

    }

    @NotNull
    @Override
    public ITickList<Block> getPendingBlockTicks()
    {
        return Minecraft.getInstance().world.getPendingBlockTicks();
    }

    @NotNull
    @Override
    public ITickList<Fluid> getPendingFluidTicks()
    {
        return Minecraft.getInstance().world.getPendingFluidTicks();
    }

    @NotNull
    @Override
    public AbstractChunkProvider getChunkProvider()
    {
        return Minecraft.getInstance().world.getChunkProvider();
    }

    @Override
    public void playEvent(@Nullable final PlayerEntity player, final int type, final BlockPos pos, final int data)
    {

    }

    @Nullable
    @Override
    public MapData func_217406_a(@NotNull final String p_217406_1_)
    {
        return null;
    }

    @Override
    public void func_217399_a(@NotNull final MapData p_217399_1_)
    {

    }

    @Override
    public int getNextMapId()
    {
        return 0;
    }

    @Override
    public void sendBlockBreakProgress(final int breakerId, @NotNull final BlockPos pos, final int progress)
    {

    }

    @NotNull
    @Override
    public Scoreboard getScoreboard()
    {
        return Minecraft.getInstance().world.getScoreboard();
    }

    @NotNull
    @Override
    public RecipeManager getRecipeManager()
    {
        return Minecraft.getInstance().world.getRecipeManager();
    }

    @NotNull
    @Override
    public NetworkTagManager getTags()
    {
        return Minecraft.getInstance().world.getTags();
    }

    @Override
    public int getStrongPower(@NotNull final BlockPos pos, @NotNull final Direction direction)
    {
        return 0;
    }

    @NotNull
    @Override
    public WorldType getWorldType()
    {
        return WorldType.DEFAULT;
    }

    @NotNull
    @Override
    public List<? extends PlayerEntity> getPlayers()
    {
        return Collections.emptyList();
    }
}
