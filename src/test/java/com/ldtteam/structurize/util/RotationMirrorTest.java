package com.ldtteam.structurize.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RotationMirrorTest
{
    @Test
    public void testCaclDifferenceTowards()
    {
        for (final RotationMirror start : RotationMirror.values())
        {
            for (final RotationMirror end : RotationMirror.values())
            {
                assertEquals(end, start.add(start.calcDifferenceTowards(end)));
            }
        }
    }
}
