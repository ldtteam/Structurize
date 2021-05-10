package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.Parsers;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import org.lwjgl.opengl.GL11;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;
import static com.ldtteam.blockout.Log.getLogger;

/**
 * Simple image element.
 */
public class Image extends Pane
{
    public static final int MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE = 256;

    protected ResourceLocation resourceLocation;
    protected int u = 0;
    protected int v = 0;
    protected int imageWidth = 0;
    protected int imageHeight = 0;
    protected int fileWidth = MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;
    protected int fileHeight = MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;
    protected boolean customSized = true;
    protected boolean autoscale = true;

    /**
     * Default Constructor.
     */
    public Image()
    {
        super();
    }

    /**
     * Constructor used by the xml loader.
     *
     * @param params PaneParams loaded from the xml.
     */
    public Image(final PaneParams params)
    {
        super(params);
        resourceLocation = params.getTexture("source", this::loadMapDimensions);

        params.applyShorthand("imageoffset", Parsers.INT, 2, a -> {
            u = a.get(0);
            v = a.get(1);
        });

        params.applyShorthand("imagesize", Parsers.INT, 2, a -> {
            imageWidth = a.get(0);
            imageHeight = a.get(1);
        });

        autoscale = params.getBoolean("autoscale", true);
    }

    private void loadMapDimensions(final ResourceLocation rl)
    {
        final Tuple<Integer, Integer> dimensions = getImageDimensions(rl);
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
        imageWidth = w;
        imageHeight = h;

        loadMapDimensions(loc);
    }

    /**
     * Set the image.
     *
     * @param loc         ResourceLocation for the image.
     * @param offsetX     image x offset.
     * @param offsetY     image y offset.
     * @param w           image width.
     * @param h           image height.
     * @param customSized is it custom sized.
     */
    public void setImage(final ResourceLocation loc,
        final int offsetX,
        final int offsetY,
        final int w,
        final int h,
        final boolean customSized)
    {
        this.customSized = customSized;
        resourceLocation = loc;
        u = offsetX;
        v = offsetY;
        imageWidth = w;
        imageHeight = h;

        loadMapDimensions(loc);
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

    /**
     * Draw this image on the GUI.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        this.mc.getTextureManager().bindTexture(resourceLocation);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (this.customSized)
        {
            blit(ms,
                x,
                y,
                getWidth(),
                getHeight(),
                u,
                v,
                imageWidth != 0 ? imageWidth : fileWidth,
                imageHeight != 0 ? imageHeight : fileHeight,
                fileWidth,
                fileHeight);
        }
        else
        {
            blit(ms, x, y, u, v, imageWidth != 0 ? imageWidth : getWidth(), imageHeight != 0 ? imageHeight : getHeight());
        }

        RenderSystem.disableBlend();
    }
}
