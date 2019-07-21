package com.ldtteam.structurize.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Mod common configuration.
 * Loaded everywhere, not synced.
 */
public class CommonConfiguration extends AbstractConfiguration
{
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> testList;
    private final ForgeConfigSpec.BooleanValue testBoolean;
    private final ForgeConfigSpec.DoubleValue testDouble;
    private final ForgeConfigSpec.LongValue testLong;
    private final ForgeConfigSpec.EnumValue<Test> testEnum;

    public enum Test
    {
        ONE,
        TWO,
        THOUSAND,
        SAND;
    }

    /**
     * Builds common configuration.
     *
     * @param builder config builder
     */
    protected CommonConfiguration(final ForgeConfigSpec.Builder builder)
    {
        builder.comment("Common").push("common");
        testList = defineList(builder, "testlist", new ArrayList<String>(), o -> o instanceof String);
        testBoolean = defineBoolean(builder, "testboolean", false);
        testDouble = defineDouble(builder, "testdouble", 0.5d, 0.0d, 1.0d);
        testLong = defineLong(builder, "testlong", 150);
        builder.pop();
        builder.comment("General").push("general");
        testEnum = defineEnum(builder, "testenum", Test.ONE);
        builder.pop();
    }

    public List<? extends String> getTestList()
    {
        return testList.get();
    }

    public boolean getTestBoolean()
    {
        return testBoolean.get();
    }

    public double getTestDouble()
    {
        return testDouble.get();
    }

    public long getTestLong()
    {
        return testLong.get();
    }

    public Test getTestEnum()
    {
        return testEnum.get();
    }
}