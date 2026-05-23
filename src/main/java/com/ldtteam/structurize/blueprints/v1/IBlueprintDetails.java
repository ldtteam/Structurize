package com.ldtteam.structurize.blueprints.v1;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Provides the details required to create a {@link Blueprint} from a world region
 * via {@link BlueprintUtil#createBlueprint(IBlueprintDetails, net.minecraft.server.level.ServerLevel)}.
 *
 * <p>Implemented by both {@link com.ldtteam.structurize.index.models.PackSchematic} (for validation
 * of stored schematics) and scan GUI providers (for live scans via {@link ScanUtil}).
 */
public interface IBlueprintDetails
{
    /**
     * Returns one corner of the bounding box.
     *
     * @return the first corner position
     */
    BlockPos getPos1();

    /**
     * Returns the opposite corner of the bounding box.
     *
     * @return the second corner position
     */
    BlockPos getPos2();

    /**
     * Returns the relative folder path of the schematic, empty for root-level schematics.
     *
     * @return the schematic folder path, never {@code null}
     */
    String getSchematicPath();

    /**
     * Returns the schematic file name without extension.
     *
     * @return the schematic name, never {@code null}
     */
    String getSchematicName();

    /**
     * Returns the building level this schematic represents (1-based).
     *
     * @return the schematic level
     */
    int getSchematicLevel();

    /**
     * Returns the full schematic path including the file name (without extension).
     * For example: {@code "huts/miner"} or just {@code "miner"} for root-level schematics.
     *
     * @return the full schematic path
     */
    default String getFullSchematicPath()
    {
        final String path = getSchematicPath();
        return path.isEmpty() ? getSchematicName() : path + "/" + getSchematicName();
    }

    /**
     * Returns the world position of the anchor block, or {@code null} if none is set.
     *
     * @return the anchor position, or {@code null}
     */
    @Nullable
    BlockPos getAnchor();

}