package com.structurize.api.util.constant;

import org.jetbrains.annotations.NonNls;

/**
 * Constants for suppression keys.
 */
public final class Suppression
{

    /**
     * Suppress warnings for unchecked type conversions.
     * <p>
     * We sometimes need this for complicated typings.
     */
    @NonNls
    public static final String UNCHECKED = "unchecked";

    /**
     * Suppress warnings for deprecations.
     * <p>
     * We sometimes need this for minecraft methods we have to keep support for.
     */
    @NonNls
    public static final String DEPRECATION = "deprecation";

    /**
     * We sometimes suppress this to ignore irrelevant error messages.
     * <p>
     * Use this sparely!
     */
    @NonNls
    public static final String EXCEPTION_HANDLERS_SHOULD_PRESERVE_THE_ORIGINAL_EXCEPTIONS = "squid:S1166";

    /**
     * We sometimes suppress this because we need to return open resources.
     * <p>
     * It is the responsibility of calling code to close this!
     */
    @NonNls
    public static final String RESOURCES_SHOULD_BE_CLOSED = "squid:S2095 ";

    private Suppression()
    {
        //empty default
    }
}
