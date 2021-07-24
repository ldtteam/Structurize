package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import com.mojang.math.Vector3f;
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
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        if (itemStack != null && !itemStack.isEmpty())
        {
            ms.pushPose();
            ms.translate(x, y, 0.0f);
            ms.scale(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE, 1.0f);

            Font font = mc.font;

            // RenderSystem.pushMatrix();
            // RenderSystem.multMatrix(ms.getLast().getMatrix());
            // mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0);
            renderItemModelIntoGUI(itemStack,
                ms,
                mc.getItemRenderer().getModel(itemStack, null, mc.player, 0));
            renderGuiItemDecorations( ms, font, itemStack);
            // RenderSystem.popMatrix();

            ms.popPose();
        }
    }

    private void renderGuiItemDecorations(
      final PoseStack matrixstack,
      final Font fontRenderer,
      final ItemStack stack)
    {
        if (stack.getCount() != 1)
        {
            String s = String.valueOf(stack.getCount());
            matrixstack.translate(0.0D, 0.0D, 200.0D);
            MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
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
            RenderSystem.disableBlend();
            double health = stack.getItem().getDurabilityForDisplay(stack);
            int i = Math.round(13.0F - (float) health * 13.0F);
            int j = stack.getItem().getRGBDurabilityForDisplay(stack);
            fill(matrixstack, 2, 13, 13, 2, 0xff000000);
            fill(matrixstack, 2, 13, i, 1, 0xff000000 | j);
            RenderSystem.enableBlend();
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
            fill(matrixstack, 0, Mth.floor(16.0F * (1.0F - f3)), 16, Mth.ceil(16.0F * f3), 0x7fffffff);
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }
    }

    // matrixstack version of mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0); with modified lighting to match the vanilla result as much as possible
    // TODO: remove when fixed upstream (vanilla)
    private static final Vector3f DEF_LIGHT = Util.make(new Vector3f(0.55F, 0.3f, -0.8F), Vector3f::normalize);
    private static final Vector3f DIF_LIGHT = Util.make(new Vector3f(-0.8F, 0.3f, 0.55F), Vector3f::normalize);
    private void renderItemModelIntoGUI(ItemStack stack, PoseStack matrixStack, BakedModel bakedmodel)
    {

        matrixStack.pushPose();
        mc.getTextureManager().bindForSetup(InventoryMenu.BLOCK_ATLAS);
        mc.getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setBlurMipmap(false, false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.translate(8.0F, 8.0F, 150.0F);
        matrixStack.scale(1.0F, -1.0F, 1.0F);
        matrixStack.scale(16.0F, 16.0F, 16.0F);
        MultiBufferSource.BufferSource irendertypebuffer$impl = mc.renderBuffers().bufferSource();
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
                ItemTransforms.TransformType.GUI,
                false,
                matrixStack,
                irendertypebuffer$impl,
                15728880,
                OverlayTexture.NO_OVERLAY,
                bakedmodel);
        irendertypebuffer$impl.endBatch();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
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
