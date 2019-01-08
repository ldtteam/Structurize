package com.structurize.structures.client;

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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.GL_QUADS;

/**
 * The wayPointTemplate render handler on the client side.
 */
public final class TemplateRenderHandler
{
    /**
     * The dispatcher.
     */
    private BlockRendererDispatcher rendererDispatcher;

    /**
     * Cached entity renderer.
     */
    private RenderManager entityRenderer;

    /**
     * Pregenerated Tile entities
     */
    private List<TileEntity> tileList;

    /**
     * Pregenerated tessellator
     */
    private TemplateTessellator tessellator;

    /**
     * The offset of the anchor for the current template
     */
    private BlockPos anchorBlockOffset;

    /**
     * Draw a wayPointTemplate with a rotation, mirror and offset.
     *
     * @param template      the wayPointTemplate to draw.
     * @param rotation      its rotation.
     * @param mirror        its mirror.
     * @param drawingOffset its offset.
     */
    public void draw(final Template template, final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset, final float partialTicks, final BlockPos pos)
    {
        if (rendererDispatcher == null)
        {
            rendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }
        if (entityRenderer == null)
        {
            entityRenderer = Minecraft.getMinecraft().getRenderManager();
        }

        // Should not happen, but generate again when missing
        if (tileList == null || tessellator == null)
        {
            pregenerateEntries(template);
        }

        tessellator.draw(rotation, mirror, drawingOffset, anchorBlockOffset);
        tileList.forEach(tileEntity -> {
            tileEntity.setPos(pos.subtract(anchorBlockOffset));
            TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, 0);
        });
    }

    /**
     * Pregenerates the tileEntity list and the Tessellator for this template.
     *
     * @param template The template to use
     */
    public void pregenerateEntries(final Template template)
    {
        if (template == null)
        {
            return;
        }

        if (rendererDispatcher == null)
        {
            rendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }

        // Calculate the anchor offset
        anchorBlockOffset = TemplateUtils.getPrimaryBlockOffset(template);

        // generate tileEntities
        tileList = template.blocks.stream()
                     .filter(blockInfo -> blockInfo.tileentityData != null)
                     .map(this::constructTileEntities)
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());

        // generate tessellator
        tessellator = new TemplateTessellator();
        final TemplateBlockAccess blockAccess = new TemplateBlockAccess(template);

        tessellator.getBuilder().begin(GL_QUADS, DefaultVertexFormats.BLOCK);

        template.blocks.stream()
          .map(b -> TemplateBlockAccessTransformHandler.getInstance().Transform(b))
          .forEach(b -> rendererDispatcher.renderBlock(b.blockState, b.pos, blockAccess, tessellator.getBuilder()));
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
    public void drawTemplateAtListOfPositions(final List<BlockPos> points, final float partialTicks, final Template template)
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

            draw(template, Rotation.NONE, Mirror.NONE, renderOffset, partialTicks, coord);
        }
    }

    /**
     * Reset pregenerated entries
     */
    public void reset()
    {
        if (tessellator != null)
        {
            tileList = null;
            tessellator.getBuffer().deleteGlBuffers();
            tessellator = null;
            anchorBlockOffset = null;
        }
    }
}
