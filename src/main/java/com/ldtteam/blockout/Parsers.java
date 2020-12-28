package com.ldtteam.blockout;

import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Parsers
{
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

    public static Any<List<IFormattableTextComponent>> MULTILINE = v ->
        Arrays.stream(v.split("(;|<br/?>|\\\\n)"))
          .map(s -> Parsers.TEXT.apply(s))
          .collect(Collectors.toList());

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

    private final Function<String,?> method;

    <T> Parsers(Function<String, T> parser)
    {
        this.method = parser;
    }

    public Function<String,?> get() { return method; }
}
