package com.ldtteam.blockout;

import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Color utility methods.
 */
public final class Color
{
    private static final Map<String, Integer> nameToColorMap = new HashMap<>();
    static
    {
        // Would love to load these from a file
        nameToColorMap.put("aqua", 0x00FFFF);
        nameToColorMap.put("black", 0x000000);
        nameToColorMap.put("blue", 0x0000FF);
        nameToColorMap.put("cyan", 0x00FFFF);
        nameToColorMap.put("fuchsia", 0xFF00FF);
        nameToColorMap.put("green", 0x008000);
        nameToColorMap.put("ivory", 0xFFFFF0);
        nameToColorMap.put("lime", 0x00FF00);
        nameToColorMap.put("magenta", 0xFF00FF);
        nameToColorMap.put("orange", 0xFFA500);
        nameToColorMap.put("orangered", 0xFF4500);
        nameToColorMap.put("purple", 0x800080);
        nameToColorMap.put("red", 0xFF0000);
        nameToColorMap.put("white", 0xFFFFFF);
        nameToColorMap.put("yellow", 0xFFFF00);
        nameToColorMap.put("gray", 0x808080);
        nameToColorMap.put("darkgray", 0xA9A9A9);
        nameToColorMap.put("dimgray", 0x696969);
        nameToColorMap.put("lightgray", 0xD3D3D3);
        nameToColorMap.put("slategray", 0x708090);
        nameToColorMap.put("darkgreen", 0x006400);
    }

    private Color()
    {
        // Hides default constructor.
    }

    /**
     * Parses a color or returns the default
     * @param color a string representation of the color, in rgba, hex, or int
     * @param def the fallback value
     * @return the parsed or defaulted color integer
     */
    public static int parse(String color, int def)
    {
        Integer result = Parsers.COLOR.apply(color);
        return result != null ? result : def;
    }

    /**
     * Get a color integer from its name.
     *
     * @param name name of the color.
     * @param def  default to use if the name doesn't exist.
     * @return the color as an integer.
     */
    public static int getByName(final String name, final int def)
    {
        final Integer i = nameToColorMap.get(name.toLowerCase(Locale.ENGLISH));
        return i != null ? i : def;
    }

    /**
     * Get a color integer from its name.
     *
     * @param name name of the color.
     * @return the color as an integer.
     */
    @Nullable
    public static Integer getByName(final String name)
    {
        return nameToColorMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Get the int from rgba.
     * @param r the red value from 0-255.
     * @param g the green value from 0-255.
     * @param b the blue value from 0-255.
     * @param a the transparency value from 0-255.
     * @return the accumulated int.
     */
    public static int rgbaToInt(final int r, final int g, final int b, final int a)
    {
        return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    public static int rgbaToInt(Matcher m)
    {
        final int r = MathHelper.clamp(Integer.parseInt(m.group(1)), 0, 255);
        final int g = MathHelper.clamp(Integer.parseInt(m.group(2)), 0, 255);
        final int b = MathHelper.clamp(Integer.parseInt(m.group(3)), 0, 255);
        final int a = MathHelper.clamp((int)Double.parseDouble(m.group(4))*255,0,255);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static net.minecraft.util.text.Color toVanilla(final int color)
    {
        return net.minecraft.util.text.Color.fromRgb(color);
    }
}
