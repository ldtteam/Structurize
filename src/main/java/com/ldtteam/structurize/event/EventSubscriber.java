package com.ldtteam.structurize.event;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.commands.EntryPoint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.ServerUUIDMessage;
import com.ldtteam.structurize.network.messages.StructurizeStylesMessage;
import com.ldtteam.structurize.update.DomumOrnamentumUpdateHandler;
import com.ldtteam.structurize.update.UpdateMode;
import com.ldtteam.structurize.util.BackUpHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fmlserverevents.FMLServerStartedEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class with methods for receiving various forge events
 */
public class EventSubscriber
{
    /**
     * Private constructor to hide implicit public one.
     */
    private EventSubscriber()
    {
        /*
         * Intentionally left empty
         */
    }

    /**
     * Called when world is about to load.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event)
    {
        EntryPoint.register(event.getDispatcher(), event.getEnvironment());
    }

    @SubscribeEvent
    public static void onServerStarted(final FMLServerStartedEvent event)
    {
        Structures.init();
        BackUpHelper.loadLinkSessionManager();
    }

    @SubscribeEvent
    public static void onServerStopping(final FMLServerStoppingEvent event)
    {
        BackUpHelper.saveLinkSessionManager();
    }

    /**
     * Called when a player logs in. If the joining player is a MP-Player, sends
     * all possible styles in a message.
     *
     * @param event {@link net.minecraftforge.event.entity.player.PlayerEvent}
     */
    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getPlayer() instanceof ServerPlayer)
        {
            Network.getNetwork().sendToPlayer(new ServerUUIDMessage(), (ServerPlayer) event.getPlayer());
            Network.getNetwork().sendToPlayer(new StructurizeStylesMessage(), (ServerPlayer) event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        if (event.world.isClientSide)
        {
            return;
        }
        Manager.onWorldTick((ServerLevel) event.world);
    }


    @SubscribeEvent
    public static void onMissingBlockMappings(final RegistryEvent.MissingMappings<Block> missingBlockEvent)
    {
        final ImmutableList<RegistryEvent.MissingMappings.Mapping<Block>> missingBlocks = missingBlockEvent.getMappings(Constants.MOD_ID);
        if (missingBlocks.isEmpty())
        {
            return;
        }

        handleMissingMappingsDueToRestructureIn9af7543d7dcefa0bf7b52c2e809d6c8d48b803a6(missingBlocks, ForgeRegistries.BLOCKS);
    }

    @SubscribeEvent
    public static void onMissingItemMappings(final RegistryEvent.MissingMappings<Item> missingBlockEvent)
    {
        final ImmutableList<RegistryEvent.MissingMappings.Mapping<Item>> missingItems = missingBlockEvent.getMappings(Constants.MOD_ID);
        if (missingItems.isEmpty())
        {
            return;
        }

        handleMissingMappingsDueToRestructureIn9af7543d7dcefa0bf7b52c2e809d6c8d48b803a6(missingItems, ForgeRegistries.ITEMS);
    }

    private static <T extends IForgeRegistryEntry<T>> void handleMissingMappingsDueToRestructureIn9af7543d7dcefa0bf7b52c2e809d6c8d48b803a6(final ImmutableList<RegistryEvent.MissingMappings.Mapping<T>> missingMappings, final IForgeRegistry<T> registry)
    {
        final Map<Pattern, List<String>> replacementPatterns = ImmutableMap.<Pattern, List<String>>builder()
                                                           .put(
                                                             Pattern.compile("(\\S+)?stone_brick(\\S+)?"),
                                                             Lists.newArrayList("stone_bricks")
                                                           )
                                                           .put(
                                                             Pattern.compile("(\\S+)?cobble_stone(\\S+)?"),
                                                             Lists.newArrayList("cobblestone")
                                                           )
                                                           .put(
                                                             Pattern.compile("(\\S+)?blockbrownbrick(\\S+)?"),
                                                             Lists.newArrayList(
                                                               "brown_brick",
                                                               "brown_brick_",
                                                               "_brown_brick",
                                                               "_brown_brick_"
                                                             )
                                                           )
                                                           .put(
                                                             Pattern.compile("(\\S+)?blockbeigebrick(\\S+)?"),
                                                             Lists.newArrayList(
                                                               "beige_brick",
                                                               "beige_brick_",
                                                               "_beige_brick",
                                                               "_beige_brick_"
                                                             )
                                                           )
                                                           .put(
                                                             Pattern.compile("(\\S+)?blockcreambrick(\\S+)?"),
                                                             Lists.newArrayList(
                                                               "cream_brick",
                                                               "cream_brick_",
                                                               "_cream_brick",
                                                               "_cream_brick_"
                                                             )
                                                           )
                                                           .put(
                                                             Pattern.compile("(\\S+)?_brick_(\\S+)?"),
                                                             Lists.newArrayList("_bricks_")
                                                           )
                                                           .build();


        missingMappings.forEach(mapping -> {
            if (mapping.key.getPath().equals("placeholderblock"))
            {
                mapping.ignore();
                return;
            }

            final String namespace = mapping.key.getNamespace();
            final String path = mapping.key.getPath();
            for (final Map.Entry<Pattern, List<String>> patternToReplace : replacementPatterns.entrySet())
            {
                final Matcher patternMatcher = patternToReplace.getKey().matcher(path);
                if (patternMatcher.find()) {
                    for (final String replacementCandidate : patternToReplace.getValue())
                    {
                        final String replacedPath = patternMatcher.replaceAll("$1" + replacementCandidate + "$2");
                        final ResourceLocation remappedObjectKey = new ResourceLocation(namespace, replacedPath);

                        if (registry.containsKey(remappedObjectKey))
                        {
                            final T remappedObject = registry.getValue(remappedObjectKey);
                            mapping.remap(remappedObject);
                            return;
                        }
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onGatherServerLevelCapabilities(final AttachCapabilitiesEvent<Level> attachCapabilitiesEvent)
    {
        if (!(attachCapabilitiesEvent.getObject() instanceof final ServerLevel serverLevel))
            return;

        if (Structurize.getConfig().getServer().updateMode.get() == UpdateMode.DISABLED)
            return;

            for (int chunkX = Structurize.getConfig().getServer().updateStartPos.get().get(0); chunkX < Structurize.getConfig().getServer().updateEndPos.get().get(0); chunkX++)
            {
                for (int chunkY = Structurize.getConfig().getServer().updateStartPos.get().get(1); chunkY < Structurize.getConfig().getServer().updateEndPos.get().get(1); chunkY++)
                {
                    CompoundTag chunkTag = null;
                    try
                    {
                        chunkTag = serverLevel.getChunkSource().chunkMap.readChunk(new ChunkPos(chunkX,chunkY));
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    if (chunkTag != null)
                    {
                        if (Structurize.getConfig().getServer().updateMode.get() == UpdateMode.DOMUM_ORNAMENTUM)
                            DomumOrnamentumUpdateHandler.updateChunkTag(chunkTag, new ChunkPos(chunkX, chunkY));
                    }
                }
            }
    }
}
