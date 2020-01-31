package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

public class ZoomView extends View
{
    private double scaleFactor = 2;

    /**
     * Required default constructor.
     */
    public ZoomView()
    {
        super();
    }

    /**
     * Constructs a View from PaneParams.
     *
     * @param params Params for the Pane.
     */
    public ZoomView(final PaneParams params)
    {
        super(params);
    }

    @Override
    public int getWidth()
    {
        return (int) (super.getWidth() * scaleFactor);
    }

    @Override
    public int getHeight()
    {
        return (int) (super.getHeight() * scaleFactor);
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        // Translate the scroll
        RenderSystem.pushMatrix();
        RenderSystem.translated((width), height/2, 0);
        RenderSystem.scaled(1.25, 1.25, 1.25);
        RenderSystem.translated(-(width), -height/2, 0);

        super.drawSelf((int) ( mx * scaleFactor ), (int) ( my * scaleFactor ) );

        RenderSystem.popMatrix();
    }

    public void zoom(final int scaleFactor)
    {
        this.scaleFactor += scaleFactor;
    }
}
