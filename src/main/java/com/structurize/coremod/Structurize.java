package com.structurize.coremod;

import com.structurize.api.util.constant.Constants;
import com.structurize.compat.optifine.OptifineCompat;
import com.structurize.coremod.event.FMLEventHandler;
import com.structurize.coremod.management.Structures;
import com.structurize.coremod.network.messages.*;
import com.structurize.coremod.placementhandlers.CopyPastePlacementHandlers;
import com.structurize.coremod.proxy.IProxy;
import com.structurize.structures.helpers.Structure;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber
@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.VERSION, dependencies="after:gbook",
  /*dependencies = Constants.FORGE_VERSION,*/ acceptedMinecraftVersions = Constants.MC_VERSION)
public class Structurize
{
    private static final Logger      logger = LogManager.getLogger(Constants.MOD_ID);
    /**
     * Forge created instance of the Mod.
     */
    @Mod.Instance(Constants.MOD_ID)
    public static        Structurize instance;
    /**
     * Access to the proxy associated with your current side. Variable updated
     * by forge.
     */
    @SidedProxy(clientSide = Constants.CLIENT_PROXY_LOCATION, serverSide = Constants.SERVER_PROXY_LOCATION)

    public static        IProxy      proxy;

    private static SimpleNetworkWrapper network;

    /**
     * Returns whether the side is client or not
     *
     * @return True when client, otherwise false
     */
    public static boolean isClient()
    {
        return proxy.isClient() && FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    /**
     * Returns whether the side is server or not
     *
     * @return True when server, otherwise false
     */
    public static boolean isServer()
    {
        return !proxy.isClient() && FMLCommonHandler.instance().getEffectiveSide().isServer();
    }

    /**
     * Getter for the structurize Logger.
     *
     * @return the logger.
     */
    public static Logger getLogger()
    {
        return logger;
    }

    /**
     * Event handler for forge pre init event.
     *
     * @param event the forge pre init event.
     */
    @Mod.EventHandler
    public void preInit(@NotNull final FMLPreInitializationEvent event)
    {
        Structure.originFolders.add(Constants.MOD_ID);
        proxy.registerEntities();
        proxy.registerEntityRendering();
        proxy.registerEvents();

        @NotNull final Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
        configuration.load();

        if (configuration.hasChanged())
        {
            configuration.save();
        }
    }

    /**
     * Event handler for forge init event.
     *
     * @param event the forge init event.
     */
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event)
    {
        initializeNetwork();

        proxy.registerTileEntities();

        proxy.registerTileEntityRendering();

        proxy.registerRenderer();

        CopyPastePlacementHandlers.initHandlers();
        OptifineCompat.getInstance().intialize();
    }

    private static synchronized void initializeNetwork()
    {
        int id = 0;
        network = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_NAME);

        getNetwork().registerMessage(ServerUUIDMessage.class, ServerUUIDMessage.class, ++id, Side.CLIENT);

        // Tool action messages
        getNetwork().registerMessage(BuildToolPasteMessage.class, BuildToolPasteMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ScanOnServerMessage.class, ScanOnServerMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(RemoveBlockMessage.class, RemoveBlockMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(RemoveEntityMessage.class, RemoveEntityMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ReplaceBlockMessage.class, ReplaceBlockMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(GenerateAndPasteMessage.class, GenerateAndPasteMessage.class, ++id, Side.SERVER);

        // Schematic transfer messages
        getNetwork().registerMessage(SchematicRequestMessage.class, SchematicRequestMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(SchematicSaveMessage.class, SchematicSaveMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(UndoMessage.class, UndoMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(StructurizeStylesMessage.class, StructurizeStylesMessage.class, ++id, Side.CLIENT);

        // Multiblock message
        getNetwork().registerMessage(MultiBlockChangeMessage.class, MultiBlockChangeMessage.class, ++id, Side.SERVER);

        // Client side only
        getNetwork().registerMessage(SaveScanMessage.class, SaveScanMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(SchematicSaveMessage.class, SchematicSaveMessage.class, ++id, Side.CLIENT);
    }

    @Mod.EventHandler
    public void serverAboutLoad(final FMLServerAboutToStartEvent event)
    {
        Structures.init();
    }

    public static SimpleNetworkWrapper getNetwork()
    {
        return network;
    }
}