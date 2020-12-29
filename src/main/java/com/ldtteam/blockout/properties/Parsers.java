package com.ldtteam.blockout.properties;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Log;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Parsers
{
    private Parsers() { /* prevent construction */ }

    /**
     * Just a convenient way to put the Function lambda
     */
    public interface Any<T>
    {
        T apply(String value);
    }

    // Primitives
    public static Any<Boolean>  BOOLEAN = v -> v == null || !v.isEmpty() && !v.equals("disabled") && Boolean.parseBoolean(v);
    public static Any<Integer>  INT = Integer::parseInt;
    public static Any<Float>    FLOAT = Float::parseFloat;
    public static Any<Double>   DOUBLE = Double::parseDouble;

    /** Parses a potentially translatable portion of text as a component */
    public static Any<IFormattableTextComponent> TEXT = v -> {
        String result = v;
        Matcher m = Pattern.compile("(\\$[({](\\w+)[})])").matcher(v);

        while (m.find())
        {
            String translated = LanguageHandler.translateKey(m.group(1));
            if (translated.equals(m.group(1)))
            {
                translated = TextFormatting.OBFUSCATED + "whoops!";
            }
            result = result.replace(m.group(0), translated);
        }

        return new StringTextComponent(result);
    };

    /** Applies the TEXT parser across multiple lines */
    public static Any<List<IFormattableTextComponent>> MULTILINE = v ->
        Arrays.stream(v.split("(;|<br/?>|\\\\n)"))
          .map(s -> Parsers.TEXT.apply(s))
          .collect(Collectors.toList());

    /** Parses a color from hex, rgba, name, or pure value */
    public static Any<Integer> COLOR = v -> {
        Matcher m = PaneParams.HEXADECIMAL.matcher(v);
        if (m.find()) return Integer.parseInt(m.group(), 16);

        m = PaneParams.RGBA_PATTERN.matcher(v);
        if (m.find()) return Color.rgbaToInt(m);

        try
        {
            return Integer.parseUnsignedInt(v);
        }
        catch (final NumberFormatException ex)
        {
            return Color.getByName(v);
        }
    };

    /**
     * Supply an enumeration class and this will parse it
     * @param clazz the enum class to parse against
     * @param <T> they enum class type
     * @return an Any value parsing method
     */
    public static <T extends Enum<T>> Any<T> ENUM(Class<T> clazz)
    {
        return v -> {
            try
            {
                return Enum.valueOf(clazz, v);
            }
            catch (IllegalArgumentException | NullPointerException e)
            {
                Log.getLogger().warn("Attempt to access non-existent enumeration '"+v+"'.");
            }
            return null;
        };
    }

    public static <T> Any<List<T>> shorthand(Parsers.Any<T> parser, int parts)
    {
        return v -> {
            final List<T> results = new ArrayList<>(parts);

            if (v == null) return null;

            for (final String segment : v.split("\\s*[,\\s]\\s*"))
            {
                results.add(parser.apply(segment));
            }

            while (results.size() < parts)
            {
                // Will duplicate in pairs, so a 4-part property defined
                // from "2 8" will become "2 8 2 8"
                // useful for syncing vertical and horizontal for each edge
                results.add(results.get(Math.max(0, results.size() - 2)));
            }

            return results;
        };
    }
}
