package com.ldtteam.structurize.util;

import com.ibm.icu.text.MessageFormat;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.FormattedText.ContentConsumer;
import net.minecraft.network.chat.FormattedText.StyledContentConsumer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Inspired by {@link TranslatableContents} to support things from {@link MessageFormat} (plurals, pronouns, etc.)
 */
public class IcuTranslatableContents implements ComponentContents
{
    private final String key;
    private final Object[] args;

    @Nullable
    private Language lastLanguage = null;
    private FormattedText translated;

    public IcuTranslatableContents(final String key, final Object[] args)
    {
        this.key = key;
        this.args = args;
    }

    public String getKey()
    {
        return key;
    }

    public Object[] getArgs()
    {
        return args;
    }

    /**
     * Performs (if outdated) and cache translated result
     */
    private void translate()
    {
        final Language curLanguage = Language.getInstance();
        if (curLanguage != lastLanguage)
        {
            lastLanguage = curLanguage;

            final String translatedKey = LanguageHandler.translateKey(key);
            if (translatedKey != key) // default quick check
            {
                try
                {
                    translated = FormattedText.of(MessageFormat.format(translatedKey, args));
                    return; // success
                }
                catch (final IllegalArgumentException e)
                {}
            }

            // failed, use key
            translated = FormattedText.of(key);
        }
    }

    @Override
    public <T> Optional<T> visit(final ContentConsumer<T> sink)
    {
        translate();
        return translated.visit(sink);
    }

    @Override
    public <T> Optional<T> visit(final StyledContentConsumer<T> sink, final Style style)
    {
        translate();
        return translated.visit(sink, style);
    }

    /**
     * INLINE:
     * 
     * @see TranslatableContents#resolve(CommandSourceStack, Entity, int)
     */
    @Override
    public MutableComponent resolve(@Nullable final CommandSourceStack sourceStack,
        @Nullable final Entity entity,
        final int recursionLevel) throws CommandSyntaxException
    {
        final Object[] newArgs = new Object[this.args.length];

        for (int i = 0; i < newArgs.length; ++i)
        {
            if (args[i] instanceof final Component component)
            {
                newArgs[i] = ComponentUtils.updateForEntity(sourceStack, component, entity, recursionLevel);
            }
            else
            {
                newArgs[i] = args[i];
            }
        }

        return MutableComponent.create(new IcuTranslatableContents(this.key, newArgs));
    }

    @Override
    public String toString()
    {
        return "IndexTranslate[key=" + key + ", args=" + Arrays.toString(args) + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(key);
        result = prime * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof final IcuTranslatableContents other))
        {
            return false;
        }
        return Objects.equals(key, other.key) && Arrays.equals(args, other.args);
    }

    public static class IcuComponent
    {
        public static MutableComponent of(final String key, final Object... args)
        {
            return MutableComponent.create(new IcuTranslatableContents(key, args));
        }
    }
}
