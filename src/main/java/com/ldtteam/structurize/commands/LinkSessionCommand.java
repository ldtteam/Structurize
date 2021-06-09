package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.commands.arguments.MultipleStringArgument;
import com.ldtteam.structurize.management.linksession.ChannelsEnum;
import com.ldtteam.structurize.management.linksession.LinkSessionManager;
import com.ldtteam.structurize.util.LanguageHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.IFormattableTextComponent;
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
        private final static String NAME = "create";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).executes(s -> onExecute(s));
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().getPlayerOrException();
            final UUID ownerUUID = sender.getUUID();
            if (!LinkSessionManager.INSTANCE.getMembersOf(ownerUUID).isEmpty())
            {
                throwSyntaxException("structurize.command.ls.create.already");
            }

            LinkSessionManager.INSTANCE.createSession(ownerUUID);
            LinkSessionManager.INSTANCE.addOrUpdateMemberInSession(ownerUUID, ownerUUID, sender.getGameProfile().getName());
            sender.sendMessage(LanguageHandler.buildChatComponent("structurize.command.ls.create.done", sender.getGameProfile().getName()), sender.getUUID());
            return 1;
        }
    }

    /**
     * Command for removing an existing sender's session
     */
    protected static class Destroy extends AbstractCommand
    {
        private final static String NAME = "destroy";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).executes(s -> onExecute(s));
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().getPlayerOrException();
            final UUID ownerUUID = sender.getUUID();

            if (LinkSessionManager.INSTANCE.destroySession(ownerUUID))
            {
                sender.sendMessage(LanguageHandler.buildChatComponent("structurize.command.ls.destroy.done", sender.getGameProfile().getName()), sender.getUUID());
            }
            else
            {
                throwSyntaxException("structurize.command.ls.generic.dontexist");
            }
            return 1;
        }
    }

    /**
     * Command for adding a new player(s) to an existing sender's session
     */
    protected static class AddPlayer extends AbstractCommand
    {
        private final static String NAME = "addplayer";
        private final static String TARGETS_ARG = "targets";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).then(newArgument(TARGETS_ARG, GameProfileArgument.gameProfile()).executes(s -> onExecute(s)));
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().getPlayerOrException();
            final MinecraftServer server = command.getSource().getServer();

            final UUID ownerUUID = sender.getUUID();
            final IFormattableTextComponent acceptButton = LanguageHandler.buildChatComponent("structurize.command.ls.invite.accept");
            final IFormattableTextComponent inviteMsg = LanguageHandler.buildChatComponent("structurize.command.ls.invite.message", sender.getGameProfile().getName());

            acceptButton.setStyle(acceptButton.getStyle()
                .withColor(TextFormatting.DARK_RED)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/structurize linksession acceptinvite " + ownerUUID.toString())));
            inviteMsg.append(acceptButton);

            if (LinkSessionManager.INSTANCE.getMembersOf(ownerUUID).isEmpty())
            {
                throwSyntaxException("structurize.command.ls.generic.dontexist");
            }

            int timesSucceeded = 0;
            for (final GameProfile gp : GameProfileArgument.getGameProfiles(command, TARGETS_ARG))
            {
                final ServerPlayerEntity target = server.getPlayerList().getPlayer(gp.getId());
                if (target != null)
                {
                    LinkSessionManager.INSTANCE.createInvite(target.getUUID(), ownerUUID);
                    target.sendMessage(inviteMsg, target.getUUID());
                    sender.sendMessage(LanguageHandler.buildChatComponent("structurize.command.ls.invite.done", gp.getName(), sender.getGameProfile().getName()), ownerUUID);
                    timesSucceeded++;
                }
            }
            return timesSucceeded;
        }
    }

    /**
     * Command for removing an existing player(s) to an existing sender's session
     */
    protected static class RemovePlayer extends AbstractCommand
    {
        private final static String NAME = "removeplayer";
        private final static String TARGETS_ARG = "targets";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).then(newArgument(TARGETS_ARG, GameProfileArgument.gameProfile()).executes(s -> onExecute(s)));
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().getPlayerOrException();
            final MinecraftServer server = command.getSource().getServer();
            final UUID ownerUUID = sender.getUUID();
            if (LinkSessionManager.INSTANCE.getMembersOf(ownerUUID).isEmpty())
            {
                throwSyntaxException("structurize.command.ls.generic.dontexist");
            }

            int timesSucceeded = 0;
            for (final GameProfile gp : GameProfileArgument.getGameProfiles(command, TARGETS_ARG))
            {
                final String name = gp.getName();
                final ServerPlayerEntity target = server.getPlayerList().getPlayerByName(name);
                if (target != null)
                {
                    LinkSessionManager.INSTANCE.removeMemberOfSession(ownerUUID, target.getUUID());
                    sender.sendMessage(LanguageHandler.buildChatComponent("structurize.command.ls.remove.done", name, sender.getGameProfile().getName()), sender.getUUID());
                    timesSucceeded++;
                }
            }
            return timesSucceeded;
        }
    }

    /**
     * Command for sending a message to "friends" of sender
     */
    protected static class SendMessage extends AbstractCommand
    {
        private final static String NAME = "sendmessage";
        private static final String MESSAGE_ARG = "message";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).then(newArgument(MESSAGE_ARG, MessageArgument.message()).executes(s -> onExecute(s)));
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().getPlayerOrException();
            final MinecraftServer server = command.getSource().getServer();

            final UUID senderUUID = sender.getUUID();
            final Set<UUID> uniqueMembers = LinkSessionManager.INSTANCE.execute(senderUUID, ChannelsEnum.COMMAND_MESSAGE);
            final TranslationTextComponent msgWithHead = new TranslationTextComponent(
                "commands.message.display.incoming",
                LanguageHandler.buildChatComponent("structurize.command.ls.message.head", Constants.MOD_NAME, sender.getGameProfile().getName()),
                    MessageArgument.getMessage(command, MESSAGE_ARG));

            if (LinkSessionManager.INSTANCE.getMuteState(senderUUID, ChannelsEnum.COMMAND_MESSAGE))
            {
                throwSyntaxException("structurize.command.ls.message.muted");
            }
            if (uniqueMembers.size() == 1)
            {
                throwSyntaxException("structurize.command.ls.message.norecipient");
            }

            msgWithHead.setStyle(msgWithHead.getStyle().withColor(TextFormatting.GRAY).withBold(true));
            uniqueMembers.forEach(member -> {
                final ServerPlayerEntity target = server.getPlayerList().getPlayer(member);
                if (target != null)
                {
                    target.sendMessage(msgWithHead, target.getUUID());
                }
            });
            return uniqueMembers.size();
        }
    }

    /**
     * Command which sends information of sender to sender
     */
    protected static class AboutMe extends AbstractCommand
    {
        private final static String NAME = "aboutme";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).executes(s -> onExecute(s));
        }

        // TODO: translations, wait for someone to cry first since it's pain to translate this :)
        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().getPlayerOrException();

            final UUID senderUUID = sender.getUUID();
            List<String> ownerSession = LinkSessionManager.INSTANCE.getMembersNamesOf(senderUUID);
            sender.sendMessage(new StringTextComponent("Info about \"" + sender.getGameProfile().getName() + "\":"), sender.getUUID());

            // has an invite?
            final String ownerName = LinkSessionManager.INSTANCE.hasInvite(senderUUID);
            if (ownerName == null)
            {
                sender.sendMessage(new StringTextComponent("  §cYou have no open invite."), sender.getUUID());
            }
            else
            {
                sender.sendMessage(new StringTextComponent("  §aYou have an open invite from " + ownerName + "."), sender.getUUID());
            }

            // is owner?
            if (ownerSession.isEmpty())
            {
                sender.sendMessage(new StringTextComponent("  §cYou don't have your own session."), sender.getUUID());
            }
            else
            {
                sender.sendMessage(new StringTextComponent("  §aYou own a session with:"), sender.getUUID());
                for (final String name : ownerSession)
                {
                    if (!name.equals("null"))
                    {
                        sender.sendMessage(new StringTextComponent("    §7" + name), sender.getUUID());
                    }
                    else
                    {
                        sender.sendMessage(new StringTextComponent("    §7Unknown name"), sender.getUUID());
                    }
                }
            }

            // is member?
            ownerSession = LinkSessionManager.INSTANCE.getSessionNamesOf(senderUUID);
            if (ownerSession.isEmpty())
            {
                sender.sendMessage(new StringTextComponent("  §cYou are not a part of other sessions."), sender.getUUID());
            }
            else
            {
                sender.sendMessage(new StringTextComponent("  §aYou are in sessions owned by:"), sender.getUUID());
                for (final String name : ownerSession)
                {
                    sender.sendMessage(new StringTextComponent("    §7" + name), sender.getUUID());
                }
            }

            // channels
            sender.sendMessage(new StringTextComponent("  §aChannels:"), sender.getUUID());
            for (final ChannelsEnum ch : ChannelsEnum.values())
            {
                if (LinkSessionManager.INSTANCE.getMuteState(senderUUID, ch))
                {
                    sender.sendMessage(new StringTextComponent(String.format("    §7%s:§r §c%s", ch.getCommandName(), "muted")), sender.getUUID());
                }
                else
                {
                    sender.sendMessage(new StringTextComponent(String.format("    §7%s:§r §a%s", ch.getCommandName(), "unmuted")), sender.getUUID());
                }
            }

            return 1;
        }
    }

    /**
     * Command for (un)muting a channel for sender
     */
    protected static class MuteChannel extends AbstractCommand
    {
        private final static String NAME = "mutechannel";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            final LiteralArgumentBuilder<CommandSource> root = newLiteral(NAME);

            for (final ChannelsEnum ch : ChannelsEnum.values())
            {
                root.then(newLiteral(ch.getCommandName()).executes(s -> onExecute(s, ch)));
            }

            return root;
        }

        public List<String> getTabCompletions()
        {
            return Stream.of(ChannelsEnum.values()).map(ch -> ch.getCommandName()).collect(Collectors.toList());
        }

        private static int onExecute(final CommandContext<CommandSource> command, final ChannelsEnum ch) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().getPlayerOrException();
            final UUID senderUUID = sender.getUUID();
            if (ch != null)
            {
                LinkSessionManager.INSTANCE.setMuteState(senderUUID, ch, !LinkSessionManager.INSTANCE.getMuteState(senderUUID, ch));
            }
            return 1;
        }
    }

    /**
     * Command for accepting the current invite
     */
    protected static class AcceptInvite extends AbstractCommand
    {
        private final static String NAME = "acceptinvite";
        private final static String UUID_ARG = "targets";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).executes(s -> onExecute(s, false)).then(newArgument(UUID_ARG, MessageArgument.message()).executes(s -> onExecute(s, true)));
        }

        private static int onExecute(final CommandContext<CommandSource> command, final boolean withUUID) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().getPlayerOrException();

            final UUID senderUUID = sender.getUUID();
            String ownerName = null;
            if (withUUID)
            {
                try
                {
                    ownerName = LinkSessionManager.INSTANCE.consumeInviteWithCheck(
                        senderUUID,
                        sender.getGameProfile().getName(),
                        UUID.fromString(MessageArgument.getMessage(command, UUID_ARG).getString()));
                }
                catch (final IllegalArgumentException e)
                {
                    throwSyntaxException("Unexpected error");
                }
                if (ownerName == null)
                {
                    throwSyntaxException("structurize.command.ls.invite.timeout");
                }
            }
            else
            {
                ownerName = LinkSessionManager.INSTANCE.consumeInvite(senderUUID, sender.getGameProfile().getName());
                if (ownerName == null)
                {
                    throwSyntaxException("structurize.command.ls.invite.noopen");
                }
            }

            sender.sendMessage(LanguageHandler.buildChatComponent("structurize.command.ls.invite.accepted", ownerName), sender.getUUID());
            return 1;
        }
    }

    /**
     * Command for leaving session of owner
     */
    protected static class Leave extends AbstractCommand
    {
        private final static String NAME = "leave";
        private final static String TARGET_ARG = "targets";

        protected static LiteralArgumentBuilder<CommandSource> build()
        {
            return newLiteral(NAME).then(
                newArgument(TARGET_ARG, MultipleStringArgument.multipleString((s, p) -> LinkSessionManager.INSTANCE.getSessionNamesOf(p.getUUID())))
                    .executes(s -> onExecute(s)));
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().getPlayerOrException();
            final MinecraftServer server = command.getSource().getServer();
            final String name = MultipleStringArgument.getResult(command, TARGET_ARG);

            if (server.getPlayerList().getPlayerByName(name) != null)
            {
                LinkSessionManager.INSTANCE.removeMemberOfSession(server.getPlayerList().getPlayerByName(name).getUUID(), sender.getUUID());
                sender.sendMessage(new StringTextComponent("Leaving a session owned by \"" + name + "\"."), sender.getUUID());
            }
            return 1;
        }
    }
}
