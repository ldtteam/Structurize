package com.ldtteam.structurize.blueprints.v1;

import com.ldtteam.structurize.index.PackValidator;
import com.ldtteam.structurize.index.packtypes.models.PackType;
import com.ldtteam.structurize.network.messages.SaveScanMessage;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for performing pack-aware schematic scans.
 *
 * <p>Given an {@link IBlueprintDetails}, this class:
 * <ol>
 *     <li>Creates a {@link Blueprint} from the described world region via {@link BlueprintUtil}</li>
 *     <li>Validates the blueprint against the pack type's schematic requirements via {@link PackValidator}</li>
 *     <li>Sends the resulting blueprint to the player's client via {@link SaveScanMessage}</li>
 * </ol>
 */
public final class ScanUtil
{
    private ScanUtil() {}

    /**
     * Performs a pack-aware schematic scan and sends the result to the player.
     *
     * <p>The blueprint is validated against the pack type's schematic requirements before being sent.
     *
     * @param details   the source providing bounding box, path, and anchor
     * @param packType  the pack type whose schematic requirements to validate against
     * @param level     the server level to scan from
     * @param player    the player to send the resulting blueprint to
     */
    public static void scan(
        final @NotNull IBlueprintDetails details,
        final @NotNull Holder<PackType> packType,
        final @NotNull ServerLevel level,
        final @NotNull ServerPlayer player)
    {
        final Blueprint blueprint = BlueprintUtil.createBlueprint(details, level);

        PackValidator.validateBlueprint(details, blueprint, packType.value().getSchematicRequirements(), level);

        new SaveScanMessage(BlueprintUtil.writeBlueprintToNBT(blueprint), blueprint.getName()).sendToPlayer(player);
    }
}
