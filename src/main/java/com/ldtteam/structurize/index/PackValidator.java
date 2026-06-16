package com.ldtteam.structurize.index;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintTagUtils;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.blueprints.v1.IBlueprintDetails;
import com.ldtteam.structurize.index.models.Pack;
import com.ldtteam.structurize.index.models.PackSchematic;
import com.ldtteam.structurize.index.models.PackSchematicValidationState;
import com.ldtteam.structurize.index.packtypes.models.PackTypeRequirement;
import com.ldtteam.structurize.index.packtypes.models.PackTypeSchematicRequirement;
import com.ldtteam.structurize.index.packtypes.models.PackTypeSchematicRequirementSeverity;
import com.ldtteam.structurize.index.packtypes.models.SchematicPredicate;
import com.ldtteam.structurize.util.BlockInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.ldtteam.structurize.api.constants.TranslationConstants.PACK_TYPE_VALIDATION_MISSING_BLOCK;
import static com.ldtteam.structurize.api.constants.TranslationConstants.PACK_TYPE_VALIDATION_MISSING_TAG;

/**
 * Utility class that centralizes all validation logic for {@link Pack}s and {@link PackSchematic}s.
 *
 * <p>Validation results are written in-place into the mutable state objects carried by the
 * pack and its schematics ({@link com.ldtteam.structurize.index.models.PackValidationState} and
 * {@link PackSchematicValidationState} respectively).
 */
public final class PackValidator
{
    private PackValidator() {}

    /**
     * Runs pack-level validation against the pack's {@link com.ldtteam.structurize.index.packtypes.models.PackType}
     * requirements and updates the pack's {@link com.ldtteam.structurize.index.models.PackValidationState} in-place.
     * Does not run per-schematic blueprint checks.
     * Equivalent to {@code validatePack(pack, level, false)}.
     *
     * @param pack  the pack to validate
     * @param level the server level used to resolve anchor block types
     */
    public static void validatePack(final @NotNull Pack pack, final @NotNull ServerLevel level)
    {
        validatePack(pack, level, false);
    }

    /**
     * Runs validation of the given pack against its {@link com.ldtteam.structurize.index.packtypes.models.PackType}
     * requirements and updates the pack's {@link com.ldtteam.structurize.index.models.PackValidationState} in-place.
     *
     * <p>Pack-level requirements (which schematic names/paths/levels must exist) are always checked.
     * If {@code fullValidation} is {@code true}, each schematic is also validated against the pack
     * type's {@link PackTypeSchematicRequirement}s.
     *
     * @param pack           the pack to validate
     * @param level          the server level used to resolve anchor block types
     * @param fullValidation {@code true} to also run per-schematic blueprint checks
     */
    public static void validatePack(final @NotNull Pack pack, final @NotNull ServerLevel level, final boolean fullValidation)
    {
        final List<Component> optionalWarnings = new ArrayList<>();
        final List<Component> requiredErrors = new ArrayList<>();

        for (final PackTypeRequirement req : pack.type().value().getPackRequirements())
        {
            final Set<Integer> matchedLevels = new HashSet<>();
            for (final PackSchematic schematic : pack.schematics())
            {
                if (matchesPackRequirement(schematic, req, level))
                {
                    matchedLevels.add(schematic.level());
                }
            }

            if (matchedLevels.isEmpty())
            {
                if (req.isOptional())
                {
                    optionalWarnings.add(req.getFailureDescription());
                }
                else
                {
                    requiredErrors.add(req.getFailureDescription());
                }
            }
            else if (req.getRequiredLevelCount() != null)
            {
                for (int i = 1; i <= req.getRequiredLevelCount(); i++)
                {
                    if (!matchedLevels.contains(i))
                    {
                        if (req.isOptional())
                        {
                            optionalWarnings.add(req.getMissingLevelDescription(i));
                        }
                        else
                        {
                            requiredErrors.add(req.getMissingLevelDescription(i));
                        }
                    }
                }
            }
        }

        pack.validationState().setOptionalWarnings(optionalWarnings);
        pack.validationState().setRequiredErrors(requiredErrors);

        if (fullValidation)
        {
            final List<PackTypeSchematicRequirement> schematicRequirements = pack.type().value().getSchematicRequirements();
            for (final PackSchematic schematic : pack.schematics())
            {
                final Blueprint blueprint = BlueprintUtil.createBlueprint(schematic, level);
                schematic.validationState().apply(validateBlueprint(schematic, blueprint, schematicRequirements, level));
            }
        }
    }

