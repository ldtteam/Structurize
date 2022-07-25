package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.Utils;
import com.ldtteam.structurize.blockentities.interfaces.ISpecialCreativeHandler;
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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;


import static com.ldtteam.structurize.api.util.constant.Constants.BLUEPRINT_FOLDER;
import static com.ldtteam.structurize.api.util.constant.Constants.CLIENT_FOLDER;

/**
 * Class where blueprint placement is handled.
 */
public class BlueprintPlacementHandling
{
    /**
     * Handle placement with the help of a placement message.
     * @param message the placement message.
     */
    public static void handlePlacement(final BuildToolPlacementMessage message)
    {
        if (!StructurePacks.packMetas.containsKey(message.structurePackId))
        {
            final Path blueprintPath = new File(".").toPath()
              .resolve(BLUEPRINT_FOLDER)
              .resolve(CLIENT_FOLDER)
              .resolve(message.player.getUUID().toString())
              .resolve(message.structurePackId)
              .resolve(message.blueprintPath);

            ServerBlueprintFutureProcessor.consumerQueue.add(new ServerBlueprintFutureProcessor.ProcessingData(StructurePacks.getBlueprintFuture(message.structurePackId, blueprintPath),
              message.world, (blueprint) -> {
                if (blueprint == null)
                {
                    Network.getNetwork().sendToPlayer(new ClientBlueprintRequestMessage(message), message.player);
                    return;
                }
                process(blueprint, message);
            }
            ));
        }
        else
        {
            ServerBlueprintFutureProcessor.consumerQueue.add(new ServerBlueprintFutureProcessor.ProcessingData(StructurePacks.getBlueprintFuture(message.structurePackId, message.blueprintPath),
              message.world, (blueprint) -> process(blueprint, message)));
        }
    }

    /**
     * Do the actual on-thread placement of the blueprint.
     * @param blueprint the blueprint.
     * @param message the context.
     */
    private static void process(Blueprint blueprint, BuildToolPlacementMessage message)
    {
        if (message.type == BuildToolPlacementMessage.HandlerType.Survival)
        {
            SurvivalBlueprintHandlers.getHandler(message.handlerId)
              .handle(blueprint,
                message.structurePackId,
                message.blueprintPath,
                message.clientPack,
                message.world,
                message.player,
                message.pos,
                new PlacementSettings(message.mirror, message.rotation));
            return;
        }

        Utils.playSuccessSound(message.player, message.pos);
        final BlockState anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
        blueprint.rotateWithMirror(message.rotation, message.mirror == Mirror.NONE ? Mirror.NONE : Mirror.FRONT_BACK, message.world);

        final IStructureHandler structure;
        if (anchor.getBlock() instanceof ISpecialCreativeHandler)
        {
           if (!((ISpecialCreativeHandler) anchor.getBlock()).setup(message.player, message.world, message.pos, blueprint, new PlacementSettings(message.mirror, message.rotation),
              message.type == BuildToolPlacementMessage.HandlerType.Pretty, message.structurePackId, message.blueprintPath))
           {
               return;
           }
            structure =
              ((ISpecialCreativeHandler) anchor.getBlock()).getStructureHandler(message.world, message.pos, blueprint, new PlacementSettings(message.mirror, message.rotation),
                message.type == BuildToolPlacementMessage.HandlerType.Pretty);
        }
        else
        {
            structure = new CreativeStructureHandler(message.world,
              message.pos,
              blueprint,
              new PlacementSettings(message.mirror, message.rotation),
              message.type == BuildToolPlacementMessage.HandlerType.Pretty);
        }

        final StructurePlacer instantPlacer = new StructurePlacer(structure);
        Manager.addToQueue(new TickedWorldOperation(instantPlacer, message.player));
    }

    /**
     * Handle placement and store the received client blueprint.
     * @param blueprintSyncMessage the message with all the data.
     */
    public static void handlePlacement(final BlueprintSyncMessage blueprintSyncMessage, final ServerPlayer player)
    {
        ServerBlueprintFutureProcessor.consumerQueue.add(new ServerBlueprintFutureProcessor.ProcessingData(Util.ioPool().submit(() ->
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

            return StructurePacks.getBlueprint(blueprintSyncMessage.structurePackId, blueprintPath);
        }), player.level, blueprint -> process(blueprint, new BuildToolPlacementMessage(blueprintSyncMessage, player, player.level))));
    }
}
