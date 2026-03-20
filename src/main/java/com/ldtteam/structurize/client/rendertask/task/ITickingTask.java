package com.ldtteam.structurize.client.rendertask.task;

public interface ITickingTask
{
    /**
     * Client Tick callback
     *
     * @return true if task finished, false if not
     */
    public boolean tick();

    /**
     * Task string identifier
     *
     * @return
     */
    public String id();
}
