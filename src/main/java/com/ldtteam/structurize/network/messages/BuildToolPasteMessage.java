package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.placement.StructurePlacementUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 */
public class BuildToolPasteMessage implements IMessage
{
    private boolean complete;
    private String                   structureName;
    private String                   workOrderName;
    private int                      rotation;
    private BlockPos                 pos;
    private boolean                  isHut;
    private boolean                  mirror;

    /**
     * Empty constructor used when registering the 
     */
    public BuildToolPasteMessage()
    {
        super();
    }

    /**
     * Create the building that was made with the build tool.
     * Item in inventory required
     *  @param structureName String representation of a structure
     * @param workOrderName String name of the work order
     * @param pos           BlockPos
     * @param rotation      int representation of the rotation
     * @param isHut         true if hut, false if decoration
     * @param mirror        the mirror of the building or decoration.
     * @param complete      paste it complete (with structure blocks) or without.
     */
    public BuildToolPasteMessage(
      final String structureName,
      final String workOrderName, final BlockPos pos,
      final Rotation rotation, final boolean isHut,
      final Mirror mirror, final boolean complete)
    {
        super();
        this.structureName = structureName;
        this.workOrderName = workOrderName;
        this.pos = pos;
        this.rotation = rotation.ordinal();
        this.isHut = isHut;
        this.mirror = mirror == Mirror.FRONT_BACK;
        this.complete = complete;
    }

    /**
     * Reads this packet from a {@link ByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        structureName = buf.readString(32767);
        workOrderName = buf.readString(32767);

        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

        rotation = buf.readInt();

        isHut = buf.readBoolean();

        mirror = buf.readBoolean();

        complete = buf.readBoolean();
    }

    /**
     * Writes this packet to a {@link ByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeString(structureName);
        buf.writeString(workOrderName);

        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        buf.writeInt(rotation);

        buf.writeBoolean(isHut);

        buf.writeBoolean(mirror);

        buf.writeBoolean(complete);
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
        final StructureName sn = new StructureName(structureName);
        if (!Structures.hasMD5(sn))
        {
            ctxIn.getSender().sendMessage(new StringTextComponent("Can not build " + workOrderName + ": schematic missing!"), ctxIn.getSender().getUniqueID());
            return;
        }

        if (ctxIn.getSender().isCreative())
        {
            StructurePlacementUtils.loadAndPlaceStructureWithRotation(ctxIn.getSender().world, structureName,
              pos, Rotation.values()[rotation], mirror ? Mirror.FRONT_BACK : Mirror.NONE, !complete, ctxIn.getSender());
        }
    }
}
