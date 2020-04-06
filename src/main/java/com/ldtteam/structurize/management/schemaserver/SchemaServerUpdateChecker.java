package com.ldtteam.structurize.management.schemaserver;

public class SchemaServerUpdateChecker
{
    private static Runnable structuresLoaderMethod;

    /**
     * Private constructor to hide implicit public one.
     */
    private SchemaServerUpdateChecker()
    {
        /*
         * Intentionally left empty
         */
    }

    public static void askUpdate(final Runnable structuresLoaderMethodIn)
    {
        structuresLoaderMethod = structuresLoaderMethodIn;

        // This setups the API to use the token.
        // You will need to refresh this as the token has a lifetime of 3600 seconds
        // Refresh the token better early then to late. Simply call this method again to update the new access token.
        // No need to recreate the api instances
        // This requests the scan using a random UUID, will never work but is an example!.
        // All api instances will need to be recreated when a user logs out.
        // (new SimpleScanApi()).simpleScanIdIdGet(UUID.randomUUID());
    }
}
