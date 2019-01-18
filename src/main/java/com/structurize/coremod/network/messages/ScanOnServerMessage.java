package com.structurize.coremod.network.messages;

import com.structurize.api.util.BlockPosUtil;
import com.structurize.coremod.items.ItemScanTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
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
     * The additional data collected by the wizard during construction.
     */
    private NBTTagCompound coreData;

    /**
     * Empty public constructor.
     */
    public ScanOnServerMessage()
    {
        super();
    }


    public ScanOnServerMessage(@NotNull final NBTTagCompound coreData)
    {
        super();
        this.coreData = coreData;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        coreData = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, coreData);
    }

    @Override
    public void messageOnServerThread(final ScanOnServerMessage message, final EntityPlayerMP player)
    {
        //TODO: Handle via scan wizard handling.
        //ItemScanTool.saveStructure(player.getEntityWorld(), message.from, message.to, player, message.name);
    }
}
