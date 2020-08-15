package com.ldtteam.blockout;

import com.ldtteam.blockout.views.View;
import com.ldtteam.blockout.views.Window;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;

import net.minecraft.util.text.LanguageMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A Pane is the root of all UI objects.
 */
public class Pane extends AbstractGui
{
    @NotNull
    private static final Deque<ScissorsInfo> scissorsInfoStack = new ConcurrentLinkedDeque<>();
    protected static Pane lastClickedPane;
    protected static Pane focus;
    protected Pane onHover;
    protected static boolean debugging = false;
    protected Minecraft mc = Minecraft.getInstance();
    // Attributes
    protected String id = "";
    protected int x = 0;
    protected int y = 0;
    protected int width = 0;
    protected int height = 0;
    protected Alignment alignment = Alignment.TOP_LEFT;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected String onHoverId = "";
    // Runtime
    protected Window window;
    protected View parent;
    protected boolean            isHovered    = false;
    private List<ITextProperties> toolTipLines = new ArrayList<>();

    /**
     * Default constructor.
     */
    public Pane()
    {
        super();
        // Required for panes.
    }

    /**
     * Constructs a Pane from PaneParams.
     *
     * @param params Params for the Pane.
     */
    public Pane(@NotNull final PaneParams params)
    {
        super();
        id = params.getStringAttribute("id", id);

        @NotNull
        final PaneParams.SizePair parentSizePair = new PaneParams.SizePair(params.getParentWidth(), params.getParentHeight());
        PaneParams.SizePair sizePair = params.getSizePairAttribute("size", null, parentSizePair);
        if (sizePair != null)
        {
            width = sizePair.getX();
            height = sizePair.getY();
        }
        else
        {
            width = params.getScalableIntegerAttribute("width", width, parentSizePair.getX());
            height = params.getScalableIntegerAttribute("height", height, parentSizePair.getY());
        }

        sizePair = params.getSizePairAttribute("pos", null, parentSizePair);
        if (sizePair != null)
        {
            x = sizePair.getX();
            y = sizePair.getY();
        }
        else
        {
            x = params.getScalableIntegerAttribute("x", x, parentSizePair.getX());
            y = params.getScalableIntegerAttribute("y", y, parentSizePair.getY());
        }

        alignment = params.getEnumAttribute("align", Alignment.class, alignment);
        visible = params.getBooleanAttribute("visible", visible);
        enabled = params.getBooleanAttribute("enabled", enabled);
        onHoverId = params.getStringAttribute("onHoverId");
        toolTipLines = params.getMultiLineAttributeAsTextComp("tooltip");
    }

    /**
     * Returns the currently focused Pane.
     *
     * @return the currently focused Pane.
     */
    public static synchronized Pane getFocus()
    {
        return focus;
    }

    /**
     * Clear the currently focused Pane.
     */
    public static void clearFocus()
    {
        setFocus(null);
    }

    /**
     * Override to respond to the Pane losing focus.
     */
    public void onFocusLost()
    {
        // Can be overloaded
    }

    /**
     * Override to respond to the Pane becoming the current focus.
     */
    public void onFocus()
    {
        // Can be overloaded
    }

    /**
     * Parse the children of the pane.
     *
     * @param params the parameter.
     */
    public void parseChildren(final PaneParams params)
    {
        // Can be overloaded
    }

    // ID

    public final String getID()
    {
        return id;
    }

    public final void setID(final String id)
    {
        this.id = id;
    }

    /**
     * Set the size of a pane.
     *
     * @param w the width.
     * @param h the height.
     */
    public void setSize(final int w, final int h)
    {
        width = w;
        height = h;
    }

    /**
     * Set the position of the pane.
     *
     * @param newX the new x.
     * @param newY the new y.
     */
    public void setPosition(final int newX, final int newY)
    {
        x = newX;
        y = newY;
    }

    /**
     * Move the pane by x and y to a place.
     *
     * @param dx the x.
     * @param dy the y.
     */
    public void moveBy(final int dx, final int dy)
    {
        x += dx;
        y += dy;
    }

    public Alignment getAlignment()
    {
        return alignment;
    }

    public void setAlignment(final Alignment alignment)
    {
        this.alignment = alignment;
    }

    // Visibility

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(final boolean v)
    {
        visible = v;
    }

    /**
     * Show this pane.
     */
    public void show()
    {
        setVisible(true);
    }

    /**
     * Hide this pane.
     */
    public void hide()
    {
        setVisible(false);
    }

    // Enabling

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(final boolean e)
    {
        enabled = e;
    }

    /**
     * Enable this pane.
     */
    public void enable()
    {
        setEnabled(true);
    }

