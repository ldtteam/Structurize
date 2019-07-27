package com.ldtteam.structurize.generation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;

/**
 * Abstract class used to create a Lang Table.
 */
public abstract class AbstractLootTableProvider implements IDataProvider
{

    /**
     * Create a loot_table for a block, that simply drops it's item when destroyed (unless by explosive)
     *
     * @param blockLocation The ResourceLocation for the block.
     * @return The loot_table JSON created.
     */
    protected JsonObject createDefaultBlockDropTable(final ResourceLocation blockLocation)
    {
        final JsonObject lootJson = new JsonObject();

        lootJson.addProperty("type", "minecraft:block");

        final JsonObject defaultPoolEntry = new JsonObject();

        setPoolEntryRolls(defaultPoolEntry, 1);
        addEntryToPoolEntry(defaultPoolEntry, "minecraft:item", blockLocation);
        addConditionToPoolEntry(defaultPoolEntry, new ResourceLocation("minecraft:survives_explosion"));

        addPoolEntry(lootJson, defaultPoolEntry);

        return lootJson;
    }

    /**
     * Add a poolEntry JSON to a loot_table JSON under the pools array.
     *
     * @param lootJson The loot_table JSON.
     * @param poolEntryJson The poolEntry JSON object.
     */
    private void addPoolEntry(final JsonObject lootJson, final JsonObject poolEntryJson)
    {
        if (lootJson.getAsJsonArray("pools") == null)
            lootJson.add("pools", new JsonArray());

        lootJson.getAsJsonArray("pools").add(poolEntryJson);
    }

    /**
     * Set the rolls count for a poolEntry JSON.
     *
     * @param poolEntry The poolEntry JSON object.
     * @param rolls The amount of rolls.
     */
    private void setPoolEntryRolls(final JsonObject poolEntry, final int rolls)
    {
        poolEntry.addProperty("rolls", rolls);
    }

    /**
     * Add a drop entry to a poolEntry.
     * (an item that is dropped if this poolEntry is chosen out of the pool)
     *
     * @param poolEntry The poolEntry JSON object.
     * @param type The type of entry, e.g. Tag, or Item
     * @param name The ResourceLocation of the item, or tag, to drop.
     */
    private void addEntryToPoolEntry(final JsonObject poolEntry, final String type, final ResourceLocation name)
    {
        final JsonObject entryJson = new JsonObject();
        entryJson.addProperty("type", type);
        entryJson.addProperty("name", name.toString());

        if (poolEntry.getAsJsonArray("entries") == null)
            poolEntry.add("entries", new JsonArray());

        poolEntry.getAsJsonArray("entries").add(entryJson);
    }

    /**
     * Add a condition to a poolEntry.
     * (This poolEntry will not be used if the condition is not met).
     *
     * @param poolEntry The poolEntry JSON object.
     * @param condition The ResourceLocation for the condition.
     */
    private void addConditionToPoolEntry(final JsonObject poolEntry, final ResourceLocation condition)
    {
        final JsonObject conditionJson = new JsonObject();

        conditionJson.addProperty("condition", condition.toString());

        if (poolEntry.getAsJsonArray("conditions") == null)
            poolEntry.add("conditions", new JsonArray());

        poolEntry.getAsJsonArray("conditions").add(conditionJson);
    }

}
