package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import org.lwjgl.opengl.GL11;

/**
 * Image element with repeatable middle part.
 */
public class ImageRepeatable extends Pane
{
    public static final int MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE = 256;

    protected ResourceLocation resourceLocation;
    protected int u = 0;
    protected int v = 0;
    protected int uWidth = 0;
    protected int vHeight = 0;
    protected int uRepeat = 0;
    protected int vRepeat = 0;
    protected int repeatWidth = 0;
    protected int repeatHeight = 0;
    protected int fileWidth = MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;
    protected int fileHeight = MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;

    /**
     * Default Constructor.
     */
    public ImageRepeatable()
    {
        super();
    }

    /**
     * Constructor used by the xml loader.
     *
     * @param params PaneParams loaded from the xml.
     */
    public ImageRepeatable(final PaneParams params)
    {
        super(params);
        final String source = params.getStringAttribute("source", null);
        if (source != null)
        {
            resourceLocation = new ResourceLocation(source);
            loadMapDimensions();
        }

        PaneParams.SizePair size = params.getSizePairAttribute("textureoffset", null, null);
        if (size != null)
        {
            u = size.getX();
            v = size.getY();
        }

        size = params.getSizePairAttribute("texturesize", null, null);
        if (size != null)
        {
            uWidth = size.getX();
            vHeight = size.getY();
        }

        size = params.getSizePairAttribute("repeatoffset", null, null);
        if (size != null)
        {
            uRepeat = size.getX();
            vRepeat = size.getY();
        }

        size = params.getSizePairAttribute("repeatsize", null, null);
        if (size != null)
        {
            repeatWidth = size.getX();
            repeatHeight = size.getY();
        }
    }

    private void loadMapDimensions()
    {
        final Tuple<Integer, Integer> dimensions = Image.getImageDimensions(resourceLocation);
        fileWidth = dimensions.getA();
        fileHeight = dimensions.getB();
    }

    /**
     * Set the image.
     *
     * @param source String path.
     */
    public void setImageLoc(final String source)
    {
        setImageLoc(source != null ? new ResourceLocation(source) : null);
    }

    /**
     * Set the image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageLoc(final ResourceLocation loc)
    {
        resourceLocation = loc;
    }

    /**
     * Set the texture box sizes.
     *
     * @param u            texture start offset [texels]
     * @param v            texture start offset [texels]
     * @param uWidth       texture rendering box [texels]
     * @param vHeight      texture rendering box [texels]
     * @param uRepeat      offset relative to u, v [texels], smaller than uWidth
     * @param vRepeat      offset relative to u, v [texels], smaller than vHeight
     * @param repeatWidth  size of repeatable box in texture [texels], smaller than or equal uWidth - uRepeat
     * @param repeatHeight size of repeatable box in texture [texels], smaller than or equal vHeight - vRepeat
     */
    public void setImageSize(final int u, final int v,
        final int uWidth, final int vHeight,
        final int uRepeat, final int vRepeat,
        final int repeatWidth, final int repeatHeight)
    {
        this.u = u;
        this.v = v;
        this.uWidth = uWidth;
        this.vHeight = vHeight;
        this.uRepeat = uRepeat;
        this.vRepeat = vRepeat;
        this.repeatWidth = repeatWidth;
        this.repeatHeight = repeatHeight;
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

        blitRepeatable(ms, x, y, width, height, u, v, uWidth, vHeight, fileWidth, fileHeight, uRepeat, vRepeat, repeatWidth, repeatHeight);

        RenderSystem.disableBlend();
    }
}
