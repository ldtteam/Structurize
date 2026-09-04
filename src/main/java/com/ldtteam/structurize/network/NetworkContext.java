package com.ldtteam.structurize.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * Compatibility facade over the NeoForge payload context. It preserves the
 * server-player lookup that the legacy message handlers relied on.
 */
public final class NetworkContext
{
    private final IPayloadContext payloadContext;

    public NetworkContext(final IPayloadContext payloadContext)
    {
        this.payloadContext = payloadContext;
    }

    public IPayloadContext payload()
    {
        return payloadContext;
    }

    @Nullable
    public ServerPlayer getSender()
    {
        return payloadContext.player() instanceof final ServerPlayer player ? player : null;
    }

    public boolean isClientOrigin()
    {
        return payloadContext.flow().isServerbound();
    }

    public void enqueueWork(final Runnable work)
    {
        payloadContext.enqueueWork(work);
    }
}
