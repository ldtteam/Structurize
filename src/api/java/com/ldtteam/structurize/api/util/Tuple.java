package com.ldtteam.structurize.api.util;

import java.util.Objects;

/**
 * Immutable pair of values. Replacement for the removed net.minecraft.util.Tuple.
 *
 * @param <A> type of the first value.
 * @param <B> type of the second value.
 */
public class Tuple<A, B>
{
    private final A a;
    private final B b;

    public Tuple(final A a, final B b)
    {
        this.a = a;
        this.b = b;
    }

    public A getA()
    {
        return a;
    }

    public B getB()
    {
        return b;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(a, tuple.a) && Objects.equals(b, tuple.b);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(a, b);
    }

    @Override
    public String toString()
    {
        return "Tuple{" + a + ", " + b + "}";
    }
}
