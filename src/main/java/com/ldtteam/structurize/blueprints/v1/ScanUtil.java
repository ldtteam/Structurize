package com.ldtteam.structurize.blueprints.v1;

import com.ldtteam.structurize.index.PackSchematicValidationCollector;
import com.ldtteam.structurize.index.PackValidator;
import com.ldtteam.structurize.index.packtypes.models.PackType;
import com.ldtteam.structurize.index.packtypes.models.PackTypeSchematicRequirementSeverity;
import com.ldtteam.structurize.network.messages.SaveScanMessage;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Utility for performing pack-aware schematic scans.
 *
 * <p>Given a pre-built {@link Blueprint}, this class validates it against the pack type's schematic
 * requirements via {@link PackValidator} and, if no blocking errors are found, sends it to the
 * player's client via {@link SaveScanMessage}.
 *
 * <p>Callers are responsible for creating the blueprint via {@link BlueprintUtil} so that the
 * (expensive) world scan is not repeated unnecessarily.
 */
public final class ScanUtil
{
    private ScanUtil() {}

    /**
     * Performs a pack-aware schematic scan and sends the result to the player if validation passes.
     *
     * <p>The collector is always returned, so callers can inspect issues at any severity. The scan
     * is only sent to the player if there are no {@link PackTypeSchematicRequirementSeverity#ERROR}
     * severity issues.
     *
     * @param details   the source providing path, name, level, and anchor metadata
     * @param blueprint the pre-built blueprint to validate and send
     * @param packType  the pack type whose schematic requirements to validate against
     * @param level     the server level used to resolve anchor block types
     * @param player    the player to send the resulting blueprint to
     * @return the validation collector, always
     */
    @NotNull
    public static PackSchematicValidationCollector scan(
        final @NotNull IBlueprintDetails details,
        final @NotNull Blueprint blueprint,
        final @NotNull Holder<PackType> packType,
        final @NotNull ServerLevel level,
        final @NotNull ServerPlayer player)
    {
        final PackSchematicValidationCollector collector = PackValidator.validateBlueprint(details, blueprint, packType.value().getSchematicRequirements(), level);

        final boolean hasErrors = !collector.getIssues().getOrDefault(PackTypeSchematicRequirementSeverity.ERROR, List.of()).isEmpty();

        if (!hasErrors)
        {
            new SaveScanMessage(BlueprintUtil.writeBlueprintToNBT(blueprint), blueprint.getName()).sendToPlayer(player);
        }

        return collector;
    }
}
