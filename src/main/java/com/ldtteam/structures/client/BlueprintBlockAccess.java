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
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.FluidState;
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
import net.minecraft.world.storage.MapData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * Our world/blockAccess dummy.
 */
public class BlueprintBlockAccess extends World
{
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
        super(Minecraft.getInstance().world.getWorldInfo(),
            Minecraft.getInstance().world.func_234923_W_(),
            Minecraft.getInstance().world.func_234922_V_(),
            Minecraft.getInstance().world.func_230315_m_(),
            () -> Minecraft.getInstance().world.getProfiler(),
            true,
            true,
            0L);
        this.blueprint = blueprint;
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

    @Nullable
    @Override
    public Entity getEntityByID(final int id)
    {
        return null;
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

    @NotNull
    @Override
    public BlockState getBlockState(@NotNull final BlockPos pos)
    {
        final BlockState state = BlueprintUtils.getBlockInfoFromPos(blueprint, pos).getState().getBlockState();
        if (state.getBlock() == ModBlocks.blockSolidSubstitution)
        {
            return Blocks.DIRT.getDefaultState();
        }
        return state.getBlock() == ModBlocks.blockSubstitution ? Blocks.AIR.getDefaultState() : state;
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
    public void playSound(@Nullable final PlayerEntity player,
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
    public void playMovingSound(@Nullable final PlayerEntity p_217384_1_,
        @NotNull final Entity entity,
        @NotNull final SoundEvent event,
        @NotNull final SoundCategory category,
        final float p_217384_5_,
        final float p_217384_6_)
    {
    }

    @Override
    public Biome getNoiseBiomeRaw(final int x, final int y, final int z)
    {
        return Biomes.DEFAULT;
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
    public Chunk getChunk(final BlockPos pos)
    {
        return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    @NotNull
    @Override
    public Chunk getChunk(final int chunkX, final int chunkZ)
    {
        return new BlueprintChunk(this, chunkX, chunkZ);
    }

    @Override
    public void notifyBlockUpdate(@NotNull final BlockPos pos,
        @NotNull final BlockState oldState,
        @NotNull final BlockState newState,
        final int flags)
    {
    }

    @NotNull
    @Override
    public ITickList<Block> getPendingBlockTicks()
    {
        return null;
    }

    @NotNull
    @Override
    public ITickList<Fluid> getPendingFluidTicks()
    {
        return null;
    }

    @NotNull
    @Override
    public AbstractChunkProvider getChunkProvider()
    {
        return null;
    }

    @Nullable
    @Override
    public MapData getMapData(final String p_217406_1_)
    {
        return null;
    }

    @Override
    public void registerMapData(final MapData p_217399_1_)
    {
    }

    @Override
    public void playEvent(@Nullable final PlayerEntity player, final int type, final BlockPos pos, final int data)
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
        return null;
    }

    @NotNull
    @Override
    public RecipeManager getRecipeManager()
    {
        return null;
    }

    @NotNull
    @Override
    public NetworkTagManager getTags()
    {
        return null;
    }

    @NotNull
    @Override
    public List<? extends PlayerEntity> getPlayers()
    {
        return null;
    }

    @Override
    public float func_230487_a_(final Direction p_230487_1_, final boolean p_230487_2_)
    {
        return 1.0F;
    }
}
