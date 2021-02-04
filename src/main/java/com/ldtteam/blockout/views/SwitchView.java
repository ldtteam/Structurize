package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.MathHelper;

/**
 * Tabbed view.
 */
public class SwitchView extends View
{
    @Nullable
    private Pane currentView;

    /**
     * Required default constructor.
     */
    public SwitchView()
    {
        super();
    }

    /**
     * Constructs a View from PaneParams.
     *
     * @param params Params for the Pane.
     */
    public SwitchView(final PaneParams params)
    {
        super(params);
    }

    @Override
    public void parseChildren(@NotNull final PaneParams params)
    {
        super.parseChildren(params);

        final String defaultView = params.getString("default");
        if (defaultView != null)
        {
            setView(defaultView);
        }
    }

    /**
     * Switch current view to view with id given as param.
     *
     * @param name id of view
     * @return true if view of given name was found, else false
     */
    public boolean setView(final String name)
    {
        // Immediate children only
        for (@NotNull
        final Pane child : children)
        {
            if (child.getID().equals(name))
            {
                setCurrentView(child);
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public Pane findPaneForClick(final double mx, final double my)
    {
        if (currentView != null && currentView.canHandleClick(mx, my))
        {
            return currentView;
        }

        return null;
    }

    @Override
    protected boolean childIsVisible(final Pane child)
    {
        return child == currentView && super.childIsVisible(child);
    }

    @Override
    public void addChild(@NotNull final Pane child)
    {
        super.addChild(child);
        if (children.size() == 1)
        {
            currentView = child;
            child.setVisible(true);
        }
        else
        {
            child.setVisible(false);
        }
    }

    @Override
    public void adjustChild(@NotNull final Pane child)
    {
        if (child.getWidth() == 0 || child.getHeight() == 0)
        {
            child.setSize(width - child.getX(), height - child.getY());
        }

        super.adjustChild(child);
    }

    @Override
    public void removeChild(final Pane child)
    {
        super.removeChild(child);
        if (child == currentView)
        {
            if (children.isEmpty())
            {
                currentView = null;
            }
            else
            {
                currentView = children.get(0);
                currentView.setVisible(true);
            }
        }
    }

    @Nullable
    public Pane getCurrentView()
    {
        return currentView;
    }

    private void setCurrentView(final Pane pane)
    {
        if (currentView != null)
        {
            currentView.setVisible(false);
        }
        currentView = pane;
        currentView.setVisible(true);
    }

    /**
     * Get the next tab/view.
     */
    public void nextView()
    {
        setView(true, 1);
    }

    /**
     * Get the last tab/view.
     */
    public void previousView()
    {
        setView(true, -1);
    }

    /**
     * Generic shift method, abstract version of {@link #previousView()} and {@link #nextView()}.
     * New index is clamped between zero and <code>children.size()</code>
     *
     * @param relative whether page param is relative or absolute
     * @param shift    if relative go x views forward/backward, if absolute go to x-th view
     */
    public void setView(final boolean relative, final int shift)
    {
        if (children.isEmpty())
        {
            return;
        }

        int newIndex = relative ? children.indexOf(currentView) + shift : shift;
        newIndex = MathHelper.clamp(newIndex, 0, getChildrenSize() - 1);

        setCurrentView(children.get(newIndex));
    }

    /**
     * Get amount of views.
     */
    public int getChildrenSize()
    {
        return children.size();
    }
}
