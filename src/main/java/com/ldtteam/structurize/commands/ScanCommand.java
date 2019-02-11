package com.ldtteam.structurize.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.util.ScanToolOperation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * Command for (un)muting a channel for sender
 */
public class ScanCommand extends CommandBase
{
    protected final static String NAME = "scan";

    @NotNull
    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/" + StructurizeCommand.NAME + " " + NAME + " <x1> <y1> <z1> <x2> <y2> <z2> [fileName] [blockToReplace > blockReplaceWith]...";
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
        if (args.length > 0 && args.length <= 3)
        {
            return getTabCompletionCoordinate(args, 0, targetPos);
        }
        else if (args.length > 3 && args.length <= 6)
        {
            return getTabCompletionCoordinate(args, 3, targetPos);
        }
        else if (args.length == 7)
        {
            return Collections.emptyList();
        }
        else if ((args.length % 3) == 0)
        {
            return Arrays.asList(">");
        }
        else
        {
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());
        }
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 6)
        {
            throw new WrongUsageException(this.getUsage(sender), new Object[0]);
        }
        
        final BlockPos from = parseBlockPos(sender, args, 0, false);
        final BlockPos to = parseBlockPos(sender, args, 3, false);

        if (args.length > 6)
        {
            final String fileName = args[6];

            if (!fileName.matches("^[\\w\\.,-]*"))
            {
                throw new CommandException("Please stick to the default characters \"A-Z a-z 0-9 . , _ -\" in the file name", new Object[0]);
            }
            
            //TODO: implement replacements and save structure to file
        }
        else
        {
            //TODO: open scan tool gui for client with given pos
        }
    }
}