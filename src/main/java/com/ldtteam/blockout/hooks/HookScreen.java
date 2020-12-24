package com.ldtteam.blockout.hooks;

import com.ldtteam.blockout.BOScreen;
import com.ldtteam.blockout.hooks.TriggerMechanism.Type;
import com.ldtteam.blockout.views.ScrollingList;
import com.mojang.blaze3d.matrix.MatrixStack;

/**
 * Screen wrapper.
 */
public class HookScreen extends BOScreen
{
    private final boolean captureScroll;
    private ScrollingList scrollListener = null;

    HookScreen(final HookWindow<?> window)
    {
        super(window);
        captureScroll = window.windowHolder.hook.trigger.getType() == Type.RAY_TRACE;
    }

    public void render(final MatrixStack ms)
    {
        if (!isOpen)
        {
            return;
        }

        ms.translate(-width / 2, -height, 0.0d);
        window.draw(ms, 0, 0);
    }

    public boolean mouseScrolled(final double scrollDiff)
    {
        if (captureScroll && scrollListener != null && scrollDiff != 0)
        {
            scrollListener.scrollInput(scrollDiff * 10, scrollListener.getX() + 1, scrollListener.getY() + 1);
            return true; // TODO: would be nice to not stop event propagation when scrolling list is not scrollable
        }
        return false;
    }

    @Override
    public void init()
    {
        // noop
    }

    @Override
    public void tick()
    {
        if (minecraft != null)
        {
            if (!isOpen)
            {
                if (captureScroll)
                {
                    scrollListener = window.findFirstPaneByType(ScrollingList.class);
                    if (scrollListener != null)
                    {
                        HookManager.setScrollListener(this);
                    }
                }
                window.onOpened();
                isOpen = true;
            }
            else
            {
                window.onUpdate();
            }
        }
    }

    @Override
    public void onClose()
    {
        window.onClosed();
        if (HookManager.getScrollListener() == this)
        {
            HookManager.setScrollListener(null);
        }
    }

    public HookWindow<?> getWindow()
    {
        return (HookWindow<?>) window;
    }
}