    /**
     * Runs per-schematic validation for all schematics in the given pack that match the provided
     * path, name, and optionally level. Each matching schematic's {@link PackSchematicValidationState}
     * is updated in-place.
     *
     * <p>When {@code schematicLevel} is {@code null} every schematic sharing the given path and
     * name is validated (all levels of that schematic group). When a specific level is supplied
     * only the schematic entry for that level is validated.
     *
     * @param pack           the pack whose schematics to validate
     * @param schematicPath  the relative folder path to match (may be empty for root-level)
     * @param schematicName  the schematic file name (without extension) to match
     * @param schematicLevel the specific level to validate (1-based), or {@code null} for all levels
     * @param level          the server level used to resolve anchor block types
     */
    public static void validateSchematic(
        final @NotNull Pack pack,
        final @NotNull String schematicPath,
        final @NotNull String schematicName,
        final @Nullable Integer schematicLevel,
        final @NotNull ServerLevel level)
    {
        final List<PackTypeSchematicRequirement> requirements = pack.type().value().getSchematicRequirements();
        for (final PackSchematic schematic : pack.schematics())
        {
            if (!schematic.path().equals(schematicPath) || !schematic.name().equals(schematicName))
            {
                continue;
            }
            if (schematicLevel != null && schematic.level() != schematicLevel)
            {
                continue;
            }

            final Blueprint blueprint = BlueprintUtil.createBlueprint(schematic, level);
            schematic.validationState().apply(validateBlueprint(schematic, blueprint, requirements, level));
        }
    }

    /**
     * Runs all provided schematic requirements against a freshly scanned blueprint.
     * Intended for scan-time validation where no stored {@link PackSchematic} exists yet.
     *
     * <p>The {@link ServerLevel} is used to resolve anchor block types in world space.
     *
     * @param details      the blueprint details providing path, name, level, and anchor
     * @param blueprint    the blueprint produced by the scan
     * @param requirements the requirements to evaluate
     * @param level        the server level used to resolve anchor block types
     * @return the validation state produced by running all matching requirements
     */
    public static PackSchematicValidationCollector validateBlueprint(
        final @NotNull IBlueprintDetails details,
        final @NotNull Blueprint blueprint,
        final @NotNull List<PackTypeSchematicRequirement> requirements,
        final @NotNull ServerLevel level)
    {
        final PackSchematicValidationCollector collector = new PackSchematicValidationCollector();

        for (final PackTypeSchematicRequirement requirement : requirements)
        {
            if (!matchesSchematicRequirement(details, requirement, blueprint, level))
            {
                continue;
            }
            checkSchematic(details, blueprint, requirement, collector);
        }

        return collector;
    }

