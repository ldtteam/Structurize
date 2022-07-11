package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Waits for blueprint futures to finish loading and then processes them.
 */
public class ClientBlueprintFutureProcessor
{
    /**
     * Queue for processing.
     */
    public static final Queue<ProcessingData> consumerQueue = new LinkedList<>();

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && !consumerQueue.isEmpty())
        {
            if (consumerQueue.peek().blueprintFuture.isDone())
            {
                final ProcessingData data = consumerQueue.poll();
                try
                {
                    data.consumer.accept(data.blueprintFuture.get());
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
    public record ProcessingData(Future<Blueprint> blueprintFuture, Consumer<Blueprint> consumer) { }
}
