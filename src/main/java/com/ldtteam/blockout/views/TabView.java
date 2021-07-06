package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.controls.Image;
import com.ldtteam.blockout.controls.Text;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Similar to a switch view, the tab set renders the selected view,
 * allowing for tabs to be placed for user's selection
 */
public class TabView extends View
{
    private static final Random RANDOM = new Random();

    protected boolean vertical;
    protected int spacing;
    protected int overlap;
    protected int offset;
    protected int tabWidth;
    protected int tabHeight;

    protected Tab selectedTab;

    /** The group of tab icons */
    protected int childPadding;
    protected final View         tabs;
    protected ResourceLocation[] tabIcons = new ResourceLocation[0];

    public TabView(PaneParams params)
    {
        super(params);
        vertical = params.getBoolean("vertical", false);
        overlap = params.getInteger("overlap", 0);

        tabWidth = params.getInteger("tabwidth", 100);
        tabHeight = params.getInteger("tabheight", 20);
        offset = params.getInteger("taboffset", 0);

        childPadding = padding;
        padding = 0;

        params.getTexture("tabimage", res -> {
            // Allow numbered images from 1 up to the total specified,
            // so tab..2.png becomes tab1.png and tab2.png, to be options
            // chosen between at random for the background image of this tab button
            Matcher m = Pattern.compile("\\.\\.([0-9]{1,2})(?=\\.png)").matcher(res.getPath());
            if (m.find())
            {
                int cap = Integer.parseInt(m.group(1));
                tabIcons = new ResourceLocation[cap];
                for (int i = 1 ; i <= cap; i++)
                {
                    tabIcons[i-1] = new ResourceLocation(
                      res.getNamespace(),
                      res.getPath().replace(m.group(0), String.valueOf(i)));
                }
            }
            else
            {
                tabIcons = new ResourceLocation[] {res};
            }
        });

        tabs = new View();
        tabs.setID("tabs");
        addChild(0, tabs);
        if (vertical)
        {
            tabs.setPosition(overlap - tabWidth, offset);
            tabs.setSize(tabWidth, height - 2*offset);
        }
        else
        {
            tabs.setPosition(offset, overlap - tabHeight);
            tabs.setSize(width - 2*offset, tabHeight);
        }

        spacing = params.getInteger("spacing", 2);
    }

    /**
     * Selects a tab from its index in the tab button list
     * @param index the index to select
     */
    public void setSelectedTab(int index)
    {
        if (index >= tabs.children.size()) return;
        Pane tab = tabs.children.get(index);
        if (!(tab instanceof ButtonTab)) return;
        selectedTab = ((ButtonTab) tab).toSelect;

        children.stream()
          .filter(child -> child instanceof Tab)
          .forEach(child -> child.setVisible(child.equals(selectedTab)));
    }

    /**
     * Changes the selected tab view to the given one
     * @param tab the tab to switch to
     */
    public void setSelectedTab(Tab tab)
    {
        children.stream()
          .filter(child -> child instanceof Tab)
          .forEach(child -> {
              child.setVisible(child.equals(tab));
              if (child.isVisible()) selectedTab = tab;
          });
    }

    /**
     * Adds the given tab to this tab set
     * @param tab the tab to add
     */
    public void addTab(Tab tab)
    {
        tab.padding = childPadding;
        addChild(tab);
        new ButtonTab(tab);
    }

    /**
     * Adds the given tab to this tab set
     * @param tab the tab to add
     * @param button an existing ButtonTab to link to the given tab
     */
    public void addTab(Tab tab, ButtonTab button)
    {
        tab.padding = childPadding;
        button.toSelect = tab;
        addChild(tab);
    }

    @Override
    public void parseChildren(@NotNull final PaneParams params)
    {
        super.parseChildren(params);

        getChildren().stream()
          .filter(child -> child instanceof Tab)
          .map(child -> (Tab) child)
          .peek(child -> child.padding = childPadding)
          .forEach(ButtonTab::new);

        setSelectedTab(0);
    }

    @Override
    protected boolean childIsVisible(final Pane child)
    {
        // Draw the tabs despite being potentially out of the tab set view
        return child.getID().equals("tabs") || super.childIsVisible(child);
    }

    @Override
    public boolean isPointInPane(final double mx, final double my)
    {
        // Essentially resize the 'bounding box' for events
        return isVisible() && vertical
            ? mx >= x + overlap - tabWidth && mx < (x + width) && my >= y && my < (y + height)
            : mx >= x && mx < (x + width) && my >= y + overlap - tabHeight && my < (y + height);
    }

    /**
     * The pane for the physical representation of the tab itself,
     * i.e. the button you click to change content.
     * Has a background and a potential icon or text otherwise
     */
    public class ButtonTab extends View
    {
        protected Tab   toSelect;
        protected Image background = new Image();
        protected Image icon = new Image();

        public ButtonTab(Tab tab)
        {
            super();
            int index = tabs.children.size();

            tabs.addChild(this);
            toSelect = tab;

            setSize(tabWidth, tabHeight);
            setPosition(
              vertical ? 0 : index*(tabWidth + spacing),
              vertical ? index*(tabHeight + spacing) : 0
            );

            if (tabIcons.length > 0)
            {
                background.setSize(width, height);
                background.setImage(tabIcons[RANDOM.nextInt(tabIcons.length)], 0, 0, this.width, this.height);
                addChild(background);
            }

            if (tab.icon != null)
            {
                Tuple<Integer, Integer> dim = Image.getImageDimensions(tab.icon);
                if (width < dim.getA())
                {
                    int scale = width / dim.getA();
                    icon.setSize(dim.getA() * scale, dim.getB() * scale);
                }
                if (height < icon.getHeight())
                {
                    int scale = height / icon.getHeight();
                    icon.setSize(icon.getWidth() * scale, icon.getHeight() * scale);
                }
                else
                {
                    icon.setSize(dim.getA(), dim.getB());
                }

                icon.setAlignment(Alignment.MIDDLE);
                icon.setPosition(vertical ? -overlap/4 : 0, vertical ? 0 : -overlap/4);
                icon.setImage(tab.icon);
                addChild(icon);

                tab.createTooltip(this);
            }
            else
            {
                Text text = tab.buildLabel();
                text.setAlignment(Alignment.MIDDLE);
                text.setTextAlignment(Alignment.MIDDLE);
                if (vertical)
                {
                    text.setPosition(-overlap/4, 0);
                    text.setSize(width - overlap/2, height);
                }
                else
                {
                    text.setPosition(0, -overlap/4);
                    text.setSize(width, height-overlap/2);
                }
                addChild(text);
            }
        }

        @Override
        public void drawSelf(final MatrixStack ms, final double mx, final double my)
        {
            if (toSelect.equals(selectedTab)) return;
            super.drawSelf(ms, mx, my);
        }

        @Override
        public void drawSelfLast(final MatrixStack ms, final double mx, final double my)
        {
            if (toSelect.equals(selectedTab)) super.drawSelf(ms, mx, my);
            super.drawSelfLast(ms, mx, my);
        }

        @Override
        public boolean click(final double mx, final double my)
        {
            setSelectedTab(toSelect);
            return super.click(mx, my);
        }
    }
}
