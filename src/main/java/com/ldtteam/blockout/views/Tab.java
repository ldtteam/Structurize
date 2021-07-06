package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.Parsers;
import com.ldtteam.blockout.controls.Text;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * A form of view to be used only in a {@link TabView}
 * and typically associated with a {@link TabView.ButtonTab} used to select it.
 */
public class Tab extends View
{
    /** The icon to be shown on any corresponding tab button */
    protected ResourceLocation icon;

    /** The text to use as a tooltip, or as the label itself when icon is null */
    protected IFormattableTextComponent label;

    /**
     * Constructs a new tab from the given information.
     * NOTE: this will not add it to a tab set, for that use {@link TabView#addTab(Tab)}
     * @param title the label for the tooltip
     * @param icon a resource string to the icon relative to the gui textures folder
     */
    public Tab(String title, String icon)
    {
        this.label = new StringTextComponent(title);
        this.icon = Parsers.RESOURCE("textures/gui/", "png").apply(icon);
    }

    public Tab(PaneParams params)
    {
        super(params);

        setPosition(0, 0);
        setSize(params.getParentWidth(), params.getParentHeight());

        this.icon = params.getTexture("icon", null);
        this.label = params.getTextComponent("title", null);
    }

    /**
     * Constructs a tooltip for this tab view and applies it to a pane
     * @param hoverPane the pane to show the tooltip on hover
     */
    public void createTooltip(Pane hoverPane)
    {
        if (label != null)
        {
            PaneBuilders.tooltipBuilder().append(label).hoverPane(hoverPane).build();
        }
    }

    /**
     * Constructs a text item for this tab view
     * @return the text pane
     */
    public Text buildLabel()
    {
        ITextComponent component = label == null ? StringTextComponent.EMPTY : label;
        return PaneBuilders.textBuilder().append(component).build();
    }
}
