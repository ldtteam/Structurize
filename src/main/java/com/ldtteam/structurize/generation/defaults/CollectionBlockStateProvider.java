package com.ldtteam.structurize.generation.defaults;

import com.ldtteam.structurize.blocks.types.IBlockCollection;
import net.minecraft.block.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CollectionBlockStateProvider extends BlockStateProvider
{
    private final List<Block>      blocks;
    private final ResourceLocation texture;

    public CollectionBlockStateProvider(final DataGenerator gen, final String modid, final ExistingFileHelper exFileHelper, List<Block> collection, String primaryTexture)
    {
        super(gen, modid, exFileHelper);
        this.blocks = collection;
        this.texture = new ResourceLocation(primaryTexture);
    }

    @Override
    protected void registerStatesAndModels()
    {
        for (Block block : blocks)
        {
            ResourceLocation tex = texture == null ? blockTexture(block) : texture;

            switch (IBlockCollection.BlockType.fromSuffix(block))
            {
                case SLAB: slabBlock((SlabBlock) block, tex, tex);
                case STAIRS: stairsBlock((StairsBlock) block, tex, tex, tex);
                case WALL: wallBlock((WallBlock) block, tex);
                case FENCE: fenceBlock((FenceBlock) block, tex);
                case FENCE_GATE: fenceGateBlock((FenceGateBlock) block, tex);
                default: simpleBlock(block);
            }
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Collection Block States";
    }
}
