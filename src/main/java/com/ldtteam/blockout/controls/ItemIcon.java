package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
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
    public static final float DEFAULT_ITEMSTACK_SIZE = 16f;
    public static final float GUI_ITEM_Z_TRANSLATE   = 32.0F;

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

        final String itemName = params.getStringAttribute("item", null);
        if (itemName != null)
        {
            final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
            if (item != null)
            {
                itemStack = new ItemStack(item, 1);
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
    public void drawSelf(final MatrixStack ms, final int mx, final int my)
    {
        if (itemStack != null && !itemStack.isEmpty())
        {
            ms.push();
            ms.translate(x, y, GUI_ITEM_Z_TRANSLATE);
            ms.scale(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE, 1f);

            FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
            if (font == null)
            {
                font = mc.fontRenderer;
            }

            RenderSystem.pushMatrix();
            RenderSystem.multMatrix(ms.getLast().getMatrix());
            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0);
            mc.getItemRenderer().renderItemOverlayIntoGUI(font, itemStack, 0, 0, null);
            RenderSystem.popMatrix();

            ms.pop();
        }
    }

    @Override
    public void drawSelfLast(final MatrixStack ms, final int mx, final int my)
    {
        if (itemStack == null || itemStack.isEmpty() || !isHovered)
        {
            return;
        }

        ms.push();
        ms.translate(mx, my, GUI_ITEM_Z_TRANSLATE);
        ms.scale(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE, 1.0f);
        window.getScreen().renderTooltipHook(ms, itemStack, 0, 0);
        ms.pop();
    }
}