    /**
     * Disable this pane.
     */
    public void disable()
    {
        setEnabled(false);
    }

    /**
     * Enable and show this pane.
     */
    public void on()
    {
        setEnabled(true);
        setVisible(true);
    }

    /**
     * Disable and hide this pane.
     */
    public void off()
    {
        setEnabled(false);
        setVisible(false);
    }

    /**
     * Set Focus to this Pane.
     */
    public final void setFocus()
    {
        setFocus(this);
    }

    /**
     * Return <tt>true</tt> if this Pane is the current focus.
     *
     * @return <tt>true</tt> if this Pane is the current focus.
     */
    public final synchronized boolean isFocus()
    {
        return focus == this;
    }

    /**
     * Set the currently focused Pane.
     *
     * @param f Pane to focus, or nil.
     */
    public static synchronized void setFocus(final Pane f)
    {
        if (focus != null)
        {
            focus.onFocusLost();
        }

        focus = f;

        if (focus != null)
        {
            focus.onFocus();
        }
    }

    /**
     * Draw the current Pane if visible.
     *
     * @param mx mouse x.
     * @param my mouse y.
     */
    public final void draw(final MatrixStack ms, final int mx, final int my)
    {
        if (visible)
        {
            drawSelf(ms, mx, my);
            if (debugging)
            {
                final boolean isMouseOver = isPointInPane(mx, my);
                final int color = isMouseOver ? 0xFF00FF00 : 0xFF0000FF;

                Render.drawOutlineRect(ms, x, y, getWidth(), getHeight(), color);

                if (isMouseOver && !id.isEmpty())
                {
                    final int stringWidth = mc.fontRenderer.getStringWidth(id);
                    mc.fontRenderer.drawString(ms, id, x + getWidth() - stringWidth, y + getHeight() - mc.fontRenderer.FONT_HEIGHT, color);
                }
            }
        }
    }

    /**
     * Draw something after finishing drawing the GUI.
     *
     * @param mx mouse x.
     * @param my mouse y.
     */
    public final void drawLast(final MatrixStack ms, final int mx, final int my)
    {
        if (visible)
        {
            drawSelfLast(ms, mx, my);

            if (isHovered && !toolTipLines.isEmpty())
            {
                window.getScreen().renderTooltip(ms, LanguageMap.getInstance().func_244260_a(toolTipLines), mx, my);
            }
        }
    }

    /**
     * Draw self. The graphics port is already relative to the appropriate location.
     * <p>
     * Override this to actually draw.
     *
     * @param mx Mouse x (relative to parent).
     * @param my Mouse y (relative to parent).
     */
    public void drawSelf(final MatrixStack ms, final int mx, final int my)
    {
        // Can be overloaded
    }

    /**
     * Draw self last. The graphics port is already relative to the appropriate location.
     * <p>
     * Override this to actually draw last.
     *
     * @param mx Mouse x (relative to parent).
     * @param my Mouse y (relative to parent).
     */
    public void drawSelfLast(final MatrixStack ms, final int mx, final int my)
    {
        // Can be overloaded
    }

    /**
     * Is a point relative to the parent's origin within the pane?
     *
     * @param mx point x.
     * @param my point y.
     * @return true if the point is in the pane.
     */
    public boolean isPointInPane(final double mx, final double my)
    {
        return isVisible() && mx >= x && mx < (x + width) && my >= y && my < (y + height);
    }

    // Dimensions
    public int getWidth()
    {
        return width;
    }

    // Drawing

    public int getHeight()
    {
        return height;
    }

    /**
     * Returns the first Pane (depth-first search) of a given ID. if it matches the specified type. Performs a depth-first search on the hierarchy of Panes and Views.
     *
     * @param idIn ID of Pane to find.
     * @param type Class of the desired Pane type.
     * @param <T>  The type of pane returned.
     * @return a Pane of the given ID, if it matches the specified type.
     */
    public final <T extends Pane> T findPaneOfTypeByID(final String idIn, @NotNull final Class<T> type)
    {
        @Nullable
        final Pane p = findPaneByID(idIn);
        try
        {
            return type.cast(p);
        }
        catch (final ClassCastException e)
        {
            throw new IllegalArgumentException(String.format("No pane with id %s and type %s was found.", idIn, type), e);
        }
    }

    // ----------Subpanes-------------//

    /**
     * Returns the first Pane of a given ID. Performs a depth-first search on the hierarchy of Panes and Views.
     *
     * @param idIn ID of Pane to find.
     * @return a Pane of the given ID.
     */
    @Nullable
    public Pane findPaneByID(final String idIn)
    {
        return id.equals(idIn) ? this : null;
    }

