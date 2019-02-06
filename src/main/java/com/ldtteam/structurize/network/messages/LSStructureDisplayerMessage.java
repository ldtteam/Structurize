package com.ldtteam.structurize.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import com.ldtteam.structurize.api.util.BlockUtils;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;

import java.util.Set;
import java.util.UUID;

/**
 * Class handling the Server UUID Message.
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
     * Copied from WindowBuildTool, TODO: make common method with new BO
     *
     * @param message Message
     * @param ctx     Context
     */
    @Override
    protected void messageOnClientThread(final LSStructureDisplayerMessage message, final MessageContext ctx)
    {
        if(message.show)
        {
            final String sname;

            Settings.instance.fromBytes(message.settings);
            if (Settings.instance.isStaticSchematicMode())
            {
                sname = Settings.instance.getStaticSchematicName();
            }
            else
            {
                sname = Settings.instance.getStructureName();
            }

            final StructureName structureName = new StructureName(sname);
            final Structure structure = new Structure(null, structureName.toString(),
                new PlacementSettings().setRotation(BlockUtils.getRotation(Settings.instance.getRotation())).setMirror(Settings.instance.getMirror()));
    
            final String md5 = Structures.getMD5(structureName.toString());
            if (structure.isTemplateMissing() || !structure.isCorrectMD5(md5))
            {
                if (structure.isTemplateMissing())
                {
                    Log.getLogger().info("Template structure " + structureName + " missing");
                }
                else
                {
                    Log.getLogger().info("structure " + structureName + " md5 error");
                }
    
                Log.getLogger().info("Request To Server for structure " + structureName);
                if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
                {
                    Structurize.getNetwork().sendToServer(new SchematicRequestMessage(structureName.toString()));
                    return;
                }
                else
                {
                    Log.getLogger().error("WindowBuildTool: Need to download schematic on a standalone client/server. This should never happen");
                }
            }
            Settings.instance.setStructureName(structureName.toString());
            Settings.instance.setActiveSchematic(structure);
        }
        else
        {
            Settings.instance.reset();
        }
    }

    @Override
    public void messageOnServerThread(final LSStructureDisplayerMessage message, final EntityPlayerMP player)
    {
        final Set<UUID> targets = Structurize.linkSessionManager.getUniquePlayersInSessionsOf(player.getUniqueID());
        targets.remove(player.getUniqueID()); // TODO: remove this to ensure no desync will appear?
        for(UUID target : targets)
        {
            if(player.getServer().getEntityFromUuid(target) instanceof EntityPlayerMP)
            {
                Structurize.getNetwork().sendTo(new LSStructureDisplayerMessage(message.settings, message.show), (EntityPlayerMP) player.getServer().getEntityFromUuid(target));
            }
        }
    }
}
