package com.ldtteam.structurize.api.blocks;

import com.ldtteam.structurize.api.generation.*;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A block collection is any set of blocks with a common material but many forms,
 * for example a brick collection consists of brick stairs, brick slab, etc.
 *
 * Implements effectively as both a class and enum (for many related sets, like brick variants).
 */
public interface IBlockCollection extends IGenerated
{
    /**
     * Gets the name applied to the whole collection as an id and prefix for registry names.
     * @return the name identifying the collection
     */
    String getName();

    /**
     * Gets the name to be used when there is no suffix, e.g. bricks instead of brick_slab
     * @return the plural form of the name
     */
    default String getPluralName() { return getName() + "s"; }

    /**
     * Retrieves the registry entries of all the blocks in this collection,
     * with the first being considered the default in some circumstances.
     * Will likely require a field when implementing.
     * @return the blocks
     */
    List<RegistryObject<Block>> getRegisteredBlocks();

    /**
     * Selects which block in the collection is the fallback, or primary block.
     * For recipe purposes mostly
     * @return the main block
     */
    default Block getMainBlock()
    {
        return getMainRegisteredBlock().get();
    }

    default RegistryObject<Block> getMainRegisteredBlock()
    {
        return getRegisteredBlocks().get(0);
    }

    /**
     * Defines the properties that should be applied to each block in the collection
     * @return the properties to use for block construction
     */
    default AbstractBlock.Properties getProperties()
    {
        // A generic wood default
        return AbstractBlock.Properties.of(Material.WOOD, MaterialColor.WOOD)
                 .strength(2.0F, 3.0F)
                 .sound(SoundType.WOOD);
    }

    /**
     * Constructs and registers each block in the collection
     * See interface comments for the preferred content.
     * @param registrar the DeferredRegistry instance to apply the block to
     * @param itemRegistrar the DeferredRegistry instance to apply the item to
     * @param group the item group (or creative tab) to place this block in
     * @param types a selection of each block type that is part of the collection
     * @return each registered block in the collection
     */
    default List<RegistryObject<Block>> create(DeferredRegister<Block> registrar, DeferredRegister<Item> itemRegistrar, ItemGroup group, BlockType... types)
    {
        List<RegistryObject<Block>> results = new LinkedList<>();

        for (BlockType type : types)
        {
            // J9+ has incompatibilities with J8-compiled default interfaces that use lambda generated suppliers.
            // Do not replace the anonymous suppliers below with a lambda.
            RegistryObject<Block> block = registrar.register(
              type.withSuffix(getName(), getPluralName()),
              new Supplier<Block>()
              {
                  @Override
                  public Block get()
                  {
                      return type.constructor.apply(getProperties());
                  }
              });

            itemRegistrar.register(
              type.withSuffix(getName(), getPluralName()),
              new Supplier<Item>()
              {

                  @Override
                  public Item get()
                  {
                      return new BlockItem(block.get(), new Item.Properties().tab(group));
                  }
              });

            results.add(block);
        }

        return results;
    }

    /**
     * Specifies the recipe of the main block, which is then the block
     * used to craft all the others.
     * @param consumer the generation method to save the recipe json
     * @param obtainment an unfortunately mandatory criterion - MUST be applied to the recipe
     */
    void provideMainRecipe(Consumer<IFinishedRecipe> consumer, ICriterionInstance obtainment);

    /**
     * Unlikely to need a non-default implementation - convenience method to find the model texture
     * @param block the block related to this texture
     * @param model the variant as a string to find the model texture for, if it exists
     * @return the texture, or a fallback, even if the fallback does not exist
     */
    default ResourceLocation findTexture(Block block, String model)
    {
        return ModBlockStateProvider.getInstance().findTexture(getTextureDirectory(), model, block, getMainBlock());
    }

    /**
     * Unlikely to need a non-default implementation - convenience method to find the block texture
     * @param block the block related to this texture
     * @return the texture, or its fallbacks
     */
    default ResourceLocation findTexture(Block block)
    {
        return findTexture(block, "");
    }

