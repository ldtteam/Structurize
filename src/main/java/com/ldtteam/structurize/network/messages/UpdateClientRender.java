package com.ldtteam.structurize.network.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import com.ldtteam.structurize.network.NetworkContext;
import org.jetbrains.annotations.Nullable;

/**
 * Marks an area of blocks for re-rendering on the client
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

    @SuppressWarnings("resource")
    @Override
    public void onExecute(final NetworkContext ctxIn, final boolean isLogicalServer)
    {
        if (!isLogicalServer)
        {
            final ClientLevel level = Minecraft.getInstance().level;
            for (BlockPos pos : BlockPos.betweenClosed(from, to))
            {
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_ALL);
            }
        }
    }
}
