package com.ldtteam.blockout.views;

import com.ldtteam.blockout.*;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A View is a Pane which can contain other Panes.
 */
public class View extends Pane
{
        protected List<Pane> children = new ArrayList<>();
    protected int padding = 0;

    /**
     * Constructs a barebones View.
     */
    public View()
    {
        super();
    }

    /**
     * Constructs a View from PaneParams.
     *
     * @param params Params for the View.
     */
    public View(final PaneParams params)
    {
        super(params);

        if (params.getParentView() != null) // might be null if created dynamically
        {
            if (width == 0) width = params.getParentView().width - x;
            if (height == 0) height = params.getParentView().height - y;
        }

        padding = params.getInteger("padding", padding);
    }

        public List<Pane> getChildren()
    {
        return children;
    }

    @Override
    public void parseChildren(final PaneParams params)
    {
        final List<PaneParams> childNodes = params.getChildren();
        if (childNodes.isEmpty())
        {
            return;
        }

        for (final PaneParams node : childNodes)
        {
            Loader.createFromPaneParams(node, this);
        }
    }

    @Override
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        // Translate the drawing origin to our x,y.
        ms.pushPose();

        final int paddedX = x + padding;
        final int paddedY = y + padding;

        ms.translate(paddedX, paddedY, 0.0d);

        // Translate Mouse into the View
        final double drawX = mx - paddedX;
        final double drawY = my - paddedY;

        for (final Pane child : children)
        {
            if (childIsVisible(child))
            {
                child.draw(ms, drawX, drawY);
            }
        }

