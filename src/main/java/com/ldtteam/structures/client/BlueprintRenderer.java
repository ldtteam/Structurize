package com.ldtteam.structures.client;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structures.lib.RenderUtil;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

/**
 * The renderer for blueprint.
 * Holds all information required to render a blueprint.
 */
public class BlueprintRenderer
{
    private static final float HALF_PERCENT_SHRINK = 0.995F;

    private final BlueprintBlockAccess blockAccess;
    private final List<TileEntity> tileEntities;
    private final List<Entity> entities;
    private final BlueprintTessellator tessellator;
    private final BlockPos primaryBlockOffset;

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

        final Random random = new Random();

        blockAccess.getBlueprint()
            .getBlockInfoAsList()
            .stream()
            .map(b -> BlueprintBlockInfoTransformHandler.getInstance().Transform(b))
            .filter(blockInfo -> blockInfo.getState().getBlock() != ModBlocks.blockSubstitution)
            .forEach(b -> {
                Minecraft.getInstance()
                    .getBlockRendererDispatcher()
                    .renderBlock(
                        b.getState(),
                        b.getPos(),
                        blockAccess,
                        tessellator.getBuilder(),
                        random,
                      EmptyModelData.INSTANCE);
                if (!b.getState().getFluidState().isEmpty())
                {
                    Minecraft.getInstance()
                        .getBlockRendererDispatcher()
                        .func_228794_a_(b.getPos(), blockAccess, tessellator.getBuilder(), b.getState().getFluidState());
                }
            });
        tessellator.finishBuilding();
    }

    /**
     * Draws an instance of the blueprint at the given position, with the given rotation, and mirroring.
     *
     * @param rotation      The rotation.
     * @param mirror        The mirroring.
     * @param drawingOffset The drawing offset.
     */
    public void draw(final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset)
    {
        // Handle things like mirror, rotation and offset.
        preBlueprintDraw(rotation, mirror, drawingOffset, primaryBlockOffset);


        Minecraft.getInstance().gameRenderer.func_228384_l_().disableLightmap();

        RenderHelper.func_227780_a_();
        final World previous = TileEntityRendererDispatcher.instance.world;
        TileEntityRendererDispatcher.instance.setWorld(blockAccess);
        TileEntityRendererDispatcher.instance.preDrawBatch();
        // Draw tile entities.
        tileEntities.forEach(tileEntity -> {
            TileEntityRendererDispatcher.instance.render(tileEntity, tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ(), 1f);
            Minecraft.getInstance().gameRenderer.func_228384_l_().disableLightmap();
            RenderSystem.disableFog();
        });
        TileEntityRendererDispatcher.instance.drawBatch();
        TileEntityRendererDispatcher.instance.setWorld(previous);
        RenderHelper.disableStandardItemLighting();

        // Draw entities
        entities.forEach(entity -> {
            Minecraft.getInstance().getRenderManager().renderEntity(entity, entity.getPositionVec().x, entity.getPositionVec().y, entity.getPositionVec().z, entity.rotationYaw, 0, true);
            Minecraft.getInstance().gameRenderer.func_228384_l_().disableLightmap();
            RenderSystem.disableFog();
        });

        // Draw normal blocks.
        tessellator.draw();

        postBlueprintDraw();
    }

    private static void preBlueprintDraw(final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset, final BlockPos inBlueprintOffset)
    {
        Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        RenderSystem.pushMatrix();
        RenderSystem.translated(drawingOffset.x, drawingOffset.y, drawingOffset.z);

        final BlockPos rotateInBlueprintOffset = inBlueprintOffset.rotate(rotation);
        RenderSystem.translated(-rotateInBlueprintOffset.getX(), -rotateInBlueprintOffset.getY(), -rotateInBlueprintOffset.getZ());

        RenderUtil.applyRotationToYAxis(rotation);
        RenderUtil.applyMirror(mirror, inBlueprintOffset);

        RenderSystem.scaled(HALF_PERCENT_SHRINK, HALF_PERCENT_SHRINK, HALF_PERCENT_SHRINK);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.clearCurrentColor();
        RenderSystem.pushMatrix();
    }

    private static void postBlueprintDraw()
    {
        RenderSystem.popMatrix();
        RenderSystem.clearCurrentColor();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
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
