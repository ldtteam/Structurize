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
