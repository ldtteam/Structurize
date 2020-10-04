package com.ldtteam.structurize.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;

/**
 * Our own fluid renderer.
 */
public class FluidRenderer
{
    public static boolean render(final ILightReader blockAccess, final BlockPos pos, final IVertexBuilder iVertexBuilder, final IFluidState fluidState)
    {
        boolean isLava = fluidState.isTagged(FluidTags.LAVA);
        TextureAtlasSprite[] atextureatlassprite = net.minecraftforge.client.ForgeHooksClient.getFluidSprites(blockAccess, pos, fluidState);
        int color = fluidState.getFluid().getAttributes().getColor(blockAccess, pos);
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        boolean isWaterUp = !isAdjacentFluidSameAs(blockAccess, pos, Direction.UP, fluidState);
        boolean isWaterDown = !isAdjacentFluidSameAs(blockAccess, pos, Direction.DOWN, fluidState) && !needsSideRendering(blockAccess, pos, Direction.DOWN, 0.8888889F);
        boolean isWaterNorth = !isAdjacentFluidSameAs(blockAccess, pos, Direction.NORTH, fluidState);
        boolean isWaterSouth = !isAdjacentFluidSameAs(blockAccess, pos, Direction.SOUTH, fluidState);
        boolean isWaterWest = !isAdjacentFluidSameAs(blockAccess, pos, Direction.WEST, fluidState);
        boolean isWaterEast = !isAdjacentFluidSameAs(blockAccess, pos, Direction.EAST, fluidState);
        if (!isWaterUp && !isWaterDown && !isWaterEast && !isWaterWest && !isWaterNorth && !isWaterSouth)
        {
            return false;
        }
        else
        {
            boolean needDepthRendering = false;
            float fluidHeight = FluidRenderer.getFluidHeight(blockAccess, pos, fluidState.getFluid());
            float fluidHeightS = FluidRenderer.getFluidHeight(blockAccess, pos.south(), fluidState.getFluid());
            float fluidHeightSE = FluidRenderer.getFluidHeight(blockAccess, pos.east().south(), fluidState.getFluid());
            float fluidHeightE = FluidRenderer.getFluidHeight(blockAccess, pos.east(), fluidState.getFluid());
            float posX = pos.getX();
            float posY = pos.getY();
            float posZ = pos.getZ();
            float waterDepth = isWaterDown ? 0.001F : 0.0F;
            if (isWaterUp && !needsSideRendering(blockAccess, pos, Direction.UP, Math.min(Math.min(fluidHeight, fluidHeightS), Math.min(fluidHeightSE, fluidHeightE))))
            {
                needDepthRendering = true;
                fluidHeight -= 0.001F;
                fluidHeightS -= 0.001F;
                fluidHeightSE -= 0.001F;
                fluidHeightE -= 0.001F;
                Vec3d vec3d = fluidState.getFlow(blockAccess, pos);
                float f13;
                float f14;
                float f15;
                float f16;
                float f17;
                float f18;
                float f19;
                float f20;
                if (vec3d.x == 0.0D && vec3d.z == 0.0D)
                {
                    TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];
                    f13 = textureatlassprite1.getInterpolatedU(0.0D);
                    f17 = textureatlassprite1.getInterpolatedV(0.0D);
                    f14 = f13;
                    f18 = textureatlassprite1.getInterpolatedV(16.0D);
                    f15 = textureatlassprite1.getInterpolatedU(16.0D);
                    f19 = f18;
                    f16 = f15;
                    f20 = f17;
                }
                else
                {
                    TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
                    float f21 = (float) MathHelper.atan2(vec3d.z, vec3d.x) - ((float) Math.PI / 2F);
                    float f22 = MathHelper.sin(f21) * 0.25F;
                    float f23 = MathHelper.cos(f21) * 0.25F;
                    f13 = textureatlassprite.getInterpolatedU((8.0F + (-f23 - f22) * 16.0F));
                    f17 = textureatlassprite.getInterpolatedV((8.0F + (-f23 + f22) * 16.0F));
                    f14 = textureatlassprite.getInterpolatedU((8.0F + (-f23 + f22) * 16.0F));
                    f18 = textureatlassprite.getInterpolatedV((8.0F + (f23 + f22) * 16.0F));
                    f15 = textureatlassprite.getInterpolatedU((8.0F + (f23 + f22) * 16.0F));
                    f19 = textureatlassprite.getInterpolatedV((8.0F + (f23 - f22) * 16.0F));
                    f16 = textureatlassprite.getInterpolatedU((8.0F + (f23 - f22) * 16.0F));
                    f20 = textureatlassprite.getInterpolatedV((8.0F + (-f23 - f22) * 16.0F));
                }

                float f43 = (f13 + f14 + f15 + f16) / 4.0F;
                float f44 = (f17 + f18 + f19 + f20) / 4.0F;
                float f45 = (float) atextureatlassprite[0].getWidth() / (atextureatlassprite[0].getMaxU() - atextureatlassprite[0].getMinU());
                float f46 = (float) atextureatlassprite[0].getHeight() / (atextureatlassprite[0].getMaxV() - atextureatlassprite[0].getMinV());
                float f47 = 4.0F / Math.max(f46, f45);
                f13 = MathHelper.lerp(f47, f13, f43);
                f14 = MathHelper.lerp(f47, f14, f43);
                f15 = MathHelper.lerp(f47, f15, f43);
                f16 = MathHelper.lerp(f47, f16, f43);
                f17 = MathHelper.lerp(f47, f17, f44);
                f18 = MathHelper.lerp(f47, f18, f44);
                f19 = MathHelper.lerp(f47, f19, f44);
                f20 = MathHelper.lerp(f47, f20, f44);
                int j = FluidRenderer.getLight(blockAccess, pos);
                float f25 = 1.0F * red;
                float f26 = 1.0F * green;
                float f27 = 1.0F * blue;
                FluidRenderer.vertex(iVertexBuilder, posX + 0.0f, posY + fluidHeight, posZ + 0.0f, f25, f26, f27, alpha, f13, f17, j);
                FluidRenderer.vertex(iVertexBuilder, posX + 0.0f, posY + fluidHeightS, posZ + 1.0f, f25, f26, f27, alpha, f14, f18, j);
                FluidRenderer.vertex(iVertexBuilder, posX + 1.0f, posY + fluidHeightSE, posZ + 1.0f, f25, f26, f27, alpha, f15, f19, j);
                FluidRenderer.vertex(iVertexBuilder, posX + 1.0f, posY + fluidHeightE, posZ + 0.0f, f25, f26, f27, alpha, f16, f20, j);
                if (fluidState.shouldRenderSides(blockAccess, pos.up()))
                {
                    FluidRenderer.vertex(iVertexBuilder, posX + 0.0f, posY + fluidHeight, posZ + 0.0f, f25, f26, f27, alpha, f13, f17, j);
                    FluidRenderer.vertex(iVertexBuilder, posX + 1.0f, posY + fluidHeightE, posZ + 0.0f, f25, f26, f27, alpha, f16, f20, j);
                    FluidRenderer.vertex(iVertexBuilder, posX + 1.0f, posY + fluidHeightSE, posZ + 1.0f, f25, f26, f27, alpha, f15, f19, j);
                    FluidRenderer.vertex(iVertexBuilder, posX + 0.0f, posY + fluidHeightS, posZ + 1.0f, f25, f26, f27, alpha, f14, f18, j);
                }
            }

            if (isWaterDown)
            {
                float f34 = atextureatlassprite[0].getMinU();
                float f35 = atextureatlassprite[0].getMaxU();
                float f37 = atextureatlassprite[0].getMinV();
                float f39 = atextureatlassprite[0].getMaxV();
                int i1 = FluidRenderer.getLight(blockAccess, pos.down());
                float f40 = 0.5F * red;
                float f41 = 0.5F * green;
                float f42 = 0.5F * blue;
                FluidRenderer.vertex(iVertexBuilder, posX, posY + waterDepth, posZ + 1.0f, f40, f41, f42, alpha, f34, f39, i1);
                FluidRenderer.vertex(iVertexBuilder, posX, posY + waterDepth, posZ, f40, f41, f42, alpha, f34, f37, i1);
                FluidRenderer.vertex(iVertexBuilder, posX + 1.0f, posY + waterDepth, posZ, f40, f41, f42, alpha, f35, f37, i1);
                FluidRenderer.vertex(iVertexBuilder, posX + 1.0f, posY + waterDepth, posZ + 1.0f, f40, f41, f42, alpha, f35, f39, i1);
                needDepthRendering = true;
            }

            for (int l = 0; l < 4; ++l)
            {
                float height;
                float fHeight;
                float x;
                float z;
                float offsetX;
                float offsetZ;
                Direction direction;
                boolean connectingWater;
                if (l == 0)
                {
                    height = fluidHeight;
                    fHeight = fluidHeightE;
                    x = posX;
                    offsetX = posX + 1.0f;
                    z = posZ + 0.001F;
                    offsetZ = posZ + 0.001F;
                    direction = Direction.NORTH;
                    connectingWater = isWaterNorth;
                }
                else if (l == 1)
                {
                    height = fluidHeightSE;
                    fHeight = fluidHeightS;
                    x = posX + 1.0f;
                    offsetX = posX;
                    z = posZ + 1.0f - 0.001F;
                    offsetZ = posZ + 1.0f - 0.001F;
                    direction = Direction.SOUTH;
                    connectingWater = isWaterSouth;
                }
                else if (l == 2)
                {
                    height = fluidHeightS;
                    fHeight = fluidHeight;
                    x = posX + 0.001F;
                    offsetX = posX + 0.001F;
                    z = posZ + 1.0f;
                    offsetZ = posZ;
                    direction = Direction.WEST;
                    connectingWater = isWaterWest;
                }
                else
                {
                    height = fluidHeightE;
                    fHeight = fluidHeightSE;
                    x = posX + 1.0f - 0.001F;
                    offsetX = posX + 1.0f - 0.001F;
                    z = posZ;
                    offsetZ = posZ + 1.0f;
                    direction = Direction.EAST;
                    connectingWater = isWaterEast;
                }

                if (connectingWater && !needsSideRendering(blockAccess, pos, direction, Math.max(height, fHeight)))
                {
                    needDepthRendering = true;
                    BlockPos blockpos = pos.offset(direction);
                    TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
                    if (!isLava)
                    {
                        Block block = blockAccess.getBlockState(blockpos).getBlock();
                        if (block == Blocks.GLASS || block instanceof StainedGlassBlock)
                        {
                            textureatlassprite2 = ModelBakery.LOCATION_WATER_OVERLAY.getSprite();
                        }
                    }

                    float f48 = textureatlassprite2.getInterpolatedU(0.0D);
                    float f49 = textureatlassprite2.getInterpolatedU(8.0D);
                    float f50 = textureatlassprite2.getInterpolatedV(((1.0F - height) * 16.0F * 0.5F));
                    float f28 = textureatlassprite2.getInterpolatedV(((1.0F - fHeight) * 16.0F * 0.5F));
                    float f29 = textureatlassprite2.getInterpolatedV(8.0D);
                    int k = FluidRenderer.getLight(blockAccess, blockpos);
                    float f30 = l < 2 ? 0.8F : 0.6F;
                    float r = 1.0F * f30 * red;
                    float g = 1.0F * f30 * green;
                    float b = 1.0F * f30 * blue;
                    FluidRenderer.vertex(iVertexBuilder, x, posY + height, z, r, g, b, alpha, f48, f50, k);
                    FluidRenderer.vertex(iVertexBuilder, offsetX, posY + fHeight, offsetZ, r, g, b, alpha, f49, f28, k);
                    FluidRenderer.vertex(iVertexBuilder, offsetX, posY + waterDepth, offsetZ, r, g, b, alpha, f49, f29, k);
                    FluidRenderer.vertex(iVertexBuilder, x, posY + waterDepth, z, r, g, b, alpha, f48, f29, k);
                    if (textureatlassprite2 != ModelBakery.LOCATION_WATER_OVERLAY.getSprite())
                    {
                        FluidRenderer.vertex(iVertexBuilder, x, posY + waterDepth, z, r, g, b, alpha, f48, f29, k);
                        FluidRenderer.vertex(iVertexBuilder, offsetX, posY + waterDepth, offsetZ, r, g, b, alpha, f49, f29, k);
                        FluidRenderer.vertex(iVertexBuilder, offsetX, posY + fHeight, offsetZ, r, g, b, alpha, f49, f28, k);
                        FluidRenderer.vertex(iVertexBuilder, x, posY + height, z, r, g, b, alpha, f48, f50, k);
                    }
                }
            }

            return needDepthRendering;
        }
    }

    private static boolean isAdjacentFluidSameAs(IBlockReader blockAccess, BlockPos pos, Direction direction, IFluidState fluid)
    {
        BlockPos blockpos = pos.offset(direction);
        IFluidState ifluidstate = blockAccess.getFluidState(blockpos);
        return ifluidstate.getFluid().isEquivalentTo(fluid.getFluid());
    }

    private static void vertex(
        IVertexBuilder iVertexBuilder,
        float x,
        float y,
        float z,
        float red,
        float green,
        float blue,
        float alpha,
        float textX,
        float textY,
        int light)
    {
        iVertexBuilder.pos(x, y, z).color(red, green, blue, alpha).tex(textX, textY).lightmap(light).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    private static int getLight(ILightReader p_228795_1_, BlockPos p_228795_2_)
    {
        int i = WorldRenderer.getCombinedLight(p_228795_1_, p_228795_2_);
        int j = WorldRenderer.getCombinedLight(p_228795_1_, p_228795_2_.up());
        int k = i & 255;
        int l = j & 255;
        int i1 = i >> 16 & 255;
        int j1 = j >> 16 & 255;
        return (Math.max(k, l)) | (Math.max(i1, j1)) << 16;
    }

    private static boolean needsSideRendering(IBlockReader blockAccess, BlockPos pos, Direction direction, float fluidHeight)
    {
        BlockPos blockpos = pos.offset(direction);
        BlockState blockstate = blockAccess.getBlockState(blockpos);
        if (blockstate.isSolid())
        {
            VoxelShape voxelshape = VoxelShapes.create(0.0D, 0.0D, 0.0D, 1.0D, fluidHeight, 1.0D);
            VoxelShape voxelshape1 = blockstate.getRenderShape(blockAccess, blockpos);
            return VoxelShapes.isCubeSideCovered(voxelshape, voxelshape1, direction);
        }
        else
        {
            return false;
        }
    }

    private static float getFluidHeight(IBlockReader blockAccess, BlockPos pos, Fluid fluid)
    {
        int occurances = 0;
        float totalHeight = 0.0F;

        for (int j = 0; j < 4; ++j)
        {
            BlockPos blockpos = pos.add(-(j & 1), 0, -(j >> 1 & 1));
            if (blockAccess.getFluidState(blockpos.up()).getFluid().isEquivalentTo(fluid))
            {
                return 1.0F;
            }

            IFluidState ifluidstate = blockAccess.getFluidState(blockpos);
            if (ifluidstate.getFluid().isEquivalentTo(fluid))
            {
                float f1 = ifluidstate.getActualHeight(blockAccess, blockpos);
                if (f1 >= 0.8F)
                {
                    totalHeight += f1 * 10.0F;
                    occurances += 10;
                }
                else
                {
                    totalHeight += f1;
                    ++occurances;
                }
            }
            else if (!blockAccess.getBlockState(blockpos).getMaterial().isSolid())
            {
                ++occurances;
            }
        }

        return occurances > 0 ? totalHeight / (float) occurances : 0f;
    }
}
