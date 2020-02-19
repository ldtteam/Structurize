package com.ldtteam.structurize.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * CommonProxy of the structurize mod (Server and Client).
 */
@Mod.EventBusSubscriber
public class CommonProxy implements IProxy
{
    @Override
    public boolean isClient()
    {
        return false;
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openScanToolWindow(final BlockPos pos1, final BlockPos pos2)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos, final String structureName, final int rotation)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openShapeToolWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public File getSchematicsFolder()
    {
        return null;
    }

    @Nullable
    @Override
    public World getWorld(final int dimension)
    {
        return ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.getById(dimension));
    }

    @NotNull
    @Override
    public RecipeBook getRecipeBookFromPlayer(@NotNull final PlayerEntity player)
    {
        return ((ServerPlayerEntity) player).getRecipeBook();
    }

    @Override
    public void openMultiBlockWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openPlaceholderBlockWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }
}
