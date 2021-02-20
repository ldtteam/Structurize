package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
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
            final float itemScale = this.getWidth() / DEFAULT_ITEMSTACK_SIZE;
            ms.translate(x, y, itemScale * -100.0f);
            ms.scale(itemScale, itemScale, itemScale);

            FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
            if (font == null)
            {
                font = mc.fontRenderer;
            }

            RenderSystem.pushMatrix();
            RenderSystem.multMatrix(ms.getLast().getMatrix());
            RenderHelper.enableStandardItemLighting();
            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0);
            mc.getItemRenderer().renderItemOverlays(font, itemStack, 0, 0);
            RenderHelper.disableStandardItemLighting();
            RenderSystem.popMatrix();

            ms.pop();
        }
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
