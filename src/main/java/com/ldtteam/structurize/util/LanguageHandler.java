package com.ldtteam.structurize.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

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
     * Send a message to the player.
     *
     * @param player  the player to send to.
     * @param key     the key of the message.
     * @param message the message to send.
     */
    public static void sendPlayerMessage(@NotNull final Player player, final String key, final Object... message)
    {
        player.sendMessage(buildChatComponent(key.toLowerCase(Locale.US), message), player.getUUID());
    }

    public static MutableComponent buildChatComponent(final String key, final Object... message)
    {
        TranslatableComponent translation = null;

        int onlyArgsUntil = 0;
        for (final Object object : message)
        {
            if (object instanceof Component)
            {
                if (onlyArgsUntil == 0)
                {
                    onlyArgsUntil = -1;
                }
                break;
            }
            onlyArgsUntil++;
        }

        if (onlyArgsUntil >= 0)
        {
            final Object[] args = new Object[onlyArgsUntil];
            System.arraycopy(message, 0, args, 0, onlyArgsUntil);

            translation = new TranslatableComponent(key, args);
        }

        for (final Object object : message)
        {
            if (translation == null)
            {
                if (object instanceof Component)
                {
                    translation = new TranslatableComponent(key);
                }
                else
                {
                    translation = new TranslatableComponent(key, object);
                    continue;
                }
            }

            if (object instanceof Component)
            {
                translation.append(TextComponent.EMPTY);
                translation.append((Component) object);
            }
            else if (object instanceof String)
            {
                boolean isInArgs = false;
                for (final Object obj : translation.getArgs())
                {
                    if (obj.equals(object))
                    {
                        isInArgs = true;
                        break;
                    }
                }

                if (!isInArgs)
                {
                    translation.append(" " + object);
                }
            }
        }

        return translation;
    }

    /**
     * Localize a string and use String.format().
     *
     * @param inputKey translation key.
     * @param args     Objects for String.format().
     * @return Localized string.
     */
    public static String format(final String inputKey, final Object... args)
    {
        final String key = inputKey.toLowerCase(Locale.US);
        final String result;
        if (args.length == 0)
        {
            result = new TranslatableComponent(key).getString();
        }
        else
        {
            result = new TranslatableComponent(key, args).getString();
        }
        return result.isEmpty() ? key : result;
    }

    /**
     * Send message to a list of players.
     *
     * @param players the list of players.
     * @param key     key of the message.
     * @param message the message.
     */
    public static void sendPlayersMessage(@Nullable final List<Player> players, final String key, final Object... message)
    {
        if (players == null || players.isEmpty())
        {
            return;
        }

        final Component textComponent = buildChatComponent(key.toLowerCase(Locale.US), message);

        for (final Player player : players)
        {
            player.sendMessage(textComponent, player.getUUID());
        }
    }

    public static void sendMessageToPlayer(final Player player, final String key, final Object... format)
    {
        player.sendMessage(new TextComponent(translateKeyWithFormat(key, format)), player.getUUID());
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
        return LanguageCache.getInstance().translateKey(key.toLowerCase(Locale.US));
    }

    /**
     * Sets our cache to use mc default one.
     */
    public static void setMClanguageLoaded()
    {
        LanguageCache.getInstance().isMCloaded = true;
        LanguageCache.getInstance().languageMap = null;
    }

    public static void loadLangPath(final String path)
    {
        LanguageCache.getInstance().load(path);
    }

    private static class LanguageCache
    {
        private static final LanguageCache instance = new LanguageCache();
        private boolean isMCloaded = false;
        private Map<String, String> languageMap;

        private LanguageCache()
        {
            load("assets/structurize/lang/%s.json");
        }

        private void load(final String path)
        {
            final String defaultLocale = "en_us";

            // Trust me, Minecraft.getInstance() can be null, when you run Data Generators!
            String locale =
                DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance() == null ? null : Minecraft.getInstance().options.languageCode);

            if (locale == null)
            {
                locale = defaultLocale;
            }

            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(String.format(path, locale));
            if (is == null)
            {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(String.format(path, defaultLocale));
            }
            languageMap = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), new TypeToken<Map<String, String>>()
            {}.getType());

            IOUtils.closeQuietly(is);
        }

        private static LanguageCache getInstance()
        {
            return instance;
        }

        private String translateKey(final String key)
        {
            if (isMCloaded)
            {
                return Language.getInstance().getOrDefault(key);
            }
            else
            {
                final String res = languageMap.get(key);
                return res == null ? Language.getInstance().getOrDefault(key) : res;
            }
        }
    }
}
