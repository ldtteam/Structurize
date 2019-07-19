package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntityMP;

/**
 * Message class which handles undoing a change to the world.
 */
public class UndoMessage extends AbstractMessage<UndoMessage, IMessage>
{
    /**
     * Empty public constructor.
     */
    public UndoMessage()
    {
        super();
    }

    @Override
    public void fromBytes(final ByteBuf buf)
    {
        /*
         * Nothing needed.
         */
    }

    @Override
    public void toBytes(final ByteBuf buf)
    {
        /*
         * Nothing needed.
         */
    }

    /**
     * Executes the message on the server thread.
     * Only if the player has the permission, toggle message.
     *
     * @param message the original message.
     * @param player  the player associated.
     */
    @Override
    public void messageOnServerThread(final UndoMessage message, final PlayerEntityMP player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            return;
        }

        Manager.undo(player);
    }
}
