package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

public class DragView extends View
{
    private double scrollX;
    private double scrollY;

    protected int contentHeight = 0;
    protected int contentWidth = 0;

    /**
     * Required default constructor.
     */
    public DragView()
    {
        super();
    }

    /**
     * Constructs a View from PaneParams.
     *
     * @param params Params for the Pane.
     */
    public DragView(final PaneParams params)
    {
        super(params);
    }

    @Override
    protected boolean childIsVisible(@NotNull final Pane child)
    {
        return child.getX() < getWidth() + scrollX
                 && child.getY() < getHeight() + scrollY
                 && (child.getY() + child.getHeight()) >= scrollY
                 && (child.getX() + child.getWidth()) >= scrollX;
    }

    @Override
    public void parseChildren(final PaneParams params)
    {
        super.parseChildren(params);
        computeContentSize();
    }

    @Override
    public void addChild(final Pane child)
    {
        super.addChild(child);
        computeContentSize();
    }

    /**
     * Compute the height in pixels of the container.
     */
    private void computeContentSize()
    {
        contentHeight = 0;
        contentWidth = 0;

        for (@NotNull final Pane child : children)
        {
            if (child != null)
            {
                contentHeight = Math.max(contentHeight, child.getY() + child.getHeight());
                contentWidth = Math.max(contentWidth, child.getX() + child.getWidth());
            }
        }

        // Recompute scroll
        setScrollY(scrollY);
        setScrollX(scrollX);
    }

    private int getMaxScrollY()
    {
        return Math.max(0, contentHeight - getHeight());
    }

    private int getMaxScrollX()
    {
        return Math.max(0, contentWidth - getWidth());
    }


    @Override
    public void drawSelf(final int mx, final int my)
    {
        scissorsStart();

        // Translate the scroll
        RenderSystem.pushMatrix();
        RenderSystem.translatef((int) -scrollX, (float) -scrollY, 0.0f);
        super.drawSelf(mx + (int) scrollX, my + (int)scrollY);
        RenderSystem.popMatrix();

        scissorsEnd();
    }

    @Override
    public void click(final int mx, final int my)
    {
        // Offset click by the scroll amounts; we'll adjust it back on clickSelf
        super.click(mx + (int) scrollX, my + (int) scrollY);
    }

    @Override
    public void onMouseDrag(final double startX, final double startY, final int speed, final double x, final double y)
    {
        setScrollY(scrollY - y);
        setScrollX(scrollX - x);
    }

    private void setScrollY(final double offset)
    {
        scrollY = offset;

        final double maxScrollY = getMaxScrollY();
        if (scrollY > maxScrollY)
        {
            scrollY = maxScrollY;
        }

        if (scrollY < 0)
        {
            scrollY = 0;
        }
    }

    private void setScrollX(final double offset)
    {
        scrollX = offset;

        final double maxScrollX = getMaxScrollX();
        if (scrollX > maxScrollX)
        {
            scrollX = maxScrollX;
        }

        if (scrollX < 0)
        {
            scrollX = 0;
        }
    }
}
