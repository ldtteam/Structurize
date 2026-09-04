package com.ldtteam.structurize.util;

import com.ldtteam.structurize.api.util.Log;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

/**
 * Class handling our IO pool.
 */
public final class IOPool
{
    /**
     * Hold our IO queue.
     */
    private static final BlockingQueue<Runnable> ioQueue = new LinkedBlockingDeque<>();

    /**
     * Holds the specific executor for the queue.
     */
    private static ThreadPoolExecutor executor;

    /**
     * Structurize specific thread factory.
     */
    public static class StructurizeThreadFactory implements ThreadFactory
    {
        /**
         * Ongoing thread IDs.
         */
        public static int id;

        @Override
        public Thread newThread(@NotNull final Runnable runnable)
        {
            final Thread thread = new Thread(runnable, "Structurize IO Worker #" + (id++));
            thread.setDaemon(true);

            thread.setUncaughtExceptionHandler((thread1, throwable) -> Log.getLogger().error("Structurize IO Thread errored! ", throwable));
            return thread;
        }
    }

    /**
     * Creates a new thread pool for pathfinding jobs
     *
     * @return the threadpool executor.
     */
    public static ThreadPoolExecutor getExecutor()
    {
        if (executor == null)
        {
            executor = new ThreadPoolExecutor(1, 2, 10, TimeUnit.SECONDS, ioQueue, new StructurizeThreadFactory());
        }
        return executor;
    }

    /**
     * Stops all running threads in this thread pool
     */
    public static void shutdown()
    {
        getExecutor().shutdownNow();
        ioQueue.clear();
        executor = null;
    }

    private IOPool()
    {
        //Hides default constructor.
    }

    /**
     * Submit a task for processing tothe pool.
     * @param task the task to run.
     * @return the future.
     */
    public static <T> Future<T> submit(@NotNull final Callable<T> task)
    {
        return getExecutor().submit(task);
    }


    /**
     * Execute a task to be processed in the pool.
     * @param task the task to run.
     */
    public static void execute(@NotNull final Runnable task)
    {
        getExecutor().execute(task);
    }
}
