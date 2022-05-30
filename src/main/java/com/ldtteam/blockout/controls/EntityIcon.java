package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.Render;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Control to render an entity as an icon
 */
public class EntityIcon extends Pane
{
    @Nullable
    private Entity entity;
    private int count = 1;
    private float yaw = 30;
    private float pitch = -10;
    private float headyaw = 0;

    public EntityIcon()
    {
        super();
    }

    public EntityIcon(@NotNull PaneParams params)
    {
        super(params);

        final String entityName = params.getString("entity");
        if (entityName != null)
        {
            setEntity(new ResourceLocation(entityName));
        }

        this.count = params.getInteger("count", this.count);
        this.yaw = params.getFloat("yaw", this.yaw);
        this.pitch = params.getFloat("pitch", this.pitch);
        this.headyaw = params.getFloat("head", this.headyaw);
    }

    public void setEntity(@NotNull ResourceLocation entityId)
    {
        final EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityId);
        if (entityType != null)
        {
            setEntity(entityType);
        }
        else
        {
            resetEntity();
        }
    }

    public void setEntity(@NotNull EntityType<?> type)
    {
        final Entity entity = type.create(Minecraft.getInstance().level);

        if (entity != null)
        {
            setEntity(entity);
        }
        else
        {
            resetEntity();
        }
    }

    public void setEntity(@NotNull Entity entity)
    {
        this.entity = entity;
        this.setHoverPane(null);
    }

    public void resetEntity()
    {
        this.entity = null;
        this.setHoverPane(null);
    }

    public void setCount(final int count)
    {
        this.count = count;
    }

    public void setYaw(final float yaw)
    {
        this.yaw = yaw;
    }

    public void setPitch(final float pitch)
    {
        this.pitch = pitch;
    }

    @Override
    public void drawSelf(final MatrixStack ms, final double mx, final double my)
    {
        if (this.entity != null)
        {
            ms.pushPose();
            ms.translate(x, y, -50);

            final AxisAlignedBB bb = this.entity.getBoundingBox();
            final float scale = (float) (getHeight() / bb.getYsize() / 1.5);
            final int cx = (getWidth() / 2);
            final int by = getHeight();
            final int offsetY = 2;
            Render.drawEntity(ms, cx, by - offsetY, scale, this.headyaw, this.yaw, this.pitch, this.entity);

            if (this.count != 1)
            {
                String s = String.valueOf(this.count);
                ms.translate(getWidth(), getHeight(), 100.0D);
                ms.scale(0.75F, 0.75F, 0.75F);
                IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
                mc.font.drawInBatch(s,
                        (float) (-4 - mc.font.width(s)),
                        (float) (-mc.font.lineHeight),
                        16777215,
                        true,
                        ms.last().pose(),
                        buffer,
                        false,
                        0,
                        15728880);
                buffer.endBatch();
            }

            ms.popPose();
        }
    }

    @Override
    public void onUpdate()
    {
        if (this.onHover == null && this.entity != null)
        {
            PaneBuilders.tooltipBuilder().hoverPane(this).build().setText(this.entity.getDisplayName());
        }
    }
}