    /**
     * Return the Pane that contains this one.
     *
     * @return the Pane that contains this one
     */
    public final View getParent()
    {
        return parent;
    }

    /**
     * Return the Window that this Pane ultimately belongs to.
     *
     * @return the Window that this Pane belongs to.
     */
    public final Window getWindow()
    {
        return window;
    }

    public void setWindow(final Window w)
    {
        window = w;
    }

    /**
     * Put this Pane inside a View. Only Views and subclasses can contain Panes.
     *
     * @param newParent the View to put this Pane into, or null to remove from Parents.
     */
    public void putInside(final View newParent)
    {
        if (parent != null)
        {
            parent.removeChild(this);
        }

        parent = newParent;

        if (parent != null)
        {
            parent.addChild(this);
        }
    }

    public boolean isClickable()
    {
        return visible && enabled;
    }

    // ----------Mouse-------------//

    /**
     * Process a mouse down on the Pane.
     * <p>
     * It is advised that only containers of other panes override this method.
     *
     * @param mx mouse X coordinate, relative to parent's top-left.
     * @param my mouse Y coordinate, relative to parent's top-left.
     * @return true if event was used or propagation needs to be stopped
     */
    public boolean click(final double mx, final double my)
    {
        setLastClickedPane(this);
        return handleClick(mx - x, my - y);
    }

    /**
     * Process a rightclick mouse down on the Pane.
     * <p>
     * It is advised that only containers of other panes override this method.
     *
     * @param mx mouse X coordinate, relative to parent's top-left.
     * @param my mouse Y coordinate, relative to parent's top-left.
     * @return true if event was used or propagation needs to be stopped
     */
    public boolean rightClick(final double mx, final double my)
    {
        setLastClickedPane(this);
        return handleRightClick(mx - x, my - y);
    }

    /**
     * Set a pane as the last clicked pane.
     *
     * @param pane pane to set.
     */
    private static synchronized void setLastClickedPane(final Pane pane)
    {
        lastClickedPane = pane;
    }

    /**
     * Process a click on the Pane.
     * <p>
     * Override this to process the actual click.
     *
     * @param mx mouse X coordinate, relative to Pane's top-left.
     * @param my mouse Y coordinate, relative to Pane's top-left.
     * @return true if event was used or propagation needs to be stopped
     */
    public boolean handleClick(final double mx, final double my)
    {
        // Can be overloaded
        return false;
    }

    /**
     * Process a right click on the Pane.
     * <p>
     * Override this to process the actual click.
     *
     * @param mx mouse X coordinate, relative to Pane's top-left.
     * @param my mouse Y coordinate, relative to Pane's top-left.
     * @return true if event was used or propagation needs to be stopped
     */
    public boolean handleRightClick(final double mx, final double my)
    {
        // Can be overloaded
        return false;
    }

    /**
     * Check if a pane can handle clicks.
     *
     * @param mx int x position.
     * @param my int y position.
     * @return true if so.
     */
    public boolean canHandleClick(final double mx, final double my)
    {
        return visible && enabled && isPointInPane(mx, my);
    }

    /**
     * Called when a key is pressed.
     *
     * @param ch  the character
     * @param key the key
     * @return true if event was used or propagation needs to be stopped
     */
    public boolean onKeyTyped(final char ch, final int key)
    {
        return false;
    }

    /**
     * On update. Can be overloaded.
     */
    public void onUpdate()
    {
        // Can be overloaded
    }

