package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.BOScreen;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.Parsers;
import com.ldtteam.blockout.views.ScrollingContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * Class handling scrollbars in our GUIs.
 */
public class Scrollbar extends Pane
{
    /**
     * Max height of the scrollbar.
     */
    private static final int MAXIMUM_HEIGHT = 20;

    /**
     * Background of the scrollbar.
     */
    protected int scrollbarBackground = 0xFF000000;

    /**
     * Color of the scrollbar.
     */
    protected int scrollbarColor = 0xFFC0C0C0;

    /**
     * Color of the scrollbar when hovered.
     */
    protected int scrollbarColorHighlight = 0xFF808080;

    /**
     * Container containing the scrollbar.
     */
    protected ScrollingContainer container;

    /**
     * The height the bar is clicked.
     */
    protected double barClickY = 0;

    /**
     * True if the bar is clicked at the moment.
     */
    protected boolean barClicked = false;

    /**
     * Offsets
     */
    protected int offsetX = 0;
    protected int offsetY = 0;

    /**
     * Instantiates the scrollbar with certain parameters.
     *
     * @param container the container of the scrollbar.
     * @param params    the parameters.
     */
    public Scrollbar(final ScrollingContainer container, final PaneParams params)
    {
        this(container);
        // TODO: Parse Scrollbar-specific Params

        params.applyShorthand("scrollbarOffset", Parsers.INT, 2, a -> {
            offsetX = a.get(0);
            offsetY = a.get(1);
        });
    }

    /**
     * Instantiates a simple scrollbar.
     *
     * @param container the container of the scrollbar.
     */
    public Scrollbar(final ScrollingContainer container)
    {
        super();
        this.container = container;
    }

    /**
     * Called when the scrollbar has been clicked.
     *
     * @param my the y it is clicked on.
     */
    public void dragScroll(final double my)
    {
        if (container.getContentHeight() == 0)
        {
            return;
        }

        final double barClickYNow = getScrollBarYPos() + barClickY;
        final double deltaFromClickPos = my - barClickYNow;

        if (deltaFromClickPos == 0)
        {
            return;
        }

        final double scaledY = deltaFromClickPos * container.getMaxScrollY() / getHeight();
        container.scrollBy(scaledY);

        if (container.getScrollY() == 0 || container.getScrollY() == container.getMaxScrollY())
        {
            barClickY = MathHelper.clamp(my - getScrollBarYPos(), 0, getBarHeight() - 1);
        }
    }

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        barClicked = barClicked && (mc.mouseHelper.isLeftDown() || BOScreen.isMouseLeftDown);
        // TODO: catch from screen
        if (barClicked)
        {
            // Current relative position of the click position on the bar
            dragScroll(my - y);
        }

        if (getContentHeightDiff() <= 0)
        {
            return;
        }

        final int scrollBarBackX1 = x + offsetX;
        final int scrollBarBackX2 = scrollBarBackX1 + (getWidth() - 2);

        // Scroll Area Back
        fill(ms, scrollBarBackX2, y + getHeight() + offsetY, scrollBarBackX1, y + offsetY, scrollbarBackground);

        final int scrollBarStartY = y + (int) getScrollBarYPos();
        final int scrollBarEndY = scrollBarStartY + getBarHeight();

        // Scroll Bar (Bottom/Right Edge line) - Fill whole Scroll area
        fill(ms, scrollBarBackX2, scrollBarEndY, scrollBarBackX1, scrollBarStartY, scrollbarColorHighlight);

        // Scroll Bar (Inset color)
        fill(ms, scrollBarBackX2 - 1, scrollBarEndY - 1, scrollBarBackX1, scrollBarStartY, scrollbarColor);
    }

    @Override
    public boolean handleClick(final double mx, final double my)
    {
        if (getContentHeightDiff() <= 0)
        {
            return false;
        }

        final int barHeight = getBarHeight();

        final double scrollBarStartY = getScrollBarYPos();
        final double scrollBarEndY = scrollBarStartY + barHeight;

        if (my < scrollBarStartY)
        {
            container.scrollBy(-container.getScrollPageSize());
        }
        else if (my > scrollBarEndY)
        {
            container.scrollBy(container.getScrollPageSize());
        }
        else
        {
            barClickY = my - scrollBarStartY;
            barClicked = true;
        }
        return true;
    }

    private int getContentHeightDiff()
    {
        return container.getContentHeight() - getHeight();
    }

    private int getBarHeight()
    {
        return Math.max(Math.min(MAXIMUM_HEIGHT, getHeight() / 2), (getHeight() * getHeight()) / container.getContentHeight());
    }

    private double getScrollBarYPos()
    {
        return container.getScrollY() * (getHeight() - getBarHeight()) / getContentHeightDiff();
    }

    public int getScrollOffsetX()
    {
        return offsetX;
    }

    @Override
    public boolean onMouseDrag(final double mx, final double my, final int speed, final double deltaX, final double deltaY)
    {
        return true;
    }
}
