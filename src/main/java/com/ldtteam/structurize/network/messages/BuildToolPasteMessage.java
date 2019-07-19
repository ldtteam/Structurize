package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.StructurePlacementUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntityMP;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 */
public class BuildToolPasteMessage extends AbstractMessage<BuildToolPasteMessage, IMessage>
{
    private boolean complete;
    private String                   structureName;
    private String                   workOrderName;
    private int                      rotation;
    private BlockPos                 pos;
    private boolean                  isHut;
    private boolean                  mirror;

    /**
     * Empty constructor used when registering the message.
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
     * @param freeMode      the free mode type.
     */
    public BuildToolPasteMessage(
      final String structureName,
      final String workOrderName, final BlockPos pos,
      final Rotation rotation, final boolean isHut,
      final Mirror mirror, final boolean complete, final WindowBuildTool.FreeMode freeMode)
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
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        structureName = ByteBufUtils.readUTF8String(buf);
        workOrderName = ByteBufUtils.readUTF8String(buf);

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
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, structureName);
        ByteBufUtils.writeUTF8String(buf, workOrderName);

        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        buf.writeInt(rotation);

        buf.writeBoolean(isHut);

        buf.writeBoolean(mirror);

        buf.writeBoolean(complete);
    }

    @Override
    public void messageOnServerThread(final BuildToolPasteMessage message, final PlayerEntityMP player)
    {
        final StructureName sn = new StructureName(message.structureName);
        if (!Structures.hasMD5(sn))
        {
            player.sendMessage(new TextComponentString("Can not build " + message.workOrderName + ": schematic missing!"));
            return;
        }

        if (player.capabilities.isCreativeMode)
        {
            StructurePlacementUtils.loadAndPlaceStructureWithRotation(player.world, message.structureName,
              message.pos, Rotation.values()[message.rotation], message.mirror ? Mirror.FRONT_BACK : Mirror.NONE, message.complete, player);
        }
    }
}
