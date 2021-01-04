package com.ldtteam.blockout.properties;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

public class TextureRepeatable extends Texture
{
    protected int uRepeat = 0;
    protected int vRepeat = 0;
    protected int repeatWidth = 0;
    protected int repeatHeight = 0;

    public TextureRepeatable(final PaneParams params)
    {
        this(params, "texture");
    }

    public TextureRepeatable(final PaneParams p, final String prefix)
    {
        super(p, prefix);

        p.shorthand("repeatoffset", Parsers.INT, 2, a -> {
            uRepeat = a.get(0);
            vRepeat = a.get(1);
        });

        p.shorthand("repeatsize", Parsers.INT, 2, a -> {
            repeatWidth = a.get(0);
            repeatHeight = a.get(1);
        });
    }

    public void setDimensions(
      final int u, final int v,
      final int w, final int h,
      final int uRepeat, final int vRepeat,
      final int repeatWidth, final int repeatHeight)
    {
        super.setDimensions(u, v, w, h);
        this.uRepeat = uRepeat;
        this.vRepeat = vRepeat;
        this.repeatWidth = repeatWidth;
        this.repeatHeight = repeatHeight;
    }

    @Override
    protected void render(final MatrixStack ms, final Pane pane)
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Pane.blitRepeatable(ms,
          pane.getX(), pane.getY(),
          pane.getWidth(), pane.getHeight(),
          u, v,
          width, height,
          fileWidth, fileHeight,
          uRepeat, vRepeat,
          repeatWidth, repeatHeight);

        RenderSystem.disableBlend();
    }
}
