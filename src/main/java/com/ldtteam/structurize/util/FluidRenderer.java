package com.ldtteam.structurize.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * Our own fluid renderer.
 */
public class FluidRenderer
{
    public static boolean render(final BlockAndTintGetter blockAccess,
        final BlockPos pos,
        final VertexConsumer iVertexBuilder,
        final FluidState fluidState)
    {
        boolean isLava = fluidState.is(FluidTags.LAVA);
        TextureAtlasSprite[] atextureatlassprite = net.minecraftforge.client.ForgeHooksClient.getFluidSprites(blockAccess, pos, fluidState);
        int color = IClientFluidTypeExtensions.of(fluidState).getTintColor(fluidState, blockAccess, pos);
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
            float fluidHeight = FluidRenderer.getFluidHeight(blockAccess, pos, fluidState.getType());
            float fluidHeightS = FluidRenderer.getFluidHeight(blockAccess, pos.south(), fluidState.getType());
            float fluidHeightSE = FluidRenderer.getFluidHeight(blockAccess, pos.east().south(), fluidState.getType());
            float fluidHeightE = FluidRenderer.getFluidHeight(blockAccess, pos.east(), fluidState.getType());
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
                Vec3 vec3d = fluidState.getFlow(blockAccess, pos);
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
                    f13 = textureatlassprite1.getU(0.0D);
                    f17 = textureatlassprite1.getV(0.0D);
                    f14 = f13;
                    f18 = textureatlassprite1.getV(16.0D);
                    f15 = textureatlassprite1.getU(16.0D);
                    f19 = f18;
                    f16 = f15;
                    f20 = f17;
                }
                else
                {
                    TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
                    float f21 = (float) Mth.atan2(vec3d.z, vec3d.x) - ((float) Math.PI / 2F);
                    float f22 = Mth.sin(f21) * 0.25F;
                    float f23 = Mth.cos(f21) * 0.25F;
                    f13 = textureatlassprite.getU((8.0F + (-f23 - f22) * 16.0F));
                    f17 = textureatlassprite.getV((8.0F + (-f23 + f22) * 16.0F));
                    f14 = textureatlassprite.getU((8.0F + (-f23 + f22) * 16.0F));
                    f18 = textureatlassprite.getV((8.0F + (f23 + f22) * 16.0F));
                    f15 = textureatlassprite.getU((8.0F + (f23 + f22) * 16.0F));
                    f19 = textureatlassprite.getV((8.0F + (f23 - f22) * 16.0F));
                    f16 = textureatlassprite.getU((8.0F + (f23 - f22) * 16.0F));
                    f20 = textureatlassprite.getV((8.0F + (-f23 - f22) * 16.0F));
                }

