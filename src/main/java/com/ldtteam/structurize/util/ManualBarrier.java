package com.ldtteam.structurize.util;

/**
 * A waitable primitive that is open/closed manually rather than counted like a
 * {@link java.util.concurrent.CyclicBarrier} or {@link java.util.concurrent.Semaphore}.
 */
public final class ManualBarrier
{
    private final Object monitor = new Object();
    private volatile boolean open = false;

    /**
     * Constructs the barrier.
     * @param open true if the barrier is initially open (allow waiters to continue)
     */
    public ManualBarrier(final boolean open)
    {
        this.open = open;
    }

    /**
     * Blocks the thread if the barrier is currently closed, until any other thread calls {@link #open()}.
     * @throws InterruptedException if the thread is interrupted during the wait
     */
    public void waitOne() throws InterruptedException
    {
        synchronized (monitor)
        {
            while (!open)
            {
                monitor.wait();
            }
        }
    }

    /**
     * Opens the barrier and allows all currently-waiting and future threads to pass.
     */
    public void open()
    {
        synchronized (monitor)
        {
            open = true;
            monitor.notifyAll();
        }
    }

    /**
     * Closes the barrier and requests all future threads to wait.
     */
    public void close()
    {
        synchronized (monitor)
        {
            open = false;
        }
    }
}
