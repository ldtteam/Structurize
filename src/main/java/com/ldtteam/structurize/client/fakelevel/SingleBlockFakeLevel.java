package com.ldtteam.structurize.client.fakelevel;

import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Simple implementation of {@link IFakeLevelBlockGetter} mostly for usage in methods where {@link Level} is needed for virtual
 * BE/entities etc.
 */
public class SingleBlockFakeLevel extends FakeLevel
{
    /**
     * Creates simple fakeLevel instance
     * 
     * @param realLevel actual valid vanilla instance to provide eg. registries
     */
    public SingleBlockFakeLevel(final Level realLevel)
    {
        super(new SingleBlockFakeLevelGetter(), IFakeLevelLightProvider.USE_CLIENT_LEVEL, null, true);
        super.setRealLevel(realLevel);
    }

    @Override
    public SingleBlockFakeLevelGetter getLevelSource()
    {
        return (SingleBlockFakeLevelGetter) super.getLevelSource();
    }

    /**
     * Do not forget to unset to prevent potential memory leaks
     *
     * @param blockState  related to blockEntity
     * @param blockEntity related to blockState
     * @param realLevel   actual valid vanilla instance to provide eg. registries
     * @see #unset(FakeLevel, BlockEntity)
     * @see FakeLevel#setEntities(Collection) FakeLevel#setEntities(Collection) if you want to add entities, do not forget to reset
     */
    public void prepare(final BlockState blockState, @Nullable final BlockEntity blockEntity, final Level realLevel)
    {
        getLevelSource().blockEntity = blockEntity;
        getLevelSource().blockState = blockState;
        setRealLevel(realLevel);

        if (blockEntity != null)
        {
            blockEntity.setLevel(this);
        }
    }

    /**
     * @param blockEntity to unlink level if needed
     * @see #prepare(FakeLevel, BlockState, BlockEntity, Level)
     */
    public void unset(@Nullable final BlockEntity blockEntity)
    {
        getLevelSource().blockEntity = null;
        getLevelSource().blockState = null;
        setRealLevel(null);

        if (blockEntity != null)
        {
            try
            {
                blockEntity.setLevel(null);
            }
            catch (final NullPointerException e)
            {
                // setLevel impls sometimes violates nullability of level field
            }
        }
    }

    /**
     * See related methods for more information.
     * 
     * @param blockState  related to blockEntity
     * @param blockEntity related to blockState
     * @param realLevel   actual valid vanilla instance to provide eg. registries
     * @param action      context action
     * @see #prepare(FakeLevel, BlockState, BlockEntity, Level)
     * @see #unset(FakeLevel, BlockEntity)
     */
    public void withFakeLevelContext(final BlockState blockState,
        @Nullable final BlockEntity blockEntity,
        final Level realLevel,
        final Consumer<Level> action)
    {
        prepare(blockState, blockEntity, realLevel);
        action.accept(this);
        unset(blockEntity);
    }

    /**
     * See related methods for more information.
     * 
     * @param blockState  related to blockEntity
     * @param blockEntity related to blockState
     * @param realLevel   actual valid vanilla instance to provide eg. registries
     * @param action      context action
     * @see #prepare(FakeLevel, BlockState, BlockEntity, Level)
     * @see #unset(FakeLevel, BlockEntity)
     */
    public <T> T useFakeLevelContext(final BlockState blockState,
        @Nullable final BlockEntity blockEntity,
        final Level realLevel,
        final Function<Level, T> action)
    {
        prepare(blockState, blockEntity, realLevel);
        final T result = action.apply(this);
        unset(blockEntity);
        return result;
    }

    public static class SingleBlockFakeLevelGetter implements IFakeLevelBlockGetter
    {
        public BlockState blockState = null;
        public BlockEntity blockEntity = null;

        @Override
        public BlockEntity getBlockEntity(final BlockPos pos)
        {
            return blockEntity;
        }

        @Override
        public BlockState getBlockState(final BlockPos pos)
        {
            return blockState;
        }

        @Override
        public int getHeight()
        {
            return 1;
        }

        @Override
        public short getSizeX()
        {
            return 1;
        }

        @Override
        public short getSizeZ()
        {
            return 1;
        }

        @Override
        public void describeSelfInCrashReport(final CrashReportCategory category)
        {
            category.setDetail("Single block", blockState::toString);
            category.setDetail("Single block entity type",
                () -> blockEntity == null ? null : ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType()).toString());
        }
    }

    public static class SidedSingleBlockFakeLevel
    {
        private SingleBlockFakeLevel client;
        private SingleBlockFakeLevel server;

        public SingleBlockFakeLevel get(final Level realLevel)
        {
            if (realLevel.isClientSide())
            {
                return client != null ? client : (client = new SingleBlockFakeLevel(realLevel));
            }
            else
            {
                return server != null ? server : (server = new SingleBlockFakeLevel(realLevel));
            }
        }
    }
}
