package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
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
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

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
            // mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0);
            renderItemModelIntoGUI(itemStack,
                ms,
                Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, Minecraft.getInstance().player));
            renderGuiItemDecorations( ms, font, itemStack);
            // RenderSystem.popMatrix();

            ms.popPose();
        }
    }

    private void renderGuiItemDecorations(
      final MatrixStack matrixstack,
      final FontRenderer fontRenderer,
      final ItemStack stack)
    {
        if (!stack.isEmpty())
        {
            if (stack.getCount() != 1)
            {
                String s = String.valueOf(stack.getCount());
                matrixstack.translate(0.0D, 0.0D, mc.getItemRenderer().blitOffset + 200.0F);
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
                int i = Math.round(13.0F - (float) health * 13.0F);
                int j = stack.getItem().getRGBDurabilityForDisplay(stack);
                fillRect(bufferbuilder,  2, 13, 13, 2, 0, 0, 0, 255);
                fillRect(bufferbuilder, 2, 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableAlphaTest();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            ClientPlayerEntity clientplayerentity = Minecraft.getInstance().player;
            float f3 = clientplayerentity == null ? 0.0F : clientplayerentity.getCooldowns().getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
            if (f3 > 0.0F)
            {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tessellator tessellator1 = Tessellator.getInstance();
                BufferBuilder bufferbuilder1 = tessellator1.getBuilder();
                fillRect(bufferbuilder1, 0, MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
        }
    }

    private void fillRect(BufferBuilder builder, int x1, int y1, int x2, int y2, int r, int g, int b, int a)
    {
        builder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        builder.vertex((x1 + 0), (y1 + 0), 0.0D).color(r, g, b, a).endVertex();
        builder.vertex((x1 + 0), (y1 + y2), 0.0D).color(r, g, b, a).endVertex();
        builder.vertex((x1 + x2), (y1 + y2), 0.0D).color(r, g, b, a).endVertex();
        builder.vertex((x1 + x2), (y1 + 0), 0.0D).color(r, g, b, a).endVertex();
        Tessellator.getInstance().end();
    }

    // matrixstack version of mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0); with modified lighting to match the vanilla result as much as possible
    // TODO: remove when fixed upstream (vanilla)
    private static final Vector3f DEF_LIGHT = Util.make(new Vector3f(0.55F, 0.3f, -0.8F), Vector3f::normalize);
    private static final Vector3f DIF_LIGHT = Util.make(new Vector3f(-0.8F, 0.3f, 0.55F), Vector3f::normalize);
    private void renderItemModelIntoGUI(ItemStack stack, MatrixStack matrixStack, IBakedModel bakedmodel)
    {

        matrixStack.pushPose();
        Minecraft.getInstance().getTextureManager().bind(AtlasTexture.LOCATION_BLOCKS);
        Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS).setBlurMipmap(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.translate(8.0F, 8.0F, 150.0F);
        matrixStack.scale(1.0F, -1.0F, 1.0F);
        matrixStack.scale(16.0F, 16.0F, 16.0F);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        if (!bakedmodel.usesBlockLight())
        {
            RenderSystem.setupGuiFlatDiffuseLighting(DEF_LIGHT, DIF_LIGHT);
        }
        else
        {
            RenderSystem.setupGui3DDiffuseLighting(DEF_LIGHT, DIF_LIGHT);
        }

        Minecraft.getInstance()
            .getItemRenderer()
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
