package com.ldtteam.structurize.network.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Send the scan tool update message to the client.
 */
public class UpdateClientRender implements IMessage
{
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
        this.from = from;
        this.to = to;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (!isLogicalServer)
        {
            Minecraft.getInstance().levelRenderer.setBlocksDirty(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
        }
    }
}
