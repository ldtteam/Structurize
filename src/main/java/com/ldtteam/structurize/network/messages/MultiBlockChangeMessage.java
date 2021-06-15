package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.tileentities.TileEntityMultiBlock;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message class which handles updating the minecolonies multiblock.
 */
public class MultiBlockChangeMessage implements IMessage
{
    /**
     * The direction it should push or pull rom.
     */
    private final Direction direction;

    /**
     * The direction it should push or pull rom.
     */
    private final Direction output;

    /**
     * The range it should pull to.
     */
    private final int range;

    /**
     * The speed it should have.
     */
    private final int speed;

    /**
     * The position of the tileEntity.
     */
    private final BlockPos pos;

    /**
     * Empty public constructor.
     */
    public MultiBlockChangeMessage(final PacketBuffer buf)
    {
        this.pos = buf.readBlockPos();
        this.direction = Direction.values()[buf.readInt()];
        this.output = Direction.values()[buf.readInt()];
        this.range = buf.readInt();
        this.speed = buf.readInt();
    }

    /**
     * Constructor to create the 
     * @param pos the position of the block.
     * @param facing the way it should be facing.
     * @param output the way it will output to.
     * @param range the range it should work.
     * @param speed the speed it should have.
     */
    public MultiBlockChangeMessage(final BlockPos pos, final Direction facing, final Direction output, final int range, final int speed)
    {
        this.pos = pos;
        this.direction = facing;
        this.range = range;
        this.output = output;
        this.speed = speed;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(direction.ordinal());
        buf.writeInt(output.ordinal());
        buf.writeInt(range);
        buf.writeInt(speed);
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
        final World world = ctxIn.getSender().getLevel();
        final TileEntity entity = world.getBlockEntity(pos);
        if (entity instanceof TileEntityMultiBlock)
        {
            ((TileEntityMultiBlock) entity).setDirection(direction);
            ((TileEntityMultiBlock) entity).setOutput(output);
            ((TileEntityMultiBlock) entity).setRange(range);
            ((TileEntityMultiBlock) entity).setSpeed(speed);
            final BlockState state = world.getBlockState(pos);
            world.sendBlockUpdated(pos, state, state, 0x3);
        }
    }
}