    /**
     * Evaluates the content checks of a matched requirement against the blueprint and
     * appends any failures to the collector.
     *
     * <p>Checks performed:
     * <ul>
     *     <li>All {@link PackTypeSchematicRequirement#getRequiredTags()} must be present on the anchor block.</li>
     *     <li>Each entry in {@link PackTypeSchematicRequirement#getRequiredBlocks()} must appear at least the specified number of times.</li>
     *     <li>All {@link PackTypeSchematicRequirement#getRequiredAdditionalChecks()} predicates must return {@code true}.</li>
     * </ul>
     *
     * @param details     the blueprint details providing path, name, level, and anchor
     * @param blueprint   the blueprint to check
     * @param requirement the requirement to evaluate
     * @param collector   the collector to append failures to
     */
    private static void checkSchematic(
        final @NotNull IBlueprintDetails details,
        final @NotNull Blueprint blueprint,
        final @NotNull PackTypeSchematicRequirement requirement,
        final @NotNull PackSchematicValidationCollector collector)
    {
        final PackTypeSchematicRequirementSeverity severity = requirement.getWarningSeverity();

        if (!requirement.getRequiredTags().isEmpty())
        {
            final Set<String> allTags = new HashSet<>();
            BlueprintTagUtils.getBlueprintTags(blueprint).values().forEach(allTags::addAll);
            for (final PackTypeSchematicRequirement.TagCheck check : requirement.getRequiredTags())
            {
                if (!allTags.contains(check.tag()))
                {
                    collector.addIssue(severity, check.message() != null
                        ? check.message()
                        : Component.translatable(PACK_TYPE_VALIDATION_MISSING_TAG, check.tag()));
                }
            }
        }

        if (!requirement.getRequiredBlocks().isEmpty())
        {
            final Map<BlockState, Integer> blockCounts = new HashMap<>();
            for (final BlockInfo blockInfo : blueprint.getBlockInfoAsList())
            {
                blockCounts.merge(blockInfo.getState(), 1, Integer::sum);
            }
            for (final PackTypeSchematicRequirement.BlockCheck check : requirement.getRequiredBlocks())
            {
                final int found = blockCounts.getOrDefault(check.block(), 0);
                if (found < check.count())
                {
                    collector.addIssue(severity, check.message() != null
                        ? check.message()
                        : Component.translatable(PACK_TYPE_VALIDATION_MISSING_BLOCK, check.block(), found, check.count()));
                }
            }
        }

        for (final PackTypeSchematicRequirement.AdditionalCheck check : requirement.getRequiredAdditionalChecks())
        {
            if (!check.predicate().test(blueprint, details))
            {
                collector.addIssue(severity, check.message().get());
            }
        }
    }

    /**
     * Returns whether the given schematic satisfies all non-level criteria of the given
     * pack-level requirement (name, path, anchor).
     *
     * @param schematic   the schematic to check
     * @param requirement the requirement to match against
     * @param level       the server level, used to resolve anchor block types
     * @return {@code true} if the schematic satisfies all non-level criteria
     */
    private static boolean matchesPackRequirement(final @NotNull PackSchematic schematic, final @NotNull PackTypeRequirement requirement, final @NotNull ServerLevel level)
    {
        if (requirement.getRequiredName() != null && !requirement.getRequiredName().equals(schematic.name()))
        {
            return false;
        }
        if (requirement.getRequiredPath() != null && !requirement.getRequiredPath().equals(schematic.path()))
        {
            return false;
        }
        return requirement.getRequiredAnchor() == null || schematic.anchor().map(level::getBlockState).filter(requirement.getRequiredAnchor()::equals).isPresent();
    }

    /**
     * Returns whether the given schematic details satisfy all filter criteria of the given
     * schematic requirement (name, path, level, anchor, tags).
     *
     * @param details     the blueprint details providing name, path, level, and anchor
     * @param requirement the requirement to match against
     * @param blueprint   the blueprint, used to resolve anchor tags
     * @param level       the server level, used to resolve anchor block types
     * @return {@code true} if the schematic satisfies all filter criteria
     */
    private static boolean matchesSchematicRequirement(
        final @NotNull IBlueprintDetails details,
        final @NotNull PackTypeSchematicRequirement requirement,
        final @NotNull Blueprint blueprint,
        final @NotNull ServerLevel level)
    {
        if (requirement.getMatchesName() != null && !requirement.getMatchesName().equals(details.getSchematicName()))
        {
            return false;
        }
        if (requirement.getMatchesPath() != null && !requirement.getMatchesPath().equals(details.getSchematicPath()))
        {
            return false;
        }
        if (requirement.getMatchesLevel() != null && !requirement.getMatchesLevel().equals(details.getSchematicLevel()))
        {
            return false;
        }
        final BlockPos anchor = details.getAnchor();
        if (requirement.getMatchesAnchor() != null && (anchor == null || !requirement.getMatchesAnchor().equals(level.getBlockState(anchor))))
        {
            return false;
        }
        if (!requirement.getMatchesTags().isEmpty())
        {
            final Set<String> allTags = new HashSet<>();
            BlueprintTagUtils.getBlueprintTags(blueprint).values().forEach(allTags::addAll);
            if (!allTags.containsAll(requirement.getMatchesTags()))
            {
                return false;
            }
        }
        for (final SchematicPredicate predicate : requirement.getMatchesAdditionalChecks())
        {
            if (!predicate.test(blueprint, details))
            {
                return false;
            }
        }
        return true;
    }
}
