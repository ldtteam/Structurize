package com.ldtteam.structurize.network;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.network.messages.*;
import com.ldtteam.structurize.network.messages.splitting.SplitPacketMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Compatibility adapter between Structurize's indexed messages and NeoForge payloads.
 */
public class NetworkChannel
{
    private final String channelName;
    private final Map<Integer, NetworkingMessageEntry<?>> messagesTypes = Maps.newHashMap();
    private final Map<Class<? extends IMessage>, Integer> messageTypeToIdMap = Maps.newHashMap();
    private final Cache<Integer, Map<Integer, byte[]>> messageCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .concurrencyLevel(8)
        .build();
    private final AtomicInteger messageCounter = new AtomicInteger();

    public NetworkChannel(final String channelName)
    {
        this.channelName = channelName;
    }

    public void registerCommonMessages(final PayloadRegistrar registrar)
    {
        messagesTypes.put(0, new NetworkingMessageEntry<>(SplitPacketMessage::new, SplitPacketMessage.class));

        int idx = 0;
        registerMessage(++idx, RemoveBlockMessage.class, RemoveBlockMessage::new);
        registerMessage(++idx, RemoveEntityMessage.class, RemoveEntityMessage::new);
        registerMessage(++idx, SaveScanMessage.class, SaveScanMessage::new);
        registerMessage(++idx, ReplaceBlockMessage.class, ReplaceBlockMessage::new);
        registerMessage(++idx, FillTopPlaceholderMessage.class, FillTopPlaceholderMessage::new);
        registerMessage(++idx, ScanOnServerMessage.class, ScanOnServerMessage::new);
        registerMessage(++idx, ServerUUIDMessage.class, ServerUUIDMessage::new);
        registerMessage(++idx, UndoRedoMessage.class, UndoRedoMessage::new);
        registerMessage(++idx, UpdateScanToolMessage.class, UpdateScanToolMessage::new);
        registerMessage(++idx, UpdateClientRender.class, UpdateClientRender::new);
        registerMessage(++idx, BuildToolPlacementMessage.class, BuildToolPlacementMessage::new);
        registerMessage(++idx, ShowScanMessage.class, ShowScanMessage::new);

        registerMessage(++idx, AddRemoveTagMessage.class, AddRemoveTagMessage::new);
        registerMessage(++idx, SetTagInTool.class, SetTagInTool::new);
        registerMessage(++idx, OperationHistoryMessage.class, OperationHistoryMessage::new);

        registerMessage(++idx, NotifyServerAboutStructurePacksMessage.class, NotifyServerAboutStructurePacksMessage::new);
        // Preserve the historical second index for this class; dispatch uses the exact registered id.
        registerMessage(++idx, BuildToolPlacementMessage.class, BuildToolPlacementMessage::new);
        registerMessage(++idx, BlueprintSyncMessage.class, BlueprintSyncMessage::new);
        registerMessage(++idx, SyncSettingsToServer.class, SyncSettingsToServer::new);
        registerMessage(++idx, SyncPreviewCacheToServer.class, SyncPreviewCacheToServer::new);

        registerMessage(++idx, NotifyClientAboutStructurePacksMessage.class, NotifyClientAboutStructurePacksMessage::new);
        registerMessage(++idx, TransferStructurePackToClient.class, TransferStructurePackToClient::new);
        registerMessage(++idx, ClientBlueprintRequestMessage.class, ClientBlueprintRequestMessage::new);
        registerMessage(++idx, SyncPreviewCacheToClient.class, SyncPreviewCacheToClient::new);

        registerMessage(++idx, ItemMiddleMouseMessage.class, ItemMiddleMouseMessage::new);
        registerMessage(++idx, ScanToolTeleportMessage.class, ScanToolTeleportMessage::new);
        registerMessage(++idx, AbsorbBlockMessage.class, AbsorbBlockMessage::new);

        for (final Map.Entry<Integer, NetworkingMessageEntry<?>> entry : messagesTypes.entrySet())
        {
            final IPayloadHandler<WrappedMessage> handler =
                (payload, context) -> handleMessage(payload.messageId(), payload.data(), context);
            registrar.playBidirectional(
                WrappedMessage.typeFor(entry.getKey()),
                WrappedMessage.CODEC,
                handler,
                handler);
        }
    }

    private <MSG extends IMessage> void registerMessage(final int id,
        final Class<MSG> msgClazz,
        final Function<FriendlyByteBuf, MSG> msgCreator)
    {
        messagesTypes.put(id, new NetworkingMessageEntry<>(msgCreator, msgClazz));
        messageTypeToIdMap.put(msgClazz, id);
    }

    public void sendToServer(final IMessage msg)
    {
        handleSplitting(msg, ClientPacketDistributor::sendToServer);
    }

    public void sendToPlayer(final IMessage msg, final ServerPlayer player)
    {
        handleSplitting(msg, payload -> PacketDistributor.sendToPlayer(player, payload));
    }

    public void sendToOrigin(final IMessage msg, final NetworkContext context)
    {
        final ServerPlayer player = context.getSender();
        if (player != null)
        {
            sendToPlayer(msg, player);
        }
        else
        {
            sendToServer(msg);
        }
    }

