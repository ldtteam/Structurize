package com.ldtteam.structurize.client.model;

import com.ldtteam.domumornamentum.client.model.baked.MateriallyTexturedBakedModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

/**
 * Simple wrapper to create {@link OverlaidBakedModel}.
 */
public class OverlaidGeometry implements IUnbakedGeometry<OverlaidGeometry>
{
    private static final Logger LOGGER = LogManager.getLogger();

    private ResourceLocation overlayModelId;
    private UnbakedModel overlayModel;

    public OverlaidGeometry(final ResourceLocation overlayModelId)
    {
        this.overlayModelId = overlayModelId;
    }

    @NotNull
    @Override
    public Collection<Material> getMaterials(@NotNull final IGeometryBakingContext owner,
                                             @NotNull final Function<ResourceLocation, UnbakedModel> modelGetter,
                                             @NotNull final Set<Pair<String, String>> missingTextureErrors)
    {
        this.overlayModel = modelGetter.apply(this.overlayModelId);
        if (this.overlayModel == null)
        {
            LOGGER.warn("No parent '{}' while loading model '{}'", this.overlayModelId, this);
        }

        if (this.overlayModel == null)
        {
            this.overlayModelId = ModelBakery.MISSING_MODEL_LOCATION;
            this.overlayModel = modelGetter.apply(this.overlayModelId);
        }

        if (!(this.overlayModel instanceof BlockModel))
        {
            throw new IllegalStateException("BlockModel parent has to be a block model.");
        }

        return overlayModel.getMaterials(modelGetter, missingTextureErrors);
    }

    @NotNull
    @Override
    public BakedModel bake(@NotNull final IGeometryBakingContext owner,
                           @NotNull final ModelBakery bakery,
                           @NotNull final Function<Material, TextureAtlasSprite> spriteGetter,
                           @NotNull final ModelState modelTransform,
                           @NotNull final ItemOverrides overrides,
                           @NotNull final ResourceLocation modelLocation)
    {
        BakedModel baked = this.overlayModel.bake(bakery, spriteGetter, modelTransform, overlayModelId);

        if (baked == null)
        {
            baked = Minecraft.getInstance().getModelManager().getMissingModel();
        }

        return new OverlaidBakedModel(baked);
    }

}
