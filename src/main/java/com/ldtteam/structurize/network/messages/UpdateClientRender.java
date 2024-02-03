package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.util.constant.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Send the scan tool update message to the client.
 */
public class UpdateClientRender extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "update_client_render", UpdateClientRender::new);

    /**
     * Position to scan from.
     */
    private final BlockPos from;

    /**
     * Position to scan to.
     */
    private final BlockPos to;

    /**
     * Empty public constructor.
     */
    public UpdateClientRender(final FriendlyByteBuf buf)
    {
        super(buf, TYPE);
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
    }

    /**
     * Update the scan tool.
     * @param from the start pos.
     * @param to the end pos.
     */
    public UpdateClientRender(final BlockPos from, final BlockPos to)
    {
        super(TYPE);
        this.from = from;
        this.to = to;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
    }

    @SuppressWarnings("resource")
    @Override
    public void onExecute(final PlayPayloadContext context, final Player player)
    {
        Minecraft.getInstance().levelRenderer.setBlocksDirty(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
    }
}
