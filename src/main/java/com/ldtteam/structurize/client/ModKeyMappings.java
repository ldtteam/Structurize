package com.ldtteam.structurize.client;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.structurize.client.gui.AbstractBlueprintManipulationWindow;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings
{
    private static final String CATEGORY = "key.structurize.categories.general";

    public static final IKeyConflictContext BLUEPRINT_WINDOW = new IKeyConflictContext()
    {
        @Override
        public boolean isActive()
        {
            if (Minecraft.getInstance().screen instanceof BOScreen screen)
            {
                return screen.getWindow() instanceof AbstractBlueprintManipulationWindow;
            }
            return false;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            return this == other;
        }
    };

    /**
     * Teleport using active Scan Tool
     */
    public static final Lazy<KeyMapping> TELEPORT = Lazy.of(() -> new KeyMapping("key.structurize.teleport",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY));

    /**
     * Move build previews
     */
    public static final Lazy<KeyMapping> MOVE_FORWARD = Lazy.of(() -> new KeyMapping("key.structurize.move_forward",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, CATEGORY));
    public static final Lazy<KeyMapping> MOVE_BACK = Lazy.of(() -> new KeyMapping("key.structurize.move_back",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, CATEGORY));
    public static final Lazy<KeyMapping> MOVE_LEFT = Lazy.of(() -> new KeyMapping("key.structurize.move_left",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, CATEGORY));
    public static final Lazy<KeyMapping> MOVE_RIGHT = Lazy.of(() -> new KeyMapping("key.structurize.move_right",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, CATEGORY));
    public static final Lazy<KeyMapping> MOVE_UP = Lazy.of(() -> new KeyMapping("key.structurize.move_up",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_ADD, CATEGORY));
    public static final Lazy<KeyMapping> MOVE_DOWN = Lazy.of(() -> new KeyMapping("key.structurize.move_down",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_SUBTRACT, CATEGORY));
    public static final Lazy<KeyMapping> ROTATE_CW = Lazy.of(() -> new KeyMapping("key.structurize.rotate_cw",
            BLUEPRINT_WINDOW, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, CATEGORY));
    public static final Lazy<KeyMapping> ROTATE_CCW = Lazy.of(() -> new KeyMapping("key.structurize.rotate_ccw",
            BLUEPRINT_WINDOW, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, CATEGORY));
    public static final Lazy<KeyMapping> MIRROR = Lazy.of(() -> new KeyMapping("key.structurize.mirror",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY));
    public static final Lazy<KeyMapping> PLACE = Lazy.of(() -> new KeyMapping("key.structurize.place",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_ENTER, CATEGORY));

    /**
     * Register key mappings
     */
    public static void register(@NotNull final RegisterKeyMappingsEvent event)
    {
        event.register(TELEPORT.get());
        event.register(MOVE_FORWARD.get());
        event.register(MOVE_BACK.get());
        event.register(MOVE_LEFT.get());
        event.register(MOVE_RIGHT.get());
        event.register(MOVE_UP.get());
        event.register(MOVE_DOWN.get());
        event.register(ROTATE_CW.get());
        event.register(ROTATE_CCW.get());
        event.register(MIRROR.get());
        event.register(PLACE.get());
    }

    /**
     * Private constructor to hide the implicit one.
     */
    private ModKeyMappings()
    {
        /*
         * Intentionally left empty.
         */
    }
}
