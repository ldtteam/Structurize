package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Messages for adding or removing a tag
 */
public class AddRemoveTagMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "add_remove_tag", AddRemoveTagMessage::new);

    /**
     * Whether we add or remove a tag
     */
    private final boolean add;

    /**
     * The tag to use
     */
    private final String tag;

    /**
     * THe te's position
     */
    private final BlockPos anchorPos;

    /**
     * The tags blockpos
     */
    private final BlockPos tagPos;

    /**
     * Empty constructor used when registering the
     */
    protected AddRemoveTagMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.add = buf.readBoolean();
        this.tag = buf.readUtf(32767);
        this.anchorPos = buf.readBlockPos();
        this.tagPos = buf.readBlockPos();
    }

    public AddRemoveTagMessage(final boolean add, final String tag, final BlockPos tagPos, final BlockPos anchorPos)
    {
        super(TYPE);
        this.anchorPos = anchorPos;
        this.tagPos = tagPos;
        this.add = add;
        this.tag = tag;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeBoolean(add);
        buf.writeUtf(tag);
        buf.writeBlockPos(anchorPos);
        buf.writeBlockPos(tagPos);
    }

    @Override

    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        final BlockEntity te = player.level().getBlockEntity(anchorPos);
        if (te instanceof IBlueprintDataProviderBE)
        {
            final IBlueprintDataProviderBE dataTE = (IBlueprintDataProviderBE) te;
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
