package com.ldtteam.structurize.generation.collections;

import com.ldtteam.structurize.blocks.types.IBlockCollection;
import net.minecraft.block.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CollectionRecipeProvider extends RecipeProvider
{
    private final IBlockCollection blocks;
    private final IItemProvider material;

    public CollectionRecipeProvider(final DataGenerator generatorIn, final IBlockCollection collection, final IItemProvider material)
    {
        super(generatorIn);
        this.blocks = collection;
        this.material = material;
    }

    public CollectionRecipeProvider(final DataGenerator generatorIn, final IBlockCollection collection)
    {
        this(generatorIn, collection, collection.getMainBlock());
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<IFinishedRecipe> consumer)
    {
        for (RegistryObject<Block> ro : blocks.getBlocks())
        {
            if (IBlockCollection.BlockType.fromSuffix(ro.get()) == IBlockCollection.BlockType.BLOCK)
            {
                blocks.provideMainRecipe(consumer, hasItem(blocks.getMainBlock()));
                continue;
            }

            IBlockCollection.BlockType.fromSuffix(ro.get())
              .formRecipe(ro.get(), material, Tags.Items.RODS_WOODEN, hasItem(material))
              .build(consumer);
        }
    }
}
