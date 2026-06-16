package com.ldtteam.structurize.client.gui.index;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.AbstractTextBuilder;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.structurize.index.packtypes.models.PackTypeSchematicRequirementSeverity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.Locale;

import static com.ldtteam.structurize.api.constants.TranslationConstants.*;

/**
 * Shared utilities for the schematic index GUI windows.
 */
class SchematicIndexWindowUtils
{
    /**
     * Renders a validation count button — sets the issue count as text and builds a tooltip with a
     * severity header, followed by each issue prefixed with "- ", up to {@code maxDisplay} entries,
     * then an "and N more" line if the list exceeds that.
     *
     * @param button     the button to render
     * @param severity   the severity level, used to derive the tooltip header
     * @param issues     the list of validation issues to display
     * @param maxDisplay the maximum number of issues to show in the tooltip before truncating
     */
    static void renderValidationCountButton(final Button button, final PackTypeSchematicRequirementSeverity severity, final List<Component> issues, final int maxDisplay)
    {
        button.setText(Component.literal(issues.isEmpty() ? "" : String.valueOf(issues.size())));

        final AbstractTextBuilder.TooltipBuilder tooltipBuilder = PaneBuilders.tooltipBuilder().hoverPane(button);
        if (!issues.isEmpty())
        {
            tooltipBuilder.append(severityHeader(severity));
            tooltipBuilder.appendNL(Component.empty());
            issues.stream().limit(maxDisplay).forEach(issue -> tooltipBuilder.appendNL(Component.literal("- ").append(issue)));
            if (issues.size() > maxDisplay)
            {
                tooltipBuilder.appendNL(Component.empty());
                tooltipBuilder.appendNL(Component.translatable(PACK_TYPE_VALIDATION_TOOLTIP_MORE, issues.size() - maxDisplay));
            }
        }
        tooltipBuilder.build();
    }

    private static Component severityHeader(final PackTypeSchematicRequirementSeverity severity)
    {
        return switch (severity)
        {
            case INFORMATIONAL -> Component.translatable(SCHEMATIC_INDEX_SEVERITY_INFO_TITLE);
            case ISSUE -> Component.translatable(SCHEMATIC_INDEX_SEVERITY_WARN_TITLE);
            case ERROR -> Component.translatable(SCHEMATIC_INDEX_SEVERITY_ERROR_TITLE);
        };
    }

    /**
     * Attaches a single-line tooltip to a pane.
     *
     * @param pane    the pane to attach the tooltip to
     * @param tooltip the tooltip text
     */
    static void simpleTooltip(final Pane pane, final Component tooltip)
    {
        PaneBuilders.tooltipBuilder().hoverPane(pane).append(tooltip).build();
    }

    /**
     * Attaches a two-line tooltip to a pane, with a title in normal color and a description in
     * gray beneath it.
     *
     * @param pane  the pane to attach the tooltip to
     * @param title the main tooltip text
     * @param desc  the secondary description shown on the second line in gray
     */
    static void tooltipWithDescription(final Pane pane, final Component title, final Component desc)
    {
        PaneBuilders.tooltipBuilder().hoverPane(pane).append(title).appendNL(Component.empty()).appendNL(desc.copy().withStyle(ChatFormatting.GRAY)).build();
    }

    /**
     * Builds a {@link Component} that highlights the first occurrence of {@code filter} within
     * {@code source} in red. If {@code filter} is {@code null}, empty, or not found, the source
     * text is returned unstyled.
     *
     * @param source the full text to display
     * @param filter the substring to highlight (case-insensitive), or {@code null}/empty to skip
     * @return the styled component
     */
    static Component buildFilteredText(final String source, final String filter)
    {
        if (filter == null || filter.isEmpty())
        {
            return Component.literal(source);
        }

        final String nameLower = source.toLowerCase(Locale.ROOT);
        final String queryLower = filter.toLowerCase(Locale.ROOT);
        final int matchStart = nameLower.indexOf(queryLower);

        if (matchStart == -1)
        {
            return Component.literal(source);
        }

        final int matchEnd = matchStart + filter.length();
        final MutableComponent result = Component.empty();

        if (matchStart > 0)
        {
            result.append(Component.literal(source.substring(0, matchStart)));
        }

        result.append(Component.literal(source.substring(matchStart, matchEnd)).withStyle(ChatFormatting.RED));

        if (matchEnd < source.length())
        {
            result.append(Component.literal(source.substring(matchEnd)));
        }

        return result;
    }
}
