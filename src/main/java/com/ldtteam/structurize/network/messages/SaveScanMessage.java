package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.storage.ClientStructurePackLoader;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;

/**
 * Handles sendScanMessages.
 */
public class SaveScanMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "save_scan", SaveScanMessage::new);

    private static final String TAG_MILLIS    = "millies";
    public static final  String TAG_SCHEMATIC = "schematic";

    private final CompoundTag compoundNBT;
    private final String      fileName;

    /**
     * Public standard constructor.
     */
    protected SaveScanMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        CompoundTag tag = null;
        String name = null;
        try (ByteBufInputStream stream = new ByteBufInputStream(buffer))
        {
            final CompoundTag wrapperCompound = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
            tag = wrapperCompound.getCompound(TAG_SCHEMATIC);
            name = wrapperCompound.getString(TAG_MILLIS);
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().info("Structure too big to be processed", e);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
        this.compoundNBT = tag;
        this.fileName = name;
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
    protected void toBytes(final RegistryFriendlyByteBuf buf)
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
    protected void onExecute(final IPayloadContext context, final Player player)
    {
        if (compoundNBT != null)
        {
            ClientStructurePackLoader.handleSaveScanMessage(compoundNBT, fileName, player.level().registryAccess());
        }
    }
}
