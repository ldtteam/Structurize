package com.ldtteam.structurize.network;

import com.ldtteam.structurize.api.util.constant.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transport frame used to migrate Structurize's indexed messages onto
 * NeoForge payloads without changing each message's buffer format.
 */
public record WrappedMessage(int messageId, byte[] data) implements CustomPacketPayload
{
    private static final Map<Integer, Type<WrappedMessage>> TYPES = new ConcurrentHashMap<>();
    public static final StreamCodec<RegistryFriendlyByteBuf, WrappedMessage> CODEC = CustomPacketPayload.codec(
        WrappedMessage::write,
        WrappedMessage::read);

    public static Type<WrappedMessage> typeFor(final int messageId)
    {
        return TYPES.computeIfAbsent(messageId, id -> new Type<>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "message/" + id)));
    }

    private static WrappedMessage read(final FriendlyByteBuf buf)
    {
        return new WrappedMessage(buf.readVarInt(), buf.readByteArray());
    }

    private void write(final FriendlyByteBuf buf)
    {
        buf.writeVarInt(messageId);
        buf.writeByteArray(data);
    }

    @Override
    public Type<?> type()
    {
        return typeFor(messageId);
    }
}
