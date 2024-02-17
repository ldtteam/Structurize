package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.PlacerholderFillOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Message to replace a block from the world with another one.
 */
public class FillTopPlaceholderMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "fill_top_placeholder", FillTopPlaceholderMessage::new);

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
    private final double yStretch;
    private final double circleRadiusMult;
    private final int    heightOffset;
    private final int    minDistToBlocks;

    /**
     * Empty constructor used when registering the message.
     */
    protected FillTopPlaceholderMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
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
        super(TYPE);
        this.from = pos1;
        this.to = pos2;
        this.yStretch = yStretch;
        this.circleRadiusMult = circleRadiusMult;
        this.heightOffset = heightOffset;
        this.minDistToBlocks = minDistToBlocks;
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeDouble(yStretch);
        buf.writeDouble(circleRadiusMult);
        buf.writeInt(heightOffset);
        buf.writeInt(minDistToBlocks);
    }

    @Override
    protected void onExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        if (!player.isCreative())
        {
            return;
        }

        Manager.addToQueue(new PlacerholderFillOperation(from, to, player, yStretch, circleRadiusMult, heightOffset, minDistToBlocks));
    }
}
