package com.ldtteam.structurize.config;

import com.ldtteam.structurize.config.AbstractConfiguration.ConfigWatcher;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForgeConfigSpec;
import net.neoforged.neoforge.common.NeoForgeConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.NeoForgeConfigSpec.ValueSpec;
import org.apache.commons.lang3.tuple.Pair;
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
     * @param modContainer from event
     */
    public Configuration(final ModContainer modContainer)
    {
        final Pair<ServerConfiguration, NeoForgeConfigSpec> ser = new NeoForgeConfigSpec.Builder().configure(ServerConfiguration::new);
        server = new ModConfig(ModConfig.Type.SERVER, ser.getRight(), modContainer);
        serverConfig = ser.getLeft();
        modContainer.addConfig(server);

        if (FMLEnvironment.dist.isClient())
        {
            final Pair<ClientConfiguration, NeoForgeConfigSpec> cli = new NeoForgeConfigSpec.Builder().configure(ClientConfiguration::new);
            client = new ModConfig(ModConfig.Type.CLIENT, cli.getRight(), modContainer);
            clientConfig = cli.getLeft();
            modContainer.addConfig(client);

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
    public void onConfigLoad(final ModConfig modConfig)
    {
        if (client != null && modConfig.getSpec() == client.getSpec())
        {
            clientConfig.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
        else if (modConfig.getSpec() == server.getSpec())
        {
            serverConfig.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
    }

    /**
     * iterate watchers and fire changes if needed
     */
    public void onConfigReload(final ModConfig modConfig)
    {
        if (client != null && modConfig.getSpec() == client.getSpec())
        {
            clientConfig.watchers.forEach(ConfigWatcher::compareAndFireChangeEvent);
        }
        else if (modConfig.getSpec() == server.getSpec())
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
                if (configWatcher.sameForgeConfig(configValue))
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
        return valueSpecCache.computeIfAbsent(value, key -> {
            for (final ModConfig cfg : activeModConfigs)
            {
                if (cfg.getSpec().get(value.getPath()) instanceof final ValueSpec valueSpec)
                {
                    return Optional.of(valueSpec);
                }
            }

            if (!FMLEnvironment.production)
            {
                throw new RuntimeException("Cannot find backing ValueSpec for: " + value.getPath());
            }

            return Optional.empty();
        });
    }
}
