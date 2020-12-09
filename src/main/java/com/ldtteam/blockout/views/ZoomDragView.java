package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import org.jetbrains.annotations.NotNull;
import net.minecraft.util.math.MathHelper;

/**
 * Zoomable and scrollable "online map"-like view
 */
public class ZoomDragView extends View
{
    private double scrollX = 0d;
    private double scrollY = 0d;
    private double scale = 1d;

    protected int contentHeight = 0;
    protected int contentWidth = 0;

    private double dragFactor = 1d;
    private boolean dragEnabled = true;

    private double zoomFactor = 1.1d;
    private boolean zoomEnabled = true;
    private double minScale = 0.2d;
    private double maxScale = 2d;

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
        return calcInverseAbsoluteX(child.getX()) < getInteriorWidth() && calcInverseAbsoluteY(child.getY()) < getInteriorHeight()
            && calcInverseAbsoluteX(child.getX() + child.getWidth()) >= 0 && calcInverseAbsoluteY(child.getY() + child.getHeight()) >= 0;
    }

    /**
     * Converts X of child to scaled and scrolled X in absolute coordinates.
     */
    private double calcInverseAbsoluteX(final double xIn)
    {
        return xIn * scale - scrollX;
    }

    /**
     * Converts Y of child to scaled and scrolled Y in absolute coordinates.
     */
    private double calcInverseAbsoluteY(final double yIn)
    {
        return yIn * scale - scrollY;
    }

    /**
     * Converts X from event to unscaled and unscrolled X for child in relative (top-left) coordinates.
     */
    private double calcRelativeX(final double xIn)
    {
        return (xIn - x + scrollX) / scale + x;
    }

    /**
     * Converts Y from event to unscaled and unscrolled Y for child in relative (top-left) coordinates.
     */
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
    protected void computeContentSize()
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

    protected void abstractDrawSelfPre(final MatrixStack ms, final double mx, final double my)
    {
    }

    protected void abstractDrawSelfPost(final MatrixStack ms, final double mx, final double my)
    {
    }

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        scissorsStart(ms);

        ms.push();
        ms.translate(-scrollX, -scrollY, 0.0d);
        ms.translate((1 - scale) * x, (1 - scale) * y, 0.0d);
        ms.scale((float) scale, (float) scale, 1.0f);
        abstractDrawSelfPre(ms, mx, my);
        super.drawSelf(ms, calcRelativeX(mx), calcRelativeY(my));
        abstractDrawSelfPost(ms, mx, my);
        ms.pop();

        scissorsEnd();
    }

    @Override
    public void drawSelfLast(final MatrixStack ms, final double mx, final double my)
    {
        scissorsStart(ms);

        ms.push();
        ms.translate(-scrollX, -scrollY, 0.0d);
        ms.translate((1 - scale) * x, (1 - scale) * y, 0.0d);
        ms.scale((float) scale, (float) scale, 1.0f);
        super.drawSelfLast(ms, calcRelativeX(mx), calcRelativeY(my));
        ms.pop();

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
        final boolean childResult = super.onMouseDrag(calcRelativeX(
            startX), calcRelativeY(startY), speed, calcRelativeX(x), calcRelativeY(y));
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
            final double childX = mx - x;
            final double childY = my - y;
            final double oldX = (childX + scrollX) / scale;
            final double oldY = (childY + scrollY) / scale;
            scale = wheel < 0 ? scale / zoomFactor : scale * zoomFactor;
            scale = MathHelper.clamp(scale, minScale, maxScale);
            setScrollX(oldX * scale - childX);
            setScrollY(oldY * scale - childY);
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
    public boolean handleHover(final double mx, final double my)
    {
        return super.handleHover(calcRelativeX(mx), calcRelativeY(my));
    }

    @Override
    public boolean rightClick(final double mx, final double my)
    {
        return super.rightClick(calcRelativeX(mx), calcRelativeY(my));
    }

    public void treeViewHelperAddChild(final Pane child)
    {
        super.addChild(child);
    }
}
