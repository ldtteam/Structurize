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

import static com.ldtteam.blockout.Log.getLogger;

public class Texture extends PropertyGroup
{
    public static final int MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE = 256;

    protected ResourceLocation resourceLocation;

    protected int u = 0;
    protected int v = 0;
    protected int width = 0;
    protected int height = 0;
    protected int fileWidth = MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;
    protected int fileHeight = MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;

    protected boolean keepAspect = true;

    public Texture(PaneParams params, String prefix)
    {
        super(params, prefix);
    }

    public Texture(PaneParams params)
    {
        super(params, "texture");
    }

    @Override
    public void apply(final PaneParams p)
    {
        final String source = p.string(prefix.equals("texture") ? "source" : prefix+"source");
        if (source != null)
        {
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

        keepAspect = !p.bool("autoscale", true);
    }

    private void loadMapDimensions()
    {
        final Tuple<Integer, Integer> dimensions = getImageDimensions(resourceLocation);
        fileWidth = dimensions.getA();
        fileHeight = dimensions.getB();
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


    @Override
    public void draw(final MatrixStack ms, final Pane pane, final double mx, final double my)
    {
        mc.getTextureManager().bindTexture(resourceLocation);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        AbstractGui.blit(ms,
          pane.getX(),
          pane.getY(),
          Math.min(width, pane.getWidth()),
          Math.min(height, pane.getHeight()),
          u, v,
          width == 0 ? fileWidth : width,
          height == 0 ? fileHeight : height,
          fileWidth,
          fileHeight);

        RenderSystem.disableBlend();
    }
}
