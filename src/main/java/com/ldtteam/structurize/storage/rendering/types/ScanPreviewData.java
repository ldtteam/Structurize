package com.ldtteam.structurize.storage.rendering.types;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Preview data for scan tool contexts.
 */
public class ScanPreviewData
{
    @NotNull
    public final BlockPos pos1;

    @NotNull
    public final BlockPos pos2;

    @NotNull
    public Optional<BlockPos> anchor;

    public ScanPreviewData(final @NotNull BlockPos pos1, final @NotNull BlockPos pos2, final @NotNull Optional<BlockPos> anchor)
    {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.anchor = anchor;
    }
}
