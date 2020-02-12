package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
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
    private static final float DEFAULT_ITEMSTACK_SIZE = 16f;
    private static final float GUI_ITEM_Z_TRANSLATE = 32.0F;
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
    public void drawSelf(final int mx, final int my)
    {
        if (itemStack != null)
        {
            RenderSystem.pushMatrix();
            RenderHelper.disableStandardItemLighting();
            drawItemStack(itemStack, x, y);
            RenderSystem.popMatrix();
        }
    }

    /**
     * Modified from GuiContainer.
     *
     * @param stack stack
     * @param x     x
     * @param y     y
     */
    private void drawItemStack(final ItemStack stack, final int x, final int y)
    {
        if (stack.isEmpty())
        {
            return;
        }

        final ItemRenderer itemRender = mc.getItemRenderer();

        RenderSystem.translatef((float) x, (float) y, GUI_ITEM_Z_TRANSLATE);
        RenderSystem.scalef(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE, 1f);

        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null)
        {
            font = mc.fontRenderer;
        }
        itemRender.renderItemAndEffectIntoGUI(stack, 0, 0);
        itemRender.renderItemOverlayIntoGUI(font, stack, 0, 0, null);
    }
}
