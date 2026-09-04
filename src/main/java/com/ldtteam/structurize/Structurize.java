package com.ldtteam.structurize;

import com.ldtteam.structurize.blueprints.v1.DataFixerUtils;
import com.ldtteam.structurize.blueprints.v1.DataVersion;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.config.Configuration;
import com.ldtteam.structurize.event.ClientEventSubscriber;
import com.ldtteam.structurize.event.ClientLifecycleSubscriber;
import com.ldtteam.structurize.event.EventSubscriber;
import com.ldtteam.structurize.event.LifecycleSubscriber;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.proxy.ClientProxy;
import com.ldtteam.structurize.proxy.IProxy;
import com.ldtteam.structurize.proxy.ServerProxy;
import com.ldtteam.structurize.blockentities.ModBlockEntities;
import com.ldtteam.structurize.storage.ClientFutureProcessor;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.ClientStructurePackLoader;
import com.ldtteam.structurize.storage.ServerStructurePackLoader;
import com.ldtteam.structurize.storage.rendering.ServerPreviewDistributor;
import net.minecraft.util.datafix.DataFixers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

/**
 * Mod main class.
 * The value in annotation should match an entry in the META-INF/mods.toml file.
 */
@Mod(Constants.MOD_ID)
public class Structurize
{
    /**
     * The proxy.
     */
    public static final IProxy proxy = createProxy();

    /**
     * The config instance.
     */
    private static Configuration config;

    /**
     * Mod init, registers events to their respective busses
     */
    public Structurize(final FMLModContainer modContainer, final Dist dist)
    {
        config = new Configuration(modContainer, modContainer.getEventBus());

        final IEventBus modBus = modContainer.getEventBus();
        final IEventBus gameBus = NeoForge.EVENT_BUS;
        ModBlocks.getRegistry().register(modBus);
        ModItems.getRegistry().register(modBus);
        ModBlockEntities.getRegistry().register(modBus);
        ModItemGroups.TAB_REG.register(modBus);

        modBus.register(LifecycleSubscriber.class);
        gameBus.register(EventSubscriber.class);

        if (dist.isClient())
        {
            gameBus.register(ClientStructurePackLoader.class);
            gameBus.register(ClientFutureProcessor.class);
            modBus.register(ClientLifecycleSubscriber.class);
            gameBus.register(ClientEventSubscriber.class);
        }
        else
        {
            ServerStructurePackLoader.onServerStarting();
        }

        gameBus.register(ServerStructurePackLoader.class);
        gameBus.register(ServerPreviewDistributor.class);
        gameBus.register(ServerFutureProcessor.class);

        if (DataFixerUtils.isVanillaDF)
        {
            if ((DataFixers.getDataFixer().getSchema(Integer.MAX_VALUE - 1).getVersionKey()) >= DataVersion.UPCOMING.getDataVersion() * 10)
            {
                throw new RuntimeException("You are trying to run old mod on much newer vanilla. Missing some newest data versions. Please update com/ldtteam/structures/blueprints/v1/DataVersion");
            }
            else if (!FMLEnvironment.isProduction() && DataVersion.CURRENT == DataVersion.UPCOMING)
            {
                throw new RuntimeException("Missing some newest data versions. Please update com/ldtteam/structures/blueprints/v1/DataVersion");
            }
        }
        else
        {
            Log.getLogger().error("----------------------------------------------------------------- \n "
                                    + "Invalid DataFixer detected, schematics might not paste correctly! \n"
                                    +  "The following DataFixer was added: " + DataFixers.getDataFixer().getClass() + "\n"
                                    + "-----------------------------------------------------------------");
        }
    }

    private static IProxy createProxy()
    {
        return FMLEnvironment.getDist().isClient() ? new ClientProxy() : new ServerProxy();
    }

    /**
     * Get the config handler.
     *
     * @return the config handler.
     */
    public static Configuration getConfig()
    {
        return config;
    }
}
