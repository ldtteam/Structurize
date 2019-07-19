package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.util.ClientStructureWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Handles sendScanMessages.
 */
public class SaveScanMessage extends AbstractMessage<SaveScanMessage, IMessage>
{
    private static final String TAG_MILLIS    = "millies";
    public static final  String TAG_SCHEMATIC = "schematic";

    private CompoundNBT CompoundNBT;
    private String           fileName;

    /**
     * Public standard constructor.
     */
    public SaveScanMessage()
    {
        super();
    }

    /**
     * Send a scan compound to the client.
     *
     * @param CompoundNBT the stream.
     * @param fileName  String with the name of the file.
     */
    public SaveScanMessage(final CompoundNBT CompoundNBT, final String fileName)
    {
        this.fileName = fileName;
        this.CompoundNBT = CompoundNBT;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        try (ByteBufInputStream stream = new ByteBufInputStream(buffer))
        {
            final CompoundNBT wrapperCompound = CompressedStreamTools.readCompressed(stream);
            CompoundNBT = wrapperCompound.getCompoundTag(TAG_SCHEMATIC);
            fileName = wrapperCompound.getString(TAG_MILLIS);
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().info("Structure too big to be processed", e);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        final CompoundNBT wrapperCompound = new CompoundNBT();
        wrapperCompound.setString(TAG_MILLIS, fileName);
        wrapperCompound.setTag(TAG_SCHEMATIC, CompoundNBT);

        final PacketBuffer buffer = new PacketBuffer(buf);
        try (ByteBufOutputStream stream = new ByteBufOutputStream(buffer))
        {
            CompressedStreamTools.writeCompressed(wrapperCompound, stream);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
    }

    @Override
    protected void messageOnClientThread(final SaveScanMessage message, final MessageContext ctx)
    {
        if (message.CompoundNBT != null)
        {
            ClientStructureWrapper.handleSaveScanMessage(message.CompoundNBT, message.fileName);
        }
    }
}
