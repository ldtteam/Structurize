package com.ldtteam.blockout.controls;

import java.util.List;
import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
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

    protected double textScale = 1.0;
    protected Alignment textAlignment = Alignment.MIDDLE;
    protected boolean shadow = true;
    private List<IReorderingProcessor> preparedLabel;
    private int labelWidth;
    private int labelHeight;

    /**
     * Default constructor.
     */
    public ButtonVanilla()
    {
        super();
        width = DEFAULT_BUTTON_WIDTH;
        height = DEFAULT_BUTTON_HEIGHT;
    }

    /**
     * Constructor called when loaded from xml.
     *
     * @param params PaneParams from xml file.
     */
    public ButtonVanilla(final PaneParams params)
    {
        super(params);
        if (width == 0)
        {
            width = DEFAULT_BUTTON_WIDTH;
        }
        if (height == 0)
        {
            height = DEFAULT_BUTTON_HEIGHT;
        }

        textScale = params.getDoubleAttribute("textscale", textScale);
        textAlignment = params.getEnumAttribute("textalign", Alignment.class, textAlignment);
        setLabel(getLabelNew()); // recalc label
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

        if (preparedLabel != null)
        {
            final int textColor = enabled ? (isMouseOver ? HOVER_COLOR : ENABLED_COLOR) : DISABLED_COLOR;
            int offsetX = TEXTURE_INNER_U_OFFSET;
            int offsetY = TEXTURE_INNER_V_OFFSET;

            if (textAlignment.isRightAligned())
            {
                offsetX += (getWidth() - (DEFAULT_BUTTON_WIDTH - TEXTURE_INNER_U_WIDTH) - labelWidth);
            }
            else if (textAlignment.isHorizontalCentered())
            {
                offsetX += (getWidth() - (DEFAULT_BUTTON_WIDTH - TEXTURE_INNER_U_WIDTH) - labelWidth) / 2;
            }

            if (textAlignment.isBottomAligned())
            {
                offsetY += (getHeight() - (DEFAULT_BUTTON_HEIGHT - TEXTURE_INNER_V_HEIGHT) - labelHeight);
            }
            else if (textAlignment.isVerticalCentered())
            {
                offsetY += (getHeight() - (DEFAULT_BUTTON_HEIGHT - TEXTURE_INNER_V_HEIGHT) - labelHeight) / 2;
            }
            // + textScale is to negate last pixel of vanilla font rendering
            offsetY += textScale;

            ms.push();
            ms.translate(getX() + offsetX, getY() + offsetY, 1.0f);
            ms.scale((float) textScale, (float) textScale, 1.0f);
            int yShift = 0;
            for (final IReorderingProcessor row : preparedLabel)
            {
                drawString(ms, row, 0, yShift, textColor, shadow);
                yShift += 9;
            }
            ms.pop();
        }

        RenderSystem.disableBlend();
    }

    @Override
    public void setLabel(final IFormattableTextComponent label)
    {
        super.setLabel(label);

        if (textScale <= 0.0d || label == null || label.getString().isEmpty())
        {
            preparedLabel = null;
            return;
        }

        final int maxWidth = (int) ((width - (DEFAULT_BUTTON_WIDTH - TEXTURE_INNER_U_WIDTH)) / textScale);
        final int maxHeight = (int) ((height - (DEFAULT_BUTTON_HEIGHT - TEXTURE_INNER_V_HEIGHT)) / textScale);

        preparedLabel = mc.fontRenderer.trimStringToWidth(label, maxWidth);
        preparedLabel = preparedLabel.subList(0, maxHeight / this.mc.fontRenderer.FONT_HEIGHT);
        labelWidth = (int) (preparedLabel.stream().mapToInt(mc.fontRenderer::func_243245_a).max().orElse(maxWidth) * textScale);
        labelHeight = (int) (Math.min(preparedLabel.size() * this.mc.fontRenderer.FONT_HEIGHT, maxHeight) * textScale);
    }
}
