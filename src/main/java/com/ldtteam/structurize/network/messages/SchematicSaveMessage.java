package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.StructureUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.ldtteam.structurize.api.util.constant.Constants.MAX_AMOUNT_OF_PIECES;

/**
 * Save Schematic Message.
 */
public class SchematicSaveMessage implements IMessage
{
    /**
     * The schematic data.
     */
    private              byte[] data           = null;

    /**
     * The amount of pieces.
     */
    private int pieces;

    /**
     * The current piece.
     */
    private int piece;

    /**
     * The UUID.
     */
    private UUID id;

    /**
     * Public standard constructor.
     */
    public SchematicSaveMessage()
    {
        super();
    }

    /**
     * Send a schematic to the client.
     *
     * @param data byte array of the schematic.
     */
    /**
     * Send a schematic between client and server or server and client.
     * @param data the schematic.
     * @param id the unique id.
     * @param pieces the amount of pieces.
     * @param piece the current piece.
     */
    public SchematicSaveMessage(final byte[] data, final UUID id, final int pieces, final int piece)
    {
        super();
        this.data = data.clone();
        this.id = id;
        this.pieces = pieces;
        this.piece = piece;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        final int length = buf.readInt();
        final byte[] compressedData = new byte[length];
        buf.readBytes(compressedData);
        data = StructureUtils.uncompress(compressedData);
        pieces = buf.readInt();
        piece = buf.readInt();
        id = buf.readUniqueId();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        final byte[] compressedData = StructureUtils.compress(data);
        if (compressedData != null)
        {
            buf.capacity(compressedData.length + buf.writerIndex());
            buf.writeInt(compressedData.length);
            buf.writeBytes(compressedData);
            buf.writeInt(pieces);
            buf.writeInt(piece);
            buf.writeUniqueId(id);
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return null;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (isLogicalServer)
        {
            if (!Structurize.getConfig().getCommon().allowPlayerSchematics.get())
            {
                Log.getLogger().info("SchematicSaveMessage: custom schematic is not allowed on this server.");
                ctxIn.getSender().sendMessage(new StringTextComponent("The server does not allow custom schematic!"));
                return;
            }

            if (pieces > MAX_AMOUNT_OF_PIECES)
            {
                Log.getLogger().error("Schematic has more than 10 pieces, discarding.");
                ctxIn.getSender().sendMessage(new StringTextComponent("Schematic has more than 10 pieces, that's too big!"));
                return;
            }

            final boolean schematicSent;
            if (data == null)
            {
                Log.getLogger().error("Received empty schematic file");
                schematicSent = false;
            }
            else
            {
                schematicSent = Structures.handleSaveSchematicMessage(data, id, pieces, piece);
            }

            if (schematicSent)
            {
                ctxIn.getSender().sendMessage(new StringTextComponent("Schematic successfully sent!"));
            }
            else
            {
                ctxIn.getSender().sendMessage(new StringTextComponent("Failed to send the Schematic!"));
            }
        }
        else
        {
            if (data == null)
            {
                Log.getLogger().error("Received empty schematic file");
            }
            else
            {
                Structures.handleSaveSchematicMessage(data);
            }
        }
    }
}
