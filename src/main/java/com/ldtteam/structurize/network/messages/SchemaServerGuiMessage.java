package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.client.gui.schemaserver.LoginGui;
import com.ldtteam.structurize.management.schemaserver.LoginHolder;
import com.ldtteam.structurize.management.schemaserver.Styles;
import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Schematic server login message. Opens login gui.
 */
public enum SchemaServerGuiMessage
{
    LOGIN(0, () -> {
        if (LoginHolder.INSTANCE.isUserLoggedIn())
        {
            Structurize.proxy.notifyClientOrServerOps(
                LanguageHandler.prepareMessage("structurize.sslogin.already_logged", LoginHolder.INSTANCE.getCurrentUsername()));
        }
        else
        {
            new LoginGui().open();
        }
    }),
    LOGOUT(1, () -> LoginHolder.INSTANCE.logout()),
    STYLES(2, () -> {
        if (!LoginHolder.INSTANCE.isUserLoggedIn())
        {
            Structurize.proxy.notifyClientOrServerOps(LanguageHandler.prepareMessage("structurize.sslogin.user_not_loggedin"));
        }
        else
        {
            Styles.openStylesGui();
        }
    });

    private final int id;
    private final Runnable action;

    private SchemaServerGuiMessage(final int id, final Runnable action)
    {
        this.id = id;
        this.action = action;
    }

    private static void execute(final int idIn)
    {
        for (final SchemaServerGuiMessage type : values())
        {
            if (type.id == idIn)
            {
                type.action.run();
            }
        }
    }

    public InnerMessage networkMessage()
    {
        return new InnerMessage(id);
    }

    public static class InnerMessage implements IMessage
    {
        private int id;

        /**
         * DON'T USE THAT TO CREATE THIS MESSAGE
         */
        public InnerMessage()
        {
            this.id = -1;
        }

        private InnerMessage(final int id)
        {
            this.id = id;
        }

        @Override
        public void toBytes(final PacketBuffer buf)
        {
            buf.writeInt(id);
        }

        @Override
        public void fromBytes(final PacketBuffer buf)
        {
            id = buf.readInt();
        }

        @Override
        public LogicalSide getExecutionSide()
        {
            return LogicalSide.CLIENT;
        }

        @Override
        public void onExecute(final Context ctxIn, final boolean isLogicalServer)
        {
            SchemaServerGuiMessage.execute(id);
        }
    }
}
