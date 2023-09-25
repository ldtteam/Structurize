package com.ldtteam.structurize.config;

import com.ldtteam.structurize.config.AbstractConfiguration.ConfigWatcher;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.ValueSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.tuple.Pair;
import java.util.IdentityHashMap;
import java.util.List;
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

    /**
     * Builds configuration tree.
     *
     * @param modContainer from event
     */
    public Configuration(final ModContainer modContainer)
    {
        final Pair<ClientConfiguration, ForgeConfigSpec> cli = new ForgeConfigSpec.Builder().configure(ClientConfiguration::new);
        final Pair<ServerConfiguration, ForgeConfigSpec> ser = new ForgeConfigSpec.Builder().configure(ServerConfiguration::new);
        client = new ModConfig(ModConfig.Type.CLIENT, cli.getRight(), modContainer);
        server = new ModConfig(ModConfig.Type.SERVER, ser.getRight(), modContainer);
        clientConfig = cli.getLeft();
        serverConfig = ser.getLeft();
        modContainer.addConfig(client);
        modContainer.addConfig(server);
    }

    public ClientConfiguration getClient()
    {
        return clientConfig;
    }

    public ServerConfiguration getServer()
    {
        return serverConfig;
    }

    public void onConfigLoad(final ModConfig modConfig)
    {
        if (modConfig.getSpec() == client.getSpec())
        {
            clientConfig.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
        else if (modConfig.getSpec() == server.getSpec())
        {
            serverConfig.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
    }

    public void onConfigReload(final ModConfig modConfig)
    {
        if (modConfig.getSpec() == client.getSpec())
        {
            clientConfig.watchers.forEach(ConfigWatcher::compareAndFireChangeEvent);
        }
        else if (modConfig.getSpec() == server.getSpec())
        {
            serverConfig.watchers.forEach(ConfigWatcher::compareAndFireChangeEvent);
        }
    }

    public void onConfigValueEdit(final ConfigValue<?> configValue)
    {
        for (final var watchers : (List<ConfigWatcher<?>>[]) new List[] {clientConfig.watchers, serverConfig.watchers})
        {
            for (final ConfigWatcher<?> configWatcher : watchers)
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
            for (final ModConfig cfg : new ModConfig[] {client, server})
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
