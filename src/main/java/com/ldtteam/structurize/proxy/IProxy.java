package com.ldtteam.structurize.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
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
    void openBuildToolWindow(BlockPos pos);

    /**
     * Opens a shape tool window.
     *
     * @param pos coordinates.
     */
    void openShapeToolWindow(BlockPos pos);

    /**
     * Opens a scan tool window.
     *
     * @param pos1 first pos.
     * @param pos2 second pos.
     * @param anchorPos Optional anchor position.
     */
    void openScanToolWindow(final BlockPos pos1, final BlockPos pos2, final Optional<BlockPos> anchorPos);

    /**
     * Opens a build tool window for a specific structure.
     *
     * @param pos           the position.
     * @param structureName the structure name.
     * @param rotation      the rotation.
     */
    void openBuildToolWindow(BlockPos pos, String structureName, int rotation);

    /**
     * Get the file representation of the additional schematics' folder.
     *
     * @return the folder for the schematic
     */
    @Nullable
    File getSchematicsFolder();

    /**
     * Method to get a side specific world from a message context anywhere.
     *
     * @param dimension the dimension.
     * @return The world.
     */
    @Nullable
    World getWorld(int dimension);

    /**
     * Returns the recipe book from the player.
     *
     * @param player THe player.
     * @return The recipe book.
     */
    @NotNull
    RecipeBook getRecipeBookFromPlayer(@NotNull PlayerEntity player);

    /**
     * Opens a build tool window.
     *
     * @param pos coordinates.
     */
    void openMultiBlockWindow(BlockPos pos);

    /**
     * Opens a placerholder block window.
     *
     * @param pos coordinates.
     */
    void openPlaceholderBlockWindow(BlockPos pos);

    /**
     * Sends given message to client chat (if client sided) or to all server OPs (if server sided).
     * Is ensured to run on main thread.
     *
     * @param message to send
     */
    void notifyClientOrServerOps(ITextComponent message);
}
