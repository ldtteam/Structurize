package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.storage.ClientStructurePackLoader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Transfer a zipped structure pack to the client.
 */
public class TransferStructurePackToClient implements IMessage
{
    /**
     * Payload of the message (to transfer to client).
     */
    private final ByteBuf payload;

    /**
     * The name of the structure pack.
     */
    private final String packname;

    /**
     * Public standard constructor.
     */
    public TransferStructurePackToClient(final FriendlyByteBuf buf)
    {
        this.packname = buf.readUtf(32767);
        this.payload = Unpooled.wrappedBuffer(buf.readByteArray());
    }

    /**
     * Transfer a zipped structure pack to the client.
     * @param packName the name of the structure pack.
     * @param payload the payload.
     */
    public TransferStructurePackToClient(final String packName, final ByteBuf payload)
    {
        this.packname = packName;
        this.payload = payload;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUtf(this.packname);
        buf.writeByteArray(this.payload.array());
        this.payload.release();
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
            ClientStructurePackLoader.onStructurePackTransfer(this.packname, this.payload);
        }
    }
}
