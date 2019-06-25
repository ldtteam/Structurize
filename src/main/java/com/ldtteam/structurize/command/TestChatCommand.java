package com.ldtteam.structurize.command;

import com.ldtteam.structurize.Instances;
import com.ldtteam.structurize.network.messages.TestMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.util.text.ITextComponent;

public class TestChatCommand extends AbstractCommand
{
    private static final String MESSAGE_ARG = "message";

    protected static LiteralArgumentBuilder<CommandSource> build()
    {
        return newLiteral("sendmessage").then(newArgument(MESSAGE_ARG, MessageArgument.message()).executes((s) -> onExecute(s)));
    }

    private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
    {
        final ITextComponent msg = MessageArgument.getMessage(command, MESSAGE_ARG);
        Instances.getNetwork().sendToEveryone(new TestMessage(command.getInput() + "<|>" + msg.getString()));
        return 1;
    }
}
