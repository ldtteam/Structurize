package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.properties.TextureRepeatable;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;

/**
 * Image element with repeatable middle part.
 */
public class ImageRepeatable extends Pane
{
    protected TextureRepeatable texture;


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

        texture = new TextureRepeatable(params);
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
        this.texture.setImage(loc);
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
        this.texture.setDimensions(u, v, uWidth, vHeight, uRepeat, vRepeat, repeatWidth, repeatHeight);
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
        texture.draw(ms, this, false);
    }
}
