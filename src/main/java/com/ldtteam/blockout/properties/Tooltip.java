package com.ldtteam.blockout.properties;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.views.Window;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.IFormattableTextComponent;

import java.util.List;

import static com.ldtteam.blockout.Pane.fillGradient;

public class Tooltip extends RichText
{
    private static final int CURSOR_BOX_SIZE = 12;
    private static final int Z_OFFSET = 400;
    private static final int BACKGROUND_COLOR = -267386864;
    private static final int BORDER_COLOR_A = 1347420415;
    private static final int BORDER_COLOR_B = 1344798847;

    public Tooltip(List<IFormattableTextComponent> text, int color)
    {
        super(Alignment.TOP_LEFT, color, color, color, false, true);

        this.text = text;
    }

    public Tooltip(String text, int color)
    {
        super(Alignment.TOP_LEFT, color, color, color, false, true);

        this.text = Parsers.MULTILINE.apply(text);
    }

    @Override
    public void applyDefaults()
    {
        super.applyDefaults();

        x = 4;
        y = 4;
        width = 208;
        color = 0xFFFFFF;
        linespace = 1;
    }

    @Override
    public void draw(final MatrixStack ms, final Pane pane, final double mx, final double my)
    {
        Window window = pane.getWindow();
        int width = renderedWidth + 8;
        int height = renderedHeight + 8;

        if (!preparedText.isEmpty())
        {
            x = (int) mx + CURSOR_BOX_SIZE - 4;
            y = (int) my - CURSOR_BOX_SIZE - 4;

            if (x + width + 3 > window.getScreen().width)
            {
                x = window.getScreen().width - width - 4;
            }

            if (y + height + 3 > window.getScreen().height)
            {
                y = window.getScreen().height - height - 4;
            }

            // vanilla Screen#renderTooltip(MatrixStack, List<? extends IReorderingProcessor>, int, int, FontRenderer)
            ms.push();
            ms.translate(x, y, Z_OFFSET);

            final BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            final Matrix4f matrix4f = ms.getLast().getMatrix();

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

            fillGradient(matrix4f, bufferbuilder, 1, 0, width - 1, height, 0, BACKGROUND_COLOR, BACKGROUND_COLOR);
            fillGradient(matrix4f, bufferbuilder, 0, 1, 1, height - 1, 0, BACKGROUND_COLOR, BACKGROUND_COLOR);
            fillGradient(matrix4f, bufferbuilder, width - 1, 1, width, height - 1, 0, BACKGROUND_COLOR, BACKGROUND_COLOR);

            fillGradient(matrix4f, bufferbuilder, 1, 2, 2, height - 2, 0, BORDER_COLOR_A, BORDER_COLOR_B);
            fillGradient(matrix4f, bufferbuilder, width - 2, 2, width - 1, height - 2, 0, BORDER_COLOR_A, BORDER_COLOR_B);
            fillGradient(matrix4f, bufferbuilder, 1, 1, width - 1, 2, 0, BORDER_COLOR_A, BORDER_COLOR_A);
            fillGradient(matrix4f, bufferbuilder, 1, height - 2, width - 1, height - 1, 0, BORDER_COLOR_B, BORDER_COLOR_B);

            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425);
            bufferbuilder.finishDrawing();
            WorldVertexBufferUploader.draw(bufferbuilder);
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();

            ms.translate(-x, -y, 0.0d);
            super.draw(ms, pane, mx, my);
            ms.pop();
        }
    }
}
