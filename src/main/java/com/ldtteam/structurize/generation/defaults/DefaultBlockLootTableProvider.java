package com.ldtteam.structurize.generation.defaults;

import com.ldtteam.datagenerators.loot_table.LootTableJson;
import com.ldtteam.datagenerators.loot_table.LootTableTypeEnum;
import com.ldtteam.datagenerators.loot_table.pool.PoolJson;
import com.ldtteam.datagenerators.loot_table.pool.conditions.survives_explosion.SurvivesExplosionConditionJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.EntryJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.EntryTypeEnum;
import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * This class generates the default loot_table for blocks.
 * (if a block is destroyed, it drops it's item).
 */
public class DefaultBlockLootTableProvider implements IDataProvider
{
    private final DataGenerator generator;

    public DefaultBlockLootTableProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        saveBlocks(ModBlocks.getTimberFrames(), cache);
        saveBlocks(ModBlocks.getPaperwalls(), cache);
        saveBlocks(ModBlocks.getShingles(), cache);
        saveBlocks(ModBlocks.getShingleSlabs(), cache);
        saveBlocks(ModBlocks.getFloatingCarpets(), cache);
        saveBlocks(ModBlocks.getBricks(), cache);

        saveBlock(ModBlocks.blockSubstitution, cache);
        saveBlock(ModBlocks.blockSolidSubstitution, cache);
        saveBlock(ModBlocks.blockFluidSubstitution, cache);

        saveBlock(ModBlocks.blockCactusPlank, cache);
        saveBlock(ModBlocks.blockCactusTrapdoor, cache);
        saveBlock(ModBlocks.blockCactusStair, cache);
        saveBlock(ModBlocks.blockCactusSlab, cache);
        saveBlock(ModBlocks.blockCactusFence, cache);
        saveBlock(ModBlocks.blockCactusFenceGate, cache);
        saveBlock(ModBlocks.blockDecoBarrel_onside, cache);
        saveBlock(ModBlocks.blockDecoBarrel_standing, cache);

        saveBlock(ModBlocks.multiBlock, cache);
    }

    private <T extends Block> void saveBlocks(final List<T> blocks, final DirectoryCache cache) throws IOException
    {
        for (Block block : blocks)
        {
            saveBlock(block, cache);
        }
    }

    private void saveBlock(final Block block, final DirectoryCache cache) throws IOException
    {
        if (block.getRegistryName() != null)
        {

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
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Default Block Loot Tables Provider";
    }
}
