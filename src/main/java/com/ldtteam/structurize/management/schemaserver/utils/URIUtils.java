package com.ldtteam.structurize.management.schemaserver.utils;

import java.net.URI;
import java.net.URISyntaxException;

public final class URIUtils {

    private URIUtils() {
        throw new IllegalStateException("Tried to initialize: URIUtils but this is a Utility class.");
    }

    public static URI ensureHttps(final URI uri)
    {
        if (uri.toString().startsWith("https://"))
            return uri;

        try {
            return new URI(uri.toString().replace("http://", "https://"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return uri;
        }
    }
}
