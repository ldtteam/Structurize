package com.ldtteam.structurize.config;

import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * Blueprint render setting singleton.
 */
public class BlueprintRenderSettings
{
    /**
     * Singleton instance of the settings for the client side.
     */
    public static BlueprintRenderSettings instance = new BlueprintRenderSettings();

    /**
     * All the settings.
     */
    public Map<String, Boolean> renderSettings = new HashMap<>();

    /**
     * Private constructor for internal use.
     */
    private BlueprintRenderSettings()
    {
        // Not needed.
    }

    /**
     * Create a new settings instance from bytebuf.
     * @param byteBuf the buffer to read it in from.
     */
    public BlueprintRenderSettings(final FriendlyByteBuf byteBuf)
    {
        final int size = byteBuf.readInt();
        for (int i = 0; i < size; i++)
        {
            renderSettings.put(byteBuf.readUtf(32767), byteBuf.readBoolean());
        }
    }

    /**
     * Write the settings to bytebuf.
     * @param byteBuf the buffer to write them to.
     */
    public void writeToBuf(final FriendlyByteBuf byteBuf)
    {
        byteBuf.writeInt(renderSettings.size());
        for (final Map.Entry<String, Boolean> entry : renderSettings.entrySet())
        {
            byteBuf.writeUtf(entry.getKey());
            byteBuf.writeBoolean(entry.getValue());
        }
    }

    /**
     * Register a new setting.
     * @param key the settings key.
     * @param value the settings value.
     */
    public void registerSetting(final String key, final boolean value)
    {
        renderSettings.put(key, value);
    }
}
