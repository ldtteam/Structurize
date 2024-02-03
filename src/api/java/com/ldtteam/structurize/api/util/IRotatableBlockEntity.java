package com.ldtteam.structurize.api.util;

import com.ldtteam.structurize.util.RotationMirror;

/**
 * BlockEntity rotation.
 */
public interface IRotatableBlockEntity
{
    /**
     * Rotates and mirrors block entity.
     */
    void rotateAndMirror(RotationMirror rotationMirror);
}
