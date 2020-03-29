package com.ldtteam.structurize.management.schemaserver.request;

import java.util.function.Consumer;

/**
 * @param <I> request data
 * @param <O> callback for receiving the result
 */
public interface IRequest<I, O>
{
    /**
     * @param dataInput request data
     * @param callback  callback for receiving the result
     */
    void create(I dataInput, Consumer<O> callback);
}
