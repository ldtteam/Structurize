package com.ldtteam.structurize.command;

import java.util.ArrayList;
import java.util.List;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;

/**
 * Interface for all commands
 */
public abstract class AbstractCommand
{
    /**
     * Builds command's tree
     * 
     * @return new built command
     */
    protected static LiteralArgumentBuilder<CommandSource> build()
    {
        throw new RuntimeException("Missing command builder!");
    }

    /**
     * Creates new subcommand, used for subcommands and type picking
     * 
     * @param name subcommand name
     * @return new node builder
     */
    protected static LiteralArgumentBuilder<CommandSource> newLiteral(String name)
    {
        return LiteralArgumentBuilder.literal(name);
    }

    /**
     * Creates new command argument, used for collection selector, number picker etc.
     * 
     * @param <T>  argument class type
     * @param name argument name, aka description/alias, but it's also id key to get argument value from command during execution
     * @param type argument type, see net.minecraft.command.arguments
     * @return new node builder
     */
    protected static <T> RequiredArgumentBuilder<CommandSource, T> newArgument(String name, ArgumentType<T> type)
    {
        return RequiredArgumentBuilder.argument(name, type);
    }

    /**
     * Class for building command trees efectively
     */
    protected static class CommandTree
    {
        private final LiteralArgumentBuilder<CommandSource> backingCommand;
        private final List<CommandTree> nodes;

        /**
         * Creates new command tree
         * 
         * @param commandName root vertex name
         */
        protected CommandTree(final String commandName)
        {
            backingCommand = newLiteral(commandName);
            nodes = new ArrayList<>();
        }

        /**
         * Adds new tree as leaf into this tree
         * 
         * @param tree new tree to add
         * @return this
         */
        protected CommandTree addNode(final CommandTree tree)
        {
            nodes.add(tree);
            return this;
        }

        /**
         * Adds new command as leaf into this tree
         * 
         * @param command new commnad to add
         * @return this
         */
        protected CommandTree addNode(final LiteralArgumentBuilder<CommandSource> command)
        {
            backingCommand.then(command.build());
            return this;
        }

        /**
         * Builds whole tree for dispatcher
         * 
         * @return tree as command node
         */
        protected LiteralArgumentBuilder<CommandSource> build()
        {
            for (CommandTree ct : nodes)
            {
                backingCommand.then(ct.build().build());
            }
            return backingCommand;
        }
    }
}