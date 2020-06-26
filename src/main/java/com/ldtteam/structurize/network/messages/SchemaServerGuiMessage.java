package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.client.gui.schemaserver.LoginGui;
import com.ldtteam.structurize.management.schemaserver.DataActions;
import com.ldtteam.structurize.management.schemaserver.LoginHolder;
import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Basically client side commands.
 */
public enum SchemaServerGuiMessage
{
    LOGIN(0, false, () -> new LoginGui().open()),
    LOGOUT(1, true, LoginHolder.INSTANCE::logout),
    STYLES(2, true, () -> {/* TODO: styles */}),
    UPDATA_DATA_FILES(3, true, DataActions::setupDataFilesDiff),
    VIEW_UPDATE_DIFF(4, true, DataActions::viewLastDataFilesDiff),
    UPLOAD_UPDATE(5, true, DataActions::uploadLastDataFilesDiff);

    private final int id;
    private final boolean checkLogin;
    private final Runnable action;

    /**
     * @param id         serializable id
     * @param checkLogin true -> user must be logged in, false -> user must not be logged in
     * @param action     clientside command action
     */
    private SchemaServerGuiMessage(final int id, final boolean checkLogin, final Runnable action)
    {
        this.id = id;
        this.checkLogin = checkLogin;
        this.action = action;
    }

    private static void execute(final int idIn)
    {
        for (final SchemaServerGuiMessage type : values())
        {
            if (type.id == idIn)
            {
                if (type.checkLogin && !LoginHolder.INSTANCE.isUserLoggedIn())
                {
                    Structurize.proxy.notifyClientOrServerOps(LanguageHandler.prepareMessage("structurize.sslogin.user_not_loggedin"));
                }
                else if (!type.checkLogin && LoginHolder.INSTANCE.isUserLoggedIn())
                {
                    Structurize.proxy.notifyClientOrServerOps(
                        LanguageHandler.prepareMessage("structurize.sslogin.already_logged", LoginHolder.INSTANCE.getCurrentUsername()));
                }
                else
                {
                    type.action.run();
                }
                return;
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
