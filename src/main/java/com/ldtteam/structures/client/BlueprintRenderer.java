package com.ldtteam.structures.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structures.lib.RenderUtil;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.util.BlockInfo;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.loot.IRandomRange;

import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;
import java.util.Map;
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
    private final Map<RenderType, BlueprintTessellator> blueprintTessellatorMap;
    private boolean isEmpty = true;
    private final List<RenderType> startedLayers = Lists.newLinkedList();
    private final BlockPos                  primaryBlockOffset;

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
        this.blueprintTessellatorMap = Maps.newHashMap();
        this.primaryBlockOffset = primaryBlockOffset;

        this.setup();
    }

    /**
     * Sets up the renders VBO
     */
    private void setup()
    {
        final MatrixStack runningStack = new MatrixStack();

        final ActiveRenderInfo activeRenderInfo = Minecraft.getInstance().getRenderManager().info;
        final Vec3d viewPosition = activeRenderInfo.getProjectedView();

        MatrixStack matrixstack = new MatrixStack();
        BlockModelRenderer.enableCache();
        Random random = new Random();
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

        for(BlockInfo blockInfo : blockAccess.getBlueprint()
                                                 .getBlockInfoAsList()) {
            final BlockPos blockpos2 = blockInfo.getPos();
            BlockState blockstate = blockInfo.getState();
            Block block = blockstate.getBlock();

            IFluidState ifluidstate = blockAccess.getFluidState(blockpos2);
            net.minecraftforge.client.model.data.IModelData modelData = EmptyModelData.INSTANCE;
            for (RenderType rendertype : RenderType.getBlockLayers()) {
                net.minecraftforge.client.ForgeHooksClient.setRenderLayer(rendertype);
                if (!ifluidstate.isEmpty() && RenderTypeLookup.canRenderInLayer(ifluidstate, rendertype)) {
                    BlueprintTessellator tessellator = this.blueprintTessellatorMap.computeIfAbsent(rendertype, (r)-> new BlueprintTessellator());
                    BufferBuilder bufferbuilder = tessellator.getBuilder();
                    if (startedLayers.add(rendertype)) {
                        tessellator.startBuilding();
                    }

                    if (blockrendererdispatcher.renderFluid(blockpos2, blockAccess, bufferbuilder, ifluidstate)) {
                        isEmpty = false;
                        startedLayers.add(rendertype);
                    }
                }

                if (blockstate.getRenderType() != BlockRenderType.INVISIBLE && RenderTypeLookup.canRenderInLayer(blockstate, rendertype)) {
                    BlueprintTessellator tessellator = this.blueprintTessellatorMap.computeIfAbsent(rendertype, (r)-> new BlueprintTessellator());
                    BufferBuilder bufferbuilder = tessellator.getBuilder();
                    if (startedLayers.add(rendertype)) {
                        tessellator.startBuilding();
                    }

                    matrixstack.push();
                    matrixstack.translate((double)(blockpos2.getX() & 15), (double)(blockpos2.getY() & 15), (double)(blockpos2.getZ() & 15));
                    if (blockrendererdispatcher.renderModel(blockstate, blockpos2, blockAccess, matrixstack, bufferbuilder, true, random, modelData)) {
                        isEmpty = false;
                        startedLayers.add(rendertype);
                    }

                    matrixstack.pop();
                }
            }
        }
        net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);

        if (startedLayers.contains(RenderType.getTranslucent())) {
            BufferBuilder bufferbuilder1 = blueprintTessellatorMap.get(RenderType.getTranslucent()).getBuilder();
            bufferbuilder1.sortVertexData(0,0,0);
        }

        startedLayers.stream().map(this.blueprintTessellatorMap::get).forEach(BlueprintTessellator::finishBuilding);
        BlockModelRenderer.disableCache();
    }

    /**
     * Draws an instance of the blueprint at the given position, with the given rotation, and mirroring.
     *
     * @param rotation      The rotation.
     * @param mirror        The mirroring.
     * @param drawingOffset The drawing offset.
     */
    public void draw(final Rotation rotation, final Mirror mirror, final Vec3d drawingOffset, final MatrixStack matrixStack, final float partialTicks)
    {
        // Handle things like mirror, rotation and offset.
        preBlueprintDraw(rotation, mirror, drawingOffset, primaryBlockOffset);

        //Minecraft.getInstance().gameRenderer.func_228384_l_().disableLightmap();

        //RenderHelper.func_227780_a_();
        //final World previous = TileEntityRendererDispatcher.instance.world;
        //TileEntityRendererDispatcher.instance.func_217665_a(previous,Minecraft.getInstance().textureManager, Minecraft.getInstance().fontRenderer, Minecraft.getInstance().getRenderManager().info, Minecraft.getInstance().objectMouseOver);

        // Draw tile entities.

        /*tileEntities.forEach(tileEntity -> {
            //float, matrixstack, rendertypebuffer
            TileEntityRendererDispatcher.instance.func_228850_a_(tileEntity, 1f, matrixStack, Minecraft.getInstance().func_228019_au_().func_228487_b_());
            Minecraft.getInstance().gameRenderer.func_228384_l_().disableLightmap();
            RenderSystem.disableFog();
        });
        RenderHelper.disableStandardItemLighting();*/

        final Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();

        // Draw entities
        entities.forEach(entity -> {
            double d0 = MathHelper.lerp((double)partialTicks, entity.lastTickPosX, entity.func_226277_ct_());
            double d1 = MathHelper.lerp((double)partialTicks, entity.lastTickPosY, entity.func_226278_cu_());
            double d2 = MathHelper.lerp((double)partialTicks, entity.lastTickPosZ, entity.func_226281_cx_());
            float f = MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw);
            Minecraft.getInstance().getRenderManager().func_229084_a_(entity, d0 - drawingOffset.x , d1 - drawingOffset.y, d2 - drawingOffset.z, f, partialTicks, matrixStack, Minecraft.getInstance().func_228019_au_().func_228487_b_(), Minecraft.getInstance().getRenderManager().func_229085_a_(entity, partialTicks));

            RenderSystem.disableFog();
        });

        // Draw normal blocks.
        startedLayers.forEach(layer -> {
            this.blueprintTessellatorMap.get(layer).draw(matrixStack.peek().getModel());
        });

        postBlueprintDraw();
    }

    private static void preBlueprintDraw(final Rotation rotation, final Mirror mirror, final Vec3d drawingOffset, final BlockPos inBlueprintOffset)
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

    public BlockPos getPrimaryBlockOffset()
    {
        return primaryBlockOffset;
    }

}
