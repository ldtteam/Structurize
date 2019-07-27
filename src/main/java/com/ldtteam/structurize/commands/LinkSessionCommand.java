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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
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
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final UUID ownerUUID = sender.getUniqueID();
            if (LinkSessionManager.INSTANCE.getMembersOf(ownerUUID) != null)
            {
                throwSyntaxException("structurize.command.ls.create.already");
            }

            LinkSessionManager.INSTANCE.createSession(ownerUUID);
            LinkSessionManager.INSTANCE.addOrUpdateMemberInSession(ownerUUID, ownerUUID, sender.getGameProfile().getName());
            sender.sendMessage(LanguageHandler.buildChatComponent("structurize.command.ls.create.done", sender.getGameProfile().getName()));
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
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final UUID ownerUUID = sender.getUniqueID();

            if (LinkSessionManager.INSTANCE.destroySession(ownerUUID))
            {
                sender.sendMessage(LanguageHandler.buildChatComponent("structurize.command.ls.destroy.done", sender.getGameProfile().getName()));
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
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final MinecraftServer server = command.getSource().getServer();

            final UUID ownerUUID = sender.getUniqueID();
            final ITextComponent acceptButton = LanguageHandler.buildChatComponent("structurize.command.ls.invite.accept");
            final ITextComponent inviteMsg = LanguageHandler.buildChatComponent("structurize.command.ls.invite.message", sender.getGameProfile().getName());

            acceptButton.getStyle()
                .setColor(TextFormatting.DARK_RED)
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/structurize linksession acceptinvite " + ownerUUID.toString()));
            inviteMsg.appendSibling(acceptButton);

            if (LinkSessionManager.INSTANCE.getMembersOf(ownerUUID) == null)
            {
                throwSyntaxException("structurize.command.ls.generic.dontexist");
            }

            int timesSucceeded = 0;
            for (final GameProfile gp : GameProfileArgument.getGameProfiles(command, TARGETS_ARG))
            {
                final String name = gp.getName();
                final ServerPlayerEntity target = server.getPlayerList().getPlayerByUsername(name);
                if (target != null)
                {
                    LinkSessionManager.INSTANCE.createInvite(target.getUniqueID(), ownerUUID);
                    target.sendMessage(inviteMsg);
                    sender.sendMessage(LanguageHandler.buildChatComponent("structurize.command.ls.invite.done", name, sender.getGameProfile().getName()));
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
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final MinecraftServer server = command.getSource().getServer();
            final UUID ownerUUID = sender.getUniqueID();
            if (LinkSessionManager.INSTANCE.getMembersOf(ownerUUID) == null)
            {
                throwSyntaxException("structurize.command.ls.generic.dontexist");
            }

            int timesSucceeded = 0;
            for (final GameProfile gp : GameProfileArgument.getGameProfiles(command, TARGETS_ARG))
            {
                final String name = gp.getName();
                final ServerPlayerEntity target = server.getPlayerList().getPlayerByUsername(name);
                if (target != null)
                {
                    LinkSessionManager.INSTANCE.removeMemberOfSession(ownerUUID, target.getUniqueID());
                    sender.sendMessage(LanguageHandler.buildChatComponent("structurize.command.ls.remove.done", name, sender.getGameProfile().getName()));
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
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final MinecraftServer server = command.getSource().getServer();

            final UUID senderUUID = sender.getUniqueID();
            final Set<UUID> uniqueMembers = LinkSessionManager.INSTANCE.execute(senderUUID, ChannelsEnum.COMMAND_MESSAGE);
            final TranslationTextComponent msgWithHead = new TranslationTextComponent(
                "commands.message.display.incoming",
                new Object[] {LanguageHandler.buildChatComponent("structurize.command.ls.message.head", Constants.MOD_NAME, sender.getGameProfile().getName()),
                    MessageArgument.getMessage(command, MESSAGE_ARG)});

            if (LinkSessionManager.INSTANCE.getMuteState(senderUUID, ChannelsEnum.COMMAND_MESSAGE))
            {
                throwSyntaxException("structurize.command.ls.message.muted");
            }
            if (uniqueMembers.size() == 1)
            {
                throwSyntaxException("structurize.command.ls.message.norecipient");
            }

            msgWithHead.getStyle().setColor(TextFormatting.GRAY).setItalic(Boolean.valueOf(true));
            uniqueMembers.forEach(member -> {
                final ServerPlayerEntity target = server.getPlayerList().getPlayerByUUID(member);
                if (target != null)
                {
                    target.sendMessage(msgWithHead);
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
            final ServerPlayerEntity sender = command.getSource().asPlayer();

            final UUID senderUUID = sender.getUniqueID();
            List<String> ownerSession = LinkSessionManager.INSTANCE.getMembersNamesOf(senderUUID);
            sender.sendMessage(new StringTextComponent("Info about \"" + sender.getGameProfile().getName() + "\":"));

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
                for (final String name : ownerSession)
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
                for (final String name : ownerSession)
                {
                    sender.sendMessage(new StringTextComponent("    §7" + name));
                }
            }

            // channels
            sender.sendMessage(new StringTextComponent("  §aChannels:"));
            for (final ChannelsEnum ch : ChannelsEnum.values())
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
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final UUID senderUUID = sender.getUniqueID();
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
            final ServerPlayerEntity sender = command.getSource().asPlayer();

            final UUID senderUUID = sender.getUniqueID();
            String ownerName = null;
            if (withUUID)
            {
                try
                {
                    ownerName = LinkSessionManager.INSTANCE.consumeInviteWithCheck(
                        senderUUID,
                        sender.getGameProfile().getName(),
                        UUID.fromString(MessageArgument.getMessage(command, UUID_ARG).getUnformattedComponentText()));
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

            sender.sendMessage(LanguageHandler.buildChatComponent("structurize.command.ls.invite.accepted", ownerName));
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
                newArgument(TARGET_ARG, MultipleStringArgument.multipleString((s, p) -> LinkSessionManager.INSTANCE.getSessionNamesOf(p.getUniqueID())))
                    .executes(s -> onExecute(s)));
        }

        private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
        {
            final ServerPlayerEntity sender = command.getSource().asPlayer();
            final MinecraftServer server = command.getSource().getServer();
            final String name = MultipleStringArgument.getResult(command, TARGET_ARG);

            if (server.getPlayerList().getPlayerByUsername(name) != null)
            {
                LinkSessionManager.INSTANCE.removeMemberOfSession(server.getPlayerList().getPlayerByUsername(name).getUniqueID(), sender.getUniqueID());
                sender.sendMessage(new StringTextComponent("Leaving a session owned by \"" + name + "\"."));
            }
            return 1;
        }
    }
}
