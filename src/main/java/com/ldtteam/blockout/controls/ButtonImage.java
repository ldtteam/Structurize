package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.Parsers;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * Clickable image.
 */
public class ButtonImage extends Button
{
    /**
     * Default size is a small square button.
     */
    private static final int DEFAULT_BUTTON_SIZE = 20;
    protected ResourceLocation image;
    protected ResourceLocation imageHighlight;
    protected ResourceLocation imageDisabled;
    protected int imageOffsetX = 0;
    protected int imageOffsetY = 0;
    protected int imageWidth = 0;
    protected int imageHeight = 0;
    protected int imageMapWidth = Image.MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;
    protected int imageMapHeight = Image.MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;
    protected int highlightOffsetX = 0;
    protected int highlightOffsetY = 0;
    protected int highlightWidth = 0;
    protected int highlightHeight = 0;
    protected int highlightMapWidth = Image.MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;
    protected int highlightMapHeight = Image.MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;
    protected int disabledOffsetX = 0;
    protected int disabledOffsetY = 0;
    protected int disabledWidth = 0;
    protected int disabledHeight = 0;
    protected int disabledMapWidth = Image.MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;
    protected int disabledMapHeight = Image.MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;

    /**
     * Default constructor. Makes a small square button.
     */
    public ButtonImage()
    {
        super(Alignment.MIDDLE, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_SHADOW, DEFAULT_TEXT_WRAP);

        width = DEFAULT_BUTTON_SIZE;
        height = DEFAULT_BUTTON_SIZE;
        recalcTextRendering();
    }

    /**
     * Constructor called by the xml loader.
     *
     * @param params PaneParams provided in the xml.
     */
    public ButtonImage(final PaneParams params)
    {
        super(params, Alignment.MIDDLE, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_SHADOW, DEFAULT_TEXT_WRAP);

        loadImageInfo(params);
        loadHighlightInfo(params);
        loadDisabledInfo(params);

        loadTextInfo(params);
    }

    /**
     * Loads the parameters for the normal image.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadImageInfo(final PaneParams params)
    {
        image = params.getResource("source", this::loadImageDimensions);

        params.applyShorthand("imageoffset", Parsers.INT, 2, a -> {
            imageOffsetX = a.get(0);
            imageOffsetY = a.get(1);
        });

        params.applyShorthand("imagesize", Parsers.INT, 2, a -> {
            imageWidth = a.get(0);
            imageHeight = a.get(1);
        });
    }

    /**
     * Loads the parameters for the hover image.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadHighlightInfo(final PaneParams params)
    {
        imageHighlight = params.getResource("highlight", this::loadImageHighlightDimensions);

        params.applyShorthand("highlightoffset", Parsers.INT, 2, a -> {
            highlightOffsetX = a.get(0);
            highlightOffsetY = a.get(1);
        });

       params.applyShorthand("highlightsize", Parsers.INT, 2, a -> {
            highlightWidth = a.get(0);
            highlightHeight = a.get(1);
        });
    }

    /**
     * Loads the parameters for the disabled image.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadDisabledInfo(final PaneParams params)
    {
        imageDisabled = params.getResource("disabled", this::loadImageDisabledDimensions);

        params.applyShorthand("disabledoffset", Parsers.INT, 2, a -> {
            disabledOffsetX = a.get(0);
            disabledOffsetY = a.get(1);
        });

       params.applyShorthand("disabledsize", Parsers.INT, 2, a -> {
            disabledWidth = a.get(0);
            disabledHeight = a.get(1);
        });
    }

    /**
     * Loads the parameters for the button textContent.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadTextInfo(final PaneParams params)
    {
        textColor = params.getColor("textcolor", textColor);
        // match textColor by default
        textHoverColor = params.getColor("texthovercolor", textColor);
        // match textColor by default
        textDisabledColor = params.getColor("textdisabledcolor", textColor);

        params.applyShorthand("textoffset", Parsers.INT, 2, a -> {
            textOffsetX = a.get(0);
            textOffsetY = a.get(1);
        });

        params.applyShorthand("textbox", Parsers.INT, 2, a -> {
            textWidth = a.get(0);
            textHeight = a.get(1);
        });

        recalcTextRendering();
    }

    /**
     * Uses {@link Image#getImageDimensions(ResourceLocation)} to determine the dimensions of image texture.
     */
    private void loadImageDimensions(final ResourceLocation rl)
    {
        final Tuple<Integer, Integer> dimensions = Image.getImageDimensions(rl);
        imageMapWidth = dimensions.getA();
        imageMapHeight = dimensions.getB();
    }

    /**
     * Uses {@link Image#getImageDimensions(ResourceLocation)} to determine the dimensions of hover image texture.
     */
    private void loadImageHighlightDimensions(final ResourceLocation rl)
    {
        final Tuple<Integer, Integer> dimensions = Image.getImageDimensions(rl);
        highlightMapWidth = dimensions.getA();
        highlightMapHeight = dimensions.getB();
    }

    /**
     * Uses {@link Image#getImageDimensions(ResourceLocation)} to determine the dimensions of disabled image texture.
     */
    private void loadImageDisabledDimensions(final ResourceLocation rl)
    {
        final Tuple<Integer, Integer> dimensions = Image.getImageDimensions(rl);
        disabledMapWidth = dimensions.getA();
        disabledMapHeight = dimensions.getB();
    }

