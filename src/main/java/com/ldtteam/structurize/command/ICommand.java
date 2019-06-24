package com.ldtteam.structurize.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;

/**
 * Interface for all commands
 */
public interface ICommand
{
    /**
     * Builds command's tree
     * 
     * @return new built command
     */
    public LiteralArgumentBuilder<CommandSource> build();

    /**
     * Called when command gets executed
     * HELL WRONG DESIGN -> change this to usage/help or whatever root command should have
     * 
     * @param command command being executed
     * @return amount of successful targets
     * @throws CommandException anything what implements should be used com.mojang.brigadier.exceptions.CommandExceptionType
     */
    public int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException;
}