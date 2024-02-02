package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.PlacerholderFillOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message to replace a block from the world with another one.
 */
public class FillTopPlaceholderMessage implements IMessage
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
     * Fill parameters
     */
    private double yStretch;
    private double circleRadiusMult;
    private int    heightOffset;
    private int    minDistToBlocks;

    /**
     * Empty constructor used when registering the message.
     */
    public FillTopPlaceholderMessage(final FriendlyByteBuf buf)
    {
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
        this.yStretch = buf.readDouble();
        this.circleRadiusMult = buf.readDouble();
        this.heightOffset = buf.readInt();
        this.minDistToBlocks = buf.readInt();
    }

    /**
     * Create a message to replace a block from the world.
     *
     * @param pos1      start coordinate.
     * @param pos2      end coordinate.
     * @param blockFrom the block to replace.
     * @param blockTo   the block to replace it with.
     */
    public FillTopPlaceholderMessage(
      final BlockPos pos1,
      final BlockPos pos2,
      final double yStretch,
      final double circleRadiusMult,
      final int heightOffset,
      final int minDistToBlocks)
    {
        this.from = pos1;
        this.to = pos2;
        this.yStretch = yStretch;
        this.circleRadiusMult = circleRadiusMult;
        this.heightOffset = heightOffset;
        this.minDistToBlocks = minDistToBlocks;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeDouble(yStretch);
        buf.writeDouble(circleRadiusMult);
        buf.writeInt(heightOffset);
        buf.writeInt(minDistToBlocks);
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
        if (!ctxIn.getSender().isCreative())
        {
            return;
        }

        Manager.addToQueue(new PlacerholderFillOperation(from, to, ctxIn.getSender(), yStretch, circleRadiusMult, heightOffset, minDistToBlocks));
    }
}