    protected synchronized void scissorsStart(final MatrixStack ms)
    {
        int scissorsX = MatrixUtils.getLastMatrixTranslateXasInt(ms) + getX();
        int scissorsY = MatrixUtils.getLastMatrixTranslateYasInt(ms) + getY();
        int h = getHeight();
        int w = getWidth();

        if (!scissorsInfoStack.isEmpty())
        {
            final ScissorsInfo parentInfo = scissorsInfoStack.peek();
            final int right = scissorsX + w;
            final int bottom = scissorsY + h;
            final int parentRight = parentInfo.x + parentInfo.width;
            final int parentBottom = parentInfo.y + parentInfo.height;

            scissorsX = Math.max(scissorsX, parentInfo.x);
            scissorsY = Math.max(scissorsY, parentInfo.y);

            w = Math.max(0, Math.min(right, parentRight) - scissorsX);
            h = Math.max(0, Math.min(bottom, parentBottom) - scissorsY);
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        @NotNull
        final ScissorsInfo info = new ScissorsInfo(scissorsX, scissorsY, w, h);
        scissorsInfoStack.push(info);

        final double scale = BOScreen.getScale();
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glScissor(
            (int) (info.x * scale),
            (int) ((mc.mainWindow.getScaledHeight() - info.y - info.height) * scale),
            (int) (info.width * scale),
            (int) (info.height * scale));
    }

    /**
     * X position.
     *
     * @return the int x.
     */
    public int getX()
    {
        return x;
    }

    /**
     * Y position.
     *
     * @return the int y.
     */
    public int getY()
    {
        return y;
    }

    protected synchronized void scissorsEnd()
    {
        scissorsInfoStack.pop();

        GL11.glPopAttrib();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (!scissorsInfoStack.isEmpty())
        {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

            final ScissorsInfo info = scissorsInfoStack.peek();
            final double scale = BOScreen.getScale();
            GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
            GL11.glScissor(
              (int) (info.x * scale),
              (int) ((mc.mainWindow.getScaledHeight() - info.y - info.height) * scale),
              (int) (info.width * scale),
              (int) (info.height * scale));
        }
    }

    /**
     * Wheel input.
     *
     * @param wheel minus for down, plus for up.
     * @param mx    mouse x
     * @param my    mouse y
     * @return true if event was used or propagation needs to be stopped
     */
    public boolean scrollInput(final double wheel, final double mx, final double my)
    {
        /**
         * Can be overwritten by child classes
         */
        return false;
    }

    /**
     * Set the parent of the child.
     *
     * @param view the parent view.
     */
    public void setParentView(final View view)
    {
        this.parent = view;
    }

    private static class ScissorsInfo
    {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        ScissorsInfo(final int x, final int y, final int w, final int h)
        {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }
    }

    /**
     * Handle unhover.
     *
     * @param mx ignored.
     * @param mz ignored.
     * @return ignored.
     */
    public boolean handleUnhover(final double mx, final double mz)
    {
        handleUnhover();
        return true;
    }

    /**
     * Handle unhover.
     */
    public void handleUnhover()
    {
        isHovered = false;

        if (onHover != null)
        {
            onHover.hide();
        }
    }

    /**
     * Handle onHover element, element must be visible. TODO: bug: must have pos set from xml (or be not in a group)
     *
     * @param mx mouse x
     * @param my mouse y
     * @return true if event was used or propagation needs to be stopped
     */
    public boolean handleHover(final double mx, final double my)
    {
        if (this.isPointInPane(mx, my))
        {
            isHovered = true;
        }
        if (onHover == null)
        {
            if (!onHoverId.isEmpty())
            {
                onHover = window.findPaneByID(onHoverId);
            }
            else
            {
                return false;
            }
        }
        if (!this.isVisible())
        {
            if (onHover.isVisible())
            {
                onHover.hide();
            }
            return false;
        }
        if (this.isPointInPane(mx, my) && !onHover.isVisible())
        {
            onHover.show();
        }
        return true;
    }

    @Deprecated
    protected int drawString(final MatrixStack ms, final String text, final float x, final float y, final int color, final boolean shadow)
    {
        if (shadow)
        {
            return mc.fontRenderer.drawStringWithShadow(ms, text, x, y, color);
        }
        else
        {
            return mc.fontRenderer.drawString(ms, text, x, y, color);
        }
    }

    protected int drawString(final MatrixStack ms, final ITextProperties text, final float x, final float y, final int color, final boolean shadow)
    {
        if (shadow)
        {
            return mc.fontRenderer.func_238407_a_(ms, LanguageMap.getInstance().func_241870_a(text), x, y, color);
        }
        else
        {
            return mc.fontRenderer.func_238422_b_(ms, LanguageMap.getInstance().func_241870_a(text), x, y, color);
        }
    }

    protected int drawString(final MatrixStack ms, final IReorderingProcessor text, final float x, final float y, final int color, final boolean shadow)
    {
        if (shadow)
        {
            return mc.fontRenderer.func_238407_a_(ms, text, x, y, color);
        }
        else
        {
            return mc.fontRenderer.func_238422_b_(ms, text, x, y, color);
        }
    }

    /**
     * Mouse drag.
     *
     * @param mx     mouse start x
     * @param my     mouse start y
     * @param speed  drag speed
     * @param deltaX relative x
     * @param deltaY relative y
     * @return true if event was used or propagation needs to be stopped
     */
    public boolean onMouseDrag(final double mx, final double my, final int speed, final double deltaX, final double deltaY)
    {
        return false;
    }

    /**
     * Sets the tooltip to render on hovering this element
     *
     * @param lines the lines to display
     */
    public void setHoverToolTip(final List<ITextProperties> lines)
    {
        this.toolTipLines = lines;
    }

    /**
     * Gets the tooltip to render on hovering this element
     *
     * @return the lines to display
     */
    public List<ITextProperties> getHoverToolTip()
    {
        return this.toolTipLines;
    }
}
