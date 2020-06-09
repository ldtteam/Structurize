package com.ldtteam.structurize.management.schemaserver;

import java.net.URI;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import com.ldtteam.server.schematics.Configuration;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.management.schemaserver.utils.URIUtils;
import com.ldtteam.structurize.util.LanguageHandler;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

/**
 * Class for holding current login
 */
public class LoginHolder
{
    private static final ClientAuthentication clientAuth = new ClientSecretBasic(new ClientID("structurize"), new Secret(""));
    private OIDCProviderMetadata authApiMetadata;
    private AccessToken currentAccessToken;
    private RefreshToken currentRefreshToken;
    private String currentUsername = "";

    public static final LoginHolder INSTANCE = new LoginHolder();

    /**
     * Private constructor to hide implicit public one.
     */
    private LoginHolder()
    {
        /*
         * Intentionally left empty
         */
    }

    /**
     * Called on client side, NEVER call this on server side, server should never be
     * logged in anyhow. However, it should be easy to adjust it to run on server side.
     *
     * @param username       gui result
     * @param password       gui result
     * @param resultCallback executed on main thread with true if login was successful, with false and error reason otherwise
     */
    public void login(final String username, final String password, final BiConsumer<Boolean, String> resultCallback)
    {
        Util.getServerExecutor().execute(() -> {
            try
            {
                if (isApiOnline())
                {
                    tryLogin(new ResourceOwnerPasswordCredentialsGrant(username, new Secret(password)));
                    currentUsername = username;
                    Minecraft.getInstance().execute(() -> resultCallback.accept(true, ""));
                }
                Minecraft.getInstance().execute(() -> resultCallback.accept(false, "API offline"));
                currentAccessToken = null;
            }
            catch (final Exception e)
            {
                Log.getLogger().info("Login failed", e);
                Minecraft.getInstance().execute(() -> resultCallback.accept(false, e.getMessage().replace('_', ' ')));
                currentAccessToken = null;
            }
        });
    }

    /**
     * Checks if user is logged in and current access token is valid. If true then runs given runnable with current valid access token on
     * main thread or in standalone thread.
     *
     * @param task       job which needs access token
     * @param onFail     job to run if anything failed, includes reason
     * @param mainThread whether run on main thread or in minecraft thread executor
     */
    public void runAuthorized(final Consumer<AccessToken> task, final Consumer<String> onFail, final boolean mainThread)
    {
        Util.getServerExecutor().execute(() -> {
            if (currentRefreshToken == null)
            {
                onFail.accept(LanguageHandler.translateKey("structurize.sslogin.user_not_loggedin"));
                return;
            }

            refreshToken();

            if (currentAccessToken == null)
            {
                onFail.accept(LanguageHandler.translateKeyWithFormat("structurize.sslogin.refresh_error",
                    LanguageHandler.translateKey("structurize.sslogin.user_not_loggedin")));
                return;
            }

            if (mainThread)
            {
                Minecraft.getInstance().execute(() -> task.accept(currentAccessToken));
            }
            else
            {
                task.accept(currentAccessToken);
            }
        });
    }

    /**
     * Helper which mimics {@link #runAuthorized(Consumer, Consumer, boolean)} but is without checking and validating access token.
     *
     * @param task       job to run
     * @param mainThread whether run on main thread or in minecraft thread executor
     */
    public void runUnuthorized(final Runnable task, final boolean mainThread)
    {
        if (mainThread)
        {
            Minecraft.getInstance().execute(task);
        }
        else
        {
            Util.getServerExecutor().execute(task);
        }
    }

    /**
     * Send refresh access token request if possible.
     */
    private void refreshToken()
    {
        try
        {
            tryLogin(new RefreshTokenGrant(currentRefreshToken));
        }
        catch (final Exception e)
        {
            Structurize.proxy.notifyClientOrServerOps(LanguageHandler.prepareMessage("structurize.sslogin.refresh_error", e.getMessage()));
            Log.getLogger().info("Refresh failed", e);
            currentAccessToken = null;
        }
    }

    /**
     * @return true if api head was fetches successfully, does not check actual onlineness of the api
     */
    private boolean isApiOnline()
    {
        if (authApiMetadata == null)
        {
            try
            {
                String providerInfo = null;
                try (Scanner s = new Scanner(new URI("https://auth.ldtteam.com/.well-known/openid-configuration").toURL().openStream()))
                {
                    providerInfo = s.useDelimiter("\\A").hasNext() ? s.next() : "";
                }
                authApiMetadata = OIDCProviderMetadata.parse(providerInfo);
            }
            catch (final Exception e)
            {
                Log.getLogger().warn("Cannot get auth api metadata", e);
                authApiMetadata = null;
            }
        }
        return authApiMetadata != null;
    }

    /**
     * Tries to login with given auth grant. If successful update AT and RT, otherwise sets AT to null.
     *
     * @param authCredentials auth grant
     * @throws Exception https connection error or api error
     */
    private void tryLogin(final AuthorizationGrant authCredentials) throws Exception
    {
        final TokenRequest request = new TokenRequest(URIUtils.ensureHttps(authApiMetadata.getTokenEndpointURI()),
            clientAuth,
            authCredentials,
            new Scope("schematic_service", "friends", "openid", "profile", "offline_access"));

        final HTTPRequest httpRequest = request.toHTTPRequest();
        httpRequest.setFollowRedirects(true);
        final TokenResponse response = TokenResponse.parse(httpRequest.send());

        if (!response.indicatesSuccess())
        {
            // We got an error response...
            final TokenErrorResponse errorResponse = response.toErrorResponse();
            Log.getLogger().warn("Cannot login, cause: {}", errorResponse.getErrorObject().toJSONObject().toJSONString());
            currentAccessToken = null;
            throw new RuntimeException(errorResponse.getErrorObject().getDescription());
        }

        final AccessTokenResponse successResponse = response.toSuccessResponse();

        // Get the access token, the server may also return a refresh token
        currentAccessToken = successResponse.getTokens().getAccessToken();
        currentRefreshToken = successResponse.getTokens().getRefreshToken();

        Configuration.getDefaultApiClient().setAccessToken(currentAccessToken.getValue());
    }

    /**
     * @return true if access and refresh tokens are ready
     */
    public boolean isUserLoggedIn()
    {
        return currentAccessToken != null && currentRefreshToken != null && !currentUsername.isEmpty();
    }

    /**
     * Logs out current user.
     */
    public void logout()
    {
        currentAccessToken = null;
        currentRefreshToken = null;
        currentUsername = "";
    }

    /**
     * @return string which was passed as username of the latest successful login
     */
    public String getCurrentUsername()
    {
        return currentUsername;
    }
}
