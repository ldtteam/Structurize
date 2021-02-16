package com.ldtteam.structurize.generation.collections;

import com.ldtteam.structurize.blocks.BlockType;
import com.ldtteam.structurize.blocks.IBlockCollection;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class CollectionItemModelProvider extends ItemModelProvider
{
    private final IBlockCollection blocks;
    private final String texture;
    protected final ExistingFileHelper exFileHelper;

    public CollectionItemModelProvider(final DataGenerator gen, final String modid, final ExistingFileHelper exFileHelper, IBlockCollection collection, String textureDirectory)
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
        if (texture == null || block.getRegistryName() == null) return null;

        ResourceLocation name = block.getRegistryName();

        if (!model.isEmpty() && exFileHelper.exists(name, ResourcePackType.CLIENT_RESOURCES, "_" + model + ".png", "textures/" + texture))
        {
            return new ResourceLocation(name.getNamespace(), texture + "/" + block.getRegistryName().getPath() + "_" + model);
        }
        else if (exFileHelper.exists(name, ResourcePackType.CLIENT_RESOURCES, "_" + model + ".png", "textures/" + texture))
        {
            return new ResourceLocation(name.getNamespace(), texture + "/" + block.getRegistryName().getPath());
        }

        return new ResourceLocation(blocks.getMainBlock().getRegistryName().getNamespace(), texture + "/" + blocks.getMainBlock().getRegistryName().getPath());
    }

    protected ResourceLocation findTexture(Block block)
    {
        return findTexture(block, "");
    }

    @Override
    protected void registerModels()
    {
        for (RegistryObject<Block> ro : blocks.getBlocks())
        {
            Block block = ro.get();
            if (block.getRegistryName() == null) continue;

            String name = block.getRegistryName().getPath();

            switch (BlockType.fromSuffix(block))
            {
                case SLAB: slab(name, findTexture(block, "side"), findTexture(block, "bottom"), findTexture(block, "top")); break;
                case STAIRS: stairs(name, findTexture(block, "side"), findTexture(block, "bottom"), findTexture(block, "top")); break;
                case WALL: wallInventory(name, findTexture(block)); break;
                case FENCE: fenceInventory(name, findTexture(block)); break;
                case FENCE_GATE: fenceGate(name, findTexture(block)); break;
                case BLOCK: withExistingParent(name,new ResourceLocation(modid, "block/" + name));break;
            }
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Collection Block States Provider - " + modid;
    }
}
