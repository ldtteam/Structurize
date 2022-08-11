package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Waits for blueprint futures to finish loading and then processes them.
 */
public class ServerFutureProcessor
{
    /**
     * Queue for processing blueprint futures.
     */
    private static final Queue<BlueprintProcessingData> blueprintConsumerQueue = new LinkedList<>();

    /**
     * Queue for processing blueprint data futures.
     */
    private static final Queue<BlueprintDataProcessingData> blueprintDataConsumerQueue = new LinkedList<>();

    /**
     * Queue processing data to be handled on tick.
     * @param processingData the data to be processed.
     */
    public static void queueBlueprint(@NotNull final BlueprintProcessingData processingData)
    {
        blueprintConsumerQueue.add(processingData);
    }

    /**
     * Queue processing data to be handled on tick.
     * @param processingData the data to be processed.
     */
    public static void queueBlueprintData(@NotNull final BlueprintDataProcessingData processingData)
    {
        blueprintDataConsumerQueue.add(processingData);
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.LevelTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            if (!blueprintConsumerQueue.isEmpty() && blueprintConsumerQueue.peek().level == event.level && blueprintConsumerQueue.peek().blueprintFuture.isDone())
            {
                final BlueprintProcessingData data = blueprintConsumerQueue.poll();
                try
                {
                    data.consumer.accept(data.blueprintFuture.get());
                }
                catch (InterruptedException | ExecutionException e)
                {
                    e.printStackTrace();
                }
            }

            if (!blueprintDataConsumerQueue.isEmpty() && blueprintDataConsumerQueue.peek().level == event.level && blueprintDataConsumerQueue.peek().blueprintDataFuture.isDone())
            {
                final BlueprintDataProcessingData data = blueprintDataConsumerQueue.poll();
                try
                {
                    data.consumer.accept(data.blueprintDataFuture.get());
                }
                catch (InterruptedException | ExecutionException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Data to be processed.
     */
    public record BlueprintProcessingData(Future<Blueprint> blueprintFuture, Level level, Consumer<Blueprint> consumer) { }

    /**
     * Data to be processed.
     */
    public record BlueprintDataProcessingData(Future<byte[]> blueprintDataFuture, Level level, Consumer<byte[]> consumer) { }
}
