package com.ldtteam.structurize.generation.defaults;

import com.google.gson.JsonParser;
import com.ldtteam.datagenerators.IJsonSerializable;
import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import com.ldtteam.datagenerators.lang.LangJson;
import com.ldtteam.datagenerators.loot_table.LootTableJson;
import com.ldtteam.datagenerators.loot_table.LootTableTypeEnum;
import com.ldtteam.datagenerators.loot_table.pool.PoolJson;
import com.ldtteam.datagenerators.loot_table.pool.conditions.survives_explosion.SurvivesExplosionConditionJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.EntryJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.EntryTypeEnum;
import com.ldtteam.datagenerators.models.item.ItemModelJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import com.ldtteam.datagenerators.recipes.RecipeResultJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedRecipeJson;
import com.ldtteam.datagenerators.recipes.shapeless.ShapelessRecipeJson;
import com.ldtteam.datagenerators.tags.TagJson;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ProviderSet implements IDataProvider
{
    public final DataGenerator generator;
    public final List<? extends Block> applicants;

    protected BiConsumer<Block, Map<String, BlockstateVariantJson>> variantProvider   = DefaultProviderTemplates.SIMPLE("");
    protected BiConsumer<Block, ItemModelJson>                   itemModelProvider = null;
    protected List<BiConsumer<Block, Consumer<IFinishedRecipe>>> recipeProviders   = null;
    protected Function<Block, String>                            langProvider      = DefaultProviderTemplates::toTranslation;

    protected boolean shaped = false;
    protected boolean noDrop = false;

    public ProviderSet(DataGenerator gen, List<? extends Block> applicants)
    {
        this.generator = gen;
        this.applicants = applicants;
    }

    @Override
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        final List<String> tags = new ArrayList<>(applicants.size());

        // For translations
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);
        if (inputPath == null) return;

        final File langFile = inputPath.resolve(DataGeneratorConstants.EN_US_LANG).toFile();
        final Reader reader = new FileReader(langFile);

        final LangJson langJson = new LangJson();
        langJson.deserialize(new JsonParser().parse(reader));

        for (Block block : applicants)
        {
            if (block.getRegistryName() == null) continue;

            provideLootTable(cache, block);
            provideBlockState(cache, block);
            provideItemModel(cache, block);
            provideRecipe(cache, block);
            provideTranslation(langJson, block);
            tags.add(block.getRegistryName().toString());
        }

        TagJson tagJson = new TagJson(false, tags);
        final Path itemsTagsPath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.TAGS_DIR).resolve("items").resolve("floating_carpets.json");
        final Path blocksTagsPath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.TAGS_DIR).resolve("blocks").resolve("floating_carpets.json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(tagJson), itemsTagsPath);
        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(tagJson), blocksTagsPath);
        IDataProvider.save(DataGeneratorConstants.GSONLang, cache, langJson.serialize(), langFile.toPath());
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Default Mass Block Data Provider";
    }

    /*
     *      == Builder Components ==
     */

    public ProviderSet loot(boolean doDropLoot)
    {
        noDrop = !doDropLoot;
        return this;
    }

    public ProviderSet variants(BiConsumer<Block, Map<String, BlockstateVariantJson>> path)
    {
        variantProvider = path;
        return this;
    }

    public ProviderSet item(BiConsumer<Block, ItemModelJson> model)
    {
        itemModelProvider = model;
        return this;
    }

    public final ProviderSet shapeless(Predicate<Block> isForThisBlock, final Function<Block, ShapelessRecipeBuilder> builder)
    {
        recipeProviders.add((block, consumer) -> {
            if (isForThisBlock.test(block))
            {
                ShapelessRecipeBuilder result = builder.apply(block);
                if (result != null) result.build(consumer);
            }
        });

        return this;
    }

    public ProviderSet shaped(Predicate<Block> isForThisBlock, Function<Block, ShapedRecipeBuilder> builder)
    {
        recipeProviders.add((block, consumer) -> {
            if(isForThisBlock.test(block)) builder.apply(block).build(consumer);
        });

        return this;
    }

    public ProviderSet translate(Function<Block, String> evaluator)
    {
        langProvider = evaluator;
        return this;
    }

    /*
     *      == Provider Actors ==
     *     where the magic happens
     */

    protected void provideLootTable(final DirectoryCache cache, final Block block) throws IOException
    {
        if (noDrop) return;

        final EntryJson entryJson = new EntryJson();
        entryJson.setType(EntryTypeEnum.ITEM);
        entryJson.setName(block.getRegistryName().toString());

        final PoolJson poolJson = new PoolJson();
        poolJson.setEntries(Collections.singletonList(entryJson));
        poolJson.setRolls(1);
        poolJson.setConditions(Collections.singletonList(new SurvivesExplosionConditionJson()));

        final LootTableJson lootTableJson = new LootTableJson();
        lootTableJson.setType(LootTableTypeEnum.BLOCK);
        lootTableJson.setPools(Collections.singletonList(poolJson));

        final Path savePath = generator.getOutputFolder().resolve(DataGeneratorConstants.LOOT_TABLES_DIR).resolve(block.getRegistryName().getPath() + ".json");
        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(lootTableJson), savePath);
    }

    protected void provideBlockState(final DirectoryCache cache, final Block block) throws IOException
    {
        BlockstateJson blockstateJson = new BlockstateJson();

        Map<String, BlockstateVariantJson> variantMap = new HashMap<>();
        variantProvider.accept(block, variantMap);
        blockstateJson.setVariants(variantMap);

        final Path blockStateFolder = this.generator.getOutputFolder().resolve(DataGeneratorConstants.BLOCKSTATE_DIR);
        final Path blockStatePath = blockStateFolder.resolve(block.getRegistryName().getPath() + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(blockstateJson), blockStatePath);
    }

    private void provideItemModel(final DirectoryCache cache, final Block block) throws IOException
    {
        final ItemModelJson itemModelJson = new ItemModelJson();
        itemModelProvider.accept(block, itemModelJson);

        final Path saveFile = this.generator.getOutputFolder().resolve(DataGeneratorConstants.ITEM_MODEL_DIR + block.getRegistryName().getPath() + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(itemModelJson), saveFile);
    }

    private void provideRecipe(final DirectoryCache cache, final Block block) throws IOException
    {
        if (recipeProviders.isEmpty()) return;

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(block.getRegistryName().getPath() + ".json");

        for (BiConsumer<Block,Consumer<IFinishedRecipe>> provider : recipeProviders)
        {
            provider.accept(block, recipe ->
            {
                try
                {
                    IDataProvider.save(
                      DataGeneratorConstants.GSON,
                      cache,
                      DataGeneratorConstants.serialize(recipe.getRecipeJson()),
                      recipePath);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
        }
    }

    private void provideTranslation(final LangJson translate, final Block block)
    {
        final String reference = "block.structurize." + block.getRegistryName().getPath();
        final String value = langProvider.apply(block);

        if (!value.isEmpty()) translate.getLang().put(reference, value);
    }
}
