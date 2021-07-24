package com.ldtteam.blockout;

import com.ldtteam.blockout.controls.AbstractTextBuilder.TooltipBuilder;
import com.ldtteam.blockout.views.View;
import com.ldtteam.blockout.views.Window;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A Pane is the root of all UI objects.
 */
public class Pane extends GuiComponent
{
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
    /**
     * Should be only used during drawing methods. Outside drawing scope value may be outdated.
     */
    protected boolean wasCursorInPane = false;
    private List<MutableComponent> toolTipLines = new ArrayList<>();

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
    public Pane(final PaneParams params)
    {
        super();
        id = params.getString("id", id);

        params.getScaledInteger("size", params.getParentWidth(), params.getParentHeight(), a -> {
            width = a.get(0);
            height = a.get(1);
        });

        params.getScaledInteger("pos", params.getParentView().x, params.getParentView().y, a -> {
            x = a.get(0);
            y = a.get(1);
        });

        alignment = params.getEnum("align", Alignment.class, alignment);
        visible = params.getBoolean("visible", visible);
        enabled = params.getBoolean("enabled", enabled);
        onHoverId = params.getString("onHoverId", onHoverId);
        toolTipLines = params.getMultilineText("tooltip", toolTipLines);
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
    public void draw(final PoseStack ms, final double mx, final double my)
    {
        wasCursorInPane = isPointInPane(mx, my);
        handleHover();

        if (visible)
        {
            drawSelf(ms, mx, my);
            if (debugging)
            {
                final int color = wasCursorInPane ? 0xFF00FF00 : 0xFF0000FF;

                Render.drawOutlineRect(ms, x, y, getWidth(), getHeight(), color);

                if (wasCursorInPane && !id.isEmpty())
                {
                    final int stringWidth = mc.font.width(id);
                    mc.font.draw(ms, id, x + getWidth() - stringWidth, y + getHeight() - mc.font.lineHeight, color);
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
    public void drawLast(final PoseStack ms, final double mx, final double my)
    {
        if (visible)
        {
            drawSelfLast(ms, mx, my);
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
    public void drawSelf(final PoseStack ms, final double mx, final double my)
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
    public void drawSelfLast(final PoseStack ms, final double mx, final double my)
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

    /**
     * Was the cursor in pane during draw method?
     *
     * @return true if the cursor was in pane, false otherwise
     */
    public boolean wasCursorInPane()
    {
        return wasCursorInPane;
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
    public final <T extends Pane> T findPaneOfTypeByID(final String idIn, final Class<T> type)
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

    /**
     * Returns the first Pane (depth-first search) of a given type.
     *
     * @param type Class of the desired Pane type.
     * @param <T>  The type of pane returned.
     * @return a Pane of the given type if found, null otherwise.
     */
    public final <T extends Pane> T findFirstPaneByType(final Class<T> type)
    {
        return findPaneByType(type);
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
     * Returns the first Pane of a given type. Performs a depth-first search on the hierarchy of Panes and Views.
     *
     * @param type type of Pane to find.
     * @return a Pane of the given type.
     */
    @Nullable
    public <T extends Pane> T findPaneByType(final Class<T> type)
    {
        return this.getClass().equals(type) ? type.cast(this) : null;
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

        // can't gen tooltip from xml until first window is set
        if (!toolTipLines.isEmpty())
        {
            final TooltipBuilder ttBuilder = PaneBuilders.tooltipBuilder().hoverPane(this);
            toolTipLines.forEach(ttBuilder::appendNL);
            toolTipLines.clear(); // do not regen it when window has changed (unlikely to happen) cuz onHover might have changed
            onHover = ttBuilder.build();
        }

        setHoverPane(onHover); // renew hover mount
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

            // Allow views to expand zero-widths
            setSize(width, height);
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

    protected synchronized void scissorsStart(final PoseStack ms, final int contentWidth, final int contentHeight)
    {
        final int fbWidth = mc.window.getWidth();
        final int fbHeight = mc.window.getHeight();

        final Vector4f start = new Vector4f(x, y, 0.0f, 1.0f);
        final Vector4f end = new Vector4f(x + width, y + height, 0.0f, 1.0f);
        start.transform(ms.last().pose());
        end.transform(ms.last().pose());

        int scissorsXstart = Mth.clamp((int) Math.floor(start.x()), 0, fbWidth);
        int scissorsXend = Mth.clamp((int) Math.floor(end.x()), 0, fbWidth);

        int scissorsYstart = Mth.clamp((int) Math.floor(start.y()), 0, fbHeight);
        int scissorsYend = Mth.clamp((int) Math.floor(end.y()), 0, fbHeight);

        // negate bottom top (opengl things)
        final int temp = scissorsYstart;
        scissorsYstart = fbHeight - scissorsYend;
        scissorsYend = fbHeight - temp;

        if (!scissorsInfoStack.isEmpty())
        {
            final ScissorsInfo parentInfo = scissorsInfoStack.peek();

            scissorsXstart = Math.max(scissorsXstart, parentInfo.xStart);
            scissorsXend = Math.max(scissorsXstart, Math.min(parentInfo.xEnd, scissorsXend));

            scissorsYstart = Math.max(scissorsYstart, parentInfo.yStart);
            scissorsYend = Math.max(scissorsYstart, Math.min(parentInfo.yEnd, scissorsYend));
        }

                final ScissorsInfo info = new ScissorsInfo(scissorsXstart, scissorsXend, scissorsYstart, scissorsYend, window.getScreen().width, window.getScreen().height);
        scissorsInfoStack.push(info);
        window.getScreen().width = contentWidth;
        window.getScreen().height = contentHeight;

        RenderSystem.enableScissor(scissorsXstart, scissorsYstart, scissorsXend - scissorsXstart, scissorsYend - scissorsYstart);
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

    protected synchronized void scissorsEnd(final PoseStack ms)
    {
        final ScissorsInfo popped = scissorsInfoStack.pop();
        if (debugging)
        {
            final int color = 0xffff0000;
            final int w = popped.xEnd - popped.xStart;
            final int h = popped.yEnd - popped.yStart;

            final int yStart = mc.window.getHeight() - popped.yEnd;

            ms.pushPose();
            ms.last().pose().setIdentity();
            Render.drawOutlineRect(ms, popped.xStart, yStart, w, h, color, 2.0f);

            final String scId = "scissor_" + (id.isEmpty() ? this.toString() : id);
            final int scale = (int) mc.window.getGuiScale();
            final int stringWidth = mc.font.width(scId);
            ms.scale(scale, scale, 1.0f);
            mc.font.draw(ms,
                scId,
                (popped.xStart + w) / scale - stringWidth,
                (yStart + h) / scale - mc.font.lineHeight,
                color);
            ms.popPose();
        }

        window.getScreen().width = popped.oldGuiWidth;
        window.getScreen().height = popped.oldGuiHeight;

        if (!scissorsInfoStack.isEmpty())
        {
            final ScissorsInfo info = scissorsInfoStack.peek();
            RenderSystem.enableScissor(info.xStart, info.yStart, info.xEnd - info.xStart, info.yEnd - info.yStart);
        }
        else
        {
            RenderSystem.disableScissor();
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
        // Can be overwritten by child classes
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
        private final int xStart;
        private final int yStart;
        private final int xEnd;
        private final int yEnd;
        private final int oldGuiWidth;
        private final int oldGuiHeight;

        ScissorsInfo(final int xStart, final int xEnd, final int yStart, final int yEnd, final int oldGuiWidth, final int oldGuiHeight)
        {
            this.xStart = xStart;
            this.xEnd = xEnd;
            this.yStart = yStart;
            this.yEnd = yEnd;
            this.oldGuiWidth = oldGuiWidth;
            this.oldGuiHeight = oldGuiHeight;
        }
    }

    /**
     * Handle onHover element, element must be visible.
     */
    protected void handleHover()
    {
        if (onHover == null && !onHoverId.isEmpty())
        {
            onHover = window.findPaneByID(onHoverId); // do not use setHoverPane, here onHover is defined in xml
            Objects.requireNonNull(onHover, String.format("Hover pane \"%s\" for \"%s\" was not found.", onHoverId, id));
        }

        if (onHover == null)
        {
            return;
        }

        if (this.wasCursorInPane && !onHover.isVisible() && onHover.isEnabled())
        {
            onHover.show();
        }
        // if onHover was already drawn then we good
        // else we have to wait for next frame
        else if (!onHover.wasCursorInPane && !this.wasCursorInPane && onHover.isVisible())
        {
            onHover.hide();
        }
    }

    /**
     * Overrides current hover pane with given pane.
     * Old hover is removed from window of this pane.
     * New hover is added (as last child) to window of this pane.
     *
     * @param hoverPane new hover pane
     * @return old hover pane
     */
    public Pane setHoverPane(final Pane hoverPane)
    {
        if (onHover != null)
        {
            // gc
            onHover.putInside(null);
        }

        final Pane oldHover = onHover;
        this.onHover = hoverPane;

        if (onHover != null)
        {
            onHover.putInside(window);
        }

        return oldHover;
    }

    public Pane getHoverPane()
    {
        return onHover;
    }

    @Deprecated
    protected int drawString(final PoseStack ms, final String text, final float x, final float y, final int color, final boolean shadow)
    {
        if (shadow)
        {
            return mc.font.drawShadow(ms, text, x, y, color);
        }
        else
        {
            return mc.font.draw(ms, text, x, y, color);
        }
    }

    protected int drawString(final PoseStack ms, final Component text, final float x, final float y, final int color, final boolean shadow)
    {
        if (shadow)
        {
            return mc.font.drawShadow(ms, text, x, y, color);
        }
        else
        {
            return mc.font.draw(ms, text, x, y, color);
        }
    }

    protected int drawString(final PoseStack ms, final FormattedCharSequence text, final float x, final float y, final int color, final boolean shadow)
    {
        if (shadow)
        {
            return mc.font.drawShadow(ms, text, x, y, color);
        }
        else
        {
            return mc.font.draw(ms, text, x, y, color);
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
     * Draws texture without scaling so one texel is one pixel, using repeatable texture center.
     *
     * @param ms            MatrixStack
     * @param x             start target coords [pixels]
     * @param y             start target coords [pixels]
     * @param width         target rendering box [pixels]
     * @param height        target rendering box [pixels]
     * @param u             texture start offset [texels]
     * @param v             texture start offset [texels]
     * @param uWidth        texture rendering box [texels]
     * @param vHeight       texture rendering box [texels]
     * @param textureWidth  texture file size [texels]
     * @param textureHeight texture file size [texels]
     * @param uRepeat       offset relative to u, v [texels], smaller than uWidth
     * @param vRepeat       offset relative to u, v [texels], smaller than vHeight
     * @param repeatWidth   size of repeatable box in texture [texels], smaller than or equal uWidth - uRepeat
     * @param repeatHeight  size of repeatable box in texture [texels], smaller than or equal vHeight - vRepeat
     */
    protected static void blitRepeatable(final PoseStack ms,
        final int x, final int y,
        final int width, final int height,
        final int u, final int v,
        final int uWidth, final int vHeight,
        final int textureWidth, final int textureHeight,
        final int uRepeat, final int vRepeat,
        final int repeatWidth, final int repeatHeight)
    {
        if (uRepeat < 0 || vRepeat < 0 || uRepeat >= uWidth || vRepeat >= vHeight || repeatWidth < 1 || repeatHeight < 1
            || repeatWidth > uWidth - uRepeat || repeatHeight > vHeight - vRepeat)
        {
            throw new IllegalArgumentException("Repeatable box is outside of texture box");
        }

        final int repeatCountX = Math.max(1, Math.max(0, width - (uWidth - repeatWidth)) / repeatWidth);
        final int repeatCountY = Math.max(1, Math.max(0, height - (vHeight - repeatHeight)) / repeatHeight);

        final Matrix4f mat = ms.last().pose();
        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // main
        for (int i = 0; i < repeatCountX; i++)
        {
            final int uAdjust = i == 0 ? 0 : uRepeat;
            final int xStart = x + uAdjust + i * repeatWidth;
            final int w = Math.min(repeatWidth + uRepeat - uAdjust, width - (uWidth - uRepeat - repeatWidth));
            final float minU = (float) (u + uAdjust) / textureWidth;
            final float maxU = (float) (u + uAdjust + w) / textureWidth;

            for (int j = 0; j < repeatCountY; j++)
            {
                final int vAdjust = j == 0 ? 0 : vRepeat;
                final int yStart = y + vAdjust + j * repeatHeight;
                final int h = Math.min(repeatHeight + vRepeat - vAdjust, height - (vHeight - vRepeat - repeatHeight));
                final float minV = (float) (v + vAdjust) / textureHeight;
                final float maxV = (float) (v + vAdjust + h) / textureHeight;

                buffer.vertex(mat, xStart, yStart + h, 0).uv(minU, maxV).endVertex();
                buffer.vertex(mat, xStart + w, yStart + h, 0).uv(maxU, maxV).endVertex();
                buffer.vertex(mat, xStart + w, yStart, 0).uv(maxU, minV).endVertex();
                buffer.vertex(mat, xStart, yStart, 0).uv(minU, minV).endVertex();
            }
        }

        final int xEnd = x + Math.min(uRepeat + repeatCountX * repeatWidth, width - (uWidth - uRepeat - repeatWidth));
        final int yEnd = y + Math.min(vRepeat + repeatCountY * repeatHeight, height - (vHeight - vRepeat - repeatHeight));
        final int uLeft = width - (xEnd - x);
        final int vLeft = height - (yEnd - y);
        final float restMinU = (float) (u + uWidth - uLeft) / textureWidth;
        final float restMaxU = (float) (u + uWidth) / textureWidth;
        final float restMinV = (float) (v + vHeight - vLeft) / textureHeight;
        final float restMaxV = (float) (v + vHeight) / textureHeight;

        // bot border
        for (int i = 0; i < repeatCountX; i++)
        {
            final int uAdjust = i == 0 ? 0 : uRepeat;
            final int xStart = x + uAdjust + i * repeatWidth;
            final int w = Math.min(repeatWidth + uRepeat - uAdjust, width - uLeft);
            final float minU = (float) (u + uAdjust) / textureWidth;
            final float maxU = (float) (u + uAdjust + w) / textureWidth;

            buffer.vertex(mat, xStart, yEnd + vLeft, 0).uv(minU, restMaxV).endVertex();
            buffer.vertex(mat, xStart + w, yEnd + vLeft, 0).uv(maxU, restMaxV).endVertex();
            buffer.vertex(mat, xStart + w, yEnd, 0).uv(maxU, restMinV).endVertex();
            buffer.vertex(mat, xStart, yEnd, 0).uv(minU, restMinV).endVertex();
        }

        // left border
        for (int j = 0; j < repeatCountY; j++)
        {
            final int vAdjust = j == 0 ? 0 : vRepeat;
            final int yStart = y + vAdjust + j * repeatHeight;
            final int h = Math.min(repeatHeight + vRepeat - vAdjust, height - vLeft);
            float minV = (float) (v + vAdjust) / textureHeight;
            float maxV = (float) (v + vAdjust + h) / textureHeight;

            buffer.vertex(mat, xEnd, yStart + h, 0).uv(restMinU, maxV).endVertex();
            buffer.vertex(mat, xEnd + uLeft, yStart + h, 0).uv(restMaxU, maxV).endVertex();
            buffer.vertex(mat, xEnd + uLeft, yStart, 0).uv(restMaxU, minV).endVertex();
            buffer.vertex(mat, xEnd, yStart, 0).uv(restMinU, minV).endVertex();
        }

        // bot left corner
        buffer.vertex(mat, xEnd, yEnd + vLeft, 0).uv(restMinU, restMaxV).endVertex();
        buffer.vertex(mat, xEnd + uLeft, yEnd + vLeft, 0).uv(restMaxU, restMaxV).endVertex();
        buffer.vertex(mat, xEnd + uLeft, yEnd, 0).uv(restMaxU, restMinV).endVertex();
        buffer.vertex(mat, xEnd, yEnd, 0).uv(restMinU, restMinV).endVertex();

        buffer.end();
        BufferUploader.end(buffer);
    }
}
