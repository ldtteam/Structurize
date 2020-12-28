package com.ldtteam.blockout;

import com.ldtteam.blockout.views.View;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ldtteam.blockout.Log.getLogger;

/**
 * Special parameters for the panes.
 */
public class PaneParams
{
    public static final Pattern       PERCENTAGE_PATTERN = Pattern.compile("([-+]?\\d+)(%|px)?", Pattern.CASE_INSENSITIVE);
    public static final Pattern       RGBA_PATTERN       = Pattern.compile("rgba?\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*(?:,\\s*([01]\\.\\d+)\\s*)?\\)", Pattern.CASE_INSENSITIVE);
    public static final Pattern       HEXADECIMAL        = Pattern.compile("#([0-9A-F]{6,8})", Pattern.CASE_INSENSITIVE);

    private final Map<String, Object> propertyCache = new HashMap<>();
    private final List<PaneParams>    children;
    private final Node                node;
    private       View                parentView;

    /**
     * Instantiates the pane parameters.
     *
     * @param n the node.
     */
    public PaneParams(final Node n)
    {
        node = n;
        children = new ArrayList<>(node.getChildNodes().getLength());
    }

    public String getType()
    {
        return node.getNodeName();
    }

    public View getParentView()
    {
        return parentView;
    }

    public void setParentView(final View parent)
    {
        parentView = parent;
    }

    public int getParentWidth()
    {
        return parentView != null ? parentView.getInteriorWidth() : 0;
    }

    public int getParentHeight()
    {
        return parentView != null ? parentView.getInteriorHeight() : 0;
    }

    public List<PaneParams> getChildren()
    {
        if (!children.isEmpty()) return children;

        Node child = node.getFirstChild();
        while (child != null)
        {
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                children.add(new PaneParams(child));
            }
            child = child.getNextSibling();
        }

