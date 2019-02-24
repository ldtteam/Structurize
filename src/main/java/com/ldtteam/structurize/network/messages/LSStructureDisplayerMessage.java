package com.ldtteam.structurize.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.client.gui.WindowShapeTool;
import com.ldtteam.structurize.management.linksession.ChannelsEnum;
import com.ldtteam.structurize.management.linksession.LinkSessionManager;

import java.util.Set;
import java.util.UUID;

/**
 * Message for sharing structure Settings between players in one session
 */
public class LSStructureDisplayerMessage extends AbstractMessage<LSStructureDisplayerMessage, IMessage>
{
    private ByteBuf settings;
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
    public LSStructureDisplayerMessage(@NotNull final ByteBuf settings, @NotNull final boolean show)
    {
        super();
        this.settings = settings;
        this.show = show;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        show = buf.readBoolean();
        if (show)
        {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            settings = Unpooled.wrappedBuffer(bytes);
        }
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeBoolean(show);
        if (show)
        {
            buf.writeBytes(settings);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Displays or updates or destroys instance on target client
     * Copied from WindowBuildTool
     *
     * @param message Message
     * @param ctx     Context
     */
    @Override
    protected void messageOnClientThread(final LSStructureDisplayerMessage message, final MessageContext ctx)
    {
        if(message.show)
        {
            Settings.instance.fromBytes(message.settings);
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

    @Override
    public void messageOnServerThread(final LSStructureDisplayerMessage message, final EntityPlayerMP player)
    {
        if (LinkSessionManager.INSTANCE.getMuteState(player.getUniqueID(), ChannelsEnum.STRUCTURE_DISPLAYER))
        {
            return;
        }
        
        final Set<UUID> targets = LinkSessionManager.INSTANCE.execute(player.getUniqueID(), ChannelsEnum.STRUCTURE_DISPLAYER);
        targets.remove(player.getUniqueID()); // TODO: remove this to ensure desync will not appear?
        for(UUID target : targets)
        {
            if(player.getServer().getEntityFromUuid(target) instanceof EntityPlayerMP)
            {
                Structurize.getNetwork().sendTo(new LSStructureDisplayerMessage(message.settings, message.show), (EntityPlayerMP) player.getServer().getEntityFromUuid(target));
            }
        }
    }
}