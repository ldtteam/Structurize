package com.ldtteam.structurize.util;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.translation.LanguageMap;

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
        return LanguageMap.getInstance().translateKey(key);
    }
}
