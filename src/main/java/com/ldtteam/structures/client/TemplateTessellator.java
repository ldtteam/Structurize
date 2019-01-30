package com.ldtteam.structures.client;

import com.ldtteam.structurize.compat.optifine.OptifineCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import static org.lwjgl.opengl.GL11.*;

public class TemplateTessellator
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
    private final VertexBuffer         buffer      = new VertexBuffer(DefaultVertexFormats.BLOCK);
    private final VertexBufferUploader vboUploader = new VertexBufferUploader();
    private       boolean              isReadOnly  = false;

    public TemplateTessellator()
    {
        this.builder = new BufferBuilder(DEFAULT_BUFFER_SIZE);
        this.vboUploader.setVertexBuffer(buffer);
    }

    /**
     * Draws the data set up in this tessellator and resets the state to prepare for new drawing.
     */
    public void draw()
    {
        GlStateManager.pushMatrix();

        this.buffer.bindBuffer();

        preTemplateDraw();

        GlStateManager.bindTexture(Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId());

        this.buffer.drawArrays(GL_QUADS);

        postTemplateDraw();

        this.buffer.unbindBuffer();

        GlStateManager.popMatrix();
    }

    private static void preTemplateDraw()
    {
        OptifineCompat.getInstance().preTemplateDraw();

        GlStateManager.glEnableClientState(GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL_COLOR_ARRAY);

        //Optifine uses its one vertexformats.
        //It handles the setting of the pointers itself.
        if (OptifineCompat.getInstance().setupArrayPointers())
        {
            return;
        }

        GlStateManager.glVertexPointer(VERTEX_COMPONENT_SIZE, GL_FLOAT, VERTEX_SIZE, VERTEX_COMPONENT_OFFSET);
        GlStateManager.glColorPointer(COLOR_COMPONENT_SIZE, GL_UNSIGNED_BYTE, VERTEX_SIZE, COLOR_COMPONENT_OFFSET);
        GlStateManager.glTexCoordPointer(TEX_COORD_COMPONENT_SIZE, GL_FLOAT, VERTEX_SIZE, TEX_COORD_COMPONENT_OFFSET);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(LIGHT_TEX_COORD_COMPONENT_SIZE, GL_SHORT, VERTEX_SIZE, LIGHT_TEXT_COORD_COMPONENT_OFFSET);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);

        GlStateManager.disableCull();
    }

    private void postTemplateDraw()
    {
        GlStateManager.enableCull();

        for (final VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements())
        {
            final VertexFormatElement.EnumUsage vfeUsage = vertexformatelement.getUsage();
            final int formatIndex = vertexformatelement.getIndex();

            switch (vfeUsage)
            {
                case POSITION:
                    GlStateManager.glDisableClientState(GL_VERTEX_ARRAY);
                    break;
                case UV:
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + formatIndex);
                    GlStateManager.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                    break;
                case COLOR:
                    GlStateManager.glDisableClientState(GL_COLOR_ARRAY);
                    GlStateManager.resetColor();
                    break;
                default:
                    //NOOP
                    break;
            }
        }

        //Disable the pointers again.
        OptifineCompat.getInstance().postTemplateDraw();
    }

    /**
     * Method to start the building of the template VBO.
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
     * Method to end the building of the template VBO.
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
            this.vboUploader.draw(this.builder);
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
