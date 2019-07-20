package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Instances;
import com.ldtteam.structurize.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;

/**
 * Class with methods for receiving various forge events.
 * Methods are sorted according to time of execution.
 */
public class LifecycleSubscriber
{
    /**
     * Private constructor to hide implicit public one.
     */
    private LifecycleSubscriber()
    {
        /**
         * Intentionally left empty
         */
    }

    /**
     * Called when blocks are supposed to be registered.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onBlockRegistry(final RegistryEvent.Register<Block> event)
    {
        Instances.getModLogger().warn("RegistryEvent.Register<Block>");
    }

    /**
     * Called when items are supposed to be registered.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onItemRegistry(final RegistryEvent.Register<Item> event)
    {
        Instances.getModLogger().warn("RegistryEvent.Register<Item>");
        ModItems.registerItems(event.getRegistry());
    }

    /**
     * Called when mod is being initialized.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onModInit(final FMLCommonSetupEvent event)
    {
        Instances.getModLogger().warn("FMLCommonSetupEvent");
        Instances.getNetwork().registerCommonMessages();
    }

    /**
     * Called when client app is initialized.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onClientInit(final FMLClientSetupEvent event)
    {
        Instances.getModLogger().warn("FMLClientSetupEvent");
    }

    /**
     * Called when server app is initialized.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onDediServerInit(final FMLDedicatedServerSetupEvent event)
    {
        Instances.getModLogger().warn("FMLDedicatedServerSetupEvent");
    }

    /**
     * Called when mod is able to send IMCs.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void enqueueIMC(final InterModEnqueueEvent event)
    {
        Instances.getModLogger().warn("InterModEnqueueEvent");
        /*
         * InterModComms.sendTo("structurize", "helloworld", () -> {
         * return "Hello world";
         * });
         */
    }

    /**
     * Called when mod is able to receive IMCs.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void processIMC(final InterModProcessEvent event)
    {
        Instances.getModLogger().warn("InterModProcessEvent");
        /*
         * LOGGER.info("Got IMC {}", event.getIMCStream().map(m -> m.getMessageSupplier().get()).collect(Collectors.toList()));
         */
    }
}
