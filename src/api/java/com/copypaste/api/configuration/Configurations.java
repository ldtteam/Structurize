package com.copypaste.api.configuration;

import net.minecraftforge.common.config.Config;

import static com.copypaste.api.util.constant.Constants.*;

@Config(modid = MOD_ID)
public class Configurations
{
    @Config.Comment("All configuration related to gameplay")
    public static Gameplay gameplay = new Gameplay();

    public static class Gameplay
    {
        @Config.Comment("Should the default schematics be ignored (from the jar)?")
        public boolean ignoreSchematicsFromJar = false;

        @Config.Comment("Should player made schematics be allowed")
        public boolean allowPlayerSchematics = false;

        @Config.Comment("Max amount of schematics to be cached on the server")
        public int maxCachedSchematics = 100;
     }
}
