package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.helpers.WallExtents;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.placement.StructurePlacementUtils;
import com.ldtteam.structurize.util.PlacementSettings;
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
    private final BlockPos pos;
    private final PlacementSettings settings;
    private final boolean isHut;

    /**
     * Empty constructor used when registering the 
     */
    public BuildToolPasteMessage(final FriendlyByteBuf buf)
    {
        this.structureName = buf.readUtf(32767);
        this.workOrderName = buf.readUtf(32767);
        this.pos = buf.readBlockPos();
        this.settings = PlacementSettings.read(buf);
        this.isHut = buf.readBoolean();
        this.complete = buf.readBoolean();
    }

    /**
     * Create the building that was made with the build tool.
     * Item in inventory required
     *  @param structureName String representation of a structure
     * @param workOrderName String name of the work order
     * @param pos           BlockPos
     * @param settings      The placement settings.
     * @param isHut         true if hut, false if decoration
     * @param complete      paste it complete (with structure blocks) or without.
     */
    public BuildToolPasteMessage(
            final String structureName,
            final String workOrderName, final BlockPos pos,
            final PlacementSettings settings, final boolean isHut,
            final boolean complete)
    {
        this.structureName = structureName;
        this.workOrderName = workOrderName;
        this.pos = pos;
        this.settings = settings;
        this.isHut = isHut;
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

        buf.writeBlockPos(pos);
        settings.write(buf);

        buf.writeBoolean(isHut);

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
              pos, settings, !complete, ctxIn.getSender());
        }
    }
}
