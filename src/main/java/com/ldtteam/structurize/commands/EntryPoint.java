package com.ldtteam.structurize.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands.EnvironmentType;

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
     * use builder#requires() to set e.g. permissions
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
    public static void register(final CommandDispatcher<CommandSource> dispatcher, final EnvironmentType environment)
    {
        final CommandTree linkSession = new CommandTree(EnvironmentType.ALL, "linksession")
            .addNode(LinkSessionCommand.AboutMe::build, AbstractCommand::getEnvironmentType)
            .addNode(LinkSessionCommand.AcceptInvite::build, AbstractCommand::getEnvironmentType)
            .addNode(LinkSessionCommand.AddPlayer::build, AbstractCommand::getEnvironmentType)
            .addNode(LinkSessionCommand.Create::build, AbstractCommand::getEnvironmentType)
            .addNode(LinkSessionCommand.Destroy::build, AbstractCommand::getEnvironmentType)
            .addNode(LinkSessionCommand.Leave::build, AbstractCommand::getEnvironmentType)
            .addNode(LinkSessionCommand.MuteChannel::build, AbstractCommand::getEnvironmentType)
            .addNode(LinkSessionCommand.RemovePlayer::build, AbstractCommand::getEnvironmentType)
            .addNode(LinkSessionCommand.SendMessage::build, AbstractCommand::getEnvironmentType);
        final CommandTree structurizeRoot = CommandTree.newRootNode()
            .addNode(linkSession)
            .addNode(UpdateSchematicsCommand::build, () -> EnvironmentType.INTEGRATED)
            .addNode(ScanCommand::build, AbstractCommand::getEnvironmentType);

        structurizeRoot.register(dispatcher, environment);
    }
}
