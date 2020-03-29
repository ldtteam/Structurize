package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.network.messages.SSLoginMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

/**
 * Opens login gui client side.
 */
public class SSLoginCommand extends AbstractCommand
{
    private static final String NAME = "schemaServerLogin";

    protected static LiteralArgumentBuilder<CommandSource> build()
    {
        return newLiteral("schemaServerLogin").executes(context -> {
            Network.getNetwork().sendToPlayer(new SSLoginMessage(), context.getSource().asPlayer());
            return 1;
        });
    }
}
