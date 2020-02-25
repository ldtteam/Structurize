package com.ldtteam.structurize.util;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import java.util.OptionalDouble;

/**
 * Holding all kind of render types of minecolonies
 */
public final class MRenderTypes extends RenderType
{
    /**
     * Private constructor to hide implicit one.
     *
     * @param name   the name of the rendertype.
     * @param format its format.
     * @param id1    no idea.
     * @param id2    no idea.
     * @param b1     no idea.
     * @param b2     no idea.
     * @param b3     no idea.
     * @param state  the rendertype state.
     */
    private MRenderTypes(final String name,
        final VertexFormat format,
        final int id1,
        final int id2,
        final boolean b1,
        final boolean b2,
        final Runnable b3,
        final Runnable state)
    {
        super(name, format, id1, id2, b1, b2, b3, state);
    }

    /**
     * Custom line renderer type.
     *
     * @return the renderType which is created.
     */
    public static RenderType customLineRenderer()
    {
        return makeType("structurizelines",
            DefaultVertexFormats.POSITION_COLOR,
            3,
            256,
            RenderType.State.getBuilder().line(new RenderState.LineState(OptionalDouble.empty())).build(false));
    }
}
