package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.linksession.ChannelsEnum;
import com.ldtteam.structurize.management.linksession.LinkSessionManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Complete management of /structurize linksession
 */
public class LinkSessionCommand
{
    /**
     * Command for creating a new session for sender
     */
    protected static class Create extends AbstractCommand
    {
       /* protected final static String NAME = "create";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME);
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final UUID ownerUUID = sender.getUniqueID();
            if (LinkSessionManager.INSTANCE.getMembersOf(ownerUUID) != null)
            {
                throw new CommandException("You have already created a session.");
            }

            LinkSessionManager.INSTANCE.createSession(ownerUUID);
            LinkSessionManager.INSTANCE.addOrUpdateMemberInSession(ownerUUID, ownerUUID, sender.getGameProfile().getName());
            sender.sendMessage(new StringTextComponent("Created session for player: " + sender.getName()));
        }

        */
    }

    /**
     * Command for removing an existing sender's session
     */
    protected static class Destroy extends AbstractCommand
    {
        /*
        protected final static String NAME = "destroy";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME);
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().asPlayer();

            final UUID ownerUUID = sender.getUniqueID();

            if (LinkSessionManager.INSTANCE.destroySession(ownerUUID))
            {
                sender.sendMessage(new StringTextComponent("Destroying session of player: " + sender.getName()));
            }
            else
            {
                throw new CommandException("You don't have a session created.");
            }
        }

         */
    }

    /**
     * Command for adding a new player(s) to an existing sender's session
     */
    protected static class AddPlayer extends AbstractCommand
    {
        /*
        protected final static String NAME = "addplayer";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME);
        }

        public boolean isUsernameIndex(String[] args, int index)
        {
            return true;
        }

        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
        {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final MinecraftServer server = command.getSource().getServer();

            final UUID ownerUUID = sender.getUniqueID();
            final StringTextComponent acceptButton = new StringTextComponent("ACCEPT");
            final StringTextComponent inviteMsg = new StringTextComponent("You have been invited to " + sender.getName() + "'s session, click the button to ");

            acceptButton.getStyle()
                .setColor(TextFormatting.DARK_RED)
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, new AcceptInvite().getUsage(sender) + " " + ownerUUID.toString()));
            inviteMsg.appendSibling(acceptButton);

            if (LinkSessionManager.INSTANCE.getMembersOf(ownerUUID) == null)
            {
                throw new CommandException("You don't have a session created.");
            }

            for (String name : args)
            {
                if (server.getPlayerList().getPlayerByUsername(name) == null)
                {
                    continue;
                }

                LinkSessionManager.INSTANCE.createInvite(server.getPlayerList().getPlayerByUsername(name).getUniqueID(), ownerUUID);
                server.getPlayerList().getPlayerByUsername(name).sendMessage(inviteMsg);
                sender.sendMessage(new StringTextComponent("Inviting player \"" + name + "\" to " + sender.getName() + "'s session."));
            }
        }

         */
    }

    /**
     * Command for removing an existing player(s) to an existing sender's session
     */
    protected static class RemovePlayer extends AbstractCommand
    {
        /*
        protected final static String NAME = "removeplayer";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME);
        }

        public boolean isUsernameIndex(String[] args, int index)
        {
            return true;
        }

        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
        {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final MinecraftServer server = command.getSource().getServer();

            final UUID ownerUUID = sender.getUniqueID();
            if (LinkSessionManager.INSTANCE.getMembersOf(ownerUUID) == null)
            {
                throw new CommandException("You don't have a session created.");
            }

            for (String name : args)
            {
                if (server.getPlayerList().getPlayerByUsername(name) == null)
                {
                    continue;
                }

                LinkSessionManager.INSTANCE.removeMemberOfSession(ownerUUID, server.getPlayerList().getPlayerByUsername(name).getUniqueID());
                sender.sendMessage(new StringTextComponent("Removing player \"" + name + "\" of " + sender.getName() + "'s session."));
            }
        }

         */
    }

    /**
     * Command for sending a message to "friends" of sender
     */
    protected static class SendMessage extends AbstractCommand
    {
        /*
        protected final static String NAME = "sendmessage";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME);
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final MinecraftServer server = command.getSource().getServer();

            final UUID senderUUID = sender.getUniqueID();
            final Set<UUID> uniqueMembers = LinkSessionManager.INSTANCE.execute(senderUUID, ChannelsEnum.COMMAND_MESSAGE);
            final TranslationTextComponent msgWithHead = new TranslationTextComponent(
                "commands.message.display.incoming",
                new Object[] {Constants.MOD_NAME + " Session Message " + sender.getName(), getChatComponentFromNthArg(sender, args, 0, true)});

            if (LinkSessionManager.INSTANCE.getMuteState(senderUUID, ChannelsEnum.COMMAND_MESSAGE))
            {
                throw new CommandException("You have messages channel muted.");
            }
            if (uniqueMembers.size() == 1)
            {
                throw new CommandException("You are not a part of any session or every other players have messages channel muted.");
            }

            msgWithHead.getStyle().setColor(TextFormatting.GRAY).setItalic(Boolean.valueOf(true));
            uniqueMembers.forEach(member -> {
                if (server.getPlayerList().getPlayerByUUID(member) != null)
                {
                    server.getPlayerList().getPlayerByUUID(member).sendMessage(msgWithHead);
                }
            });
        }

         */
    }

