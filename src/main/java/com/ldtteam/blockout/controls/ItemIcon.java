package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Class of itemIcons in our GUIs.
 */
public class ItemIcon extends Pane
{
    private static final float DEFAULT_ITEMSTACK_SIZE = 16f;

    /**
     * ItemStack represented in the itemIcon.
     */
    private ItemStack itemStack;

    /**
     * Standard constructor instantiating the itemIcon without any additional settings.
     */
    public ItemIcon()
    {
        super();
    }

    /**
     * Constructor instantiating the itemIcon with specified parameters.
     *
     * @param params the parameters.
     */
    public ItemIcon(final PaneParams params)
    {
        super(params);

        final String itemName = params.getString("item");
        if (itemName != null)
        {
            final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
            if (item != null)
            {
                setItem(item.getDefaultInstance());
            }
        }
    }

    /**
     * Set the item of the icon.
     *
     * @param itemStackIn the itemstack to set.
     */
    public void setItem(final ItemStack itemStackIn)
    {
        this.itemStack = itemStackIn;
        if (onHover instanceof Tooltip)
        {
            ((Tooltip) onHover).setTextOld(window.getScreen().getTooltipFromItem(itemStack));
        }
    }

    /**
     * Get the itemstack of the icon.
     *
     * @return the stack of it.
     */
    public ItemStack getItem()
    {
        return this.itemStack;
    }

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        if (itemStack != null && !itemStack.isEmpty())
        {
            ms.pushPose();
            ms.translate(x, y, 0.0f);
            ms.scale(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE, 1.0f);

            FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
            if (font == null)
            {
                font = mc.font;
            }

            // RenderSystem.pushMatrix();
            // RenderSystem.multMatrix(ms.getLast().getMatrix());
            // mc.getItemRenderer().renderAndDecorateItem(itemStack, 0, 0);
            renderItemModelIntoGUI(itemStack,
                ms,
                mc.getItemRenderer().getModel(itemStack, null, mc.player));
            renderGuiItemDecorations( ms, font, itemStack);
            // RenderSystem.popMatrix();

            ms.popPose();
        }
    }

    // VANILLA INLINE: matrix stack version
    private void renderGuiItemDecorations(
      final MatrixStack matrixstack,
      final FontRenderer fontRenderer,
      final ItemStack stack)
    {
        if (stack.getCount() != 1)
        {
            String s = String.valueOf(stack.getCount());
            matrixstack.pushPose();
            matrixstack.translate(0.0D, 0.0D, 100.0D); // z translate nerf from 200
            IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
            fontRenderer.drawInBatch(s,
              (float) (19 - 2 - fontRenderer.width(s)),
              (float) (6 + 3),
              16777215,
              true,
              matrixstack.last().pose(),
              irendertypebuffer$impl,
              false,
              0,
              15728880);
            irendertypebuffer$impl.endBatch();
            matrixstack.popPose();
        }

        if (stack.getItem().showDurabilityBar(stack))
        {
            RenderSystem.disableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();
            double health = stack.getItem().getDurabilityForDisplay(stack);
            int i = Math.round(13.0F - (float)health * 13.0F);
            int j = stack.getItem().getRGBDurabilityForDisplay(stack);
            fillRect(matrixstack, bufferbuilder, 2, 13, 13, 2, 0, 0, 0, 255);
            fillRect(matrixstack, bufferbuilder, 2, 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }

        float f3 = mc.player == null ? 0.0F : mc.player.getCooldowns().getCooldownPercent(stack.getItem(), mc.getFrameTime());
        if (f3 > 0.0F)
        {
            RenderSystem.disableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Tessellator tessellator1 = Tessellator.getInstance();
            BufferBuilder bufferbuilder1 = tessellator1.getBuilder();
            fillRect(matrixstack, bufferbuilder1, 0, MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }
    }

    // VANILLA INLINE: matrix stack version
    private void fillRect(MatrixStack matrixstack, BufferBuilder p_181565_1_, int p_181565_2_, int p_181565_3_, int p_181565_4_, int p_181565_5_, int p_181565_6_, int p_181565_7_, int p_181565_8_, int p_181565_9_)
    {
        p_181565_1_.begin(7, DefaultVertexFormats.POSITION_COLOR);
        final Matrix4f pose = matrixstack.last().pose();
        p_181565_1_.vertex(pose, (float)(p_181565_2_ + 0), (float)(p_181565_3_ + 0), 0.0f).color(p_181565_6_, p_181565_7_, p_181565_8_, p_181565_9_).endVertex();
        p_181565_1_.vertex(pose, (float)(p_181565_2_ + 0), (float)(p_181565_3_ + p_181565_5_), 0.0f).color(p_181565_6_, p_181565_7_, p_181565_8_, p_181565_9_).endVertex();
        p_181565_1_.vertex(pose, (float)(p_181565_2_ + p_181565_4_), (float)(p_181565_3_ + p_181565_5_), 0.0f).color(p_181565_6_, p_181565_7_, p_181565_8_, p_181565_9_).endVertex();
        p_181565_1_.vertex(pose, (float)(p_181565_2_ + p_181565_4_), (float)(p_181565_3_ + 0), 0.0f).color(p_181565_6_, p_181565_7_, p_181565_8_, p_181565_9_).endVertex();
        Tessellator.getInstance().end();
    }

    // VANILLA INLINE: matrixstack version of mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0); with modified lighting to match the vanilla result as much as possible
    // TODO: remove when fixed upstream (vanilla)
    private static final Vector3f DEF_LIGHT = Util.make(new Vector3f(0.55F, 0.3f, -0.8F), Vector3f::normalize);
    private static final Vector3f DIF_LIGHT = Util.make(new Vector3f(-0.8F, 0.3f, 0.55F), Vector3f::normalize);
    private void renderItemModelIntoGUI(ItemStack stack, MatrixStack matrixStack, IBakedModel bakedmodel)
    {

        matrixStack.pushPose();
        mc.getTextureManager().bind(AtlasTexture.LOCATION_BLOCKS);
        mc.getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS).setBlurMipmap(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.translate(8.0F, 8.0F, 0.0F);
        matrixStack.scale(1.0F, -1.0F, 1.0F);
        matrixStack.scale(16.0F, 16.0F, 16.0F);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = mc.renderBuffers().bufferSource();
        if (!bakedmodel.usesBlockLight())
        {
            RenderSystem.setupGuiFlatDiffuseLighting(DEF_LIGHT, DIF_LIGHT);
        }
        else
        {
            RenderSystem.setupGui3DDiffuseLighting(DEF_LIGHT, DIF_LIGHT);
        }

        mc.getItemRenderer()
            .render(stack,
                ItemCameraTransforms.TransformType.GUI,
                false,
                matrixStack,
                irendertypebuffer$impl,
                15728880,
                OverlayTexture.NO_OVERLAY,
                bakedmodel);
        irendertypebuffer$impl.endBatch();
        RenderSystem.enableDepthTest();
        RenderHelper.setupFor3DItems();

        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        matrixStack.popPose();
    }

    @Override
    public void onUpdate()
    {
        if (onHover == null && itemStack != null && !itemStack.isEmpty())
        {
            PaneBuilders.tooltipBuilder().hoverPane(this).build().setTextOld(window.getScreen().getTooltipFromItem(itemStack));
        }
    }
}
