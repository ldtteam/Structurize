package com.ldtteam.structurize.network.messages;

import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.client.gui.WindowShapeTool;
import com.ldtteam.structurize.management.linksession.ChannelsEnum;
import com.ldtteam.structurize.management.linksession.LinkSessionManager;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Message for sharing structure Settings between players in one session
 */
public class LSStructureDisplayerMessage implements IMessage
{
    private PacketBuffer settings;
    private boolean show;

    /**
     * Empty constructor used when registering the message.
     */
    public LSStructureDisplayerMessage()
    {
        super();
    }

    /**
     * Message for sharing structure Settings between players in one session
     * 
     * @param settings structure settings
     * @param show if true create or update, if false destroy
     */
    public LSStructureDisplayerMessage(@NotNull final PacketBuffer settings, @NotNull final boolean show)
    {
        super();
        this.settings = settings;
        this.show = show;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        show = buf.readBoolean();
        if (show)
        {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            settings = new PacketBuffer(Unpooled.wrappedBuffer(bytes));
        }
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeBoolean(show);
        if (show)
        {
            buf.writeBytes(settings);
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
            final PlayerEntity player = ctxIn.getSender();
            if (LinkSessionManager.INSTANCE.getMuteState(player.getUniqueID(), ChannelsEnum.STRUCTURE_DISPLAYER))
            {
                return;
            }

            final Set<UUID> targets = LinkSessionManager.INSTANCE.execute(player.getUniqueID(), ChannelsEnum.STRUCTURE_DISPLAYER);
            targets.remove(player.getUniqueID()); // remove this to ensure desync will not appear
            for(UUID target : targets)
            {
                final ServerPlayerEntity playerEntity = player.getServer().getPlayerList().getPlayerByUUID(target);
                if(playerEntity != null)
                {
                    Structurize.getNetwork().sendToPlayer(new LSStructureDisplayerMessage(settings, show), playerEntity);
                }
            }
        }
        else
        {
            if (show)
            {
                Settings.instance.fromBytes(settings);
                // TODO: better solution would be great
                if (Settings.instance.getStructureName() == null && Settings.instance.getStaticSchematicName() == null)
                {
                    WindowShapeTool.commonStructureUpdate();
                }
                else
                {
                    WindowBuildTool.commonStructureUpdate();
                }
            }
            else
            {
                Settings.instance.reset();
            }
        }
    }
}
