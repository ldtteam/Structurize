package com.ldtteam.structurize.commands.arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * Command argument for selecting from string collection.
 * String must be made only from ascii characters.
 */
public class MultipleStringArgument implements ArgumentType<String>
{
    private final BiFunction<CommandContext<CommandSource>, ServerPlayerEntity, Collection<String>> dataProvider;

    public MultipleStringArgument(final BiFunction<CommandContext<CommandSource>, ServerPlayerEntity, Collection<String>> dataProvider)
    {
        this.dataProvider = dataProvider;
    }

    /**
     * Constructs new select from given collection argument.
     *
     * @param dataProvider collection of choices
     * @return argument type
     */
    public static MultipleStringArgument multipleString(final BiFunction<CommandContext<CommandSource>, ServerPlayerEntity, Collection<String>> dataProvider)
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
    public static String getResult(final CommandContext<CommandSource> context, final String name) throws CommandSyntaxException
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
            final CommandContext<ISuggestionProvider> ctx = (CommandContext<ISuggestionProvider>) context;
            return ctx.getSource().getSuggestionsFromServer(ctx, builder);
        }
        final CommandContext<CommandSource> ctx = (CommandContext<CommandSource>) context;
        final StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());
        final int cursorBegin = reader.getCursor();

        while (reader.canRead() && isValidCharacter(reader.peek()))
        {
            reader.skip();
        }

        final String argument = reader.getString().substring(cursorBegin, reader.getCursor());
        final int suggestionLength = argument.length();
        List<String> suggestions = new ArrayList<>();

        try
        {
            for (final String s : dataProvider.apply(ctx, ctx.getSource().asPlayer()))
            {
                if (argument.equals(s))
                {
                    suggestions = Arrays.asList(s);
                    break;
                }
                if (argument.equals(s.substring(0, suggestionLength)))
                {
                    suggestions.add(s);
                }
            }
        }
        catch (final CommandSyntaxException | NullPointerException e)
        {
            return Suggestions.empty();
        }

        if (suggestions.isEmpty())
        {
            return Suggestions.empty();
        }
        return ISuggestionProvider.suggest(suggestions, builder);
    }

    /**
     * Checks if char is non-space ascii char.
     */
    private static boolean isValidCharacter(final char charIn)
    {
        return charIn >= '!' && charIn <= '~';
    }
}