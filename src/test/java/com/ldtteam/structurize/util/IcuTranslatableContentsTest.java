package com.ldtteam.structurize.util;

import com.ldtteam.structurize.util.IcuTranslatableContents.IcuComponent;
import net.minecraft.network.chat.MutableComponent;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IcuTranslatableContentsTest
{
    @Test
    public void testPercentTranslation()
    {
        final MutableComponent tested = IcuComponent.of("I'm %s%% cool %s", 100, "translation");
        assertEquals("i'm 100% cool translation", tested.getString());
    }

    @Test
    public void testCombinedTranslationResultsInPercentOnly()
    {
        final MutableComponent tested = IcuComponent.of("I'm {0} %s%% cool %s", 100, "translation");
        assertEquals("i'm {0} 100% cool translation", tested.getString());
    }

    @Test
    public void testBracketTranslation()
    {
        final MutableComponent tested = IcuComponent.of("I'm more than {0}% cool {0, plural, =1 {translation} other {translations}}", 100, "translation");
        assertEquals("i'm more than 100% cool translations", tested.getString());
    }
}