    @Override
    default void generateBlockStates(ModBlockStateProvider states)
    {
        for (RegistryObject<Block> ro : getRegisteredBlocks())
        {
            Block block = ro.get();
            ResourceLocation name = block.getRegistryName();
            if (name == null) return;

            switch (BlockType.fromSuffix(block))
            {
                case STAIRS: states.stairsBlock((StairsBlock) block, findTexture(block, "side"), findTexture(block, "bottom"), findTexture(block, "top")); break;
                case WALL: states.wallBlock((WallBlock) block, findTexture(block)); break;
                case FENCE: states.fenceBlock((FenceBlock) block, findTexture(block)); break;
                case FENCE_GATE: states.fenceGateBlock((FenceGateBlock) block, findTexture(block)); break;
                case SLAB:
                    ResourceLocation side = findTexture(block, "side");
                    ResourceLocation bottom = findTexture(block, "bottom");
                    ResourceLocation top  = findTexture(block, "top");
                    states.slabBlock((SlabBlock) block,
                      states.models().slab(name.getPath(), side, bottom, top),
                      states.models().slabTop(name.getPath() + "_top", side, bottom, top),
                      states.models().cubeBottomTop(name.getPath() + "_double", side, bottom, top));
                    break;
                case PLANKS:
                case BLOCK: states.simpleBlock(block, states.models().cubeAll(block.getRegistryName().getPath(), findTexture(block))); break;
                case TRAPDOOR: states.trapdoorBlock((TrapDoorBlock) block, findTexture(block), false); break;
                case DOOR: states.doorBlock((DoorBlock) block, findTexture(block, "lower"), findTexture(block, "upper")); break;
            }
        }
    }

    @Override
    default void generateItemModels(ModItemModelProvider models)
    {
        for (RegistryObject<Block> ro : getRegisteredBlocks())
        {
            Block block = ro.get();
            if (block.getRegistryName() == null) continue;

            String name = block.getRegistryName().getPath();

            switch (BlockType.fromSuffix(block))
            {
                case SLAB: models.slab(name, findTexture(block, "side"), findTexture(block, "bottom"), findTexture(block, "top")); break;
                case STAIRS: models.stairs(name, findTexture(block, "side"), findTexture(block, "bottom"), findTexture(block, "top")); break;
                case WALL: models.wallInventory(name, findTexture(block)); break;
                case FENCE: models.fenceInventory(name, findTexture(block)); break;
                case FENCE_GATE: models.fenceGate(name, findTexture(block)); break;
                case PLANKS:
                case BLOCK: models.withUncheckedParent(name, "block/" + name); break;
                case TRAPDOOR: models.withUncheckedParent(name, "block/" + name + "_bottom"); break;
                case DOOR: models.singleTexture(name, models.mcLoc("item/generated"), "layer0", models.modLoc("items/" + name)); break;
            }
        }
    }

    @Override
    default void generateRecipes(ModRecipeProvider provider)
    {
        getRegisteredBlocks().forEach(
          ro -> provider.add(consumer -> {
            if (ro.get() == getMainBlock())
            {
                provideMainRecipe(consumer, ModRecipeProvider.getDefaultCriterion(getMainBlock()));
                return;
            }
            BlockType.fromSuffix(ro.get())
              .formRecipe(ro.get(), getMainBlock(), ModRecipeProvider.getDefaultCriterion(getMainBlock()))
              .save(consumer);
        }));
    }

    @Override
    default void generateTags(ModBlockTagsProvider blocks, ModItemTagsProvider items)
    {
        getRegisteredBlocks().forEach(
          ro -> BlockType.fromSuffix(ro.get()).blockTag.forEach(
            tag -> ModBlockTagsProvider.getInstance().buildTag(tag).add(ro.get())
        ));
    }

    @Override
    default void generateTranslations(ModLanguageProvider lang)
    {
        lang.autoTranslate(getRegisteredBlocks().stream().map(RegistryObject::get).collect(Collectors.toList()));
    }
}
