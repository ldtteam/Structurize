package com.ldtteam.blockout;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;

import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.*;
import net.minecraft.util.Tuple;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Utilities to load xml files.
 */
public final class Loader
{
    private static final int CACHE_CAP = 20;

    private static final Map<ResourceLocation, Tuple<Integer,Document>> parsedCache = Collections.synchronizedMap(new HashMap<ResourceLocation, Tuple<Integer, Document>>()
    {
        @Override
        public Tuple<Integer, Document> get(final Object o)
        {
            Tuple<Integer, Document> me = super.get(o);
            this.replace((ResourceLocation) o, new Tuple<>(me.getA()+1,me.getB()));
            return me;
        }
    });
    private static final Map<ResourceLocation, Function<PaneParams,? extends Pane>> paneFactories = new HashMap<>();
    static
    {
        register("view", View::new);
        register("group", Group::new);
        register("scrollgroup", ScrollingGroup::new);
        register("list", ScrollingList::new);
        register("text", Text::new);
        register("button", ButtonVanilla::new);
        register("buttonimage", ButtonImage::new);
        register("label", Text::new);
        register("input", TextFieldVanilla::new);
        register("image", Image::new);
        register("imagerepeatable", ImageRepeatable::new);
        register("box", Box::new);
        register("itemicon", ItemIcon::new);
        register("switch", SwitchView::new);
        register("dropdown", DropDownList::new);
        register("overlay", OverlayView::new);
        register("gradient", Gradient::new);
        register("zoomdragview", ZoomDragView::new);
        register("treeview", TreeView::new);
    }

    private Loader()
    {
        // Hides default constructor.
    }

    private static void register(final String name, final Function<PaneParams, ? extends Pane> factoryMethod)
    {
        final ResourceLocation key = new ResourceLocation(name);

        if (paneFactories.containsKey(key))
        {
            throw new IllegalArgumentException("Duplicate pane type '" + name + "' when registering Pane class method.");
        }

        paneFactories.put(key, factoryMethod);
    }

    private static Pane createFromPaneParams(final PaneParams params)
    {
        final ResourceLocation paneType = new ResourceLocation(params.getType());

        if (paneFactories.containsKey(paneType))
        {
            return paneFactories.get(paneType).apply(params);
        }

        if (paneFactories.containsKey(new ResourceLocation(paneType.getPath())))
        {
            Log.getLogger().warn("Namespace override for " + paneType.getPath() + " not found. Using default.");
            return paneFactories.get(new ResourceLocation(paneType.getPath())).apply(params);
        }

        Log.getLogger().error("There is no factory method for " + paneType.getPath());
        return null;
    }

    /**
     * Create a pane from its xml parameters.
     *
     * @param params xml parameters.
     * @param parent parent view.
     * @return the new pane.
     */
    public static Pane createFromPaneParams(final PaneParams params, final View parent)
    {
        if ("layout".equalsIgnoreCase(params.getType()))
        {
            final String resource = params.string("source");
            if (resource != null)
            {
                createFromXMLFile(resource, parent);
            }

            return null;
        }

        params.setParentView(parent);
        final Pane pane = createFromPaneParams(params);

        if (pane != null)
        {
            pane.putInside(parent);
            pane.parseChildren(params);
        }

        return pane;
    }

    /**
     * Parse an XML Document into contents for a View.
     *
     * @param doc    xml document.
     * @param parent parent view.
     */
    private static void createFromXML(@Nullable final Document doc, final View parent)
    {
        if (doc == null) return;

        doc.getDocumentElement().normalize();

        final PaneParams root = new PaneParams(doc.getDocumentElement());
        if (parent instanceof Window)
        {
            ((Window) parent).loadParams(root);
        }

        for (final PaneParams child : root.getChildren())
        {
            createFromPaneParams(child, parent);
        }
    }

    /**
     * Parse XML from an InputSource into contents for a View.
     *
     * @param input  xml file.
     */
    private static Document parseXML(final InputSource input)
    {
        try
        {
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(input);
            input.getByteStream().close();

            return doc;
        }
        catch (final ParserConfigurationException | SAXException | IOException exc)
        {
            Log.getLogger().error("Exception when parsing XML.", exc);
        }

        return null;
    }

    /**
     * Parse an XML String into contents for a View.
     *
     * @param xmlString the xml data.
     * @param parent    parent view.
     */
    public static void createFromXML(final String xmlString, final View parent)
    {
        createFromXML(parseXML(new InputSource(new StringReader(xmlString))), parent);
    }

    /**
     * Parse XML contains in a ResourceLocation into contents for a Window.
     *
     * @param filename the xml file.
     * @param parent   parent view.
     */
    public static void createFromXMLFile(final String filename, final View parent)
    {
        createFromXMLFile(new ResourceLocation(filename), parent);
    }

    /**
     * Parse XML contains in a ResourceLocation into contents for a Window.
     *
     * @param resource xml as a {@link ResourceLocation}.
     * @param parent   parent view.
     */
    public static void createFromXMLFile(final ResourceLocation resource, final View parent)
    {
        if (parsedCache.containsKey(resource))
        {
            createFromXML(parsedCache.get(resource).getB(), parent);
        }
        else
        {
            Document doc = parseXML(new InputSource(createInputStream(resource)));
            addToCache(resource, doc);
            createFromXML(doc, parent);
        }
    }

    /**
     * Create an InputStream from a ResourceLocation.
     *
     * @param res ResourceLocation to get an InputStream from.
     * @return the InputStream created from the ResourceLocation.
     */
    private static InputStream createInputStream(final ResourceLocation res)
    {
        try
        {
            InputStream is = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().getResourceManager().getResource(res).getInputStream());
            if (is == null)
            {
                is = DistExecutor
                    .unsafeCallWhenOn(Dist.DEDICATED_SERVER, () -> () -> Loader.class.getResourceAsStream(String.format("/assets/%s/%s", res.getNamespace(), res.getPath())));
            }
            return is;
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().error("IOException Loader.java", e.getCause());
        }
        return null;
    }

    // ------ Cache Handling ------

    public static void addToCache(ResourceLocation loc, Document doc)
    {
        if (parsedCache.size() >= CACHE_CAP)
        {
            parsedCache.replace(
              parsedCache.entrySet().stream()
                .min((a,b) -> Math.min(a.getValue().getA(), b.getValue().getA())).get().getKey(),
              new Tuple<>(1,doc));
        }
        else
        {
            parsedCache.put(loc, new Tuple<>(1, doc));
        }
    }
}
