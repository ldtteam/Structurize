package com.ldtteam.structurize.commands;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.linksession.ChannelsEnum;
import com.ldtteam.structurize.management.linksession.LinkSessionManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.command.CommandTreeBase;

/**
 * Complete management of /structurize linksession
 */
public class LinkSessionCommand extends CommandTreeBase
{
    protected final static String NAME = "linksession";

    public LinkSessionCommand()
    {
        super.addSubcommand(new AboutMe());
        super.addSubcommand(new AcceptInvite());
        super.addSubcommand(new AddPlayer());
        super.addSubcommand(new Create());
        super.addSubcommand(new Destroy());
        super.addSubcommand(new MuteChannel());
        super.addSubcommand(new Leave());
        super.addSubcommand(new RemovePlayer());
        super.addSubcommand(new SendMessage());
    }

    @NotNull
    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        final StringBuilder usage = new StringBuilder();
        usage.append("/");
        usage.append(StructurizeCommand.NAME);
        usage.append(" ");
        usage.append(NAME);
        usage.append(" <");
        for (final String sub : super.getCommandMap().keySet())
        {
            usage.append(sub);
            usage.append(" | ");
        }
        usage.delete(usage.length() - 3, usage.length());
        usage.append(">");
        return usage.toString();
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!(sender instanceof EntityPlayer))
        {
            throw new CommandException("Can be only used from clients.", new Object[0]);
        }
        if (args.length < 1)
        {
            throw new WrongUsageException(this.getUsage(sender), new Object[0]);
        }
        super.execute(server, sender, args);
    }

    /**
     * Command for creating a new session for sender
     */
    private class Create extends CommandBase
    {
        protected final static String NAME = "create";

        @NotNull
        @Override
        public String getName()
        {
            return NAME;
        }

        @Override
        public String getUsage(ICommandSender sender)
        {
            return "/" + StructurizeCommand.NAME + " " + LinkSessionCommand.NAME + " " + NAME;
        }

        @Override
        public int getRequiredPermissionLevel()
        {
            return 0;
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return true;
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            if (args.length > 0)
            {
                throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            }

            final UUID ownerUUID = sender.getCommandSenderEntity().getUniqueID();
            if (LinkSessionManager.INSTANCE.getMembersOf(ownerUUID) != null)
            {
                throw new CommandException("You have already created a session.");
            }

            LinkSessionManager.INSTANCE.createSession(ownerUUID);
            LinkSessionManager.INSTANCE.addOrUpdateMemberInSession(ownerUUID, ownerUUID, sender.getName());
            sender.sendMessage(new TextComponentString("Created session for player: " + sender.getName()));
        }
    }

    /**
     * Command for removing an existing sender's session
     */
    private class Destroy extends CommandBase
    {
        protected final static String NAME = "destroy";

        @NotNull
        @Override
        public String getName()
        {
            return NAME;
        }

        @Override
        public String getUsage(ICommandSender sender)
        {
            return "/" + StructurizeCommand.NAME + " " + LinkSessionCommand.NAME + " " + NAME;
        }

        @Override
        public int getRequiredPermissionLevel()
        {
            return 0;
        }
        
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return true;
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            if (args.length > 0)
            {
                throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            }

            final UUID ownerUUID = sender.getCommandSenderEntity().getUniqueID();

            if (LinkSessionManager.INSTANCE.destroySession(ownerUUID))
            {
                sender.sendMessage(new TextComponentString("Destroying session of player: " + sender.getName())); 
            }
            else
            {
                throw new CommandException("You don't have a session created.");
            }
        }
    }

    /**
     * Command for adding a new player(s) to an existing sender's session
     */
    private class AddPlayer extends CommandBase
    {
        protected final static String NAME = "addplayer";

        @NotNull
        @Override
        public String getName()
        {
            return NAME;
        }

        @Override
        public String getUsage(ICommandSender sender)
        {
            return "/" + StructurizeCommand.NAME + " " + LinkSessionCommand.NAME + " " + NAME + " <nickname>";
        }

        @Override
        public int getRequiredPermissionLevel()
        {
            return 0;
        }
        
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return true;
        }

        @Override
        public boolean isUsernameIndex(String[] args, int index)
        {
            return true;
        }
        
        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
        {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            if (args.length < 1)
            {
                throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            }

            final UUID ownerUUID = sender.getCommandSenderEntity().getUniqueID();
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
                sender.sendMessage(new TextComponentString("Inviting player \"" + name + "\" to " + sender.getName() + "'s session."));
            }
        }
    }

    /**
     * Command for removing an existing player(s) to an existing sender's session
     */
    private class RemovePlayer extends CommandBase
    {
        protected final static String NAME = "removeplayer";

        @NotNull
        @Override
        public String getName()
        {
            return NAME;
        }

        @Override
        public String getUsage(ICommandSender sender)
        {
            return "/" + StructurizeCommand.NAME + " " + LinkSessionCommand.NAME + " " + NAME + " <nickname>";
        }

        @Override
        public int getRequiredPermissionLevel()
        {
            return 0;
        }
        
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return true;
        }

        @Override
        public boolean isUsernameIndex(String[] args, int index)
        {
            return true;
        }
        
        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
        {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            if (args.length < 1)
            {
                throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            }

            final UUID ownerUUID = sender.getCommandSenderEntity().getUniqueID();
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
                sender.sendMessage(new TextComponentString("Removing player \"" + name + "\" of " + sender.getName() + "'s session."));
            }
        }
    }

    /**
     * Command for sending a message to "friends" of sender
     */
    private class SendMessage extends CommandBase
    {
        protected final static String NAME = "sendmessage";

        @NotNull
        @Override
        public String getName()
        {
            return NAME;
        }

        @Override
        public String getUsage(ICommandSender sender)
        {
            return "/" + StructurizeCommand.NAME + " " + LinkSessionCommand.NAME + " " + NAME + " <message>";
        }

        @Override
        public int getRequiredPermissionLevel()
        {
            return 0;
        }
        
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return true;
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            if (args.length < 1)
            {
                throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            }
            
            final UUID senderUUID = sender.getCommandSenderEntity().getUniqueID();
            final Set<UUID> uniqueMembers = LinkSessionManager.INSTANCE.execute(senderUUID, ChannelsEnum.COMMAND_MESSAGE);
            final TextComponentTranslation msgWithHead = new TextComponentTranslation("commands.message.display.incoming",
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
                if(server.getEntityFromUuid(member) != null)
                {
                    server.getEntityFromUuid(member).sendMessage(msgWithHead);
                }
            });
        }
    }

    /**
     * Command which sends information of sender to sender
     */
    private class AboutMe extends CommandBase
    {
        protected final static String NAME = "aboutme";

        @NotNull
        @Override
        public String getName()
        {
            return NAME;
        }

        @Override
        public String getUsage(ICommandSender sender)
        {
            return "/" + StructurizeCommand.NAME + " " + LinkSessionCommand.NAME + " " + NAME;
        }

        @Override
        public int getRequiredPermissionLevel()
        {
            return 0;
        }
        
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return true;
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            if (args.length > 0)
            {
                throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            }

            final UUID senderUUID = sender.getCommandSenderEntity().getUniqueID();
            List<String> ownerSession = LinkSessionManager.INSTANCE.getMembersNamesOf(senderUUID);
            sender.sendMessage(new TextComponentString("Info about \"" + sender.getName() + "\":"));

            // has an invite?
            final String ownerName = LinkSessionManager.INSTANCE.hasInvite(senderUUID);
            if (ownerName == null)
            {
                sender.sendMessage(new TextComponentString("  §cYou have no open invite."));
            }
            else
            {
                sender.sendMessage(new TextComponentString("  §aYou have an open invite from " + ownerName + "."));
            }

            // is owner?
            if(ownerSession == null)
            {
                sender.sendMessage(new TextComponentString("  §cYou don't have your own session."));
            }
            else
            {
                sender.sendMessage(new TextComponentString("  §aYou own a session with:"));
                for(String name : ownerSession)
                {
                    if(!name.equals("null"))
                    {
                        sender.sendMessage(new TextComponentString("    §7" + name));
                    }
                    else
                    {
                        sender.sendMessage(new TextComponentString("    §7Unknown name"));
                    }
                }
            }

            // is member?
            ownerSession = LinkSessionManager.INSTANCE.getSessionNamesOf(senderUUID);
            if(ownerSession == null)
            {
                sender.sendMessage(new TextComponentString("  §cYou are not a part of other sessions."));
            }
            else
            {
                sender.sendMessage(new TextComponentString("  §aYou are in sessions owned by:"));
                for(String name : ownerSession)
                {
                    sender.sendMessage(new TextComponentString("    §7" + name));
                }
            }

            // channels
            sender.sendMessage(new TextComponentString("  §aChannels:"));
            for(ChannelsEnum ch : ChannelsEnum.values())
            {
                if(LinkSessionManager.INSTANCE.getMuteState(senderUUID, ch))
                {
                    sender.sendMessage(new TextComponentString(String.format("    §7%s:§r §c%s", ch.getCommandName(), "muted")));
                }
                else
                {
                    sender.sendMessage(new TextComponentString(String.format("    §7%s:§r §a%s", ch.getCommandName(), "unmuted")));
                }
            }
        }
    }

    /**
     * Command for (un)muting a channel for sender
     */
    private class MuteChannel extends CommandBase
    {
        protected final static String NAME = "mutechannel";

        @NotNull
        @Override
        public String getName()
        {
            return NAME;
        }

        @Override
        public String getUsage(ICommandSender sender)
        {
            return "/" + StructurizeCommand.NAME + " " + LinkSessionCommand.NAME + " " + NAME;
        }

        @Override
        public int getRequiredPermissionLevel()
        {
            return 0;
        }
        
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return true;
        }
        
        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
        {
            return Stream.of(ChannelsEnum.values()).map(ch -> ch.getCommandName()).collect(Collectors.toList());
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            if (args.length < 1)
            {
                throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            }

            final UUID senderUUID = sender.getCommandSenderEntity().getUniqueID();

            for(String arg : args)
            {
                final ChannelsEnum ch = ChannelsEnum.getEnumByCommandName(arg);
                if(ch != null)
                {
                    LinkSessionManager.INSTANCE.setMuteState(senderUUID, ch, !LinkSessionManager.INSTANCE.getMuteState(senderUUID, ch));
                }
            }
        }
    }

    /**
     * Command for accepting the current invite
     */
    private class AcceptInvite extends CommandBase
    {
        protected final static String NAME = "acceptinvite";

        @NotNull
        @Override
        public String getName()
        {
            return NAME;
        }

        @Override
        public String getUsage(ICommandSender sender)
        {
            return "/" + StructurizeCommand.NAME + " " + LinkSessionCommand.NAME + " " + NAME;
        }

        @Override
        public int getRequiredPermissionLevel()
        {
            return 0;
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return true;
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            if (args.length > 0)
            {
                throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            }

            final UUID senderUUID = sender.getCommandSenderEntity().getUniqueID();
            final String ownerName = LinkSessionManager.INSTANCE.consumeInvite(senderUUID, sender.getCommandSenderEntity().getName());

            if (ownerName == null)
            {
                throw new CommandException("You have no open invite.");
            }
            sender.sendMessage(new TextComponentString("You have successfully joined to " + sender.getName() + "'s linksession."));
        }
    }

    /**
     * Command for leaving session of owner
     */
    private class Leave extends CommandBase
    {
        protected final static String NAME = "leave";

        @NotNull
        @Override
        public String getName()
        {
            return NAME;
        }

        @Override
        public String getUsage(ICommandSender sender)
        {
            return "/" + StructurizeCommand.NAME + " " + LinkSessionCommand.NAME + " " + NAME + " <nickname>";
        }

        @Override
        public int getRequiredPermissionLevel()
        {
            return 0;
        }
        
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return true;
        }

        @Override
        public boolean isUsernameIndex(String[] args, int index)
        {
            return true;
        }
        
        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
        {
            return getListOfStringsMatchingLastWord(args, LinkSessionManager.INSTANCE.getSessionNamesOf(sender.getCommandSenderEntity().getUniqueID()));
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            if (args.length < 1)
            {
                throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            }

            final UUID senderUUID = sender.getCommandSenderEntity().getUniqueID();

            for (String name : args)
            {
                if (server.getPlayerList().getPlayerByUsername(name) == null)
                {
                    continue;
                }
                
                LinkSessionManager.INSTANCE.removeMemberOfSession(server.getPlayerList().getPlayerByUsername(name).getUniqueID(), senderUUID);
                sender.sendMessage(new TextComponentString("Leaving a session owned by \"" + name + "\"."));
            }
        }
    }
}