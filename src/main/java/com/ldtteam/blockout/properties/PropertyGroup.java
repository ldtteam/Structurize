package com.ldtteam.blockout.properties;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;

/**
 * A property group is a neat way to contain many repeating
 * parameters that are common to each other, for example
 * textures or text
 */
public abstract class PropertyGroup
{
    protected final Minecraft mc = Minecraft.getInstance();
    protected String prefix;

    /** Allow subclasses to have custom impl without params or prefix */
    protected PropertyGroup()
    {
        applyDefaults();
    }

    /**
     * Construct a new property group and apply the provided parameters.
     * @param p the parameters to apply
     */
    public PropertyGroup(PaneParams p, String prefix)
    {
        this.prefix = prefix;

        // Override and apply parameters here
    }

    /**
     * Sets or resets the properties to their default values.
     * Useful especially for subclasses that should have different
     * fallback values.
     */
    public abstract void applyDefaults();

    /**
     * Draws the contents of the property group on the pane
     * @param ms the rendering stack
     * @param pane the pane this is attached to
     * @param mx the mouse x coordinate
     * @param my the mouse y coordinate
     */
    public abstract void draw(MatrixStack ms, Pane pane, double mx, double my);
}
