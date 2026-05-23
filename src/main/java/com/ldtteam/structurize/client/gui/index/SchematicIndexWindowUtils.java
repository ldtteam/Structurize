package com.ldtteam.structurize.client.gui.index;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

/**
 * Shared utilities for the schematic index GUI windows.
 */
class SchematicIndexWindowUtils
{
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
