package com.ldtteam.blockout.hooks;

import java.util.function.Supplier;

/**
 * Triggers for opening guis
 */
public class TriggerMechanism<T>
{
    static final TriggerMechanism<?>[] TRIGGER_MECHANISMS = new TriggerMechanism[2];

    private static final TriggerMechanism<Double> DISTANCE_TRIGGER = new TriggerMechanism<>(Type.DISTANCE, "dist", () -> 16.0d);
    private static final TriggerMechanism<?> RAY_TRACE_TRIGGER = new TriggerMechanism<>(Type.RAY_TRACE, "ray_trace", () -> null);

    private final Type type;
    private final String name;
    private final Supplier<T> config;

    private TriggerMechanism(final Type id, final String name, final Supplier<T> config)
    {
        this.type = id;
        this.name = name;
        this.config = config;

        TRIGGER_MECHANISMS[type.id] = this;
    }

    /**
     * Gui will open once player points crosshair over target thing.
     * If satisfied then overrides any other trigger.
     *
     * @return ray trace trigger
     */
    public static TriggerMechanism<?> getRayTrace()
    {
        return RAY_TRACE_TRIGGER;
    }

    /**
     * Gui will open once player is 16 or less blocks close to target thing.
     *
     * @return 16 blocks distance trigger
     */
    public static TriggerMechanism<Double> getDistance()
    {
        return DISTANCE_TRIGGER;
    }

    /**
     * Gui will open once player is x or less blocks close to target thing.
     *
     * @param blocks detection radius
     * @return <code>blocks</code> blocks distance trigger
     */
    public static TriggerMechanism<Double> getDistance(final double blocks)
    {
        return new TriggerMechanism<>(DISTANCE_TRIGGER.type, DISTANCE_TRIGGER.name, () -> blocks);
    }

    public Type getType()
    {
        return type;
    }

    int getId()
    {
        return type.id;
    }

    String getName()
    {
        return name;
    }

    /**
     * @return specific data for trigger
     */
    public T getConfig()
    {
        return config.get();
    }

    /**
     * Trigger type
     */
    public enum Type
    {
        DISTANCE(0), RAY_TRACE(1);

        private final int id;
        private Type(final int id)
        {
            this.id = id;
        }
    }
}
