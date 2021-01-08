package com.ldtteam.blockout.views;

import com.ldtteam.blockout.BOScreen;
import com.ldtteam.blockout.Loader;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.properties.Parsers;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.function.ToDoubleBiFunction;

/**
 * Blockout window, high level root pane.
 */
@OnlyIn(Dist.CLIENT)
public class Window extends View
{
    /**
     * The default width.
     */
    protected static final int DEFAULT_WIDTH = 420;

    /**
     * The default height.
     */
    protected static final int DEFAULT_HEIGHT = 240;

    /**
     * The screen of the window.
     */
    protected BOScreen screen;

    /**
     * Defines if the window should pause the game.
     */
    protected boolean windowPausesGame = true;

    /**
     * Defines if the window should have a lightbox.
     */
    protected boolean lightbox = true;

    /**
     * Render using size or attemp to scale to fullscreen.
     */
    protected WindowRenderType windowRenderType = WindowRenderType.OVERSIZED_VANILLA;

    /**
     * Create a window from an xml file.
     *
     * @param resource ResourceLocation to get file from.
     */
    public Window(final ResourceLocation resource)
    {
        this();
        Loader.createFromXMLFile(resource, this);
    }

    /**
     * Make default sized window.
     */
    public Window()
    {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Window constructor when there is a fixed Width and Height.
     *
     * @param w Width of the window, in pixels.
     * @param h Height of the window, in pixels.
     */
    public Window(final int w, final int h)
    {
        super();
        width = w;
        height = h;

        screen = new BOScreen(this);
        window = this;
    }

    /**
     * Create a window from an xml file.
     *
     * @param resource location to get file from.
     */
    public Window(final String resource)
    {
        this();
        Loader.createFromXMLFile(resource, this);
    }

    /**
     * Load the xml parameters.
     *
     * @param params xml parameters.
     */
    public void loadParams(@NotNull final PaneParams params)
    {
        final String inherit = params.string("inherit");
        if (inherit != null)
        {
            Loader.createFromXMLFile(new ResourceLocation(inherit), this);
        }

        params.shorthand("size", Parsers.INT, 2, a -> {
            width = a.get(0);
            height = a.get(1);
        });

        lightbox = params.bool("lightbox", lightbox);
        windowPausesGame = params.bool("pause", windowPausesGame);
        windowRenderType = params.enumeration("type", WindowRenderType.class, windowRenderType);
    }

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        updateDebugging();

        super.drawSelf(ms, mx, my);
    }

    private boolean isKeyDown(final int keyCode)
    {
        return InputMappings.isKeyDown(mc.mainWindow.getHandle(), keyCode);
    }