        ms.popPose();
    }

    @Override
    public void drawSelfLast(final PoseStack ms, final double mx, final double my)
    {
        // Translate the drawing origin to our x,y.
        ms.pushPose();

        final int paddedX = x + padding;
        final int paddedY = y + padding;

        ms.translate(paddedX, paddedY, 0.0d);

        // Translate Mouse into the View
        final double drawX = mx - paddedX;
        final double drawY = my - paddedY;

        for (final Pane child : children)
        {
            if (childIsVisible(child))
            {
                child.drawLast(ms, drawX, drawY);
            }
        }

        ms.popPose();
    }

    @Override
    public boolean scrollInput(final double wheel, final double mx, final double my)
    {
        return mousePointableEventHandler(mx, my, (child, mxChild, myChild) -> child.scrollInput(wheel, mxChild, myChild), null);
    }

    @Nullable
    @Override
    public Pane findPaneByID(final String id)
    {
        if (this.id.equals(id))
        {
            return this;
        }

        for (final Pane child : children)
        {
            final Pane found = child.findPaneByID(id);
            if (found != null)
            {
                return found;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public <T extends Pane> T findPaneByType(final Class<T> type)
    {
        if (super.findPaneByType(type) != null)
        {
            return type.cast(this);
        }

        for (final Pane child : children)
        {
            final T found = child.findPaneByType(type);
            if (found != null)
            {
                return found;
            }
        }

        return null;
    }

    @Override
    public void setWindow(final Window w)
    {
        super.setWindow(w);
        for (final Pane child : children)
        {
            child.setWindow(w);
        }
    }

    @Override
    public void setSize(final int w, final int h)
    {
        super.setSize(w, h);

        // Allow elements to have their size expanded when zero
        View p = parent;
        if (p == null) return;

        if (width == 0)
        {
            while (p.width == 0 && p.parent != null)
            {
                p = p.parent;
            }
            width = Math.max(0, p.width - x);
        }
        if (height == 0)
        {
            while (p.height == 0 && p.parent != null)
            {
                p = p.parent;
            }
            height = Math.max(0, p.height - y);
        }
    }

    // Mouse
    @Override
    public boolean rightClick(final double mx, final double my)
    {
        return mouseClickableEventHandler(mx, my, Pane::rightClick);
    }

    // Mouse
    @Override
    public boolean click(final double mx, final double my)
    {
        return mouseClickableEventHandler(mx, my, Pane::click);
    }

    /**
     * Return a Pane that will handle a click action at the specified mouse
     * coordinates.
     *
     * @param mx Mouse X, relative to the top-left of this Pane.
     * @param my Mouse Y, relative to the top-left of this Pane.
     * @return a Pane that will handle a click action.
     */
    @Nullable
    public Pane findPaneForClick(final double mx, final double my)
    {
        final AtomicReference<Pane> result = new AtomicReference<>();
        mouseClickableEventHandler(mx, my, (child, mxChild, myChild) -> {
            result.set(child);
            return true;
        });
        return result.get();
    }

    @Override
    public void onUpdate()
    {
        // copy to prevent CME during scrolling list updates, ctor uses fast array copy so it's cheap
        new ArrayList<>(children).forEach(Pane::onUpdate);
    }

    protected boolean childIsVisible(final Pane child)
    {
        return child.getX() < getInteriorWidth() && child.getY() < getInteriorHeight() && (child.getX() + child.getWidth()) >= 0 &&
            (child.getY() + child.getHeight()) >= 0;
    }

    public int getInteriorWidth()
    {
        return width - (padding * 2);
    }

    public int getInteriorHeight()
    {
        return height - (padding * 2);
    }

    /**
     * Add child Pane to this view.
     *
     * @param child pane to add.
     */
    public void addChild(final Pane child)
    {
        child.setWindow(getWindow());
        children.add(child);
        adjustChild(child);
        child.setParentView(this);
    }

    protected void adjustChild(final Pane child)
    {
        int childX = child.getX();
        int childY = child.getY();
        int childWidth = child.getWidth();
        int childHeight = child.getHeight();

        // Negative width = 100% of parents width minus abs(width).
        if (childWidth < 0)
        {
            childWidth = Math.max(0, getInteriorWidth() + childWidth);
        }

        final Alignment alignment = child.getAlignment();

        // Adjust for horizontal alignment.
        if (alignment.isRightAligned())
        {
            childX = (getInteriorWidth() - childWidth) - childX;
        }
        else if (alignment.isHorizontalCentered())
        {
            childX = ((getInteriorWidth() - childWidth) / 2) + childX;
        }

        // Negative height = 100% of parents height minus abs(height).
        if (childHeight < 0)
        {
            childHeight = Math.max(0, getInteriorHeight() + childHeight);
        }

        // Adjust for vertical alignment.
        if (alignment.isBottomAligned())
        {
            childY = (getInteriorHeight() - childHeight) - childY;
        }
        else if (alignment.isVerticalCentered())
        {
            childY = ((getInteriorHeight() - childHeight) / 2) + childY;
        }

        child.setSize(childWidth, childHeight);
        child.setPosition(childX, childY);
    }

    /**
     * Remove pane from view.
     *
     * @param child pane to remove.
     */
    public void removeChild(final Pane child)
    {
        children.remove(child);
    }

    @Override
    public boolean onMouseDrag(final double x, final double y, final int speed, final double deltaX, final double deltaY)
    {
        return mousePointableEventHandler(x, y, (child, mxChild, myChild) -> child.onMouseDrag(mxChild, myChild, speed, deltaX, deltaY), null);
    }

    /**
     * Select first children using reverse iteration over {@link #children} that is enabled
     * {@link Pane#canHandleClick(double, double)}.
     *
     * @param mx            mouse x relative to parent
     * @param my            mouse y relative to parent
     * @param eventCallback event callback
     * @return true if event was used or propagation needs to be stopped
     */
    public boolean mouseClickableEventHandler(final double mx, final double my, final MouseEventCallback eventCallback)
    {
        return mouseEventProcessor(mx, my, Pane::canHandleClick, eventCallback, null);
    }

    /**
     * Select first children using reverse iteration over {@link #children} that is rendered
     * {@link Pane#isPointInPane(double, double)}.
     *
     * @param mx            mouse x relative to parent
     * @param my            mouse y relative to parent
     * @param eventCallbackPositive event callback if accept.
     * @param eventCallbackNegative event callback if deny.
     * @return true if event was used or propagation needs to be stopped
     */
    public boolean mousePointableEventHandler(final double mx, final double my,
        final MouseEventCallback eventCallbackPositive, @Nullable final MouseEventCallback eventCallbackNegative)
    {
        return mouseEventProcessor(mx, my, Pane::isPointInPane, eventCallbackPositive, eventCallbackNegative);
    }

    /**
     * Select first children using reverse iteration over {@link #children} that is accepted by panePredicate.
     *
     * @param mx            mouse x relative to parent
     * @param my            mouse y relative to parent
     * @param panePredicate test child pane if it can accept current event
     * @param eventCallbackPositive event callback
     * @param eventCallbackNegative negative event callback.
     * @return true if event was used or propagation needs to be stopped
     */
    public boolean mouseEventProcessor(final double mx, final double my, final MouseEventCallback panePredicate,
        final MouseEventCallback eventCallbackPositive, final MouseEventCallback eventCallbackNegative)
    {
        final ListIterator<Pane> it = children.listIterator(children.size());
        final double mxChild = mx - x - padding;
        final double myChild = my - y - padding;
        boolean invokedPositive = false;
        while (it.hasPrevious())
        {
            final Pane child = it.previous();
            if (panePredicate.accept(child, mxChild, myChild) && !invokedPositive)
            {
                if (eventCallbackNegative != null)
                {
                    invokedPositive = eventCallbackPositive.accept(child, mxChild, myChild);
                }
                else
                {
                    return eventCallbackPositive.accept(child, mxChild, myChild);
                }
            }
            else if (eventCallbackNegative != null)
            {
                eventCallbackNegative.accept(child, mxChild, myChild);
            }
        }
        return invokedPositive;
    }
}