    public void sendToDimension(final IMessage msg, final ServerLevel dimension)
    {
        PacketDistributor.sendToPlayersInDimension(dimension, wrapForTransport(msg));
    }

    public void sendToPosition(final IMessage msg,
        final ServerLevel level,
        final ServerPlayer excludedPlayer,
        final double x,
        final double y,
        final double z,
        final double radius)
    {
        PacketDistributor.sendToPlayersNear(level, excludedPlayer, x, y, z, radius, wrapForTransport(msg));
    }

    public void sendToEveryone(final IMessage msg)
    {
        PacketDistributor.sendToAllPlayers(wrapForTransport(msg));
    }

    public void sendToTrackingEntity(final IMessage msg, final Entity entity)
    {
        PacketDistributor.sendToPlayersTrackingEntity(entity, wrapForTransport(msg));
    }

    public void sendToTrackingEntityAndSelf(final IMessage msg, final Entity entity)
    {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, wrapForTransport(msg));
    }

    public void sendToTrackingChunk(final IMessage msg, final LevelChunk chunk)
    {
        if (!(chunk.getLevel() instanceof final ServerLevel level))
        {
            throw new IllegalArgumentException("Cannot track a client chunk: " + chunk.getLevel().getClass().getName());
        }
        PacketDistributor.sendToPlayersTrackingChunk(level, chunk.getPos(), wrapForTransport(msg));
    }

    private CustomPacketPayload wrapForTransport(final IMessage msg)
    {
        final int messageId = messageTypeToIdMap.getOrDefault(msg.getClass(), -1);
        if (messageId == -1)
        {
            throw new IllegalArgumentException("The message is unknown to this channel!");
        }
        return new WrappedMessage(messageId, serialize(msg));
    }

    private byte[] serialize(final IMessage msg)
    {
        final ByteBuf buffer = Unpooled.buffer();
        try
        {
            msg.toBytes(new FriendlyByteBuf(buffer));
            return Arrays.copyOf(buffer.array(), buffer.readableBytes());
        }
        finally
        {
            buffer.release();
        }
    }

    private void handleSplitting(final IMessage msg, final Consumer<CustomPacketPayload> sender)
    {
        final int messageId = messageTypeToIdMap.getOrDefault(msg.getClass(), -1);
        if (messageId == -1)
        {
            throw new IllegalArgumentException("The message is unknown to this channel!");
        }

        final byte[] data = serialize(msg);
        final int maxPacketSize = msg.getExecutionSide() == LogicalSide.SERVER ? 30000 : 943718;
        int currentIndex = 0;
        int packetIndex = 0;
        final int communicationId = messageCounter.getAndIncrement();

        while (currentIndex < data.length)
        {
            messagesTypes.get(messageId).onSplitting(packetIndex);
            final int length = Math.min(maxPacketSize, data.length - currentIndex);
            final byte[] packetData = Arrays.copyOfRange(data, currentIndex, currentIndex + length);
            sender.accept(new WrappedMessage(
                0,
                serialize(new SplitPacketMessage(
                    communicationId,
                    packetIndex++,
                    currentIndex + length >= data.length,
                    messageId,
                    packetData))));
            currentIndex += length;
        }
    }

    private void handleMessage(final int messageId, final byte[] data, final IPayloadContext neoContext)
    {
        final NetworkContext context = new NetworkContext(neoContext);
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        final IMessage message;
        try
        {
            if (messageId == 0)
            {
                message = new SplitPacketMessage(buffer);
            }
            else
            {
                final NetworkingMessageEntry<?> entry = messagesTypes.get(messageId);
                if (entry == null)
                {
                    throw new IllegalArgumentException("Unknown Structurize message id: " + messageId);
                }
                message = entry.getCreator().apply(buffer);
            }
        }
        finally
        {
            buffer.release();
        }

        final LogicalSide receivingSide = neoContext.flow().getReceptionSide();
        if (message.getExecutionSide() != null && receivingSide != message.getExecutionSide())
        {
            Log.getLogger().warn("Receiving {} at wrong side!", message.getClass().getName());
            return;
        }

        neoContext.enqueueWork(() -> message.onExecute(context, receivingSide == LogicalSide.SERVER));
    }

    public Cache<Integer, Map<Integer, byte[]>> getMessageCache()
    {
        return messageCache;
    }

    public Map<Integer, NetworkingMessageEntry<?>> getMessagesTypes()
    {
        return messagesTypes;
    }

    public String getChannelName()
    {
        return channelName;
    }

    public static final class NetworkingMessageEntry<MSG extends IMessage>
    {
        private final AtomicBoolean hasWarned = new AtomicBoolean(true);
        private final Function<FriendlyByteBuf, MSG> creator;
        private final Class<? extends IMessage> clazz;

        private NetworkingMessageEntry(final Function<FriendlyByteBuf, MSG> creator, final Class<? extends IMessage> clazz)
        {
            this.creator = creator;
            this.clazz = clazz;
        }

        public Function<FriendlyByteBuf, MSG> getCreator()
        {
            return creator;
        }

        public void onSplitting(final int packetIndex)
        {
            if (packetIndex != 1)
            {
                return;
            }

            if (hasWarned.getAndSet(false))
            {
                Log.getLogger()
                    .warn("Splitting message: {} it is too big to send normally. This message is only printed once", clazz.getName());
            }
        }
    }
}