        return children;
    }

    @NotNull
    public String getText()
    {
        return node.getTextContent().trim();
    }

    @SuppressWarnings("unchecked")
    public <T> T property(String name, Parsers.Any<T> parser, T fallback)
    {
        T result = null;

        if (propertyCache.containsKey(name))
        {
            try
            {
                result = (T) propertyCache.get(name);
                if (result != null) return result;
            }
            catch (ClassCastException cce)
            {
                Log.getLogger().warn("Invalid property: previous value of key does not match type.");
            }
        }

        final Node attr = getAttribute(name);
        if (attr != null) result = parser.apply(attr.getNodeValue());

        propertyCache.put(name, result);
        return result != null ? result : fallback;
    }

    /**
     * Get the string attribute.
     *
     * @param name the name to search.
     * @return the attribute.
     */
    public String string(final String name)
    {
        return string(name, null);
    }

    /**
     * Get the String attribute from the name and definition.
     *
     * @param name      the name.
     * @param fallback  the default value.
     * @return the String.
     */
    public String string(final String name, final String fallback)
    {
        return property(name, String::toString, fallback);
    }

    /**
     * Get the String attribute from the name.
     *
     * @param name the name.
     * @return the String.
     */
    public List<IFormattableTextComponent> multiline(final String name)
    {
        return multiline(name, Collections.emptyList());
    }

    /**
     * Get the String attribute from the name.
     *
     * @param name the name.
     * @return the String.
     */
    public List<IFormattableTextComponent> multiline(final String name, List<IFormattableTextComponent> fallback)
    {
        return property(name, Parsers.MULTILINE, fallback);
    }

    private Node getAttribute(final String name)
    {
        return node.getAttributes().getNamedItem(name);
    }

    public boolean hasAttribute(final String name)
    {
        return node.getAttributes().getNamedItem(name) != null;
    }


    /**
     * Get the localized String attribute from the name and definition.
     *
     * @param name      the name.
     * @param fallback  the default value.
     * @return the string.
     */
    @Nullable
    public IFormattableTextComponent text(final String name, final IFormattableTextComponent fallback)
    {
        return property(name, Parsers.TEXT, fallback);
    }

    /**
     * Get the integer attribute from name and definition.
     *
     * @param name     the name.
     * @param fallback the default value.
     * @return the int.
     */
    public int numeral(final String name, final int fallback)
    {
        return property(name, Parsers.INT, fallback);
    }

    /**
     * Get the float attribute from name and definition.
     *
     * @param name     the name.
     * @param fallback the definition.
     * @return the float.
     */
    public float numeral(final String name, final float fallback)
    {
        return property(name, Parsers.FLOAT, fallback);
    }

    /**
     * Get the double attribute from name and definition.
     *
     * @param name     the name.
     * @param fallback the definition.
     * @return the double.
     */
    public double numeral(final String name, final double fallback)
    {
        return property(name, Double::parseDouble, fallback);
    }

    /**
     * Get the boolean attribute from name and definition.
     *
     * @param name     the name.
     * @param fallback the definition.
     * @return the boolean.
     */
    public boolean bool(final String name, final boolean fallback)
    {
        return property(name, Parsers.BOOLEAN, fallback);
    }

    /**
     * Get the boolean attribute from name and class and definition..
     *
     * @param name      the name.
     * @param clazz     the class.
     * @param fallback  the default value.
     * @param <T>       the type of class.
     * @return the enum attribute.
     */
    public <T extends Enum<T>> T enumeration(final String name, final Class<T> clazz, final T fallback)
    {
        return property(name, Parsers.ENUM(clazz), fallback);
    }

    /**
     * Get the scalable integer attribute from name and definition.
     *
     * @param name  the name.
     * @param def   the definition.
     * @param scale the scale.
     * @return the integer.
     */
    public int getScalableIntegerAttribute(final String name, final int def, final int scale)
    {
        final String attr = string(name);
        if (attr != null)
        {
            final Matcher m = PERCENTAGE_PATTERN.matcher(attr);
            if (m.find())
            {
                return parseScalableIntegerRegexMatch(m, def, scale);
            }
        }

        return def;
    }

    private static int parseScalableIntegerRegexMatch(final Matcher m, final int def, final int scale)
    {
        try
        {
            int value = Integer.parseInt(m.group(1));

            if ("%".equals(m.group(2)))
            {
                value = scale * MathHelper.clamp(value, 0, 100) / 100;
            }
            // DO NOT attempt to do a "value < 0" treated as (100% of parent) - abs(size)
            // without differentiating between 'size' and 'position' value types
            // even then, it's probably not actually necessary...

            return value;
        }
        catch (final NumberFormatException | IndexOutOfBoundsException | IllegalStateException ex)
        {
            getLogger().warn(ex);
        }

        return def;
    }

    /**
     * Get the size pair attribute.
     *
     * @param name  the name.
     * @param def   the definition.
     * @param scale the scale.
     * @return the SizePair.
     */
    @Nullable
    public SizePair getSizePairAttribute(final String name, final SizePair def, final SizePair scale)
    {
        final String attr = string(name);
        if (attr != null)
        {
            int w = def != null ? def.x : 0;
            int h = def != null ? def.y : 0;

            final Matcher m = PERCENTAGE_PATTERN.matcher(attr);
            if (m.find())
            {
                w = parseScalableIntegerRegexMatch(m, w, scale != null ? scale.x : 0);

                if (m.find() || m.find(0))
                {
                    // If no second value is passed, use the first value
                    h = parseScalableIntegerRegexMatch(m, h, scale != null ? scale.y : 0);
                }
            }

            return new SizePair(w, h);
        }

        return def;
    }

    /**
     * Get the color attribute from name and definition.
     *
     * @param name the name.
     * @param def  the definition
     * @return int color value.
     */
    public int color(final String name, final int def)
    {
        return property(name, Parsers.COLOR, def);
    }

    /**
     * Size pair of width and height.
     */
    public static class SizePair
    {
        private final int x;
        private final int y;

        /**
         * Instantiates a SizePair object.
         *
         * @param w width.
         * @param h height.
         */
        public SizePair(final int w, final int h)
        {
            x = w;
            y = h;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }
    }

    public <T> void multiProperty(String name, Function<String, T> parser, Consumer<List<T>> applier)
    {
        final String value = string(name);
        final List<T> results = new LinkedList<>();
        for (final String segment : value.split("\\s*[,\\s]\\s*"))
        {
            results.add(parser.apply(segment));
        }

        applier.accept(results);
    }

    public <T> T propertyAliases(T fallback, Parsers.Any<T> parser, String... aliases)
    {
        final NamedNodeMap map = node.getAttributes();
        for (final String name : aliases)
        {
            if (map.getNamedItem(name) != null)
            {
                return property(name, parser, fallback);
            }
        }

        return fallback;
    }

    /**
     * Checks if any of attribute names are present and return first found, else return default.
     *
     * @param def default attribute name
     * @param attributes attributes names to check
     * @return first found attribute or default
     */
    public String hasAnyAttribute(final String def, final String... attributes)
    {
        final NamedNodeMap nodeMap = node.getAttributes();
        for (final String attr : attributes)
        {
            if (nodeMap.getNamedItem(attr) != null) // inlined hasAttribute
            {
                return attr;
            }
        }
        return def;
    }
}
