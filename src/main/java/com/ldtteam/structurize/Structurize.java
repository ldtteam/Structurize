package com.ldtteam.structurize;

import com.ldtteam.structurize.event.EventSubscriber;
import com.ldtteam.structurize.event.LifecycleSubscriber;
import com.ldtteam.structurize.util.constants.GeneralConstants;
import net.minecraftforge.fml.common.Mod;

/**
 * Mod main class.
 * The value in annotation should match an entry in the META-INF/mods.toml file.
 */
@Mod(GeneralConstants.MOD_ID)
public class Structurize
{
    /**
     * Mod init, registers events to their respective busses
     */
    public Structurize()
    {
        Instances.getModLogger().warn("Structurize");
        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(LifecycleSubscriber.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(EventSubscriber.class);
    }
}
