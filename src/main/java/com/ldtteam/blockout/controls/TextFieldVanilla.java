package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.SharedConstants;

/**
 * Mimics Vanilla text fields.
 */
public class TextFieldVanilla extends TextField
{
    private static final int BACKGROUND_WIDTH_OFFSET = 8;
    private static final float BACKGROUND_X_TRANSLATE = 4F;
    private static final int BACKGROUND_Y_TRANSLATE_OFFSET = 8;
    private static final int BACKGROUND_MOUSE_OFFSET_X = 4;
    private boolean backgroundEnabled = true;
    private int backgroundOuterColor = 0xFFA0A0A0;
    private int backgroundInnerColor = 0xFF000000;

    /**
     * Required default constructor.
     */
    public TextFieldVanilla()
    {
        super();
        filter = new FilterVanilla();
    }

    /**
     * Constructor called when creating an object from xml.
     *
     * @param params xml parameters.
     */
    public TextFieldVanilla(final PaneParams params)
    {
        super(params);
        backgroundEnabled = params.getBooleanAttribute("background", backgroundEnabled);
        backgroundOuterColor = params.getColorAttribute("backgroundOuter", backgroundOuterColor);
        backgroundInnerColor = params.getColorAttribute("backgroundInner", backgroundInnerColor);
        filter = new FilterVanilla();
    }

    public boolean isBackgroundEnabled()
    {
        return backgroundEnabled;
    }

    public void setBackgroundEnabled(final boolean e)
    {
        backgroundEnabled = e;
    }

    public int getBackgroundOuterColor()
    {
        return backgroundOuterColor;
    }

    public void setBackgroundOuterColor(final int c)
    {
        backgroundOuterColor = c;
    }

    public int getBackgroundInnerColor()
    {
        return backgroundInnerColor;
    }

    public void setBackgroundInnerColor(final int c)
    {
        backgroundInnerColor = c;
    }

    @Override
    public int getInternalWidth()
    {
        return backgroundEnabled ? (getWidth() - BACKGROUND_WIDTH_OFFSET) : getWidth();
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        if (backgroundEnabled)
        {
            // Draw box
            fill(x - 1, y - 1, x + width + 1, y + height + 1, backgroundOuterColor);
            fill(x, y, x + width, y + height, backgroundInnerColor);

            RenderSystem.pushMatrix();
            RenderSystem.translatef(BACKGROUND_X_TRANSLATE, (float) ((height - BACKGROUND_Y_TRANSLATE_OFFSET) / 2.0), 0);
        }

        super.drawSelf(mx, my);

        if (backgroundEnabled)
        {
            RenderSystem.popMatrix();
        }
    }

    @Override
    public boolean handleClick(final int mx, final int my)
    {
        int mouseX = mx;

        if (backgroundEnabled)
        {
            mouseX -= BACKGROUND_MOUSE_OFFSET_X;
        }

        super.handleClick(mouseX, my);
        return true;
    }

    /*
     * private static class FilterNumeric implements Filter
     * {
     * @Override
     * public String filter(final String s)
     * {
     * final StringBuilder sb = new StringBuilder();
     * for (final char c : s.toCharArray())
     * {
     * if (isAllowedCharacter(c))
     * {
     * sb.append(c);
     * }
     * }
     * return sb.toString();
     * }
     * @Override
     * public boolean isAllowedCharacter(final char c)
     * {
     * return Character.isDigit(c);
     * }
     * }
     */

    private static class FilterVanilla implements Filter
    {
        @Override
        public String filter(final String s)
        {
            return SharedConstants.filterAllowedCharacters(s);
        }

        @Override
        public boolean isAllowedCharacter(final char c)
        {
            return SharedConstants.isAllowedCharacter(c);
        }
    }
}
