package com.ldtteam.structurize.command;

import com.ldtteam.structurize.util.constants.GeneralConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;

/**
 * Mod entry command
 */
public class EntryPoint
{
    /*
     * BRIGADIER manual:
     * using then on the same level creates more level nodes
     * use #newLiteral() for subcommand or finite type selection
     * use #newArgument() for collection selectors/number or message input etc., try to use Minecraft premade ones from
     * net.minecraft.command.arguments
     * use builder#requires() to set e.g. permissions
     * use builder#executes()
     */

    private EntryPoint()
    {
        /**
         * Intentionally left empty
         */
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(newLiteral(GeneralConstants.MOD_ID).then(buildCommand(new TestChatCommand())));
    }

    private static LiteralArgumentBuilder<CommandSource> buildCommand(final ICommand cmd)
    {
        return cmd.build().executes((source) -> cmd.onExecute(source));
    }

    /**
     * Creates new subcommand, used for subcommands and type picking
     * 
     * @param name subcommand name
     * @return new node builder
     */
    public static LiteralArgumentBuilder<CommandSource> newLiteral(String name)
    {
        return LiteralArgumentBuilder.literal(name);
    }

    /**
     * Creates new command argument, used for collection selector, number picker etc.
     * 
     * @param <T>
     * @param name
     * @param type
     * @return new node builder
     */
    public static <T> RequiredArgumentBuilder<CommandSource, T> newArgument(String name, ArgumentType<T> type)
    {
        return RequiredArgumentBuilder.argument(name, type);
    }
}