package com.ldtteam.structurize.config;

import com.google.common.base.Function;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.network.messages.SyncSettingsToServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.ValueSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Blueprint render setting singleton.
 * This class gets shared via network.
 */
public class BlueprintRenderSettings
{
    /**
     * If this player wants to receive shared previews or not
     */
    public static final SharedSettings<Boolean> DISPLAY_SHARED =
        new SharedSettings<>(Structurize.getConfig().getClient().displayShared,
            FriendlyByteBuf::writeBoolean,
            FriendlyByteBuf::readBoolean);

    /**
     * Singleton instance of the settings for the client side.
     */
    public static final BlueprintRenderSettings instance = new BlueprintRenderSettings();

    /**
     * All the settings.
     */
    private final List<Object> renderSettings;

    /**
     * Private constructor for internal use.
     */
    private BlueprintRenderSettings()
    {
        renderSettings = null;
    }

    /**
     * Create a new settings instance from bytebuf.
     * @param byteBuf the buffer to read it in from.
     */
    public BlueprintRenderSettings(final FriendlyByteBuf byteBuf)
    {
        renderSettings = new ArrayList<>(SharedSettings.VALUES.size());

        for (final SharedSettings<?> setting : SharedSettings.VALUES)
        {
            renderSettings.add(setting.bufferReader.apply(byteBuf));
        }
    }

    /**
     * Write the settings to bytebuf.
     * @param byteBuf the buffer to write them to.
     */
    public void writeToBuf(final FriendlyByteBuf byteBuf)
    {
        if (renderSettings == null) // client
        {
            for (final SharedSettings<?> setting : SharedSettings.VALUES)
            {
                setting.writeClientToBuf(byteBuf);
            }
        }
        else // server
        {
            for (final SharedSettings<?> setting : SharedSettings.VALUES)
            {
                setting.writeToBuf(byteBuf, renderSettings.get(setting.id));
            }
        }
    }

    /**
     * @param  key settings key
     * @return     settings value from given key
     */
    @SuppressWarnings("unchecked")
    public <T> T getSetting(final SharedSettings<T> key)
    {
        return renderSettings == null ?
            // client instance
            key.forgeConfig.get() :
            // server instance (or client instance from buffer)
            (T) renderSettings.get(key.id);
    }

    /**
     * @param key   settings key
     * @param value value which will be written into key
     */
    public <T> void setSetting(final SharedSettings<T> key, final T value)
    {
        if (renderSettings == null)
        {
            key.forgeConfig.set(value);
            Structurize.getConfig().onConfigValueEdit(key.forgeConfig);
        }
        else
        {
            renderSettings.set(key.id, value);
        }
    }

    /**
     * @return list of client config values via their origin (config or shared settings)
     */
    public static List<EitherConfig<?>> gatherClientRendererConfigs()
    {
        final List<EitherConfig<?>> result = new ArrayList<>();
        final Set<ConfigValue<?>> deduplicator = Collections.newSetFromMap(new IdentityHashMap<>());

        // add shared first so the gui knows the proper origin
        SharedSettings.VALUES.forEach(value -> {
            if (deduplicator.add(value.forgeConfig))
            {
                result.add(EitherConfig.shared(value));
            }
        });
        Structurize.getConfig().getClient().collectPreviewRendererSettings(value -> {
            if (deduplicator.add(value))
            {
                result.add(EitherConfig.normal(value));
            }
        });

        return result;
    }

    /**
     * Enum-like record for settings which should be shared through preview sharing
     */
    public record SharedSettings<T>(int id,
        ConfigValue<T> forgeConfig,
        BiConsumer<FriendlyByteBuf, T> bufferWriter,
        Function<FriendlyByteBuf, T> bufferReader)
    {
        public static final List<SharedSettings<?>> VALUES = new ArrayList<>();

        private SharedSettings(final ConfigValue<T> forgeConfig,
            final BiConsumer<FriendlyByteBuf, T> bufferWriter,
            final Function<FriendlyByteBuf, T> bufferReader)
        {
            // id from size ensures that #renderSettings have same order
            this(VALUES.size(), forgeConfig, bufferWriter, bufferReader);
            VALUES.add(this);
        }

        public void writeClientToBuf(final FriendlyByteBuf buffer)
        {
            bufferWriter.accept(buffer, forgeConfig.get());
        }

        @SuppressWarnings("unchecked")
        public void writeToBuf(final FriendlyByteBuf buffer, final Object obj)
        {
            bufferWriter.accept(buffer, (T) obj);
        }
    }

    /**
     * Wrapper to merge normal and shared configs
     */
    public record EitherConfig<T>(ConfigValue<T> normal, SharedSettings<T> wrapped)
    {
        public static <U> EitherConfig<U> normal(final ConfigValue<U> configValue)
        {
            return new EitherConfig<>(configValue, null);
        }

        public static <U> EitherConfig<U> shared(final SharedSettings<U> sharedSettings)
        {
            return new EitherConfig<>(null, sharedSettings);
        }

        public Optional<ValueSpec> getValueSpec()
        {
            return Structurize.getConfig().getSpecFromValue(getConfigValue());
        }

        private ConfigValue<T> getConfigValue()
        {
            return normal != null ? normal : wrapped.forgeConfig;
        }

        public T getValue(final BlueprintRenderSettings blueprintRenderSettings)
        {
            return normal != null ? normal.get() : blueprintRenderSettings.getSetting(wrapped);
        }

        public void setValue(final BlueprintRenderSettings blueprintRenderSettings, final T value)
        {
            if (normal != null)
            {
                normal.set(value);
                Structurize.getConfig().onConfigValueEdit(normal);
            }
            else
            {
                blueprintRenderSettings.setSetting(wrapped, value);
                Network.getNetwork().sendToServer(new SyncSettingsToServer(blueprintRenderSettings));
            }
        }
    }
}
