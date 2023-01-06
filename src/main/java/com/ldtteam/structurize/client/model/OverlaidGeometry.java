package com.ldtteam.structurize.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

/**
 * Simple wrapper to create {@link OverlaidBakedModel}.
 */
public class OverlaidGeometry implements IUnbakedGeometry<OverlaidGeometry>
{
    private static final Logger LOGGER = LogManager.getLogger();

    private ResourceLocation overlayModelId;

    public OverlaidGeometry(final ResourceLocation overlayModelId)
    {
        this.overlayModelId = overlayModelId;
    }

    @Override
    public BakedModel bake(
      final IGeometryBakingContext context,
      final ModelBaker baker,
      final Function<Material, TextureAtlasSprite> spriteGetter,
      final ModelState modelState,
      final ItemOverrides overrides,
      final ResourceLocation modelLocation)
    {
        UnbakedModel unbaked = baker.getModel(overlayModelId);
        BakedModel baked = unbaked.bake(baker, spriteGetter, modelState, overlayModelId);

        if (baked == null)
        {
            baked = Minecraft.getInstance().getModelManager().getMissingModel();
        }

        return new OverlaidBakedModel(baked);
    }
}
