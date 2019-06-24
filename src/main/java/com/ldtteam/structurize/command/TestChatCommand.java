package com.ldtteam.structurize.command;

import com.ldtteam.structurize.Instances;
import com.ldtteam.structurize.network.messages.TestMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.util.text.ITextComponent;

public class TestChatCommand implements ICommand
{
    private static final String MESSAGE_ARG = "message";

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return EntryPoint.newLiteral("sendmessage").then(EntryPoint.newArgument(MESSAGE_ARG, MessageArgument.message()).executes((s) -> onExecute(s)));
    }

    @Override
    public int onExecute(CommandContext<CommandSource> command) throws CommandSyntaxException
    {
        ITextComponent msg = MessageArgument.getMessage(command, MESSAGE_ARG);
        Instances.getNetwork().sendToEveryone(new TestMessage(msg.getString()));
        return 1;
    }
}