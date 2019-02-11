package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.api.util.constant.Constants;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.command.CommandTreeBase;

/**
 * Structurize root command.
 * <p>
 * Manages all sub commands.
 */
public class StructurizeCommand extends CommandTreeBase
{
    protected final static String NAME = Constants.MOD_ID;

    public StructurizeCommand()
    {
        super.addSubcommand(new LinkSessionCommand());
        super.addSubcommand(new ScanCommand());
        super.addSubcommand(new UpdateSchematicsCommand());
    }

    /**
     * Gets the name of the command
     */
    @Override
    public String getName()
    {
        return NAME;
    }

    /**
     * Return the required permission level for this command.
     */
    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    /**
     * Check if the given ICommandSender has permission to execute this command
     */
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    /**
     * Gets the usage string for the command.
     */
    @Override
    public String getUsage(ICommandSender sender)
    {
        
        final StringBuilder usage = new StringBuilder();
        usage.append("/");
        usage.append(NAME);
        usage.append(" <");
        for(final String sub : super.getCommandMap().keySet())
        {
            usage.append(sub);
            usage.append(" | ");
        }
        usage.delete(usage.length() - 3, usage.length());
        usage.append(">");
        return usage.toString();
    }

    /**
     * Callback for when the command is executed
     */
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException(this.getUsage(sender), new Object[0]);
        }
        super.execute(server, sender, args);
    }
}