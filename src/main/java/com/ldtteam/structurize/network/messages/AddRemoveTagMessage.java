package com.ldtteam.structurize.network.messages;

import com.ldtteam.blockout.Log;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Messages for adding or removing a tag
 */
public class AddRemoveTagMessage implements IMessage
{
    /**
     * Whether we add or remove a tag
     */
    private boolean add = false;

    /**
     * The tag to use
     */
    private String tag = "";

    /**
     * THe te's position
     */
    private BlockPos anchorPos;

    /**
     * The tags blockpos
     */
    private BlockPos tagPos;

    /**
     * Empty constructor used when registering the
     */
    public AddRemoveTagMessage()
    {
        super();
    }

    public AddRemoveTagMessage(final boolean add, final String tag, final BlockPos tagPos, final BlockPos anchorPos)
    {
        this.anchorPos = anchorPos;
        this.tagPos = tagPos;
        this.add = add;
        this.tag = tag;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeBoolean(add);
        buf.writeString(tag);
        buf.writeBlockPos(anchorPos);
        buf.writeBlockPos(tagPos);
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        add = buf.readBoolean();
        tag = buf.readString();
        anchorPos = buf.readBlockPos();
        tagPos = buf.readBlockPos();
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
        if (ctxIn.getSender() == null)
        {
            return;
        }

        TileEntity te = ctxIn.getSender().world.getTileEntity(anchorPos);
        if (te instanceof IBlueprintDataProvider)

        {
            IBlueprintDataProvider dataTE = (IBlueprintDataProvider) te;
            if (add)
            {
                dataTE.addTag(tagPos, tag);
            }
            else
            {
                dataTE.removeTag(tagPos, tag);
            }
        }
        else
        {
            Log.getLogger().info("Tried to add data tag to invalid tileentity:" + te);
        }
    }
}
