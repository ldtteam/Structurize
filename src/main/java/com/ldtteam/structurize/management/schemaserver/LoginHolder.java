package com.ldtteam.structurize.management.schemaserver;

import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.util.LanguageHandler;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

/**
 * CLIENTSIDE CLASS
 */
public class LoginHolder
{
    private static final ClientAuthentication clientAuth = new ClientSecretBasic(new ClientID("structurize"), new Secret(""));
    private static OIDCProviderMetadata authApiMetadata;

    private LoginHolder()
    {
    }

    /**
     * Called on client side, NEVER call this on server side, server should never be
     * logged in anyhow.
     *
     * @param username gui result
     * @param password gui result
     */
    public static void login(final String username, final String password)
    {
        new Thread(() -> {
            try
            {
                if (isApiOnline())
                {
                    tryLogin(new ResourceOwnerPasswordCredentialsGrant(username, new Secret(password)));
                }
            }
            catch (final Exception e)
            {
                Log.getLogger().info("woof", e);
            }
        }).start();
    }

    private static boolean isApiOnline()
    {
        if (authApiMetadata == null)
        {
            try
            {
                final InputStream stream = new URI("https://auth.ldtteam.com/.well-known/openid-configuration").toURL().openStream();
                String providerInfo = null;
                try (Scanner s = new Scanner(stream))
                {
                    providerInfo = s.useDelimiter("\\A").hasNext() ? s.next() : "";
                }
                authApiMetadata = OIDCProviderMetadata.parse(providerInfo);
            }
            catch (final Exception e)
            {
                Log.getLogger().warn("Cannot get auth api metadata", e);
                return false;
            }
        }
        return authApiMetadata != null;
    }

    private static void tryLogin(final AuthorizationGrant authCredentials) throws Exception
    {
        final TokenRequest request = new TokenRequest(authApiMetadata.getTokenEndpointURI(),
            clientAuth,
            authCredentials,
            new Scope("schematic_auth"));

        final TokenResponse response = TokenResponse.parse(request.toHTTPRequest().send());

        if (!response.indicatesSuccess())
        {
            // We got an error response...
            final TokenErrorResponse errorResponse = response.toErrorResponse();
            Log.getLogger().warn("Cannot login, cause: {}", errorResponse.getErrorObject().toJSONObject().toJSONString());
            return;
        }

        final AccessTokenResponse successResponse = response.toSuccessResponse();

        // Get the access token, the server may also return a refresh token
        final AccessToken accessToken = successResponse.getTokens().getAccessToken();
        final RefreshToken refreshToken = successResponse.getTokens().getRefreshToken();
        Log.getLogger().warn("login at {} rt {}", accessToken.toJSONString(), refreshToken.toJSONString());
    }

    public static boolean isUserLoggedIn()
    {
        return false;
    }
}
