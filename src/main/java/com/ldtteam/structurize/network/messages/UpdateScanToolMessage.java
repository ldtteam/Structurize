package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.items.ModItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntityMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.FIRST_POS_STRING;
import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.SECOND_POS_STRING;

/**
 * Send the scan tool update message to the client.
 */
public class UpdateScanToolMessage extends AbstractMessage<UpdateScanToolMessage, IMessage>
{
    /**
     * Position to scan from.
     */
    private BlockPos from;

    /**
     * Position to scan to.
     */
    private BlockPos to;

    /**
     * Empty public constructor.
     */
    public UpdateScanToolMessage()
    {
        super();
    }

    /**
     * Update the scan tool.
     * @param from the start pos.
     * @param to the end pos.
     */
    public UpdateScanToolMessage(@NotNull final BlockPos from, @NotNull final BlockPos to)
    {
        super();
        final ItemStack stack = Minecraft.getInstance().player.getHeldItemMainhand();
        if (stack.getItem() == ModItems.scanTool)
        {
            final CompoundNBT compound = stack.getTagCompound();
            if (compound != null)
            {
                BlockPosUtil.writeToNBT(compound, FIRST_POS_STRING, from);
                BlockPosUtil.writeToNBT(compound, SECOND_POS_STRING, to);
                stack.setTagCompound(compound);
            }
        }
        this.from = from;
        this.to = to;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        from = BlockPosUtil.readFromByteBuf(buf);
        to = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        BlockPosUtil.writeToByteBuf(buf, from);
        BlockPosUtil.writeToByteBuf(buf, to);
    }

    @Override
    public void messageOnServerThread(final UpdateScanToolMessage message, final PlayerEntityMP player)
    {
        final ItemStack stack = player.getHeldItemMainhand();
        if (stack.getItem() == ModItems.scanTool)
        {
            final CompoundNBT compound = stack.getTagCompound();
            if (compound != null)
            {
                BlockPosUtil.writeToNBT(compound, FIRST_POS_STRING, message.from);
                BlockPosUtil.writeToNBT(compound, SECOND_POS_STRING, message.to);
                stack.setTagCompound(compound);
            }
        }
    }
}
