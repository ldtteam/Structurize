package com.structurize.coremod.network.messages;

import com.structurize.coremod.management.StructureName;
import com.structurize.coremod.management.Structures;
import com.structurize.coremod.util.StructureWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Send shape tool data to the server. Verify the data on the server side and then place the shape.
 */
public class ShapeToolPasteMessage extends AbstractMessage<ShapeToolPasteMessage, IMessage>
{
    private int                      rotation;
    private BlockPos                 pos;
    private boolean                  mirror;

    /**
     * Empty constructor used when registering the message.
     */
    public ShapeToolPasteMessage()
    {
        super();
    }

    /**
     * Create the shape that was made with the shape tool.
     *
     * @param pos           BlockPos
     * @param rotation      int representation of the rotation
     * @param mirror        the mirror of the building or decoration.
     */
    public ShapeToolPasteMessage(
      final BlockPos pos,
            final int rotation,
            final Mirror mirror)
    {
        super();
        this.pos = pos;
        this.rotation = rotation;
        this.mirror = mirror == Mirror.FRONT_BACK;
    }

    /**
     * Reads this packet from a {@link ByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {

        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

        rotation = buf.readInt();

        mirror = buf.readBoolean();
    }

    /**
     * Writes this packet to a {@link ByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        buf.writeInt(rotation);

        buf.writeBoolean(mirror);
    }

    @Override
    public void messageOnServerThread(final ShapeToolPasteMessage message, final EntityPlayerMP player)
    {
        if (player.capabilities.isCreativeMode)
        {
            StructureWrapper.loadAndPlaceShapeWithRotation(player.getServerWorld(),
              message.pos, message.rotation, message.mirror ? Mirror.FRONT_BACK : Mirror.NONE, player);
        }
    }
}
