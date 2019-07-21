package com.ldtteam.structurize.commands;

import java.util.ArrayList;
import java.util.List;
import com.ldtteam.structurize.util.LanguageHandler;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;

/**
 * Interface for all commands
 */
public abstract class AbstractCommand
{
    /**
     * Builds command's tree.
     *
     * @return new built command
     */
    protected static LiteralArgumentBuilder<CommandSource> build()
    {
        throw new RuntimeException("Missing command builder!");
    }

    /**
     * Creates new subcommand, used for subcommands and type picking.
     *
     * @param name subcommand name
     * @return new node builder
     */
    protected static LiteralArgumentBuilder<CommandSource> newLiteral(final String name)
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
    protected static <T> RequiredArgumentBuilder<CommandSource, T> newArgument(final String name, final ArgumentType<T> type)
    {
        return RequiredArgumentBuilder.argument(name, type);
    }

    /**
     * Throws command syntax exception.
     *
     * @param key language key to translate
     */
    protected static void throwSyntaxException(final String key) throws CommandSyntaxException
    {
        throw new CommandSyntaxException(new StructurizeCommandExceptionType(), new LiteralMessage(LanguageHandler.translateKey(key)));
    }

    /**
     * Throws command syntax exception.
     *
     * @param key    language key to translate
     * @param format String.format() attributes
     */
    protected static void throwSyntaxException(final String key, final Object... format) throws CommandSyntaxException
    {
        throw new CommandSyntaxException(new StructurizeCommandExceptionType(), new LiteralMessage(LanguageHandler.translateKeyWithFormat(key, format)));
    }

    /**
     * Our dummy exception type
     */
    public static class StructurizeCommandExceptionType implements CommandExceptionType
    {
        /**
         * Creates a dummy exception type
         */
        public StructurizeCommandExceptionType()
        {
            /**
             * Intentionally left empty
             */
        }
    }

    /**
     * Class for building command trees efectively
     */
    protected static class CommandTree
    {
        /**
         * Tree root node
         */
        private final LiteralArgumentBuilder<CommandSource> rootNode;
        /**
         * List of child trees, commands are directly baked into rootNode
         */
        private final List<CommandTree> childNodes;

        /**
         * Creates new command tree.
         *
         * @param commandName root vertex name
         */
        protected CommandTree(final String commandName)
        {
            rootNode = newLiteral(commandName);
            childNodes = new ArrayList<>();
        }

        /**
         * Adds new tree as leaf into this tree.
         *
         * @param tree new tree to add
         * @return this
         */
        protected CommandTree addNode(final CommandTree tree)
        {
            childNodes.add(tree);
            return this;
        }

        /**
         * Adds new command as leaf into this tree.
         *
         * @param command new commnad to add
         * @return this
         */
        protected CommandTree addNode(final LiteralArgumentBuilder<CommandSource> command)
        {
            rootNode.then(command.build());
            return this;
        }

        /**
         * Builds whole tree for dispatcher.
         *
         * @return tree as command node
         */
        protected LiteralArgumentBuilder<CommandSource> build()
        {
            for (final CommandTree ct : childNodes)
            {
                addNode(ct.build());
            }
            return rootNode;
        }
    }
}
