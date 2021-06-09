package com.ldtteam.blockout.controls;

import java.util.function.Consumer;

/**
 * Used for windows that have inputs that are clicked.
 */
@FunctionalInterface
public interface InputHandler extends Consumer<TextField>
{
    default void accept(final TextField input)
    {
        onInput(input);
    }

    /**
     * Called when input is written to.
     *
     * @param input the input field that received content.
     */
    void onInput(final TextField input);
}
