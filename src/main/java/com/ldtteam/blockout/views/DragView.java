package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

public class DragView extends View
{
    private static final int PERCENT_90 = 90;
    private static final int PERCENT_FULL = 100;

    private int scrollX;
    private int scrollY;

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
        computeContentHeight();
    }

    /**
     * Compute the height in pixels of the container.
     */
    public void computeContentHeight()
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

    /**
     * Compute the height in pixels of the container.
     */
    public void setContentHeight(final int size)
    {
        contentHeight = size;
        // Recompute scroll
        setScrollY(scrollY);
        setScrollX(scrollX);
    }

    /**
     * Compute the height in pixels of the container.
     */
    public void setContentWidth(final int size)
    {
        contentWidth = size;
        // Recompute scroll
        setScrollY(scrollY);
        setScrollX(scrollX);
    }

    public int getMaxScrollY()
    {
        return Math.max(0, contentHeight - getHeight());
    }

    public int getMaxScrollX()
    {
        return Math.max(0, contentWidth - getWidth());
    }


    @Override
    public void drawSelf(final int mx, final int my)
    {
        scissorsStart();

        // Translate the scroll
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0f -scrollX, (float) -scrollY, 0.0f);
        super.drawSelf(mx + scrollX, my + scrollY);
        RenderSystem.popMatrix();

        scissorsEnd();
    }

    @Override
    public void click(final int mx, final int my)
    {
        // Offset click by the scroll amounts; we'll adjust it back on clickSelf
        super.click(mx + scrollX, my + scrollY);
    }

    public int getScrollY()
    {
        return scrollY;
    }

    public int getScrollX()
    {
        return scrollX;
    }

    @Override
    public void onMouseDrag(final double startX, final double startY, final int speed, final double x, final double y)
    {
        int deltaX = (int) (startX - x);
        int deltaY = (int) (startY - y);
        setScrollY(scrollY + deltaY);
        setScrollX(scrollX + deltaX);
    }

    public void setScrollY(final int offset)
    {
        scrollY = offset;

        final int maxScrollY = getMaxScrollY();
        if (scrollY > maxScrollY)
        {
            scrollY = maxScrollY;
        }

        if (scrollY < 0)
        {
            scrollY = 0;
        }
    }

    public void setScrollX(final int offset)
    {
        scrollX = offset;

        final int maxScrollX = getMaxScrollX();
        if (scrollX > maxScrollX)
        {
            scrollX = maxScrollX;
        }

        if (scrollX < 0)
        {
            scrollX = 0;
        }
    }

    public int getContentHeight()
    {
        return contentHeight;
    }

    public int getContentWidth()
    {
        return contentWidth;
    }


    public int getScrollPageSizeY()
    {
        return getHeight() * PERCENT_90 / PERCENT_FULL;
    }

    public int getScrollPageSizeX()
    {
        return getWidth() * PERCENT_90 / PERCENT_FULL;
    }

    public void drag()
    {

    }

    /**
     * Scroll down a certain amount of pixels.
     *
     * @param deltaY number of pixels on y to scroll.
     * @param deltaX number of pixels on x to scroll.
     */
    public void scrollBy(final int deltaY, final int deltaX)
    {
        setScrollY(scrollY + deltaY);
        setScrollY(scrollX + deltaX);
    }
}
