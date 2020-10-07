package com.ldtteam.structurize.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Optional;

/**
 * Basic proxy interface.
 */
public interface IProxy
{
    /**
     * Returns whether or not the proxy is client sided or server sided.
     *
     * @return true when client, false when server.
     */
    boolean isClient();

    /**
     * Opens a build tool window.
     *
     * @param pos coordinates.
     */
    void openBuildToolWindow(final BlockPos pos);

    /**
     * Opens a shape tool window.
     *
     * @param pos coordinates.
     */
    void openShapeToolWindow(final BlockPos pos);

    /**
     * Opens a scan tool window.
     * @param pos1 first pos.
     * @param pos2 second pos.
     * @param anchorPos Optional anchor position.
     */
    void openScanToolWindow(final BlockPos pos1, final BlockPos pos2, final Optional<BlockPos> anchorPos);

    /**
     * Opens a build tool window for a specific structure.
     * @param pos the position.
     * @param structureName the structure name.
     * @param rotation the rotation.
     */
    void openBuildToolWindow(final BlockPos pos, final String structureName, final int rotation);

    /**
     * Get the file representation of the additional schematics' folder.
     *
     * @return the folder for the schematic
     */
    @Nullable
    File getSchematicsFolder();

    /**
     * Method to get a side specific world from a message context anywhere.
     * @param dimension the dimension.
     * @return The world.
     */
    @Nullable
    World getWorld(final int dimension);

    /**
     * Opens a build tool window.
     *
     * @param pos coordinates.
     */
    void openMultiBlockWindow(final BlockPos pos);

    /**
     * Opens a placerholder block window.
     *
     * @param pos coordinates.
     */
    void openPlaceholderBlockWindow(final BlockPos pos);
}
