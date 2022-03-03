package com.ldtteam.structurize.commands.arguments;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Command argument for selecting from string collection.
 * String must be made only from ascii characters.
 */
public class MultipleStringArgument implements ArgumentType<String>
{
    private final BiFunction<CommandContext<CommandSourceStack>, ServerPlayer, Collection<String>> dataProvider;

    public MultipleStringArgument(final BiFunction<CommandContext<CommandSourceStack>, ServerPlayer, Collection<String>> dataProvider)
    {
        this.dataProvider = dataProvider;
    }

    /**
     * Constructs new select from given collection argument.
     *
     * @param dataProvider choices provider, called server-side only
     * @return argument type
     */
    public static MultipleStringArgument multipleString(final BiFunction<CommandContext<CommandSourceStack>, ServerPlayer, Collection<String>> dataProvider)
    {
        return new MultipleStringArgument(dataProvider);
    }

    /**
     * Gets argument from command context.
     *
     * @param context command context
     * @param name    argument key
     * @return command argument as string
     */
    public static String getResult(final CommandContext<CommandSourceStack> context, final String name) throws CommandSyntaxException
    {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(final StringReader reader) throws CommandSyntaxException
    {
        return StringArgumentType.string().parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
    {
        if (context.getSource() instanceof ClientSuggestionProvider)
        {
            final CommandContext<SharedSuggestionProvider> ctx = (CommandContext<SharedSuggestionProvider>) context;
            return ctx.getSource().customSuggestion(context);
        }
        try
        {
            final CommandContext<CommandSourceStack> ctx = (CommandContext<CommandSourceStack>) context;
            return SharedSuggestionProvider.suggest(dataProvider.apply(ctx, ctx.getSource().getPlayerOrException()), builder);
        }
        catch (final CommandSyntaxException | NullPointerException e)
        {
            e.printStackTrace();
            return Suggestions.empty();
        }
    }

    /**
     * Noop serializer, we always go for server suggestions and it's impossible to create suggestions without command source
     */
    public static class Serializer implements ArgumentSerializer<MultipleStringArgument>
    {
        @Override
        public void serializeToNetwork(final MultipleStringArgument argument, final FriendlyByteBuf buffer)
        {
            // noop
        }

        @Override
        public MultipleStringArgument deserializeFromNetwork(final FriendlyByteBuf buffer)
        {
            // noop
            return null;
        }

        @Override
        public void serializeToJson(final MultipleStringArgument argument, final JsonObject json)
        {
            // noop
        }
    }
}