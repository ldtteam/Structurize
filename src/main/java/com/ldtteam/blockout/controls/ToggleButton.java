package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.Parsers;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Button pane for conveniently cycling through different states on click
 * with a shorthand to define different options quickly from the parameters.
 */
public class ToggleButton extends Button
{
    private static final Pattern SHORT_TRANSLATION = Pattern.compile("(\\$[({]\\S+)\\.\\S+([})])\\|(\\$\\.[^$|\\s]+)");

    protected List<String> rawStates;
    protected List<IFormattableTextComponent> states;
    protected int active = 0;

    protected Button button;

    public ToggleButton(final PaneParams params)
    {
        super(params);
        button = Button.construct(params);

        setStateList(params.getString("options", ""));
    }

    /**
     * Creates a new toggleable vanilla button
     * @param options the available states as raw text strings
     */
    public ToggleButton(String... options)
    {
        button = new ButtonVanilla();
        setStateList(String.join("|", options));
    }

    /**
     * Creates a new custom image button
     * @param image the image to set as the button's background
     * @param options the available states as raw text strings
     */
    public ToggleButton(ResourceLocation image, String... options)
    {
        button = new ButtonImage();
        ((ButtonImage) button).setImage(image);
        setStateList(String.join("|", options));
    }

    /**
     * Initializes the states and raw states from the given raw text
     * @param options the available options, delimited with a pipe (|) $. will copy the previous translation option
     */
    protected void setStateList(String options)
    {
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
            button.setText(states.get(active));
        }
        else
        {
            button.clearText();
        }
    }

    /**
     * @param raw true if the result should not be localized
     * @return the list of states as strings
     */
    public List<String> getStateStrings(boolean raw)
    {
        return raw ? rawStates : states.stream().map(IFormattableTextComponent::getString).collect(Collectors.toList());
    }

    public List<IFormattableTextComponent> getStates()
    {
        return states;
    }

    /**
     * Reports if the given state pattern is currently the active one
     *
     * @param state the state or raw state to check against
     * @return true if the state is active
     */
    public boolean isActiveState(String state)
    {
        return states.get(active).getString().equals(state)
                 || rawStates.get(active).equals(state)
                 // take off the bracket for a translation string
                 || rawStates.get(active).substring(0, rawStates.get(active).length() - 1).endsWith(state);
    }

    /**
     * Attempts to set the active state displayed on the button via a raw text string
     *
     * @param state the state to set, if it exists as an option
     * @return whether the active state was changed
     */
    public boolean setActiveState(String state)
    {
        int index = -1;

        for (int i = 0; i < rawStates.size(); i++)
        {
            String s = rawStates.get(i);
            if (s.equals(state) || s.substring(0, s.length() - 1).endsWith(state))
            {
                index = i;
                break;
            }
        }

        if (index >= 0)
        {
            active = index;
            button.setText(states.get(active));
            return true;
        }
        else
        {
            button.clearText();
            return false;
        }
    }

    /**
     * Change the underlying button pane
     * @param button the new button pane to render
     */
    public void setButton(Button button)
    {
        this.button = button;
        if (!states.isEmpty())
        {
            button.setText(states.get(active));
        }
        else
        {
            button.clearText();
        }
    }

    public Button getButton()
    {
        return button;
    }

    @Override
    public boolean handleClick(final double mx, final double my)
    {
        if (!states.isEmpty())
        {
            active = (active + 1) % states.size();
            button.setText(states.get(active));
        }

        return super.handleClick(mx, my);
    }

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        button.drawSelf(ms, mx, my);
    }

    @Override
    public void drawSelfLast(final MatrixStack ms, final double mx, final double my)
    {
        button.drawSelfLast(ms, mx, my);
    }
}
