package com.ldtteam.structurize.client.model;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;

/**
 * Loads and bakes the parent model used by Structurize's overlaid model JSON.
 */
public class OverlaidUnbakedModel implements UnbakedModel
{
    private final Identifier parentModelId;

    public OverlaidUnbakedModel(final Identifier parentModelId)
    {
        this.parentModelId = parentModelId;
    }

    @Override
    public UnbakedGeometry geometry()
    {
        return new UnbakedGeometry()
        {
            @Override
            public QuadCollection bake(
                final TextureSlots textureSlots,
                final ModelBaker baker,
                final ModelState state,
                final ModelDebugName name)
            {
                return bakeParent(baker, state, ContextMap.EMPTY);
            }

            @Override
            public QuadCollection bake(
                final TextureSlots textureSlots,
                final ModelBaker baker,
                final ModelState state,
                final ModelDebugName name,
                final ContextMap properties)
            {
                return bakeParent(baker, state, properties);
            }
        };
    }

    private QuadCollection bakeParent(final ModelBaker baker, final ModelState state, final ContextMap properties)
    {
        final ResolvedModel parent = baker.getModel(parentModelId);
        return parent.getTopGeometry().bake(parent.getTopTextureSlots(), baker, state, parent, properties);
    }

    @Override
    public void resolveDependencies(final Resolver resolver)
    {
        resolver.markDependency(parentModelId);
    }

    /**
     * Keep the wrapped overlay model in the 26.2 parent chain so vanilla can
     * resolve its texture slots (including the particle material) before the
     * custom geometry is baked.
     */
    @Override
    public Identifier parent()
    {
        return parentModelId;
    }
}
