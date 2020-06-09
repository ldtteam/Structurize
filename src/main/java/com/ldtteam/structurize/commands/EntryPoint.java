package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

/**
 * Mod entry command
 */
public class EntryPoint extends AbstractCommand
{
    /*
     * BRIGADIER manual:
     * use command tree to build complicated trees
     * use argument/command builder to create leaf commands:
     * - use #newLiteral() for subcommand or finite type selection
     * - use #newArgument() for collection selectors/number or message input etc., see net.minecraft.command.arguments
     * use builder#requires() to set eg. permissions
     * use builder#executes() to set action on execute
     * use builder#redirect() to set alias, use aliases sparely! (they might cause confusion)
     * use builder#fork() to create fork - note: I don't know why would you fork arguments and how does this work, the only usage is
     * mc execute command
     * client sided commands are not possible now (as of 25-06-2019)
     */
    /**
     * Private constructor to hide implicit public one.
     */
    private EntryPoint()
    {
        /**
         * Intentionally left empty
         */
    }

    /**
     * Registers mod command tree to given dispatcher.
     *
     * @param dispatcher main server command dispatcher
     */
    public static void register(final CommandDispatcher<CommandSource> dispatcher)
    {
        final CommandTree linkSession = new CommandTree("linksession").addNode(LinkSessionCommand.AboutMe.build())
            .addNode(LinkSessionCommand.AcceptInvite.build())
            .addNode(LinkSessionCommand.AddPlayer.build())
            .addNode(LinkSessionCommand.Create.build())
            .addNode(LinkSessionCommand.Destroy.build())
            .addNode(LinkSessionCommand.Leave.build())
            .addNode(LinkSessionCommand.MuteChannel.build())
            .addNode(LinkSessionCommand.RemovePlayer.build())
            .addNode(LinkSessionCommand.SendMessage.build());
        final CommandTree schemaServer = new CommandTree("schemaserver").addNode(SchemaServerCommand.Login.build())
            .addNode(SchemaServerCommand.Logout.build())
            .addNode(SchemaServerCommand.Styles.build());
        final CommandTree structurizeRoot = new CommandTree(Constants.MOD_ID).addNode(linkSession)
            .addNode(schemaServer)
            .addNode(UpdateSchematicsCommand.build())
            .addNode(ScanCommand.build());

        dispatcher.register(structurizeRoot.build());
    }
}
