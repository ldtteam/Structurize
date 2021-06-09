package com.ldtteam.blockout.views;

import com.ldtteam.blockout.MouseEventCallback;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
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
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        scissorsStart(ms, width, contentHeight);

        // Translate the scroll
        ms.pushPose();
        ms.translate(0.0d, -scrollY, 0.0d);
        super.drawSelf(ms, mx, my + scrollY);
        ms.popPose();

        scissorsEnd(ms);
    }

    @Override
    public void drawSelfLast(final MatrixStack ms, final double mx, final double my)
    {
        // Translate the scroll
        ms.pushPose();
        ms.translate(0.0d, -scrollY, 0.0d);
        super.drawSelfLast(ms, mx, my + scrollY);
        ms.popPose();
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

    @Override
    public boolean mouseEventProcessor(final double mx,
        final double my,
        final MouseEventCallback panePredicate,
        final MouseEventCallback eventCallbackPositive,
        final MouseEventCallback eventCallbackNegative)
    {
        return super.mouseEventProcessor(mx, my + scrollY, panePredicate, eventCallbackPositive, eventCallbackNegative);
    }
}
