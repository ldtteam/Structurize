package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.config.BlueprintRenderSettings;
import com.ldtteam.structurize.storage.rendering.ServerPreviewDistributor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Sync player settings to server.
 */
public class SyncSettingsToServer implements IMessage
{
    /**
     * Settings map.
     */
    private final BlueprintRenderSettings settings;

    /**
     * Buffer reading message constructor.
     */
    public SyncSettingsToServer(final FriendlyByteBuf buf)
    {
        this.settings = new BlueprintRenderSettings(buf);
    }

    /**
     * Send setting data from the client.
     */
    public SyncSettingsToServer()
    {
        this.settings = BlueprintRenderSettings.instance;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        BlueprintRenderSettings.instance.writeToBuf(buf);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        ServerPreviewDistributor.register(ctxIn.getSender().getUUID(), new Tuple<>(ctxIn.getSender(), this.settings));
    }
}
