package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.storage.ClientStructurePackLoader;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.io.IOException;

/**
 * Handles sendScanMessages.
 */
public class SaveScanMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "save_scan", SaveScanMessage::new);

    private static final String TAG_MILLIS    = "millies";
    public static final  String TAG_SCHEMATIC = "schematic";

    private CompoundTag compoundNBT;
    private String      fileName;

    /**
     * Public standard constructor.
     */
    public SaveScanMessage(final FriendlyByteBuf buf)
    {
        super(buf, TYPE);
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        try (ByteBufInputStream stream = new ByteBufInputStream(buffer))
        {
            final CompoundTag wrapperCompound = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
            this.compoundNBT = wrapperCompound.getCompound(TAG_SCHEMATIC);
            this.fileName = wrapperCompound.getString(TAG_MILLIS);
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

    /**
     * Send a scan compound to the client.
     *
     * @param CompoundNBT the stream.
     * @param fileName  String with the name of the file.
     */
    public SaveScanMessage(final CompoundTag CompoundNBT, final String fileName)
    {
        super(TYPE);
        this.fileName = fileName;
        this.compoundNBT = CompoundNBT;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        final CompoundTag wrapperCompound = new CompoundTag();
        wrapperCompound.putString(TAG_MILLIS, fileName);
        wrapperCompound.put(TAG_SCHEMATIC, compoundNBT);

        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        try (ByteBufOutputStream stream = new ByteBufOutputStream(buffer))
        {
            NbtIo.writeCompressed(wrapperCompound, stream);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
    }

    @Override
    public void onExecute(final PlayPayloadContext context, final Player player)
    {
        if (compoundNBT != null)
        {
            ClientStructurePackLoader.handleSaveScanMessage(compoundNBT, fileName);
        }
    }
}
