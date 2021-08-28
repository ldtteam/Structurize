package com.ldtteam.structurize.api.util;

import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/**
 * BlockEntity rotation.
 */
public interface IRotatableBlockEntity
{
    /**
     * Rotate the block entity.
     * @param rotationIn the rotation.
     */
     void rotate(final Rotation rotationIn);

    /**
     * Mirror the block entity.
     * @param mirror the mirror.
     */
    void mirror(final Mirror mirror);
}