    /**
     * Set the default image.
     *
     * @param source String path.
     */
    public void setImage(final String source)
    {
        setImage(source, 0, 0, 0, 0);
    }

    /**
     * Set the default image.
     *
     * @param source  String path.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImage(final String source, final int offsetX, final int offsetY, final int w, final int h)
    {
        setImage(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    /**
     * Set the default image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param width       image width.
     * @param height       image height.
     */
    public void setImage(final ResourceLocation loc, final int offsetX, final int offsetY, final int width, final int height)
    {
        image = loc;
        imageOffsetX = offsetX;
        imageOffsetY = offsetY;
        imageWidth = width;
        imageHeight = height;

        loadImageDimensions(loc);
    }

    /**
     * Set the default image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImage(final ResourceLocation loc)
    {
        setImage(loc, 0, 0, 0, 0);
    }

    /**
     * Set the hover image.
     *
     * @param source String path.
     */
    public void setImageHighlight(final String source)
    {
        setImageHighlight(source, 0, 0, 0, 0);
    }

    /**
     * Set the hover image.
     *
     * @param source  String path.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImageHighlight(final String source, final int offsetX, final int offsetY, final int w, final int h)
    {
        setImageHighlight(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    /**
     * Set the hover image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImageHighlight(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        imageHighlight = loc;
        highlightOffsetX = offsetX;
        highlightOffsetY = offsetY;
        highlightWidth = w;
        highlightHeight = h;

        loadImageHighlightDimensions(loc);
    }

    /**
     * Set the hover image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageHighlight(final ResourceLocation loc)
    {
        setImageHighlight(loc, 0, 0, 0, 0);
    }

    /**
     * Set the disabled image.
     *
     * @param source String path.
     */
    public void setImageDisabled(final String source)
    {
        setImageHighlight(source, 0, 0, 0, 0);
    }

    /**
     * Set the disabled image.
     *
     * @param source  String path.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImageDisabled(final String source, final int offsetX, final int offsetY, final int w, final int h)
    {
        setImageHighlight(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    /**
     * Set the disabled image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageDisabled(final ResourceLocation loc)
    {
        setImageHighlight(loc, 0, 0, 0, 0);
    }

    /**
     * Set the disabled image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImageDisabled(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        imageDisabled = loc;
        disabledOffsetX = offsetX;
        disabledOffsetY = offsetY;
        disabledWidth = w;
        disabledHeight = h;

        loadImageDisabledDimensions(loc);
    }

    /**
     * Draw the button.
     * Decide what image to use, and possibly draw textContent.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        ResourceLocation bind = image;
        int u = imageOffsetX;
        int v = imageOffsetY;
        int w = imageWidth;
        int h = imageHeight;
        int mapWidth = imageMapWidth;
        int mapHeight = imageMapHeight;

        if (!enabled)
        {
            if (imageDisabled != null)
            {
                bind = imageDisabled;
                u = disabledOffsetX;
                v = disabledOffsetY;
                w = disabledWidth;
                h = disabledHeight;
                mapWidth = disabledMapWidth;
                mapHeight = disabledMapHeight;
            }
            else
            {
                return;
            }
        }
        else if (wasCursorInPane && imageHighlight != null)
        {
            bind = imageHighlight;
            u = highlightOffsetX;
            v = highlightOffsetY;
            w = highlightWidth;
            h = highlightHeight;
            mapWidth = highlightMapWidth;
            mapHeight = highlightMapHeight;
        }

        if (w == 0 || w > getWidth())
        {
            w = getWidth();
        }
        if (h == 0 || h > getHeight())
        {
            h = getHeight();
        }

        mc.getTextureManager().bind(bind);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Draw
        if (enabled || imageDisabled != null)
        {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            blit(ms, x, y, u, v, w, h, mapWidth, mapHeight);
        }
        else
        {
            RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
            blit(ms, x, y, u, v, w, h, mapWidth, mapHeight);
        }

        RenderSystem.disableBlend();

        super.drawSelf(ms, mx, my);
    }

    @Override
    public void setSize(final int w, final int h)
    {
        final int newTextWidth = (int) ((double) (textWidth * w) / width);
        final int newTextHeight = (int) ((double) (textHeight * h) / height);

        super.setSize(w, h);

        textWidth = newTextWidth;
        textHeight = newTextHeight;
        recalcTextRendering();
    }

    /**
     * Sets text offset for rendering, relative to element start.
     * Is automatically shrinked to element width and height.
     *
     * @param textOffsetX left offset
     * @param textOffsetY top offset
     */
    public void setTextOffset(final int textOffsetX, final int textOffsetY)
    {
        this.textOffsetX = MathHelper.clamp(textOffsetX, 0, width);
        this.textOffsetY = MathHelper.clamp(textOffsetY, 0, height);
    }

    /**
     * Sets text rendering box.
     * Is automatically shrinked to element width and height minus text offsets.
     *
     * @param textWidth  horizontal size
     * @param textHeight vertical size
     */
    public void setTextRenderBox(final int textWidth, final int textHeight)
    {
        this.textWidth = MathHelper.clamp(textWidth, 0, width - textOffsetX);
        this.textHeight = MathHelper.clamp(textHeight, 0, height - textOffsetY);
        recalcTextRendering();
    }
}
