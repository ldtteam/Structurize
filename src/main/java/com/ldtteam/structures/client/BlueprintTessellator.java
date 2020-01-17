package com.ldtteam.structures.client;

import com.ldtteam.structurize.api.util.constant.OpenGlHelper;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.inventory.container.PlayerContainer;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class BlueprintTessellator
{

    private static final int   VERTEX_COMPONENT_SIZE             = 3;
    private static final int   COLOR_COMPONENT_SIZE              = 4;
    private static final int   TEX_COORD_COMPONENT_SIZE          = 2;
    private static final int   LIGHT_TEX_COORD_COMPONENT_SIZE    = TEX_COORD_COMPONENT_SIZE;
    private static final int   VERTEX_SIZE                       = 28;
    private static final int   VERTEX_COMPONENT_OFFSET           = 0;
    private static final int   COLOR_COMPONENT_OFFSET            = 12;
    private static final int   TEX_COORD_COMPONENT_OFFSET        = 16;
    private static final int   LIGHT_TEXT_COORD_COMPONENT_OFFSET = 24;
    private static final int   DEFAULT_BUFFER_SIZE               = 2097152;

    private final BufferBuilder        builder;
    private final VertexBuffer              buffer      = new VertexBuffer(DefaultVertexFormats.BLOCK);
    private       boolean                   isReadOnly  = false;

    public BlueprintTessellator()
    {
        this.builder = new BufferBuilder(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Draws the data set up in this tessellator and resets the state to prepare for new drawing.
     * @param stack
     */
    public void draw(MatrixStack stack)
    {
        RenderSystem.pushMatrix();

        Minecraft.getInstance().getTextureManager().bindTexture(PlayerContainer.BLOCK_ATLAS_TEXTURE);

        final Matrix4f matrix4f = new Matrix4f();
        matrix4f.loadIdentity();

        buffer.bindBuffer();
        DefaultVertexFormats.BLOCK.startDrawing(0L);
        buffer.draw(matrix4f, 7);
        VertexBuffer.unbindBuffer();
        DefaultVertexFormats.BLOCK.endDrawing();

        RenderSystem.popMatrix();
    }

    private static void preBlueprintDraw()
    {
        OptifineCompat.getInstance().preBlueprintDraw();

        //DefaultVertexFormats.BLOCK.startDrawing();

        // GlStateManager.disableCull();
    }

    private void postBlueprintDraw()
    {
        // GlStateManager.enableCull();

        /*for (final VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements())
        {
            final VertexFormatElement.Usage vfeUsage = vertexformatelement.getUsage();
            final int formatIndex = vertexformatelement.getIndex();

            switch (vfeUsage)
            {
                case POSITION:
                    GL11.glDisableClientState(GL_VERTEX_ARRAY);
                    break;
                case UV:
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + formatIndex);
                    GL11.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                    break;
                case COLOR:
                    GL11.glDisableClientState(GL_COLOR_ARRAY);
                    GlStateManager.disableColorLogicOp();
                    break;
                default:
                    //NOOP
                    break;
            }
        }*/

        DefaultVertexFormats.BLOCK.endDrawing();

        //Disable the pointers again.
        OptifineCompat.getInstance().postBlueprintDraw();
    }

    /**
     * Method to start the building of the blueprint VBO.
     * Can only be called once.
     */
    public void startBuilding()
    {
        if (isReadOnly)
        {
            throw new IllegalStateException("Tessellator already build before");
        }

        builder.begin(GL_QUADS, DefaultVertexFormats.BLOCK);
    }

    /**
     * Method to end the building of the blueprint VBO.
     * Can only be called once.
     */
    public void finishBuilding()
    {
        if (!isReadOnly)
        {
            this.builder.finishDrawing();

            //Tell optifine that we are loading a new instance into the GPU.
            //This ensures that normals are calculated so that we know in which direction a face is facing. (Aka what is outside and what inside)
            OptifineCompat.getInstance().beforeBuilderUpload(this);
            buffer.submitUpload(builder);
            this.isReadOnly = true;
        }
        else
        {
            throw new IllegalStateException("Tessellator already build before");
        }
    }

    public BufferBuilder getBuilder()
    {
        if (isReadOnly)
        {
            throw new IllegalStateException("Cannot retrieve BufferBuilder when Tessellator is in readonly.");
        }

        return this.builder;
    }

    public VertexBuffer getBuffer()
    {
        return buffer;
    }
}