    /**
     * Command which sends information of sender to sender
     */
    protected static class AboutMe extends AbstractCommand
    {
        /*
        protected final static String NAME = "aboutme";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME);
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final MinecraftServer server = command.getSource().getServer();

            final UUID senderUUID = sender.getUniqueID();
            List<String> ownerSession = LinkSessionManager.INSTANCE.getMembersNamesOf(senderUUID);
            sender.sendMessage(new StringTextComponent("Info about \"" + sender.getName() + "\":"));

            // has an invite?
            final String ownerName = LinkSessionManager.INSTANCE.hasInvite(senderUUID);
            if (ownerName == null)
            {
                sender.sendMessage(new StringTextComponent("  §cYou have no open invite."));
            }
            else
            {
                sender.sendMessage(new StringTextComponent("  §aYou have an open invite from " + ownerName + "."));
            }

            // is owner?
            if (ownerSession == null)
            {
                sender.sendMessage(new StringTextComponent("  §cYou don't have your own session."));
            }
            else
            {
                sender.sendMessage(new StringTextComponent("  §aYou own a session with:"));
                for (String name : ownerSession)
                {
                    if (!name.equals("null"))
                    {
                        sender.sendMessage(new StringTextComponent("    §7" + name));
                    }
                    else
                    {
                        sender.sendMessage(new StringTextComponent("    §7Unknown name"));
                    }
                }
            }

            // is member?
            ownerSession = LinkSessionManager.INSTANCE.getSessionNamesOf(senderUUID);
            if (ownerSession == null)
            {
                sender.sendMessage(new StringTextComponent("  §cYou are not a part of other sessions."));
            }
            else
            {
                sender.sendMessage(new StringTextComponent("  §aYou are in sessions owned by:"));
                for (String name : ownerSession)
                {
                    sender.sendMessage(new StringTextComponent("    §7" + name));
                }
            }

            // channels
            sender.sendMessage(new StringTextComponent("  §aChannels:"));
            for (ChannelsEnum ch : ChannelsEnum.values())
            {
                if (LinkSessionManager.INSTANCE.getMuteState(senderUUID, ch))
                {
                    sender.sendMessage(new StringTextComponent(String.format("    §7%s:§r §c%s", ch.getCommandName(), "muted")));
                }
                else
                {
                    sender.sendMessage(new StringTextComponent(String.format("    §7%s:§r §a%s", ch.getCommandName(), "unmuted")));
                }
            }
        }

         */
    }

    /**
     * Command for (un)muting a channel for sender
     */
    protected static class MuteChannel extends AbstractCommand
    {
        /*
        protected final static String NAME = "mutechannel";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME);
        }

        public List<String> getTabCompletions()
        {
            return Stream.of(ChannelsEnum.values()).map(ch -> ch.getCommandName()).collect(Collectors.toList());
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final MinecraftServer server = command.getSource().getServer();

            final UUID senderUUID = sender.getUniqueID();

            for (String arg : args)
            {
                final ChannelsEnum ch = ChannelsEnum.getEnumByCommandName(arg);
                if (ch != null)
                {
                    LinkSessionManager.INSTANCE.setMuteState(senderUUID, ch, !LinkSessionManager.INSTANCE.getMuteState(senderUUID, ch));
                }
            }
        }

         */
    }

    /**
     * Command for accepting the current invite
     */
    protected static class AcceptInvite extends AbstractCommand
    {
        /*
        protected final static String NAME = "acceptinvite";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME);
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final MinecraftServer server = command.getSource().getServer();

            final UUID senderUUID = sender.getUniqueID();
            String ownerName = null;
            if (args.length > 0)
            {
                try
                {
                    ownerName = LinkSessionManager.INSTANCE.consumeInviteWithCheck(senderUUID, sender.getName(), UUID.fromString(args[0]));
                }
                catch (IllegalArgumentException e)
                {
                    throw new WrongUsageException(this.getUsage(sender), new Object[0]);
                }
                if (ownerName == null)
                {
                    throw new CommandException("This invite does not exist anymore.");
                }
            }
            else
            {
                ownerName = LinkSessionManager.INSTANCE.consumeInvite(senderUUID, sender.getGameProfile().getName());
                if (ownerName == null)
                {
                    throw new CommandException("You have no open invite.");
                }
            }

            sender.sendMessage(new StringTextComponent("You have successfully joined to " + ownerName + "'s linksession."));
        }

         */
    }

    /**
     * Command for leaving session of owner
     */
    protected static class Leave extends AbstractCommand
    {
        /*
        protected final static String NAME = "leave";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME);
        }

        public boolean isUsernameIndex(String[] args, int index)
        {
            return true;
        }

        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
        {
            return getListOfStringsMatchingLastWord(args, LinkSessionManager.INSTANCE.getSessionNamesOf(sender.getCommandSenderEntity().getUniqueID()));
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final MinecraftServer server = command.getSource().getServer();

            final UUID senderUUID = sender.getUniqueID();

            for (String name : args)
            {
                if (server.getPlayerList().getPlayerByUsername(name) == null)
                {
                    continue;
                }

                LinkSessionManager.INSTANCE.removeMemberOfSession(server.getPlayerList().getPlayerByUsername(name).getUniqueID(), senderUUID);
                sender.sendMessage(new StringTextComponent("Leaving a session owned by \"" + name + "\"."));
            }
        }

         */
    }


}
