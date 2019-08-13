package com.ldtteam.structurize.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.text.TranslationTextComponent;
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
     * Send a message to the player.
     *
     * @param player  the player to send to.
     * @param key     the key of the message.
     * @param message the message to send.
     */
    public static void sendPlayerMessage(@NotNull final PlayerEntity player, final String key, final Object... message)
    {
        player.sendMessage(buildChatComponent(key, message));
    }

    public static ITextComponent buildChatComponent(final String key, final Object... message)
    {
        TranslationTextComponent translation = null;

        int onlyArgsUntil = 0;
        for (final Object object : message)
        {
            if (object instanceof ITextComponent)
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

            translation = new TranslationTextComponent(key, args);
        }

        for (final Object object : message)
        {
            if (translation == null)
            {
                if (object instanceof ITextComponent)
                {
                    translation = new TranslationTextComponent(key);
                }
                else
                {
                    translation = new TranslationTextComponent(key, object);
                    continue;
                }
            }

            if (object instanceof ITextComponent)
            {
                translation.appendSibling((ITextComponent) object);
            }
            else if (object instanceof String)
            {
                boolean isInArgs = false;
                for (final Object obj : translation.getFormatArgs())
                {
                    if (obj.equals(object))
                    {
                        isInArgs = true;
                        break;
                    }
                }

                if (!isInArgs)
                {
                    translation.appendText((String) object);
                }
            }
        }

        if (translation == null)
        {
            translation = new TranslationTextComponent(key);
        }

        return translation;
    }

    /**
     * Localize a string and use String.format().
     *
     * @param inputKey  translation key.
     * @param args Objects for String.format().
     * @return Localized string.
     */
    public static String format(final String inputKey, final Object... args)
    {
        final String key = inputKey.toLowerCase(Locale.US);
        final String result;
        if (args.length == 0)
        {
            result = new TranslationTextComponent(key).getUnformattedComponentText();
        }
        else
        {
            result = new TranslationTextComponent(key, args).getUnformattedComponentText();
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
    public static void sendPlayersMessage(@Nullable final List<PlayerEntity> players, final String key, final Object... message)
    {
        if (players == null || players.isEmpty())
        {
            return;
        }

        final ITextComponent textComponent = buildChatComponent(key, message);

        for (@NotNull final PlayerEntity player : players)
        {
            player.sendMessage(textComponent);
        }
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

            //Trust me, Minecraft.getInstance() can be null, when you run Data Generators!
            String locale = DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance() == null ? null : Minecraft.getInstance().gameSettings.language);

            if (locale == null)
            {
                locale = defaultLocale;
            }
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(String.format(fileLoc, locale));
            if (is == null)
            {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(String.format(fileLoc, defaultLocale));
            }
            languageMap = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), new TypeToken<Map<String, String>>(){
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
