package com.ldtteam.structurize.generation.collections;

import com.ldtteam.structurize.blocks.types.IBlockCollection;
import net.minecraft.block.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CollectionBlockStateProvider extends BlockStateProvider
{
    private final List<RegistryObject<Block>> blocks;
    private final String texture;
    protected final ExistingFileHelper exFileHelper;

    public CollectionBlockStateProvider(final DataGenerator gen, final String modid, final ExistingFileHelper exFileHelper, List<RegistryObject<Block>> collection, String textureDirectory)
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
            return new ResourceLocation(name.getNamespace(), texture + "/" + block.getRegistryName().getPath() + "_" + model);
        }
        else if (exFileHelper.exists(name, ResourcePackType.CLIENT_RESOURCES, "_" + model + ".png", "textures/" + texture))
        {
            return new ResourceLocation(name.getNamespace(), texture + "/" + block.getRegistryName().getPath());
        }

        return new ResourceLocation(blocks.get(0).get().getRegistryName().getNamespace(), texture + "/" + blocks.get(0).get().getRegistryName().getPath());
    }

    protected ResourceLocation findTexture(Block block)
    {
        return findTexture(block, "");
    }

    @Override
    protected void registerStatesAndModels()
    {
        for (RegistryObject<Block> ro : blocks)
        {
            Block block = ro.get();
            ResourceLocation name = block.getRegistryName();
            if (name == null) continue;

            switch (IBlockCollection.BlockType.fromSuffix(block))
            {
                case STAIRS: stairsBlock((StairsBlock) block, findTexture(block, "side"), findTexture(block, "bottom"), findTexture(block, "top")); break;
                case WALL: wallBlock((WallBlock) block, findTexture(block)); break;
                case FENCE: fenceBlock((FenceBlock) block, findTexture(block)); break;
                case FENCE_GATE: fenceGateBlock((FenceGateBlock) block, findTexture(block)); break;
                case SLAB:
                    ResourceLocation side = findTexture(block, "side");
                    ResourceLocation bottom = findTexture(block, "bottom");
                    ResourceLocation top  = findTexture(block, "top");
                    slabBlock((SlabBlock) block,
                      models().slab(name.getPath(), side, bottom, top),
                      models().slabTop(name.getPath() + "_top", side, bottom, top),
                      models().cubeBottomTop(name.getPath() + "_double", side, bottom, top));
                    break;
                case BLOCK:
                    simpleBlock(block, models().cubeAll(
                      block.getRegistryName().getPath(),
                      new ResourceLocation(block.getRegistryName().getNamespace(),texture + "/" + block.getRegistryName().getPath())));
                    break;
            }
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Collection Block States Provider";
    }
}