    private void updateDebugging()
    {
        debugging = isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) && isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) &&
            (isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || isKeyDown(GLFW.GLFW_KEY_LEFT_SUPER));
    }

    /**
     * Return <tt>true</tt> if the 'lightbox' (default dark background) should be displayed.
     *
     * @return <tt>true</tt> if the 'lightbox' should be displayed.
     */
    public boolean hasLightbox()
    {
        return lightbox;
    }

    /**
     * Return <tt>true</tt> if the game should be paused when the Window is displayed.
     *
     * @return <tt>true</tt> if the game should be paused when the Window is displayed.
     */
    public boolean doesWindowPauseGame()
    {
        return windowPausesGame;
    }

    /**
     * @return {@link WindowRenderType}
     */
    public WindowRenderType getRenderType()
    {
        return windowRenderType;
    }

    /**
     * Open the window.
     */
    public void open()
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> mc.runImmediately(() -> mc.displayGuiScreen(screen)));
    }

    /**
     * Windows wrap a GuiScreen.
     *
     * @return The current GuiScreen.
     */
    public BOScreen getScreen()
    {
        return screen;
    }

    /**
     * Mouse click released handler.
     * <p>
     * Currently does nothing.
     *
     * @param mx Mouse X position
     * @param my Mouse Y position
     */
    public boolean onMouseReleased(final double mx, final double my)
    {
        // Can be overridden
        return false;
    }

    /**
     * Key input handler. Directs keystrokes to focused Pane, or to onUnhandledKeyTyped() if no
     * Pane handles the keystroke.
     * <p>
     * It is advised not to override this method.
     *
     * @param ch  Character of key pressed.
     * @param key Keycode of key pressed.
     * @return <tt>true</tt> if the key was handled by a Pane.
     */
    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        if (getFocus() != null && getFocus().onKeyTyped(ch, key))
        {
            return true;
        }

        return onUnhandledKeyTyped(ch, key);
    }

    /**
     * Key input handler when a focused pane did not handle the key.
     * <p>
     * Override this to handle key input at the Window level.
     *
     * @param ch  Character of key pressed.
     * @param key Keycode of key pressed.
     */
    public boolean onUnhandledKeyTyped(final int ch, final int key)
    {
        if (key == GLFW.GLFW_KEY_ESCAPE)
        {
            close();
            return true;
        }
        return false;
    }

    /**
     * Close the Window.
     */
    public void close()
    {
        screen.onClose();
        this.mc.player.closeScreen();
    }

    /**
     * Called when the Window is displayed.
     */
    public void onOpened()
    {
        // Can be overridden
    }

    /**
     * Called when the Window is closed.
     */
    public void onClosed()
    {
        // Can be overridden
    }

    /**
     * Defines how gui should be rendered.
     */
    public enum WindowRenderType
    {
        /**
         * upscaling according to minecraft gui scale settings, no downscaling, max gui resolution is 320*240 px, anything above might not be rendered
         */
        VANILLA((mcWindow, window) -> Math.max(mcWindow.getGuiScaleFactor(), 1.0d)),
        /**
         * scaling to size of framebuffer with no lower limit, max gui resolution is unlimited
         */
        FULLSCREEN((mcWindow, window) -> {
            final double widthScale = ((double) mcWindow.getFramebufferWidth()) / window.getWidth();
            final double heightScale = ((double) mcWindow.getFramebufferHeight()) / window.getHeight();

            return Math.min(widthScale, heightScale);
        }),
        /**
         * scaling to size of framebuffer with lower limit of 320*240 px, max gui resolution is unlimited
         */
        FULLSCREEN_VANILLA((mcWindow, window) -> {
            final double widthScale = Math.max((double) mcWindow.getFramebufferWidth(), 320.0d) / window.getWidth();
            final double heightScale = Math.max((double) mcWindow.getFramebufferHeight(), 240.0d) / window.getHeight();

            return Math.min(widthScale, heightScale);
        }),
        /**
         * no upscaling, no downscalling, max gui resolution is unlimited
         */
        FIXED((mcWindow, window) -> 1.0d),
        /**
         * no upscaling, downscaling down to 320*240 px according to size of framebuffer, max gui resolution is unlimited
         */
        FIXED_VANILLA((mcWindow, window) -> Math.min(FULLSCREEN_VANILLA.calcRenderScale(mcWindow, window), 1.0d)),
        /**
         * integer upscaling up to mc gui scale, downscaling down to size of framebuffer, max gui resolution is unlimited
         */
        OVERSIZED((mcWindow, window) -> {
            final double fs = FULLSCREEN.calcRenderScale(mcWindow, window);
            final int userScale = Minecraft.getInstance().gameSettings.guiScale;
            return fs < 1.0d ? fs : Math.min(Math.floor(fs), userScale == 0 ? Double.MAX_VALUE : userScale);
        }),
        /**
         * integer upscaling up to mc gui scale, downscaling down to 320*240 px according to size of framebuffer, max gui resolution is unlimited
         */
        OVERSIZED_VANILLA((mcWindow, window) -> {
            final double fs_vanilla = FULLSCREEN_VANILLA.calcRenderScale(mcWindow, window);
            final int userScale = Minecraft.getInstance().gameSettings.guiScale;
            return fs_vanilla < 1.0d ? fs_vanilla : Math.min(Math.floor(fs_vanilla), userScale == 0 ? Double.MAX_VALUE : userScale);
        });

        private final ToDoubleBiFunction<MainWindow, Window> renderScaleCalculator;

        private WindowRenderType(final ToDoubleBiFunction<MainWindow, Window> renderScaleCalculator)
        {
            this.renderScaleCalculator = renderScaleCalculator;
        }

        public double calcRenderScale(final MainWindow mcWindow, final Window window)
        {
            return renderScaleCalculator.applyAsDouble(mcWindow, window);
        }
    }
}
