package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

/**
 * Basic scrollable pane.
 */
public class ScrollingContainer extends View
{
    private static final int PERCENT_90 = 90;
    private static final int PERCENT_FULL = 100;

    protected ScrollingView owner;
    protected double scrollY = 0;
    protected int contentHeight = 0;

    ScrollingContainer(final ScrollingView owner)
    {
        super();
        this.owner = owner;
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

        for (final Pane child : children)
        {
            if (child != null)
            {
                contentHeight = Math.max(contentHeight, child.getY() + child.getHeight());
            }
        }

        // Recompute scroll
        setScrollY(scrollY);
    }

    /**
     * Compute the height in pixels of the container.
     */
    public void setContentHeight(final int size)
    {
        contentHeight = size;
        // Recompute scroll
        setScrollY(scrollY);
    }

    public int getMaxScrollY()
    {
        return Math.max(0, contentHeight - getHeight());
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        scissorsStart();

        // Translate the scroll
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0f, (float) -scrollY, 0.0f);
        super.drawSelf(mx, my + (int) scrollY);
        RenderSystem.popMatrix();

        scissorsEnd();
    }

    @Override
    public boolean click(final double mx, final double my)
    {
        // Offset click by the scroll amounts; we'll adjust it back on clickSelf
        return super.click(mx, my + scrollY);
    }

    @Override
    protected boolean childIsVisible(@NotNull final Pane child)
    {
        return child.getX() < getWidth() && child.getY() < getHeight() + scrollY && (child.getX() + child.getWidth()) >= 0 &&
            (child.getY() + child.getHeight()) >= scrollY;
    }

    public double getScrollY()
    {
        return scrollY;
    }

    public void setScrollY(final double offset)
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

    public int getContentHeight()
    {
        return contentHeight;
    }

    public int getScrollPageSize()
    {
        return getHeight() * PERCENT_90 / PERCENT_FULL;
    }

    /**
     * Scroll down a certain amount of pixels.
     *
     * @param deltaY number of pixels to scroll.
     */
    public void scrollBy(final double deltaY)
    {
        setScrollY(scrollY + deltaY);
    }
}
