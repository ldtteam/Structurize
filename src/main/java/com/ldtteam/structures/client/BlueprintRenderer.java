package com.ldtteam.structures.client;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.lib.RenderUtil;
import com.ldtteam.structures.lib.BlueprintUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * The renderer for blueprint.
 * Holds all information required to render a blueprint.
 */
public class BlueprintRenderer
{
    private static final float HALF_PERCENT_SHRINK               = 0.995F;

    private final BlueprintBlockAccess blockAccess;
    private final List<TileEntity>     tileEntities;
    private final List<Entity>         entities;
    private final BlueprintTessellator tessellator;
    private final BlockPos             primaryBlockOffset;

    /**
     * Static factory utility method to handle the extraction of the values from the blueprint.
     *
     * @param blueprint The blueprint to create an instance for.
     * @return The renderer.
     */
    public static BlueprintRenderer buildRendererForBlueprint(final Blueprint blueprint)
    {
        final BlueprintBlockAccess blockAccess = new BlueprintBlockAccess(blueprint);
        final List<TileEntity> tileEntities = BlueprintUtils.instantiateTileEntities(blueprint, blockAccess);
        final List<Entity> entities = BlueprintUtils.instantiateEntities(blueprint, blockAccess);
        final BlueprintTessellator blueprintTessellator = new BlueprintTessellator();
        final BlockPos primaryBlockOffset = BlueprintUtils.getPrimaryBlockOffset(blueprint);

        return new BlueprintRenderer(blockAccess, tileEntities, entities, blueprintTessellator, primaryBlockOffset);
    }

    private BlueprintRenderer(
      final BlueprintBlockAccess blockAccess,
      final List<TileEntity> tileEntities,
      final List<Entity> entities,
      final BlueprintTessellator tessellator,
      final BlockPos primaryBlockOffset)
    {
        this.blockAccess = blockAccess;
        this.tileEntities = tileEntities;
        this.entities = entities;
        this.tessellator = tessellator;
        this.primaryBlockOffset = primaryBlockOffset;

        this.setup();
    }

    /**
     * Sets up the renders VBO
     */
    private void setup()
    {
        tessellator.startBuilding();

        blockAccess.getBlueprint().getBlockInfoAsList().stream()
          .map(b -> BlueprintBlockInfoTransformHandler.getInstance().Transform(b))
          .forEach(b -> Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(b.getState(), b.getPos(), blockAccess, tessellator.getBuilder()));

        tessellator.finishBuilding();
    }

    /**
     * Draws an instance of the blueprint at the given position, with the given rotation, and mirroring.
     *
     * @param rotation The rotation.
     * @param mirror The mirroring.
     * @param drawingOffset The drawing offset.
     */
    public void draw(final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset)
    {
        //Handle things like mirror, rotation and offset.
        preBlueprintDraw(rotation, mirror, drawingOffset, primaryBlockOffset);

        //Draw normal blocks.
        tessellator.draw();

        RenderHelper.enableStandardItemLighting();

        //Draw tile entities.
        tileEntities.forEach(tileEntity -> {
            GlStateManager.pushMatrix();
            int combinedLight = tileEntity.getWorld().getCombinedLight(tileEntity.getPos(), 0);
            int lightMapX = combinedLight % 65536;
            int lightMapY = combinedLight / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lightMapX, (float)lightMapY);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            TileEntityRendererDispatcher.instance.render(tileEntity, tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ(), 0, 1f);
            GlStateManager.popMatrix();
        });

        RenderHelper.disableStandardItemLighting();

        //Draw entities
        entities.forEach(entity -> {
            GlStateManager.pushMatrix();
            int brightnessForRender = entity.getBrightnessForRender();

            if (entity.isBurning())
            {
                brightnessForRender = 15728880;
            }

            int lightMapX = brightnessForRender % 65536;
            int lightMapY = brightnessForRender / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lightMapX, (float)lightMapY);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            Minecraft.getMinecraft().getRenderManager().renderEntity(entity, entity.posX, entity.posY, entity.posZ, 0f, 0, true);

            GlStateManager.popMatrix();
        });

        postBlueprintDraw();

    }

    private static void preBlueprintDraw(final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset, final BlockPos inBlueprintOffset)
    {
        final ITextureObject textureObject = Minecraft.getMinecraft().getTextureMapBlocks();
        GlStateManager.bindTexture(textureObject.getGlTextureId());

        GlStateManager.pushMatrix();
        GlStateManager.translate(drawingOffset.x, drawingOffset.y, drawingOffset.z);

        final BlockPos rotateInBlueprintOffset = inBlueprintOffset.rotate(rotation);
        GlStateManager.translate(-rotateInBlueprintOffset.getX(), -rotateInBlueprintOffset.getY(), -rotateInBlueprintOffset.getZ());

        RenderUtil.applyRotationToYAxis(rotation);
        RenderUtil.applyMirror(mirror, inBlueprintOffset);

        GlStateManager.scale(HALF_PERCENT_SHRINK, HALF_PERCENT_SHRINK, HALF_PERCENT_SHRINK);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.resetColor();
        GlStateManager.pushMatrix();
    }

    private static void postBlueprintDraw()
    {
        GlStateManager.popMatrix();
        GlStateManager.resetColor();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public BlueprintBlockAccess getBlockAccess()
    {
        return blockAccess;
    }

    public List<TileEntity> getTileEntities()
    {
        return tileEntities;
    }

    public BlueprintTessellator getTessellator()
    {
        return tessellator;
    }

    public BlockPos getPrimaryBlockOffset()
    {
        return primaryBlockOffset;
    }

}
