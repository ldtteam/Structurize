package com.ldtteam.structurize.generation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ldtteam.datagenerators.loot_table.LootTableJson;
import com.ldtteam.datagenerators.loot_table.LootTableTypeEnum;
import com.ldtteam.datagenerators.loot_table.pool.PoolJson;
import com.ldtteam.datagenerators.loot_table.pool.conditions.survives_explosion.SurvivesExplosionConditionJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.EntryJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.EntryTypeEnum;
import com.ldtteam.structurize.api.blocks.BlockType;
import com.ldtteam.structurize.api.blocks.IBlockCollection;
import com.ldtteam.structurize.api.blocks.IBlockList;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraftforge.fml.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * This class generates the default loot_table for blocks
 * (if a block is destroyed, it drops its item).
 */
public class DefaultBlockLootTableProvider implements IDataProvider
{
    private static final String DATAPACK_DIR = "data/" + Constants.MOD_ID + "/";
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final String LOOT_TABLES_DIR = DATAPACK_DIR + "loot_tables/blocks";

    private final DataGenerator generator;

    public DefaultBlockLootTableProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void run(@NotNull DirectoryCache cache) throws IOException
    {
        saveBlocks(ModBlocks.paperWalls.getRegisteredBlocks(), cache);
        saveBlocks(ModBlocks.shingleSlabs.getRegisteredBlocks(), cache);
        saveBlocks(ModBlocks.floatingCarpets.getRegisteredBlocks(), cache);

        saveBlockCollection(ModBlocks.BRICKS, cache);
        saveBlockCollection(ModBlocks.CACTI_BLOCKS, cache);

        saveBlockList(ModBlocks.timberFrames, cache);
        saveBlockList(ModBlocks.shingles, cache);

        saveBlock(ModBlocks.blockSubstitution, cache);
        saveBlock(ModBlocks.blockTagSubstitution, cache);
        saveBlock(ModBlocks.blockSolidSubstitution, cache);
        saveBlock(ModBlocks.blockFluidSubstitution, cache);

        saveBlock(ModBlocks.blockDecoBarrel_onside, cache);
        saveBlock(ModBlocks.blockDecoBarrel_standing, cache);

        saveBlock(ModBlocks.multiBlock, cache);
    }

    private void saveBlockCollection(final List<IBlockCollection> blocks, final DirectoryCache cache) throws  IOException
    {
        for (IBlockCollection collection : blocks)
        {
            saveBlockCollection(collection, cache);
        }
    }

    private void saveBlockCollection(final IBlockCollection blocks, final DirectoryCache cache) throws IOException
    {
        for (RegistryObject<Block> block : blocks.getRegisteredBlocks())
        {
            // Do the door manually, so it doesn't drop one for each half
            if (BlockType.fromSuffix(block.get()) == BlockType.DOOR) continue;
            saveBlock(block, cache);
        }
    }

    private <B extends Block, L extends IBlockList<B>> void saveBlockList(final List<L> blocks, final DirectoryCache cache) throws IOException
    {
        for (L list : blocks)
        {
            saveBlockList(list, cache);
        }
    }

    private <B extends Block> void saveBlockList(final IBlockList<B> blocks, final DirectoryCache cache) throws IOException
    {
        for (RegistryObject<B> block : blocks.getRegisteredBlocks())
        {
            saveBlock(block, cache);
        }
    }

    private <T extends Block> void saveBlocks(final List<RegistryObject<T>> blocks, final DirectoryCache cache) throws IOException
    {
        for (RegistryObject<T> block : blocks)
        {
            saveBlock(block, cache);
        }
    }

    private void saveBlock(final RegistryObject<? extends Block> block, final DirectoryCache cache) throws IOException
    {
        if (block.get().getRegistryName() != null)
        {

            final EntryJson entryJson = new EntryJson();
            entryJson.setType(EntryTypeEnum.ITEM);
            entryJson.setName(block.get().getRegistryName().toString());

            final PoolJson poolJson = new PoolJson();
            poolJson.setEntries(Collections.singletonList(entryJson));
            poolJson.setRolls(1);
            poolJson.setConditions(Collections.singletonList(new SurvivesExplosionConditionJson()));

            final LootTableJson lootTableJson = new LootTableJson();
            lootTableJson.setType(LootTableTypeEnum.BLOCK);
            lootTableJson.setPools(Collections.singletonList(poolJson));

            final Path savePath = generator.getOutputFolder().resolve(LOOT_TABLES_DIR).resolve(block.get().getRegistryName().getPath() + ".json");
            IDataProvider.save(GSON, cache, lootTableJson.serialize(), savePath);
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Default Block Loot Tables Provider";
    }
}
