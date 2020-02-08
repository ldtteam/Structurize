package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.items.ItemScanTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Send the scan message for a player to the server.
 */
public class ScanOnServerMessage extends AbstractMessage<ScanOnServerMessage, IMessage>
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
     * Name of the file.
     */
    private String name;

    /**
     * Whether to scan entities
     */
    private boolean saveEntities = true;

    /**
     * Empty public constructor.
     */
    public ScanOnServerMessage()
    {
        super();
    }

    public ScanOnServerMessage(@NotNull final BlockPos from, @NotNull final BlockPos to, @NotNull final String name, final boolean saveEntities)
    {
        super();
        this.from = from;
        this.to = to;
        this.name = name;
        this.saveEntities = saveEntities;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        name = ByteBufUtils.readUTF8String(buf);
        from = BlockPosUtil.readFromByteBuf(buf);
        to = BlockPosUtil.readFromByteBuf(buf);
        saveEntities = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name);
        BlockPosUtil.writeToByteBuf(buf, from);
        BlockPosUtil.writeToByteBuf(buf, to);
        buf.writeBoolean(saveEntities);
    }

    @Override
    public void messageOnServerThread(final ScanOnServerMessage message, final EntityPlayerMP player)
    {
        ItemScanTool.saveStructure(player.getEntityWorld(), message.from, message.to, player, message.name, message.saveEntities);
    }
}
