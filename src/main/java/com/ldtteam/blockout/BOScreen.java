package com.ldtteam.blockout;

import com.ldtteam.blockout.views.Window;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BitArray;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/**
 * Wraps MineCrafts GuiScreen for BlockOut's Window.
 */
public class BOScreen extends Screen
{
    protected double renderScale = 1.0d;
    protected double mcScale = 1.0d;
    protected Window window;
    protected double x = 0;
    protected double y = 0;
    public static boolean isMouseLeftDown = false;
    private boolean isOpen = false;
    private static final BitArray ACCEPTED_KEY_PRESSED_MAP = new BitArray(1, GLFW.GLFW_KEY_LAST + 1);

    static
    {
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_A, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_C, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_V, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_X, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_ESCAPE, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_ENTER, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_TAB, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_BACKSPACE, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_INSERT, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_DELETE, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_RIGHT, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_LEFT, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_DOWN, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_UP, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_PAGE_UP, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_PAGE_DOWN, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_HOME, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_END, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_CAPS_LOCK, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_SCROLL_LOCK, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_NUM_LOCK, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_PRINT_SCREEN, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_PAUSE, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F1, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F2, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F3, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F4, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F5, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F6, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F7, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F8, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F9, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F10, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F11, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F12, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F13, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F14, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F15, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F16, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F17, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F18, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F19, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F20, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F21, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F22, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F23, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F24, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_F25, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_0, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_1, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_2, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_3, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_4, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_5, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_6, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_7, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_8, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_9, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_DECIMAL, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_DIVIDE, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_MULTIPLY, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_SUBTRACT, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_ADD, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_ENTER, 1);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_KP_EQUAL, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_LEFT_SHIFT, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_LEFT_CONTROL, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_LEFT_ALT, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_LEFT_SUPER, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_RIGHT_SHIFT, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_RIGHT_CONTROL, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_RIGHT_ALT, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_RIGHT_SUPER, 0);
        ACCEPTED_KEY_PRESSED_MAP.setAt(GLFW.GLFW_KEY_MENU, 1);
    }

    /**
     * Create a GuiScreen from a BlockOut window.
     *
     * @param w blockout window.
     */
    public BOScreen(final Window w)
    {
        super(new StringTextComponent("Blockout GUI"));
        window = w;
    }

    @Override
    public void render(final MatrixStack ms, final int mx, final int my, final float f)
    {
        if (minecraft == null) // should never happen though
        {
            return;
        }

        final double fbWidth = minecraft.mainWindow.getFramebufferWidth();
        final double fbHeight = minecraft.mainWindow.getFramebufferHeight();
        final double guiWidth = Math.max(fbWidth, 320.0d);
        final double guiHeight = Math.max(fbHeight, 240.0d);

        final float renderZlevel = MatrixUtils.getLastMatrixTranslateZ(ms);
        final float oldZ = minecraft.getItemRenderer().zLevel;
        minecraft.getItemRenderer().zLevel = renderZlevel;

        mcScale = minecraft.mainWindow.getGuiScaleFactor();
        renderScale = window.getRenderType().calcRenderScale(minecraft.mainWindow, window);

        if (window.hasLightbox())
        {
            width = (int) fbWidth;
            height = (int) fbHeight;
            super.renderBackground(ms);
        }

        width = window.getWidth();
        height = window.getHeight();
        x = Math.floor((guiWidth - width * renderScale) / 2.0d);
        y = Math.floor((guiHeight - height * renderScale) / 2.0d);

        // replace vanilla projection
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, fbWidth, fbHeight, 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);

        ms.push();
        ms.getLast().getMatrix().setIdentity();
        ms.translate(x, y, renderZlevel);
        ms.scale((float) renderScale, (float) renderScale, 1.0f);
        window.draw(ms, calcRelativeX(mx), calcRelativeY(my));
        window.drawLast(ms, calcRelativeX(mx), calcRelativeY(my));
        ms.pop();

        // restore vanilla state
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, fbWidth / mcScale, fbHeight / mcScale, 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);

        minecraft.getItemRenderer().zLevel = oldZ;
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers)
    {
        // keys without printable representation
        if (key >= 0 && key <= GLFW.GLFW_KEY_LAST)
        {
            return ACCEPTED_KEY_PRESSED_MAP.getAt(key) == 0 || window.onKeyTyped('\0', key);
        }
        return false;
    }

    @Override
    public boolean charTyped(final char ch, final int key)
    {
        return window.onKeyTyped(ch, key);
    }

    @Override
    public boolean mouseClicked(final double mxIn, final double myIn, final int keyCode)
    {
        final double mx = calcRelativeX(mxIn);
        final double my = calcRelativeY(myIn);
        if (keyCode == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            // Adjust coordinate to origin of window
            isMouseLeftDown = true;
            return window.click(mx, my);
        }
        else if (keyCode == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            return window.rightClick(mx, my);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(final double mx, final double my, final double scrollDiff)
    {
        if (scrollDiff != 0)
        {
            return window.scrollInput(scrollDiff * 10, calcRelativeX(mx), calcRelativeY(my));
        }
        return false;
    }

    @Override
    public boolean mouseDragged(final double xIn, final double yIn, final int speed, final double deltaX, final double deltaY)
    {
        return window.onMouseDrag(calcRelativeX(xIn), calcRelativeY(yIn), speed, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(final double mxIn, final double myIn, final int keyCode)
    {
        if (keyCode == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            // Adjust coordinate to origin of window
            isMouseLeftDown = false;
            return window.onMouseReleased(calcRelativeX(mxIn), calcRelativeY(myIn));
        }
        return false;
    }

    @Override
    public void init()
    {
        minecraft.keyboardListener.enableRepeatEvents(true);
    }

    @Override
    public void tick()
    {
        if (minecraft != null)
        {
            if (!isOpen)
            {
                window.onOpened();
                isOpen = true;
            }
            else
            {
                window.onUpdate();

                if (!minecraft.player.isAlive() || minecraft.player.dead)
                {
                    minecraft.player.closeScreen();
                }
            }
        }
    }

    @Override
    public void onClose()
    {
        window.onClosed();
        Window.clearFocus();
        minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean isPauseScreen()
    {
        return window.doesWindowPauseGame();
    }

    /**
     * Render the tooltip.
     * @param ms the matrix stack.
     * @param stack the stack to render.
     * @param mouseX x pos.
     * @param mouseY y pos.
     */
    public void renderTooltipHook(final MatrixStack ms, final ItemStack stack, final int mouseX, final int mouseY)
    {
        renderTooltip(ms, stack, mouseX, mouseY);
    }

    /**
     * Converts X from event to unscaled and unscrolled X for child in relative (top-left) coordinates.
     */
    private double calcRelativeX(final double xIn)
    {
        return (xIn * mcScale - x) / renderScale;
    }

    /**
     * Converts Y from event to unscaled and unscrolled Y for child in relative (top-left) coordinates.
     */
    private double calcRelativeY(final double yIn)
    {
        return (yIn * mcScale - y) / renderScale;
    }
}
