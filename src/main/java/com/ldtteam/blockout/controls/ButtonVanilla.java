package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * BlockOut implementation of a Vanilla Button.
 */
public class ButtonVanilla extends Button
{
    /**
     * Texture map that contains the button texture.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/widgets.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int TEXTURE_INNER_U_OFFSET = 2;
    private static final int TEXTURE_INNER_V_OFFSET = 2;
    private static final int TEXTURE_INNER_U_WIDTH = 196;
    private static final int TEXTURE_INNER_V_HEIGHT = 15;

    private static final int DEFAULT_BUTTON_WIDTH = 200;
    private static final int DEFAULT_BUTTON_HEIGHT = 20;

    private static final int ENABLED_COLOR = 0xE0E0E0;
    private static final int HOVER_COLOR = 0xFFFFA0;
    private static final int DISABLED_COLOR = 0xA0A0A0;

    private static final int ENABLED_TEXTURE_V = 66;
    private static final int HOVER_TEXTURE_V = 86;
    private static final int DISABLED_TEXTURE_V = 46;

    /**
     * Default constructor.
     */
    public ButtonVanilla()
    {
        super(Alignment.MIDDLE, ENABLED_COLOR, HOVER_COLOR, DISABLED_COLOR, true, true);

        width = DEFAULT_BUTTON_WIDTH;
        height = DEFAULT_BUTTON_HEIGHT;
        recalcTextBox();
    }

    /**
     * Constructor called when loaded from xml.
     *
     * @param params PaneParams from xml file.
     */
    public ButtonVanilla(final PaneParams params)
    {
        super(params, Alignment.MIDDLE, ENABLED_COLOR, HOVER_COLOR, DISABLED_COLOR, true, true);

        if (width == 0)
        {
            width = DEFAULT_BUTTON_WIDTH;
        }
        if (height == 0)
        {
            height = DEFAULT_BUTTON_HEIGHT;
        }
        recalcTextBox();
    }

    /**
     * Draws a vanilla button.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        mc.getTextureManager().bindTexture(TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        final boolean isMouseOver = isPointInPane(mx, my);

        final int u = 0;
        final int v = enabled ? (isMouseOver ? HOVER_TEXTURE_V : ENABLED_TEXTURE_V) : DISABLED_TEXTURE_V;

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (width == DEFAULT_BUTTON_WIDTH && height == DEFAULT_BUTTON_HEIGHT)
        {
            // Full size button
            blit(ms, x, y, u, v, width, height);
        }
        else
        {
            blitRepeatable(ms,
              x, y,
              width, height,
              u, v,
              DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT,
              TEXTURE_SIZE, TEXTURE_SIZE,
              TEXTURE_INNER_U_OFFSET, TEXTURE_INNER_V_OFFSET,
              TEXTURE_INNER_U_WIDTH, TEXTURE_INNER_V_HEIGHT);
        }

        RenderSystem.disableBlend();

        super.drawSelf(ms, mx, my);
    }

    private void recalcTextBox()
    {
        textOffsetX = TEXTURE_INNER_U_OFFSET;
        textOffsetY = TEXTURE_INNER_V_OFFSET;
        textWidth = width - (DEFAULT_BUTTON_WIDTH - TEXTURE_INNER_U_WIDTH);
        textHeight = height - (DEFAULT_BUTTON_HEIGHT - TEXTURE_INNER_V_HEIGHT);
        recalcTextRendering();
    }

    @Override
    public void setSize(final int w, final int h)
    {
        super.setSize(w, h);
        recalcTextBox();
    }
}
