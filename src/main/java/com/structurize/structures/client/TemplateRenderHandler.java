package com.structurize.structures.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.structurize.api.util.Log;
import com.structurize.structures.lib.TemplateUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.GL_QUADS;

/**
 * The Template render handler on the client side.
 */
public final class TemplateRenderHandler
{
    /**
     * A static instance on the client.
     */
    private static final TemplateRenderHandler ourInstance = new TemplateRenderHandler();

    /**
     * The builder cache.
     */
    private final Cache<Template, TemplateTessellator> templateBufferBuilderCache =
      CacheBuilder.newBuilder()
        .maximumSize(50)
        .removalListener((RemovalListener<Template, TemplateTessellator>) notification -> notification.getValue().getBuffer().deleteGlBuffers())
        .build();

    /**
     * The dispatcher.
     */
    private BlockRendererDispatcher rendererDispatcher;

    /**
     * Cached entity renderer.
     */
    private RenderManager entityRenderer;

    /**
     * Map of templates.
     */
    private final Map<String, TemplateRenderWrapper> templateMap = new HashMap<>();

    /**
     * Private constructor to hide public one.
     */
    private TemplateRenderHandler()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Get the static instance.
     *
     * @return a static instance of this class.
     */
    public static TemplateRenderHandler getInstance()
    {
        return ourInstance;
    }

    /**
     * Draw a wayPointTemplate with a rotation, mirror and offset.
     *
     * @param template      the wayPointTemplate to draw.
     * @param rotation      its rotation.
     * @param mirror        its mirror.
     * @param drawingOffset its offset.
     */
    public void draw(final Template template, final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset, final float partialTicks, final BlockPos pos, final String identifier, final boolean add)
    {
        if (rendererDispatcher == null)
        {
            rendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }
        if (entityRenderer == null)
        {
            entityRenderer = Minecraft.getMinecraft().getRenderManager();
        }

        if (!templateMap.containsKey(identifier) || add)
        {
            pregenerateEntries(template, add, identifier);
        }
        final TemplateRenderWrapper wrapper = templateMap.get(identifier);
        wrapper.tessellator.draw(rotation, mirror, drawingOffset, wrapper.anchorBlockOffset);
        wrapper.tileList.forEach(tileEntity -> {
            tileEntity.setPos(pos.subtract(wrapper.anchorBlockOffset));
            TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, 0);
        });
    }

    /**
     * Pregenerates the tileEntity list and the Tessellator for this template.
     *
     * @param template The template to use
     * @param add if should add to existing wrapper.
     */
    public void pregenerateEntries(final Template template, final boolean add, final String identifier)
    {
        if (template == null)
        {
            return;
        }

        if (rendererDispatcher == null)
        {
            rendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }

        final TemplateRenderWrapper wrapper;
        final boolean first;
        if (templateMap.containsKey(identifier) && add)
        {
            wrapper = templateMap.get(identifier);
            first = false;
        }
        else
        {
            first = true;
            wrapper = new TemplateRenderWrapper();
        }

        // Calculate the anchor offset
        wrapper.anchorBlockOffset = TemplateUtils.getPrimaryBlockOffset(template);

        // generate tileEntities
        wrapper.tileList.addAll(template.blocks.stream()
                     .filter(blockInfo -> blockInfo.tileentityData != null)
                     .map(this::constructTileEntities)
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList()));

        // generate tessellator
        final TemplateBlockAccess blockAccess = new TemplateBlockAccess(template);
        try
        {
            wrapper.tessellator = templateBufferBuilderCache.get(template, () -> {
                final TemplateTessellator tessellator = new TemplateTessellator();
                tessellator.getBuilder().begin(GL_QUADS, DefaultVertexFormats.BLOCK);
                template.blocks.stream()
                  .map(b -> TemplateBlockAccessTransformHandler.getInstance().Transform(b))
                  .forEach(b -> rendererDispatcher.renderBlock(b.blockState, b.pos, blockAccess, tessellator.getBuilder()));

                return tessellator;
            });
        }
        catch (ExecutionException e)
        {
            Log.getLogger().error(e);
        }
        templateMap.put(identifier, wrapper);
    }

    @Nullable
    private TileEntity constructTileEntities(final Template.BlockInfo info)
    {
        final TileEntity entity = TileEntity.create(null, info.tileentityData);
        if (!(entity instanceof TileEntityBanner))
        {
            return null;
        }
        return entity;
    }

    /**
     * Render a template at a list of points.
     *
     * @param points       the points to render it at.
     * @param partialTicks the partial ticks.
     * @param template     the template.
     */
    public void drawTemplateAtListOfPositions(final List<BlockPos> points, final float partialTicks, final Template template, final String identifier)
    {
        if (points.isEmpty())
        {
            return;
        }
        final EntityPlayer perspectiveEntity = Minecraft.getMinecraft().player;
        final double interpolatedEntityPosX = perspectiveEntity.lastTickPosX + (perspectiveEntity.posX - perspectiveEntity.lastTickPosX) * partialTicks;
        final double interpolatedEntityPosY = perspectiveEntity.lastTickPosY + (perspectiveEntity.posY - perspectiveEntity.lastTickPosY) * partialTicks;
        final double interpolatedEntityPosZ = perspectiveEntity.lastTickPosZ + (perspectiveEntity.posZ - perspectiveEntity.lastTickPosZ) * partialTicks;

        for (final BlockPos coord : points)
        {
            final BlockPos pos = coord.down();
            final double renderOffsetX = pos.getX() - interpolatedEntityPosX;
            final double renderOffsetY = pos.getY() - interpolatedEntityPosY;
            final double renderOffsetZ = pos.getZ() - interpolatedEntityPosZ;
            final Vector3d renderOffset = new Vector3d();
            renderOffset.x = renderOffsetX;
            renderOffset.y = renderOffsetY;
            renderOffset.z = renderOffsetZ;

            draw(template, Rotation.NONE, Mirror.NONE, renderOffset, partialTicks, coord, identifier, true);
        }
    }

    /**
     * Reset pregenerated entries
     */
    public void reset(final String desc)
    {
        templateMap.remove(desc);
    }

    private class TemplateRenderWrapper
    {
        /**
         * Pregenerated Tile entities
         */
        private List<TileEntity> tileList = new ArrayList<>();

        /**
         * Pregenerated tessellator
         */
        private TemplateTessellator tessellator = new TemplateTessellator();

        /**
         * The offset of the anchor for the current template
         */
        private BlockPos anchorBlockOffset;
    }
}