                float f43 = (f13 + f14 + f15 + f16) / 4.0F;
                float f44 = (f17 + f18 + f19 + f20) / 4.0F;
                float f45 = (float) atextureatlassprite[0].contents().width() / (atextureatlassprite[0].getU1() - atextureatlassprite[0].getU0());
                float f46 = (float) atextureatlassprite[0].contents().height() / (atextureatlassprite[0].getV1() - atextureatlassprite[0].getV0());
                float f47 = 4.0F / Math.max(f46, f45);
                f13 = Mth.lerp(f47, f13, f43);
                f14 = Mth.lerp(f47, f14, f43);
                f15 = Mth.lerp(f47, f15, f43);
                f16 = Mth.lerp(f47, f16, f43);
                f17 = Mth.lerp(f47, f17, f44);
                f18 = Mth.lerp(f47, f18, f44);
                f19 = Mth.lerp(f47, f19, f44);
                f20 = Mth.lerp(f47, f20, f44);
                int j = FluidRenderer.getLight(blockAccess, pos);
                float f25 = 1.0F * red;
                float f26 = 1.0F * green;
                float f27 = 1.0F * blue;
                FluidRenderer.vertex(iVertexBuilder, posX + 0.0f, posY + fluidHeight, posZ + 0.0f, f25, f26, f27, alpha, f13, f17, j);
                FluidRenderer.vertex(iVertexBuilder, posX + 0.0f, posY + fluidHeightS, posZ + 1.0f, f25, f26, f27, alpha, f14, f18, j);
                FluidRenderer.vertex(iVertexBuilder, posX + 1.0f, posY + fluidHeightSE, posZ + 1.0f, f25, f26, f27, alpha, f15, f19, j);
                FluidRenderer.vertex(iVertexBuilder, posX + 1.0f, posY + fluidHeightE, posZ + 0.0f, f25, f26, f27, alpha, f16, f20, j);
                if (fluidState.shouldRenderBackwardUpFace(blockAccess, pos.above()))
                {
                    FluidRenderer.vertex(iVertexBuilder, posX + 0.0f, posY + fluidHeight, posZ + 0.0f, f25, f26, f27, alpha, f13, f17, j);
                    FluidRenderer.vertex(iVertexBuilder, posX + 1.0f, posY + fluidHeightE, posZ + 0.0f, f25, f26, f27, alpha, f16, f20, j);
                    FluidRenderer.vertex(iVertexBuilder, posX + 1.0f, posY + fluidHeightSE, posZ + 1.0f, f25, f26, f27, alpha, f15, f19, j);
                    FluidRenderer.vertex(iVertexBuilder, posX + 0.0f, posY + fluidHeightS, posZ + 1.0f, f25, f26, f27, alpha, f14, f18, j);
                }
            }

            if (isWaterDown)
            {
                float f34 = atextureatlassprite[0].getU0();
                float f35 = atextureatlassprite[0].getU1();
                float f37 = atextureatlassprite[0].getV0();
                float f39 = atextureatlassprite[0].getV1();
                int i1 = FluidRenderer.getLight(blockAccess, pos.below());
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
                    BlockPos blockpos = pos.relative(direction);
                    TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
                    if (!isLava)
                    {
                        Block block = blockAccess.getBlockState(blockpos).getBlock();
                        if (block == Blocks.GLASS || block instanceof StainedGlassBlock)
                        {
                            textureatlassprite2 = ModelBakery.WATER_OVERLAY.sprite();
                        }
                    }

                    float f48 = textureatlassprite2.getU(0.0D);
                    float f49 = textureatlassprite2.getU(8.0D);
                    float f50 = textureatlassprite2.getV(((1.0F - height) * 16.0F * 0.5F));
                    float f28 = textureatlassprite2.getV(((1.0F - fHeight) * 16.0F * 0.5F));
                    float f29 = textureatlassprite2.getV(8.0D);
                    int k = FluidRenderer.getLight(blockAccess, blockpos);
                    float f30 = l < 2 ? 0.8F : 0.6F;
                    float r = 1.0F * f30 * red;
                    float g = 1.0F * f30 * green;
                    float b = 1.0F * f30 * blue;
                    FluidRenderer.vertex(iVertexBuilder, x, posY + height, z, r, g, b, alpha, f48, f50, k);
                    FluidRenderer.vertex(iVertexBuilder, offsetX, posY + fHeight, offsetZ, r, g, b, alpha, f49, f28, k);
                    FluidRenderer.vertex(iVertexBuilder, offsetX, posY + waterDepth, offsetZ, r, g, b, alpha, f49, f29, k);
                    FluidRenderer.vertex(iVertexBuilder, x, posY + waterDepth, z, r, g, b, alpha, f48, f29, k);
                    if (textureatlassprite2 != ModelBakery.WATER_OVERLAY.sprite())
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

    private static boolean isAdjacentFluidSameAs(BlockGetter blockAccess, BlockPos pos, Direction direction, FluidState fluid)
    {
        BlockPos blockpos = pos.relative(direction);
        FluidState ifluidstate = blockAccess.getFluidState(blockpos);
        return ifluidstate.getType().isSame(fluid.getType());
    }

    private static void vertex(
        VertexConsumer iVertexBuilder,
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
        iVertexBuilder.vertex(x, y, z).color(red, green, blue, alpha).uv(textX, textY).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    private static int getLight(BlockAndTintGetter p_228795_1_, BlockPos p_228795_2_)
    {
        int i = LevelRenderer.getLightColor(p_228795_1_, p_228795_2_);
        int j = LevelRenderer.getLightColor(p_228795_1_, p_228795_2_.above());
        int k = i & 255;
        int l = j & 255;
        int i1 = i >> 16 & 255;
        int j1 = j >> 16 & 255;
        return (Math.max(k, l)) | (Math.max(i1, j1)) << 16;
    }

    private static boolean needsSideRendering(BlockGetter blockAccess, BlockPos pos, Direction direction, float fluidHeight)
    {
        BlockPos blockpos = pos.relative(direction);
        BlockState blockstate = blockAccess.getBlockState(blockpos);
        if (blockstate.canOcclude())
        {
            VoxelShape voxelshape = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, fluidHeight, 1.0D);
            VoxelShape voxelshape1 = blockstate.getBlockSupportShape(blockAccess, blockpos);
            return Shapes.blockOccudes(voxelshape, voxelshape1, direction);
        }
        else
        {
            return false;
        }
    }

    private static float getFluidHeight(BlockGetter blockAccess, BlockPos pos, Fluid fluid)
    {
        int occurances = 0;
        float totalHeight = 0.0F;

        for (int j = 0; j < 4; ++j)
        {
            BlockPos blockpos = pos.offset(-(j & 1), 0, -(j >> 1 & 1));
            if (blockAccess.getFluidState(blockpos.above()).getType().isSame(fluid))
            {
                return 1.0F;
            }

            FluidState ifluidstate = blockAccess.getFluidState(blockpos);
            if (ifluidstate.getType().isSame(fluid))
            {
                float f1 = ifluidstate.getHeight(blockAccess, blockpos);
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
