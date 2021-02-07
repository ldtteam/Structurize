package com.ldtteam.structurize.generation.defaults;

import com.ldtteam.structurize.blocks.types.IBlockCollection;
import net.minecraft.block.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CollectionBlockStateProvider extends BlockStateProvider
{
    private final List<Block>      blocks;
    private final String texture;
    protected final ExistingFileHelper exFileHelper;

    public CollectionBlockStateProvider(final DataGenerator gen, final String modid, final ExistingFileHelper exFileHelper, List<Block> collection, String textureDirectory)
    {
        super(gen, modid, exFileHelper);
        this.blocks = collection;
        this.texture = textureDirectory;
        this.exFileHelper = exFileHelper;
    }

    /**
     * Find the right texture, first with the model, then with the block, then with the default form
     * @param block the block to find the texture for
     * @param model the model variation to seek
     * @return the first viable resource found, or the default
     */
    protected ResourceLocation findTexture(Block block, String model)
    {
        if (texture == null || block.getRegistryName() == null) return blockTexture(block);

        ResourceLocation name = block.getRegistryName();

        if (!model.isEmpty() && exFileHelper.exists(name, ResourcePackType.CLIENT_RESOURCES, "_" + model + ".png", "textures/" + texture))
        {
            return new ResourceLocation(name.getNamespace(), texture + block.getRegistryName().getPath() + "_" + model);
        }
        else if (exFileHelper.exists(name, ResourcePackType.CLIENT_RESOURCES, "_" + model + ".png", "textures/" + texture))
        {
            return new ResourceLocation(name.getNamespace(), texture + block.getRegistryName().getPath());
        }

        return new ResourceLocation(blocks.get(0).getRegistryName().getNamespace(), texture + blocks.get(0).getRegistryName().getPath());
    }

    protected ResourceLocation findTexture(Block block)
    {
        return findTexture(block, "");
    }

    @Override
    protected void registerStatesAndModels()
    {
        for (Block block : blocks)
        {
            switch (IBlockCollection.BlockType.fromSuffix(block))
            {
                case SLAB: slabBlock((SlabBlock) block, findTexture(block, "double"), findTexture(block));
                case STAIRS: stairsBlock((StairsBlock) block, findTexture(block, "side"), findTexture(block, "bottom"), findTexture(block, "top"));
                case WALL: wallBlock((WallBlock) block, findTexture(block));
                case FENCE: fenceBlock((FenceBlock) block, findTexture(block));
                case FENCE_GATE: fenceGateBlock((FenceGateBlock) block, findTexture(block));
                default:
                    simpleBlock(block, models().cubeAll(
                      block.getRegistryName().getPath(),
                      new ResourceLocation(block.getRegistryName().getNamespace(),texture + "/" + block.getRegistryName().getPath())));
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
