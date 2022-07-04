package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.network.messages.BlueprintSyncMessage;
import com.ldtteam.structurize.network.messages.BuildToolPlacementMessage;
import com.ldtteam.structurize.network.messages.ClientBlueprintRequestMessage;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.TickedWorldOperation;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Mirror;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.ldtteam.structurize.api.util.constant.Constants.BLUEPRINT_FOLDER;
import static com.ldtteam.structurize.api.util.constant.Constants.CLIENT_FOLDER;

/**
 * Class where blueprint placement is handled.
 */
public class BlueprintPlacementHandling
{
    /**
     * List of blueprints to be loaded and placed.
     */
    public static Queue<Tuple<Future<Blueprint>, BuildToolPlacementMessage>> loadingStorage = new LinkedList<>();

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        if (!loadingStorage.isEmpty())
        {
            final Tuple<Future<Blueprint>, BuildToolPlacementMessage> tuple = loadingStorage.peek();
            if (!tuple.getA().isDone())
            {
                return;
            }

            if (tuple.getB().world == event.world)
            {
                return;
            }

            // Remove
            loadingStorage.poll();
            try
            {
                final Blueprint blueprint = tuple.getA().get();
                final BuildToolPlacementMessage message = tuple.getB();
                if (blueprint == null)
                {
                    Network.getNetwork().sendToPlayer(new ClientBlueprintRequestMessage(message), message.player);
                    return;
                }

                if (message.type == BuildToolPlacementMessage.HandlerType.Survival)
                {
                    SurvivalBlueprintHandlers.getHandler(message.handlerId).handle(blueprint, message.clientPack, message.world, message.player, message.pos, new PlacementSettings(message.mirror, message.rotation));
                    return;
                }

                final IStructureHandler structure = new CreativeStructureHandler(message.world,
                  message.pos,
                  blueprint,
                  new PlacementSettings(message.mirror, message.rotation),
                  message.type == BuildToolPlacementMessage.HandlerType.Pretty);
                structure.getBluePrint().rotateWithMirror(message.rotation, message.mirror == Mirror.NONE ? Mirror.NONE : Mirror.FRONT_BACK, message.world);

                final StructurePlacer instantPlacer = new StructurePlacer(structure);
                Manager.addToQueue(new TickedWorldOperation(instantPlacer, message.player));
            }
            catch (final InterruptedException | ExecutionException ex)
            {
                Log.getLogger().error("Something went wrong getting blueprint for placement.", ex);
            }
        }
    }

    /**
     * Handle placement with the help of a placement message.
     * @param message the placement message.
     */
    public static void handlePlacement(final BuildToolPlacementMessage message)
    {
        if (!StructurePacks.packMetas.containsKey(message.structurePackId))
        {
            BlueprintPlacementHandling.loadingStorage.add(new Tuple<>(Util.ioPool().submit(() -> {
                final Path blueprintPath = new File(".").toPath()
                  .resolve(BLUEPRINT_FOLDER)
                  .resolve(CLIENT_FOLDER)
                  .resolve(message.player.getUUID().toString())
                  .resolve(message.structurePackId)
                  .resolve(message.blueprintPath);

                return StructurePacks.getBlueprint(blueprintPath);
            }), message));
        }
        else
        {
            BlueprintPlacementHandling.loadingStorage.add(new Tuple<>(StructurePacks.getBlueprintFuture(message.structurePackId, message.blueprintPath), message));
        }
    }

    /**
     * Handle placement and store the received client blueprint.
     * @param blueprintSyncMessage the message with all the data.
     */
    public static void handlePlacement(final BlueprintSyncMessage blueprintSyncMessage, final ServerPlayer player)
    {
        loadingStorage.add(new Tuple<>(Util.ioPool().submit(() ->
          {
              final Path blueprintPath = new File(".").toPath()
                .resolve(BLUEPRINT_FOLDER)
                .resolve(CLIENT_FOLDER)
                .resolve(player.getUUID().toString())
                .resolve(blueprintSyncMessage.structurePackId)
                .resolve(blueprintSyncMessage.blueprintPath);

              try
              {
                  Files.createDirectories(blueprintPath.getParent());
              }
              catch (IOException e)
              {
                  Log.getLogger().error("Failed to create folder structure for client blueprint: " + blueprintSyncMessage.blueprintPath, e);
                  return null;
              }

              try
              {
                  Files.write(blueprintPath, blueprintSyncMessage.blueprintData);
              }
              catch (IOException e)
              {
                  Log.getLogger().error("Failed to save blueprint file for client blueprint: " + blueprintSyncMessage.blueprintPath, e);
              }

              return StructurePacks.getBlueprint(blueprintPath);
          }), new BuildToolPlacementMessage(blueprintSyncMessage, player, player.level)));
    }
}
