package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.placement.StructurePlacementUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 */
public class BuildToolPasteMessage implements IMessage
{
    private final boolean complete;
    private final String structureName;
    private final String workOrderName;
    private final int rotation;
    private final BlockPos pos;
    private final boolean isHut;
    private final boolean mirror;

    /**
     * Empty constructor used when registering the 
     */
    public BuildToolPasteMessage(final FriendlyByteBuf buf)
    {
        this.structureName = buf.readUtf(32767);
        this.workOrderName = buf.readUtf(32767);
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.rotation = buf.readInt();
        this.isHut = buf.readBoolean();
        this.mirror = buf.readBoolean();
        this.complete = buf.readBoolean();
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
        this.structureName = structureName;
        this.workOrderName = workOrderName;
        this.pos = pos;
        this.rotation = rotation.ordinal();
        this.isHut = isHut;
        this.mirror = mirror == Mirror.FRONT_BACK;
        this.complete = complete;
    }

    /**
     * Writes this packet to a {@link ByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUtf(structureName);
        buf.writeUtf(workOrderName);

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
            ctxIn.getSender().sendMessage(new TextComponent("Can not build " + workOrderName + ": schematic missing!"), ctxIn.getSender().getUUID());
            return;
        }

        if (ctxIn.getSender().isCreative())
        {
            StructurePlacementUtils.loadAndPlaceStructureWithRotation(ctxIn.getSender().level, structureName,
              pos, Rotation.values()[rotation], mirror ? Mirror.FRONT_BACK : Mirror.NONE, !complete, ctxIn.getSender());
        }
    }
}
