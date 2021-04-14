package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
            ms.push();
            ms.translate(x, y, 0.0f);
            ms.scale(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE, 1.0f);

            FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
            if (font == null)
            {
                font = mc.fontRenderer;
            }

            // RenderSystem.pushMatrix();
            // RenderSystem.multMatrix(ms.getLast().getMatrix());
            // mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0);
            renderItemModelIntoGUI(itemStack,
                ms,
                Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(itemStack, null, Minecraft.getInstance().player));
            mc.getItemRenderer().renderItemOverlays(font, itemStack, 0, 0);
            // RenderSystem.popMatrix();

            ms.pop();
        }
    }

    // matrixstack version of mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0);
    private void renderItemModelIntoGUI(ItemStack stack, MatrixStack matrixStack, IBakedModel bakedmodel)
    {
        matrixStack.push();
        Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmapDirect(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        // matrixStack.translate(0.0F, 0.0F, 100.0F + Minecraft.getInstance().getItemRenderer().zLevel);
        matrixStack.translate(8.0F, 8.0F, 0.0F);
        matrixStack.scale(1.0F, -1.0F, 1.0F);
        matrixStack.scale(16.0F, 16.0F, 16.0F);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        boolean flag = !bakedmodel.isSideLit();
        if (flag)
        {
            RenderHelper.setupGuiFlatDiffuseLighting();
        }

        Minecraft.getInstance()
            .getItemRenderer()
            .renderItem(stack,
                ItemCameraTransforms.TransformType.GUI,
                false,
                matrixStack,
                irendertypebuffer$impl,
                15728880,
                OverlayTexture.NO_OVERLAY,
                bakedmodel);
        irendertypebuffer$impl.finish();
        RenderSystem.enableDepthTest();
        if (flag)
        {
            RenderHelper.setupGui3DDiffuseLighting();
        }

        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        matrixStack.pop();
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
