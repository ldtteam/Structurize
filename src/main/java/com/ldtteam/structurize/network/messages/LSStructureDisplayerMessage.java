package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.client.gui.WindowShapeTool;
import com.ldtteam.structurize.management.linksession.ChannelsEnum;
import com.ldtteam.structurize.management.linksession.LinkSessionManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Message for sharing structure Settings between players in one session
 */
public class LSStructureDisplayerMessage implements IMessage
{
    private final CompoundTag settings;
    private final boolean show;

    /**
     * Empty constructor used when registering the message.
     */
    public LSStructureDisplayerMessage(final FriendlyByteBuf buf)
    {
        this.show = buf.readBoolean();
        this.settings = show ? buf.readNbt() : null;
    }

    /**
     * Message for sharing structure Settings between players in one session
     * 
     * @param compoundNBT structure settings
     * @param show if true create or update, if false destroy
     */
    public LSStructureDisplayerMessage(final CompoundTag compoundNBT, final boolean show)
    {
        this.settings = compoundNBT;
        this.show = show;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBoolean(show);
        if (show)
        {
            buf.writeNbt(settings);
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
            final Player player = ctxIn.getSender();
            if (LinkSessionManager.INSTANCE.getMuteState(player.getUUID(), ChannelsEnum.STRUCTURE_DISPLAYER))
            {
                return;
            }

            final Set<UUID> targets = LinkSessionManager.INSTANCE.execute(player.getUUID(), ChannelsEnum.STRUCTURE_DISPLAYER);
            targets.remove(player.getUUID()); // remove this to ensure desync will not appear
            for (final UUID target : targets)
            {
                final ServerPlayer playerEntity = player.getServer().getPlayerList().getPlayer(target);
                if (playerEntity != null)
                {
                    Network.getNetwork().sendToPlayer(new LSStructureDisplayerMessage(settings, show), playerEntity);
                }
            }
        }
        else
        {
            if (show)
            {
                Settings.instance.deserializeNBT(settings);
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
