package com.ldtteam.structurize.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.ldtteam.structurize.Instances;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * Helper class for localization and sending player messages.
 */
public final class LanguageHandler
{
    /**
     * Private constructor to hide implicit one.
     */
    private LanguageHandler()
    {
        // Intentionally left empty.
    }

    /**
     * Send message to collection of players.
     *
     * @param players collection of target players
     * @param key     translation key
     * @param format  String.format() attributes
     */
    public static void sendMessageToPlayers(@Nullable final Collection<PlayerEntity> players, final String key, final Object... format)
    {
        final ITextComponent message = buildChatComponent(key, format);
        for (final PlayerEntity player : players)
        {
            player.sendMessage(message);
        }
    }

    /**
     * Send a message to player.
     *
     * @param player target player
     * @param key    translation key
     * @param format String.format() attributes
     */
    public static void sendMessageToPlayer(@NotNull final PlayerEntity player, final String key, final Object... format)
    {
        player.sendMessage(buildChatComponent(key, format));
    }

    /**
     * Builds new chat component.
     *
     * @param key    translation key
     * @param format String.format() attributes
     * @return new chat component
     */
    public static ITextComponent buildChatComponent(final String key, final Object... format)
    {
        final String buildedMessage = String.format(LanguageMap.getInstance().translateKey(key), format);

        return new StringTextComponent(buildedMessage);
    }

    /**
     * Translates key to readable string and formats it.
     *
     * @param key    translation key
     * @param format String.format() attributes
     * @return formatted string
     */
    public static String translateKeyWithFormat(final String key, final Object... format)
    {
        return String.format(translateKey(key), format);
    }

    /**
     * Translates key to readable string.
     *
     * @param key translation key
     * @return readable string
     */
    public static String translateKey(final String key)
    {
        return LanguageCache.getInstance().translateKey(key);
    }

    /**
     * Sets our cache to use mc default one.
     */
    public static void setMClanguageLoaded()
    {
        LanguageCache.getInstance().isMCloaded = true;
        LanguageCache.getInstance().languageMap = null;
    }

    // TODO: javadoc, move constants
    private static class LanguageCache
    {
        private static LanguageCache instance;
        private boolean isMCloaded = false;
        private Map<String, String> languageMap;

        private LanguageCache()
        {
            final String fileLoc = "assets/structurize/lang/%s.json";
            final String defaultLocale = "en_us";
            String locale = DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().gameSettings.language);
            if (locale == null)
            {
                locale = defaultLocale;
            }
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(String.format(fileLoc, locale));
            if (is == null)
            {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(String.format(fileLoc, defaultLocale));
            }
            languageMap = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), new TypeToken<Map<String, String>>()
            {
            }.getType());
        }

        private static LanguageCache getInstance()
        {
            return instance == null ? instance = new LanguageCache() : instance;
        }

        private String translateKey(final String key)
        {
            if (isMCloaded)
            {
                return LanguageMap.getInstance().translateKey(key);
            }
            else
            {
                final String res = languageMap.get(key);
                return res == null ? key : res;
            }
        }
    }
}
