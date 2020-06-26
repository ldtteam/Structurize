package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.network.messages.SchemaServerGuiMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;

/**
 * Opens login gui client side.
 */
public class SchemaServerCommand
{
    protected static class Login extends AbstractCommand
    {
        private static final String NAME = "login";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).executes(Login::onExecute);
        }

        private static int onExecute(final CommandContext<CommandSource> context) throws CommandSyntaxException
        {
            Network.getNetwork().sendToPlayer(SchemaServerGuiMessage.LOGIN.networkMessage(), context.getSource().asPlayer());
            return 1;
        }
    }

    protected static class Logout extends AbstractCommand
    {
        private static final String NAME = "logout";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).executes(Logout::onExecute);
        }

        private static int onExecute(final CommandContext<CommandSource> context) throws CommandSyntaxException
        {
            Network.getNetwork().sendToPlayer(SchemaServerGuiMessage.LOGOUT.networkMessage(), context.getSource().asPlayer());
            return 1;
        }
    }

    protected static class Styles extends AbstractCommand
    {
        private static final String NAME = "styles";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).executes(Styles::onExecute);
        }

        private static int onExecute(final CommandContext<CommandSource> context) throws CommandSyntaxException
        {
            Network.getNetwork().sendToPlayer(SchemaServerGuiMessage.STYLES.networkMessage(), context.getSource().asPlayer());
            return 1;
        }
    }

    protected static class UpdateDataFiles extends AbstractCommand
    {
        private static final String NAME = "updateDataFiles";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).executes(UpdateDataFiles::onExecute);
        }

        private static int onExecute(final CommandContext<CommandSource> context) throws CommandSyntaxException
        {
            Network.getNetwork().sendToPlayer(SchemaServerGuiMessage.UPDATA_DATA_FILES.networkMessage(), context.getSource().asPlayer());
            return 1;
        }
    }

    protected static class ViewUpdateDiff extends AbstractCommand
    {
        private static final String NAME = "viewUpdateDiff";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).executes(ViewUpdateDiff::onExecute);
        }

        private static int onExecute(final CommandContext<CommandSource> context) throws CommandSyntaxException
        {
            Network.getNetwork().sendToPlayer(SchemaServerGuiMessage.VIEW_UPDATE_DIFF.networkMessage(), context.getSource().asPlayer());
            return 1;
        }
    }

    protected static class UploadUpdate extends AbstractCommand
    {
        private static final String NAME = "uploadUpdate";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).executes(UploadUpdate::onExecute);
        }

        private static int onExecute(final CommandContext<CommandSource> context) throws CommandSyntaxException
        {
            Network.getNetwork().sendToPlayer(SchemaServerGuiMessage.UPLOAD_UPDATE.networkMessage(), context.getSource().asPlayer());
            return 1;
        }
    }
}
