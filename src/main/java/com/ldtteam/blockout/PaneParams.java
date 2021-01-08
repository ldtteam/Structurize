package com.ldtteam.blockout;

import com.ldtteam.blockout.properties.Parsers;
import com.ldtteam.blockout.views.View;
import net.minecraft.util.text.IFormattableTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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

    /**
     * Finds an attribute by name from the XML node
     * and parses it using the provided parser method
     * @param name the attribute name to search for
     * @param parser the parser to convert the attribute to its property
     * @param fallback the default result value if one cannot be parsed
     * @param <T> the type of value to work with
     * @return the parsed value
     */
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
        return property(name, Parsers.DOUBLE, fallback);
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
     * @param name  the name
     * @param scale the total value to be a fraction of
     * @param fallback the default value
     * @return the parsed value
     */
    private int scalable(String name, final int scale, final int fallback)
    {
        return property(name, Parsers.SCALED(scale), fallback);
    }

    /**
     * Parses two scalable values and processes them through an applicant
     * @param name the attribute name to search for
     * @param scaleX the first fraction total
     * @param scaleY the second fraction total
     * @param applier the method to utilise the result values
     */
    public void scalable(final String name, final int scaleX, final int scaleY, Consumer<List<Integer>> applier)
    {
        List<Integer> results = Parsers.SCALED(scaleX, scaleY).apply(string(name));
        if (results != null) applier.accept(results);
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
     * Fetches a property and runs the result through a given method.
     * Commonly used for shorthand properties.
     * @param name the name of the attribute to retrieve
     * @param parser the parser applied to each part
     * @param parts the maximum number of parts to fill to if less are given
     * @param applier the method to utilise the parsed values
     * @param <T> the type of each part
     */
    public <T> void shorthand(String name, Parsers.Any<T> parser, int parts, Consumer<List<T>> applier)
    {
        List<T> results = Parsers.shorthand(parser, parts).apply(string(name));
        if (results != null) applier.accept(results);
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
