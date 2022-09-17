package com.ldtteam.structurize.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.jetbrains.annotations.NotNull;

public class ModKeyMappings
{
    private static final String CATEGORY = "key.structurize.categories.general";

    /**
     * Teleport using active Scan Tool
     */
    public static final KeyMapping TELEPORT = new KeyMapping("key.structurize.teleport",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY);

    /**
     * Register key mappings
     */
    public static void register(@NotNull final RegisterKeyMappingsEvent event)
    {
        event.register(TELEPORT);
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
