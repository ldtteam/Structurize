package com.ldtteam.structurize.client.rendertask.task;

public abstract class TimedTask implements IClientTask
{
    private final String id;
    private       int    ticksLeft = 0;

    protected TimedTask(final String id, final int seconds)
    {
        this.id = id;
        this.ticksLeft = seconds * 20;
    }

    /**
     * Duration of the task
     */
    public void setDurationSeconds(final int seconds)
    {
        this.ticksLeft = seconds * 20;
    }

    public boolean isExpired()
    {
        return ticksLeft <= 0;
    }

    @Override
    public boolean tick()
    {
        ticksLeft--;
        return isExpired();
    }

    @Override
    public String id()
    {
        return id;
    }
}
