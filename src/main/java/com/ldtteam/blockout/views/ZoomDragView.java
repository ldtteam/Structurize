package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;
import net.minecraft.util.math.MathHelper;

public class ZoomDragView extends View
{
    private double scrollX = 0d;
    private double scrollY = 0d;
    private double scale = 1d;

    protected int contentHeight = 0;
    protected int contentWidth = 0;

    private double dragFactor = 1d;
    private boolean dragEnabled = true;

    private double zoomFactor = 0.01d;
    private boolean zoomEnabled = true;
    private double minScale = zoomFactor;
    private double maxScale = 50d;

    /**
     * Required default constructor.
     */
    public ZoomDragView()
    {
        super();
    }

    /**
     * Constructs a View from PaneParams.
     *
     * @param params Params for the Pane.
     */
    public ZoomDragView(final PaneParams params)
    {
        super(params);
        dragFactor = params.getDoubleAttribute("dragfactor", dragFactor);
        dragEnabled = params.getBooleanAttribute("dragenabled", dragEnabled);
        zoomFactor = params.getDoubleAttribute("zoomfactor", zoomFactor);
        zoomEnabled = params.getBooleanAttribute("zoomenabled", zoomEnabled);
        minScale = params.getDoubleAttribute("minscale", minScale);
        maxScale = params.getDoubleAttribute("maxscale", maxScale);
    }

    @Override
    protected boolean childIsVisible(@NotNull final Pane child)
    {
        return calcInverseAbsoluteX(child.getX()) < getInteriorWidth() && calcInverseAbsoluteY(child.getY()) < getInteriorHeight() &&
            calcInverseAbsoluteX(child.getX() + child.getWidth()) >= 0 && calcInverseAbsoluteY(child.getY() + child.getHeight()) >= 0;
        // return child.getX() < getWidth() + scrollX && child.getY() < getHeight() + scrollY && (child.getY() + child.getHeight()) >=
        // scrollY && (child.getX() + child.getWidth()) >= scrollX;
    }

    private double calcInverseAbsoluteX(final double xIn)
    {
        return xIn * scale - scrollX;
    }

    private double calcInverseAbsoluteY(final double yIn)
    {
        return yIn * scale - scrollY;
    }

    private double calcRelativeX(final double xIn)
    {
        return (xIn - x + scrollX) / scale + x;
    }

    private double calcRelativeY(final double yIn)
    {
        return (yIn - y + scrollY) / scale + y;
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

        for (@NotNull
        final Pane child : children)
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

    private double getMaxScrollY()
    {
        return Math.max(0, (double) contentHeight * scale - getHeight());
    }

    private double getMaxScrollX()
    {
        return Math.max(0, (double) contentWidth * scale - getWidth());
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        scissorsStart();

        RenderSystem.pushMatrix();
        RenderSystem.translated(-scrollX, -scrollY, 0.0d);
        RenderSystem.translated((1 - scale) * x, (1 - scale) * y, 0.0d);
        RenderSystem.scaled(scale, scale, 1.0d);
        super.drawSelf((int) calcRelativeX(mx), (int) calcRelativeY(my));
        RenderSystem.popMatrix();

        scissorsEnd();
    }

    private void setScrollY(final double offset)
    {
        scrollY = MathHelper.clamp(offset, 0, getMaxScrollY());
    }

    private void setScrollX(final double offset)
    {
        scrollX = MathHelper.clamp(offset, 0, getMaxScrollX());
    }

    @Override
    public boolean onMouseDrag(final double startX, final double startY, final int speed, final double x, final double y)
    {
        final boolean childResult = super.onMouseDrag(calcRelativeX(startX), calcRelativeY(startY), speed, calcRelativeX(x), calcRelativeY(y));
        if (!childResult && dragEnabled)
        {
            setScrollX(scrollX - x * dragFactor);
            setScrollY(scrollY - y * dragFactor);
            return true;
        }
        return childResult;
    }

    @Override
    public boolean scrollInput(final double wheel, final double mx, final double my)
    {
        final boolean childResult = super.scrollInput(wheel, mx, my);
        if (!childResult && zoomEnabled)
        {
            scale += wheel * zoomFactor;
            scale = MathHelper.clamp(scale, minScale, maxScale);
            setScrollY(scrollY);
            setScrollX(scrollX);
            return true;
        }
        return childResult;
    }

    @Override
    public boolean click(final double mx, final double my)
    {
        return super.click(calcRelativeX(mx), calcRelativeY(my));
    }

    @Override
    public Pane findPaneForClick(final double mx, final double my)
    {
        return super.findPaneForClick(calcRelativeX(mx), calcRelativeY(my));
    }

    @Override
    public void handleHover(final double mx, final double my)
    {
        super.handleHover(calcRelativeX(mx), calcRelativeY(my));
    }

    @Override
    public boolean rightClick(final double mx, final double my)
    {
        return super.rightClick(calcRelativeX(mx), calcRelativeY(my));
    }
}
