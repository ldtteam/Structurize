package com.ldtteam.blockout.properties;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ldtteam.blockout.Log.getLogger;

public class Texture extends PropertyGroup
{
    public static final Pattern IMAGE_SOURCE_PATTERN = Pattern.compile("(\\S+:)?(textures/gui/)?(\\S+(?=\\.png)|\\S+)(\\.png)?");
    public static final int     MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE = 256;

    protected ResourceLocation resourceLocation;

    protected int u = 0;
    protected int v = 0;
    protected int width = 0;
    protected int height = 0;
    protected int fileWidth = MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;
    protected int fileHeight = MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;

    protected boolean stretch = true;

    public Texture(final ResourceLocation loc, final int x, final int y, final int w, final int h)
    {
        setImage(loc, x, y, w, h);
    }

    public Texture(ResourceLocation loc)
    {
        this(loc, 0, 0, 0, 0);
    }

    public Texture(PaneParams params)
    {
        this(params, "texture");
    }

    public Texture(PaneParams p, String prefix)
    {
        super(p, prefix);
        String source = p.string(prefix.equals("texture") ? "source" : prefix+"source");
        if (source != null)
        {
            Matcher m = IMAGE_SOURCE_PATTERN.matcher(source);
            if (m.find())
            {
                if (m.group(2) == null || m.group(4) == null)
                {
                    String namespace = m.group(1) != null ? m.group(1) : "";
                    source = namespace + "textures/gui/" + m.group(3) + ".png";
                }
            }

            resourceLocation = new ResourceLocation(source);
            loadMapDimensions();
        }

        p.shorthand(prefix+"offset", Parsers.INT, 2, a -> {
            u = a.get(0);
            v = a.get(1);
        });

        p.shorthand(prefix+"size", Parsers.INT, 2, a -> {
            width = a.get(0);
            height = a.get(1);
        });

        stretch = p.bool("autoscale", true);
    }

    @Override
    public void applyDefaults()
    {
        u = 0;
        v = 0;
        width = 0;
        height = 0;
        stretch = true;
    }

    private void loadMapDimensions()
    {
        final Tuple<Integer, Integer> dimensions = getImageDimensions(resourceLocation);
        fileWidth = dimensions.getA();
        fileHeight = dimensions.getB();

        if (width == 0) width = fileWidth;
        if (height == 0) height = fileHeight;
    }

    /**
     * Load and image from a {@link ResourceLocation} and return a {@link Tuple} containing its width and height.
     *
     * @param resourceLocation The {@link ResourceLocation} pointing to the image.
     * @return Width and height.
     */
    public static Tuple<Integer, Integer> getImageDimensions(final ResourceLocation resourceLocation)
    {
        final int pos = resourceLocation.getPath().lastIndexOf(".");

        if (pos == -1)
        {
            throw new IllegalStateException("No extension for file: " + resourceLocation.toString());
        }

        final String suffix = resourceLocation.getPath().substring(pos + 1);
        final Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(suffix);

        while (it.hasNext())
        {
            final ImageReader reader = it.next();
            try (ImageInputStream stream = ImageIO
                                             .createImageInputStream(Minecraft.getInstance().getResourceManager().getResource(resourceLocation).getInputStream()))
            {
                reader.setInput(stream);

                return new Tuple<>(reader.getWidth(reader.getMinIndex()), reader.getHeight(reader.getMinIndex()));
            }
            catch (final IOException e)
            {
                getLogger().warn(e);
            }
            finally
            {
                reader.dispose();
            }
        }

        return new Tuple<>(0, 0);
    }

    /**
     * Set the image.
     *
     * @param source String path.
     */
    public void setImage(final String source)
    {
        setImage(source, 0, 0, 0, 0);
    }

    /**
     * Set the image.
     *
     * @param source  String path.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImage(final String source, final int offsetX, final int offsetY, final int w, final int h)
    {
        setImage((source != null) ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    /**
     * Set the image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImage(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        resourceLocation = loc;
        u = offsetX;
        v = offsetY;
        width = w;
        height = h;

        loadMapDimensions();
    }

    /**
     * Set the image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImage(final ResourceLocation loc)
    {
        setImage(loc, 0, 0, 0, 0);
    }

    public void setDimensions(final int u, final int v, final int w, final int h)
    {
        setImage(this.resourceLocation, u, v, w, h);
    }

    public boolean hasSource()
    {
        return resourceLocation != null;
    }

    @Override
    public void draw(final MatrixStack ms, final Pane pane, final double mx, final double my)
    {
        if (!hasSource()) return;

        mc.getTextureManager().bindTexture(resourceLocation);
        applyColor(1.0F, 1.0F, 1.0F, 1.0F);
        render(ms, pane);
    }

    /**
     * Draws the contents of the property group on the pane
     * @param ms the rendering stack
     * @param pane the pane this is attached to
     * @param dim if the texture should be made dimmer
     */
    public void draw(final MatrixStack ms, final Pane pane, final boolean dim)
    {
        if (!hasSource()) return;

        if (!dim)
        {
            draw(ms, pane, 0, 0);
            return;
        }

        mc.getTextureManager().bindTexture(resourceLocation);
        applyColor(0.5F, 0.5F, 0.5F, 1.0F);
        render(ms, pane);
    }

    protected void render(final MatrixStack ms, final Pane pane)
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        AbstractGui.blit(ms,
          pane.getX(), pane.getY(),
          stretch ? pane.getWidth() : width,
          stretch ? pane.getHeight() : height,
          u, v,
          width == 0 ? fileWidth : Math.min(width, fileWidth - u),
          height == 0 ? fileHeight : Math.min(height, fileHeight - v),
          fileWidth,
          fileHeight);

        RenderSystem.disableBlend();
    }

    /**
     * A convenience method for the "depracated" color4f function
     * @param r a 0-1 red value
     * @param g a 0-1 green value
     * @param b a 0-1 blue value
     * @param a a 0-1 alpha value
     */
    public static void applyColor(float r, float g, float b, float a)
    {
        RenderSystem.color4f(r, g, b, a);
    }
}
