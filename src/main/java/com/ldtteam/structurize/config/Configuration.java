package com.ldtteam.structurize.config;

import com.ldtteam.structurize.config.AbstractConfiguration.ConfigWatcher;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.ValueSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Mod root configuration.
 */
public class Configuration
{
    /**
     * Loaded clientside, not synced
     */
    private final ModConfig client;
    private final ClientConfiguration clientConfig;
    /**
     * Loaded serverside, synced on connection
     */
    private final ModConfig server;
    private final ServerConfiguration serverConfig;

    private final ModConfig[] activeModConfigs;
    private final AbstractConfiguration[] activeConfigs;


    /**
     * Builds configuration tree.
     *
     * @param modContainer mod container
     * @param modBus       mod lifecycle event bus
     */
    public Configuration(final ModContainer modContainer, final IEventBus modBus)
    {
        final Pair<ServerConfiguration, ModConfig> ser =
            register(ServerConfiguration::new, ModConfig.Type.SERVER, modContainer);
        server = ser.getRight();
        serverConfig = ser.getLeft();

        if (FMLEnvironment.getDist().isClient())
        {
            final Pair<ClientConfiguration, ModConfig> cli =
                register(ClientConfiguration::new, ModConfig.Type.CLIENT, modContainer);
            client = cli.getRight();
            clientConfig = cli.getLeft();

            activeModConfigs = new ModConfig[] {client, server};
            activeConfigs = new AbstractConfiguration[] {clientConfig, serverConfig};
        }
        else
        {
            client = null;
            clientConfig = null;

            activeModConfigs = new ModConfig[] {server};
            activeConfigs = new AbstractConfiguration[] {serverConfig};
        }

        modBus.addListener(ModConfigEvent.Loading.class, event -> onConfigLoad(event.getConfig()));
        modBus.addListener(ModConfigEvent.Reloading.class, event -> onConfigReload(event.getConfig()));
    }

    private <T extends AbstractConfiguration> Pair<T, ModConfig> register(
        final Function<ModConfigSpec.Builder, T> factory,
        final ModConfig.Type type,
        final ModContainer modContainer)
    {
        if (type == ModConfig.Type.CLIENT && !FMLEnvironment.getDist().isClient())
        {
            throw new IllegalStateException("Client configuration cannot be created on the dedicated server");
        }

        final Pair<T, ModConfigSpec> built = new ModConfigSpec.Builder().configure(factory);
        return Pair.of(built.getLeft(), ConfigTracker.INSTANCE.registerConfig(type, built.getRight(), modContainer));
    }

    public ClientConfiguration getClient()
    {
        return clientConfig;
    }

    public ServerConfiguration getServer()
    {
        return serverConfig;
    }

    /**
     * cache starting values for watchers
     */
    private void onConfigLoad(final ModConfig modConfig)
    {
        if (client != null && modConfig.getSpec() == client.getSpec())
        {
            clientConfig.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
        else if (server != null && modConfig.getSpec() == server.getSpec())
        {
            serverConfig.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
    }

    /**
     * iterate watchers and fire changes if needed
     */
    private void onConfigReload(final ModConfig modConfig)
    {
        if (client != null && modConfig.getSpec() == client.getSpec())
        {
            clientConfig.watchers.forEach(ConfigWatcher::compareAndFireChangeEvent);
        }
        else if (server != null && modConfig.getSpec() == server.getSpec())
        {
            serverConfig.watchers.forEach(ConfigWatcher::compareAndFireChangeEvent);
        }
    }

    /**
     * Setter wrapper so watchers are fine.
     * This should be called from any code that manually changes ConfigValues using set functions.
     * (Mostly done by settings UIs)
     */
    public <T> void set(final ConfigValue<T> configValue, final T value)
    {
        configValue.set(value);
        configValue.save();
        onConfigValueEdit(configValue);
    }

    /**
     * This should be called from any code that manually changes ConfigValues using set functions.
     * (Mostly done by settings UIs)
     *
     * @param configValue which config value was changed
     */
    public void onConfigValueEdit(final ConfigValue<?> configValue)
    {
        for (final AbstractConfiguration cfg : activeConfigs)
        {
            for (final ConfigWatcher<?> configWatcher : cfg.watchers)
            {
                if (configWatcher.isSameForgeConfig(configValue))
                {
                    configWatcher.compareAndFireChangeEvent();
                }
            }
        }
    }

    private final Map<ConfigValue<?>, Optional<ValueSpec>> valueSpecCache = new IdentityHashMap<>();

    /**
     * @param value config value from this mod
     * @return value spec, crashes in dev if not found
     */
    public Optional<ValueSpec> getSpecFromValue(final ConfigValue<?> value)
    {
        return Optional.of(value.getSpec());
    }
}
