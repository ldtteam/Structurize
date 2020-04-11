package com.ldtteam.structures.client;

import java.nio.ByteBuffer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import org.lwjgl.system.MemoryUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderTimeManager;
import net.minecraft.client.renderer.BufferBuilder.DrawState;
import net.minecraft.client.renderer.RenderType;

public class RenderUtils
{
    public static RenderTimeManager renderTimeManager = new RenderTimeManager(100);

    public static BufferBuilder createAndBeginBuffer(final RenderType renderType)
    {
        final BufferBuilder bufferBuilder = new BufferBuilder(renderType.getBufferSize());
        bufferBuilder.begin(renderType.getDrawMode(), renderType.getVertexFormat());
        return bufferBuilder;
    }

    /**
     * @see RenderType#finish()
     */
    public static BuiltBuffer finishBuffer(final BufferBuilder bufferBuilder, final RenderType renderType)
    {
        if (bufferBuilder.isDrawing())
        {
            /*
             * todo: if anyone needs sorted rendertype fix this
             * if (renderType.needsSorting)
             * {
             * bufferBuilder.sortVertexData(0, 0, 0);
             * }
             */

            bufferBuilder.finishDrawing();
            return new BuiltBuffer(bufferBuilder.getNextBuffer(), renderType);
        }
        throw new RuntimeException("bufferbuilder not in drawing state");
    }

    /**
     * @see RenderType#finish()
     */
    public static void drawBuiltBuffer(final BuiltBuffer builtBuffer)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        builtBuffer.getRenderType().setupRenderState();
        builtBuffer.getByteBuffer().clear();
        if (builtBuffer.getDrawState().getVertexCount() > 0)
        {
            builtBuffer.getDrawState().getFormat().setupBufferState(MemoryUtil.memAddress(builtBuffer.getByteBuffer()));
            GlStateManager.drawArrays(builtBuffer.getDrawState().getDrawMode(), 0, builtBuffer.getDrawState().getVertexCount());
            builtBuffer.getDrawState().getFormat().clearBufferState();
        }
        builtBuffer.getRenderType().clearRenderState();
    }

    public static class BuiltBuffer extends Pair<DrawState, ByteBuffer>
    {
        private final RenderType renderType;

        public BuiltBuffer(final Pair<DrawState, ByteBuffer> pair, final RenderType renderTypeIn)
        {
            super(pair.getFirst(), pair.getSecond());
            renderType = renderTypeIn;
        }

        public RenderType getRenderType()
        {
            return renderType;
        }

        public ByteBuffer getByteBuffer()
        {
            return super.getSecond();
        }

        public DrawState getDrawState()
        {
            return super.getFirst();
        }
    }
}
