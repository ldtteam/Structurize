package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.properties.Parsers;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.IFormattableTextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ToggleButton extends Button
{
    private static final Pattern SHORT_TRANSLATION = Pattern.compile("(\\$[({]\\S+)\\.\\S+([})])\\|(\\$\\.[^$|\\s]+)");

    protected List<String> rawStates;
    protected List<IFormattableTextComponent> states;
    protected IFormattableTextComponent defaultLabel;
    protected int active = 0;

    public ToggleButton(final PaneParams params)
    {
        super(params);
        defaultLabel = text.get();

        String options = params.getString("options", "");

        Matcher m = SHORT_TRANSLATION.matcher(options);
        while (m.find())
        {
            options = options.replace(m.group(3), m.group(1) + m.group(3).substring(1) + m.group(2));
            m = SHORT_TRANSLATION.matcher(options);
        }

        rawStates = Arrays.asList(options.split("\\s*\\|(?!\\|)\\s*"));
        states = rawStates.stream().map(option -> Parsers.TEXT.apply(option)).collect(Collectors.toList());

        if (!states.isEmpty())
        {
            text.set(states.get(active));
        }
    }

    public boolean isActiveState(String state)
    {
        return states.get(active).getString().equals(state)
                 || rawStates.get(active).equals(state)
                 // take off the bracket for a translation string
                 || rawStates.get(active).substring(0, rawStates.get(active).length() - 1).endsWith(state);
    }

    @Override
    public boolean handleClick(final double mx, final double my)
    {
        if (!states.isEmpty())
        {
            active = (active + 1) % states.size();
            text.set(states.get(active));
        }

        return super.handleClick(mx, my);
    }

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        super.drawSelf(ms, mx, my);
    }
}
