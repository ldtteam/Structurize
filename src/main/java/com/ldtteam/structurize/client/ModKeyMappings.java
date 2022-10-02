package com.ldtteam.structurize.client;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.structurize.client.gui.AbstractBlueprintManipulationWindow;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
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
    public static final KeyMapping TELEPORT = new KeyMapping("key.structurize.teleport",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY);

    /**
     * Move build previews
     */
    public static final KeyMapping MOVE_FORWARD = new KeyMapping("key.structurize.move_forward",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, CATEGORY);
    public static final KeyMapping MOVE_BACK = new KeyMapping("key.structurize.move_back",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, CATEGORY);
    public static final KeyMapping MOVE_LEFT = new KeyMapping("key.structurize.move_left",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, CATEGORY);
    public static final KeyMapping MOVE_RIGHT = new KeyMapping("key.structurize.move_right",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, CATEGORY);
    public static final KeyMapping MOVE_UP = new KeyMapping("key.structurize.move_up",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_ADD, CATEGORY);
    public static final KeyMapping MOVE_DOWN = new KeyMapping("key.structurize.move_down",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_SUBTRACT, CATEGORY);
    public static final KeyMapping ROTATE_CW = new KeyMapping("key.structurize.rotate_cw",
            BLUEPRINT_WINDOW, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, CATEGORY);
    public static final KeyMapping ROTATE_CCW = new KeyMapping("key.structurize.rotate_ccw",
            BLUEPRINT_WINDOW, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, CATEGORY);
    public static final KeyMapping MIRROR = new KeyMapping("key.structurize.mirror",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY);
    public static final KeyMapping PLACE = new KeyMapping("key.structurize.place",
            BLUEPRINT_WINDOW, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_ENTER, CATEGORY);

    /**
     * Register key mappings
     */
    public static void register(@NotNull final RegisterKeyMappingsEvent event)
    {
        event.register(TELEPORT);
        event.register(MOVE_FORWARD);
        event.register(MOVE_BACK);
        event.register(MOVE_LEFT);
        event.register(MOVE_RIGHT);
        event.register(MOVE_UP);
        event.register(MOVE_DOWN);
        event.register(ROTATE_CW);
        event.register(ROTATE_CCW);
        event.register(MIRROR);
        event.register(PLACE);
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
