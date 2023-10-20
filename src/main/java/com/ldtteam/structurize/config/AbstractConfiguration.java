package com.ldtteam.structurize.config;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.server.TickTask;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.ForgeConfigSpec.LongValue;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractConfiguration
{
    private static final String DEFAULT_KEY_PREFIX = "structurize.config.default.";
    public static final String COMMENT_SUFFIX = ".comment";

    final List<ConfigWatcher<?>> watchers = new ArrayList<>();

    protected static void createCategory(final Builder builder, final String key)
    {
        builder.comment(LanguageHandler.translateKey(commentTKey(key))).push(key);
    }

    protected static void swapToCategory(final Builder builder, final String key)
    {
        finishCategory(builder);
        createCategory(builder, key);
    }

    protected static void finishCategory(final Builder builder)
    {
        builder.pop();
    }

    private static String nameTKey(final String key)
    {
        return Constants.MOD_ID + ".config." + key;
    }

    private static String commentTKey(final String key)
    {
        return nameTKey(key) + COMMENT_SUFFIX;
    }

    private static Builder buildBase(final Builder builder, final String key, final String defaultDesc)
    {
        return builder.comment(LanguageHandler.translateKey(commentTKey(key)) + " " + defaultDesc).translation(nameTKey(key));
    }

    // TODO: inline with icu component
    private static String translate(final String key, final Object... args)
    {
        return LanguageHandler.translateKey(key).formatted(args);
    }

    protected static BooleanValue defineBoolean(final Builder builder, final String key, final boolean defaultValue)
    {
        return buildBase(builder, key, translate(DEFAULT_KEY_PREFIX + "boolean", defaultValue)).define(key, defaultValue);
    }

    protected static IntValue defineInteger(final Builder builder, final String key, final int defaultValue)
    {
        return defineInteger(builder, key, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    protected static IntValue defineInteger(final Builder builder, final String key, final int defaultValue, final int min, final int max)
    {
        return buildBase(builder, key, translate(DEFAULT_KEY_PREFIX + "number", defaultValue, min, max)).defineInRange(key, defaultValue, min, max);
    }

    protected static ConfigValue<String> defineString(final Builder builder, final String key, final String defaultValue)
    {
        return buildBase(builder, key, translate(DEFAULT_KEY_PREFIX + "string", defaultValue)).define(key, defaultValue);
    }

    protected static LongValue defineLong(final Builder builder, final String key, final long defaultValue)
    {
        return defineLong(builder, key, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    protected static LongValue defineLong(final Builder builder, final String key, final long defaultValue, final long min, final long max)
    {
        return buildBase(builder, key, translate(DEFAULT_KEY_PREFIX + "number", defaultValue, min, max)).defineInRange(key, defaultValue, min, max);
    }

    protected static DoubleValue defineDouble(final Builder builder, final String key, final double defaultValue)
    {
        return defineDouble(builder, key, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    protected static DoubleValue defineDouble(final Builder builder, final String key, final double defaultValue, final double min, final double max)
    {
        return buildBase(builder, key, translate(DEFAULT_KEY_PREFIX + "number", defaultValue, min, max)).defineInRange(key, defaultValue, min, max);
    }

    protected static <T> ConfigValue<List<? extends T>> defineList(
        final Builder builder,
        final String key,
        final List<? extends T> defaultValue,
        final Predicate<Object> elementValidator)
    {
        return buildBase(builder, key, "").defineList(key, defaultValue, elementValidator);
    }

    protected static <V extends Enum<V>> EnumValue<V> defineEnum(final Builder builder, final String key, final V defaultValue)
    {
        return buildBase(builder,
            key,
            translate(DEFAULT_KEY_PREFIX + "enum",
                defaultValue,
                Arrays.stream(defaultValue.getDeclaringClass().getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "))))
            .defineEnum(key, defaultValue);
    }

    protected <T> void addWatcher(final ConfigValue<T> configValue, final ConfigListener<T> listener)
    {
        watchers.add(new ConfigWatcher<>(listener, configValue));
    }

    @SuppressWarnings("unchecked")
    protected void addWatcher(final Runnable listener, final ConfigValue<?>... configValues)
    {
        final ConfigListener<Object> typedListener = (o, n) -> listener.run();
        for (final ConfigValue<?> c : configValues)
        {
            watchers.add(new ConfigWatcher<>(typedListener, (ConfigValue<Object>) c));
        }
    }

    @FunctionalInterface
    public static interface ConfigListener<T>
    {
        void onChange(T oldValue, T newValue);
    }

    /**
     * synchronized due to nature of config events
     */
    static class ConfigWatcher<T>
    {
        private final ConfigListener<T> listener;
        private final ConfigValue<T> forgeConfig;
        
        private T lastValue;
        
        private ConfigWatcher(final ConfigListener<T> listener, final ConfigValue<T> forgeConfig)
        {
            this.listener = listener;
            this.forgeConfig = forgeConfig;
        }

        boolean sameForgeConfig(final ConfigValue<?> other)
        {
            return other == forgeConfig;
        }
        
        synchronized void cacheLastValue()
        {
            lastValue = forgeConfig.get();
        }

        synchronized void compareAndFireChangeEvent()
        {
            final T newValue = forgeConfig.get();

            if (!newValue.equals(lastValue))
            {
                LogicalSidedProvider.WORKQUEUE.get(FMLEnvironment.dist.isClient() ? LogicalSide.CLIENT : LogicalSide.SERVER)
                    .tell(new TickTask(0, () -> listener.onChange(lastValue, newValue)));
                lastValue = newValue;
            }
        }
    }
}
