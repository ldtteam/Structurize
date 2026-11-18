package com.ldtteam.structurize.index.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the result of a validation run for a {@link Pack}.
 */
public class PackValidationState
{
    public static final Codec<PackValidationState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ComponentSerialization.CODEC.listOf().optionalFieldOf("optional-warnings", List.of()).forGetter(s -> s.optionalWarnings),
        ComponentSerialization.CODEC.listOf().optionalFieldOf("required-errors", List.of()).forGetter(s -> s.requiredErrors)
    ).apply(instance, (optional, required) -> {
        final PackValidationState state = new PackValidationState();
        state.setOptionalWarnings(optional);
        state.setRequiredErrors(required);
        return state;
    }));

    @NotNull
    private List<Component> optionalWarnings = new ArrayList<>();

    @NotNull
    private List<Component> requiredErrors = new ArrayList<>();

    /**
     * Replaces the list of optional (non-blocking) warnings with the given list.
     *
     * @param warnings the new optional warning messages
     */
    public void setOptionalWarnings(final @NotNull List<Component> warnings)
    {
        this.optionalWarnings = new ArrayList<>(warnings);
    }

    /**
     * Replaces the list of required (blocking) errors with the given list.
     *
     * @param errors the new required error messages
     */
    public void setRequiredErrors(final @NotNull List<Component> errors)
    {
        this.requiredErrors = new ArrayList<>(errors);
    }

    /**
     * Returns an unmodifiable view of the optional warning messages produced by the last validation run.
     *
     * @return the optional warnings, never {@code null}
     */
    @NotNull
    public List<Component> getOptionalWarnings()
    {
        return Collections.unmodifiableList(optionalWarnings);
    }

    /**
     * Returns an unmodifiable view of the required error messages produced by the last validation run.
     *
     * @return the required errors, never {@code null}
     */
    @NotNull
    public List<Component> getRequiredErrors()
    {
        return Collections.unmodifiableList(requiredErrors);
    }
}
