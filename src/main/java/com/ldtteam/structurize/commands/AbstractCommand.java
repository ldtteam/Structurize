package com.ldtteam.structurize.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.util.LanguageHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands.EnvironmentType;
import net.minecraft.util.Tuple;

/**
 * Interface for all commands
 */
public abstract class AbstractCommand
{
    /**
     * Get's command's server environment type.
     * <li>{@link EnvironmentType#INTEGRATED} register only when on integrated server</li>
     * <li>{@link EnvironmentType#DEDICATED} register only when on dedicated server</li>
     * <li>{@link EnvironmentType#ALL} register always</li>
     *
     * @return in which command environment should be command registered
     */
    protected static EnvironmentType getEnvironmentType()
    {
        return EnvironmentType.ALL;
    }

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
    public static void throwSyntaxException(final String key) throws CommandSyntaxException
    {
        throw new CommandSyntaxException(new StructurizeCommandExceptionType(), new LiteralMessage(LanguageHandler.translateKey(key)));
    }

    /**
     * Throws command syntax exception.
     *
     * @param key    language key to translate
     * @param format String.format() attributes
     */
    public static void throwSyntaxException(final String key, final Object... format) throws CommandSyntaxException
    {
        throw new CommandSyntaxException(new StructurizeCommandExceptionType(),
            new LiteralMessage(LanguageHandler.translateKeyWithFormat(key, format)));
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
         * List of child trees, commands are directly baked into rootNode
         */
        private final List<CommandTree> childTrees;
        private final List<Tuple<Supplier<EnvironmentType>, Supplier<LiteralArgumentBuilder<CommandSource>>>> childNodes;
        /**
         * Target environment type.
         */
        private final EnvironmentType buildWhenOn;
        private final String commandName;

        /**
         * @return constructs new root node
         */
        protected static CommandTree newRootNode()
        {
            return new CommandTree(EnvironmentType.ALL, Constants.MOD_ID);
        }

        /**
         * Creates new command tree.
         *
         * @param commandName root vertex name
         */
        protected CommandTree(final EnvironmentType environment, final String commandName)
        {
            this.childTrees = new ArrayList<>();
            this.childNodes = new ArrayList<>();
            this.buildWhenOn = environment;
            this.commandName = commandName;
        }

        /**
         * Adds new tree as leaf into this tree.
         *
         * @param tree new tree to add
         * @return this
         */
        protected CommandTree addNode(final CommandTree tree)
        {
            childTrees.add(tree);
            return this;
        }

        /**
         * Adds new command as leaf into this tree.
         *
         * @param commandBuilder    command to add
         * @param commandEnviroment command's enviroment getter
         * @return this
         */
        protected CommandTree addNode(final Supplier<LiteralArgumentBuilder<CommandSource>> commandBuilder,
            final Supplier<EnvironmentType> commandEnviroment)
        {
            childNodes.add(new Tuple<>(commandEnviroment, commandBuilder));
            return this;
        }

        /**
         * Builds whole tree for dispatcher.
         *
         * @return tree as command node
         */
        protected Optional<LiteralArgumentBuilder<CommandSource>> build(final EnvironmentType environment)
        {
            if (!checkEnvironment(environment, buildWhenOn))
            {
                return Optional.empty();
            }

            final LiteralArgumentBuilder<CommandSource> rootNode = newLiteral(commandName);

            for (final Tuple<Supplier<EnvironmentType>, Supplier<LiteralArgumentBuilder<CommandSource>>> node : childNodes)
            {
                if (checkEnvironment(environment, node.getA().get()))
                {
                    rootNode.then(node.getB().get());
                }
            }
            for (final CommandTree tree : childTrees)
            {
                final Optional<LiteralArgumentBuilder<CommandSource>> builtTree = tree.build(environment);
                if (builtTree.isPresent())
                {
                    rootNode.then(builtTree.get().build());
                }
            }

            return childNodes.isEmpty() && childTrees.isEmpty() ? Optional.empty() : Optional.of(rootNode);
        }

        protected void register(final CommandDispatcher<CommandSource> commandDispatcher, final EnvironmentType serverEnvironmentType)
        {
            final Optional<LiteralArgumentBuilder<CommandSource>> builtTree = build(serverEnvironmentType);

            if (builtTree.isPresent())
            {
                commandDispatcher.register(builtTree.get());
            }
        }

        /**
         * @return true if either of arguments is {@link EnvironmentType#ALL} or arguments are of the same type
         */
        private boolean checkEnvironment(final EnvironmentType server, final EnvironmentType command)
        {
            return server == EnvironmentType.ALL || command == EnvironmentType.ALL || server == command;
        }
    }
}