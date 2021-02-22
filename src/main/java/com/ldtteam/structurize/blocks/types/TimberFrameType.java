package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.api.blocks.*;
import com.ldtteam.structurize.api.generation.*;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockTimberFrame;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.data.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fml.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;

//Creates types for TimberFrame with different variants of wood and texture

public enum TimberFrameType implements IBlockList<BlockTimberFrame>
{
    PLAIN("plain", "Vertical", false),
    DOUBLE_CROSSED("double_crossed", "Crossed", false),
    FRAMED("framed", "Plain", false),
    SIDE_FRAMED("side_framed", "Side Framed", true),
    UP_GATED("up_gated", "Up-Gate", true),
    DOWN_GATED("down_gated", "Down-Gate", true),
    ONE_CROSSED_LR("one_crossed_lr", "Right-Hand Slope", false),
    ONE_CROSSED_RL("one_crossed_rl", "Left-Hand Slope", false),
    HORIZONTAL_PLAIN("horizontal_plain", "Horizontal", false),
    SIDE_FRAMED_HORIZONTAL("side_framed_horizontal", "Side Framed Horizontal", true);

    private final String name;
    private final String langName;
    private final boolean rotatable;
    private final List<RegistryObject<BlockTimberFrame>> blocks = new LinkedList<>();

    // <centre, pair<tag group, map<wood, tag>>
    public static final Map<TimberFrameCentreType, Tuple<ITag.INamedTag<Block>, Map<WoodType, ITag.INamedTag<Block>>>> blockTags = new HashMap<>();
    public static final ITag.INamedTag<Block> BLOCK_TAG = BlockTags.makeWrapperTag("structurize:timber_frames/timber_frames");

    TimberFrameType(final String name, final String langName, final boolean rotatable)
    {
        this.name = name;
        this.langName = langName;
        this.rotatable = rotatable;

        for (WoodType wood : WoodType.values())
        {
            for (TimberFrameCentreType centre : TimberFrameCentreType.values())
            {
                blocks.add(ModBlocks.register(
                  BlockTimberFrame.getName(this, wood, centre),
                  () -> new BlockTimberFrame(this, wood, centre),
                  ModItemGroups.TIMBER_FRAMES));
            }
        }
    }

    /**
     * Get the Type previous to the current (used by data generators for recipes)
     * @return the previous type.
     */
    public TimberFrameType getPrevious()
    {
        if (this.ordinal() - 1 < 0)
            return values()[values().length - 1];
        return values()[(this.ordinal() - 1) % values().length];
    }

    @NotNull
    public String getName()
    {
        return this.name;
    }

    public String getLangName()
    {
        return this.langName;
    }

    public boolean isRotatable()
    {
        return this.rotatable;
    }

    @Override
    public List<RegistryObject<BlockTimberFrame>> getRegisteredBlocks()
    {
        return blocks;
    }

    @Override
    public void generateBlockStates(final ModBlockStateProvider states)
    {
        getRegisteredBlocks().forEach(
          block -> states.directionalBlock(
            block.get(),
            states.models()
              .getBuilder(block.get().getRegistryName().getPath())
              .parent(new ModelFile.UncheckedModelFile(states.modLoc("block/timber_frames/" + this.getName())))
                .texture("frame", block.get().getFrameType() == WoodType.CACTUS ? "blocks/cactus/blockcactusplank" : "minecraft:block/" + block.get().getFrameType().getMaterial().getRegistryName().getPath())
                .texture("centre", block.get().getCentreType().textureLocation)
                .texture("particle", block.get().getCentreType().textureLocation)
          ));
    }

    @Override
    public void generateRecipes(final ModRecipeProvider provider)
    {
        getRegisteredBlocks().forEach(block -> {
            if (this == PLAIN)
            {
                provider.add(
                  consumer -> new ShapedRecipeBuilder(block.get(), 4)
                    .patternLine("F")
                    .patternLine("C")
                    .patternLine("S")
                    .key('F', block.get().getFrameType().getMaterial())
                    .key('C', block.get().getCentreType().getMaterial())
                    .key('S', ModItems.buildTool.get())
                    .addCriterion("has_" + block.get().getRegistryName().getPath(), provider.getCriterion(block.get()))
                    .build(consumer, new ResourceLocation(Constants.MOD_ID, block.get().getRegistryName().getPath() + "_crafted")));
            }

            BlockTimberFrame previous = search(block.get());

            if (previous == null) return;

            provider.add(consumer -> ShapelessRecipeBuilder.shapelessRecipe(block.get())
                .addIngredient(previous)
                .addCriterion("has_" + block.get().getRegistryName().getPath(), provider.getCriterion(block.get()))
                .build(consumer));
        });

    }

    private BlockTimberFrame search(BlockTimberFrame block)
    {
        for (BlockTimberFrame prev : this.getPrevious().getBlocks())
        {
            if (prev.getCentreType() == block.getCentreType() && prev.getFrameType() == block.getFrameType())
            {
                return prev;
            }
        }

        return null;
    }

    @Override
    public void generateTags(final ModBlockTagsProvider blocks, final ModItemTagsProvider items)
    {
        getRegisteredBlocks().forEach(block -> {
            TimberFrameCentreType centre = block.get().getCentreType();
            WoodType wood = block.get().getFrameType();

            if (!blockTags.containsKey(centre))
            {
                ITag.INamedTag<Block> tag = blocks.createTag("timber_frames/" + centre.getString());
                blockTags.put(centre, new Tuple<>(tag, new HashMap<>()));
                blocks.buildTag(BLOCK_TAG).addTag(tag);
            }

            blockTags.get(centre).getB().putIfAbsent(
              wood,
              blocks.createTag("timber_frames/" + centre.getString() + "/" + wood.getString())
            );

            blocks.buildTag(blockTags.get(centre).getB().get(wood)).add(block.get());
        });

        blockTags.forEach((c, pair) -> {
            TagsProvider.Builder<Block> builder = blocks.buildTag(pair.getA());
            pair.getB().values().forEach(builder::addTag);

            items.copy(pair.getA());
            pair.getB().values().forEach(items::copy);
        });

        items.copy(BLOCK_TAG);
    }

    @Override
    public void generateTranslations(final ModLanguageProvider lang)
    {
        lang.translate(getBlocks(), block ->
            block.getTimberFrameType().getLangName() + " " +
              ModLanguageProvider.format(block.getFrameType().getString()) + " " +
              (block.getCentreType().getString().equals(block.getFrameType().getString()) ? "" : block.getCentreType().getLangName() + " ") +
              "Timber Frame");
    }

    public static List<TimberFrameType> getAll()
    {
        return Arrays.asList(TimberFrameType.values());
    }
}
