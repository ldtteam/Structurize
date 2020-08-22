package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Matrix4f;
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
        final String source = params.getStringAttribute("source", null);
        if (source != null)
        {
            resourceLocation = new ResourceLocation(source);
            loadMapDimensions();
        }

        PaneParams.SizePair size = params.getSizePairAttribute("imageoffset", null, null);
        if (size != null)
        {
            u = size.getX();
            v = size.getY();
        }

        size = params.getSizePairAttribute("imagesize", null, null);
        if (size != null)
        {
            imageWidth = size.getX();
            imageHeight = size.getY();
        }

        autoscale = params.getBooleanAttribute("autoscale", true);
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
        imageWidth = w;
        imageHeight = h;

        loadMapDimensions();
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

    /**
     * Draw this image on the GUI.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final MatrixStack ms, final int mx, final int my)
    {
        this.mc.getTextureManager().bindTexture(resourceLocation);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (this.customSized)
        {
            // /Draw
            drawScaledCustomSizeModalRect(ms.getLast().getMatrix(),
              x,
              y,
              getWidth(),
              getHeight(),
              u,
              v,
              imageWidth != 0 ? imageWidth : getWidth(),
              imageHeight != 0 ? imageHeight : getHeight(),
              fileWidth,
              fileHeight);
        }
        else
        {
            blit(ms, x, y, u, v, imageWidth != 0 ? imageWidth : getWidth(), imageHeight != 0 ? imageHeight : getHeight());
        }

        RenderSystem.disableBlend();
    }

    public static void drawScaledCustomSizeModalRect(
      final Matrix4f ms,
      final int x,
      final int y,
      final float u,
      final float v,
      final int uWidth,
      final int vHeight,
      final int width,
      final int height,
      final float tileWidth,
      final float tileHeight)
    {
        final float f = 1.0F / tileWidth;
        final float f1 = 1.0F / tileHeight;
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(ms, x, (y + height), 0.0f).tex((u * f), ((v + (float) vHeight) * f1)).endVertex();
        bufferbuilder.pos(ms, (x + width), (y + height), 0.0f).tex(((u + (float) uWidth) * f), ((v + (float) vHeight) * f1)).endVertex();
        bufferbuilder.pos(ms, (x + width), y, 0.0f).tex(((u + (float) uWidth) * f), (v * f1)).endVertex();
        bufferbuilder.pos(ms, x, y, 0.0f).tex((u * f), (v * f1)).endVertex();
        tessellator.draw();
    }
}
