package com.ldtteam.blockout;

import com.ldtteam.blockout.views.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.BitArray;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

/**
 * Wraps MineCrafts GuiScreen for BlockOut's Window.
 */
public class BOScreen extends Screen
{
    protected static double scale = 0;
    protected Window window;
    protected int x = 0;
    protected int y = 0;
    public static boolean isMouseLeftDown = false;
    private boolean isOpen = false;
    private static final BitArray ACCEPTED_KEY_PRESSED_MAP = new BitArray(1, GLFW.GLFW_KEY_LAST + 1);

    static
    {
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

    public static double getScale()
    {
        return scale;
    }

    private static void setScale(final Minecraft mc)
    {
        // Seems to work without the sides now
        // Failsave
        if (mc != null)
        {
            scale = mc.mainWindow.getGuiScaleFactor();
        }
    }

    @Override
    public void render(final int mx, final int my, final float f)
    {
        if (window.hasLightbox() && super.minecraft != null)
        {
            super.renderBackground();
        }

        setScale(minecraft);

        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x, (float) y, 0);
        window.draw(mx - x, my - y);
        RenderSystem.popMatrix();
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers)
    {
        // keys without printable representation
        if (ACCEPTED_KEY_PRESSED_MAP.getAt(key) == 1)
        {
            return window.onKeyTyped('\0', key);
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
        final double mx = mxIn - x;
        final double my = myIn - y;
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
            return window.scrollInput(scrollDiff * 10, mx - x, my - y);
        }
        return false;
    }

    @Override
    public void mouseMoved(final double mxIn, final double myIn)
    {
        // final int mx = (int) (super.minecraft.mouseHelper.getMouseX() * super.width / super.minecraft.mainWindow.getWidth()) - x;
        // final int my = (int) (super.height - super.minecraft.mouseHelper.getMouseY() * super.height /
        // super.minecraft.mainWindow.getHeight()) - 1 - y;
        window.handleHover(mxIn - x, myIn - y);
    }

    @Override
    public boolean mouseDragged(final double xIn, final double yIn, final int speed, final double deltaX, final double deltaY)
    {
        return window.onMouseDrag(xIn - x, yIn - y, speed, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(final double mxIn, final double myIn, final int keyCode)
    {
        if (keyCode == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            // Adjust coordinate to origin of window
            isMouseLeftDown = false;
            return window.onMouseReleased(mxIn - x, myIn - y);
        }
        return false;
    }

    @Override
    public void init()
    {
        x = (width - window.getWidth()) / 2;
        y = (height - window.getHeight()) / 2;

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
}
