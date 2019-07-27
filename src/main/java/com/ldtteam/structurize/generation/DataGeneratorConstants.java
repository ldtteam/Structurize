package com.ldtteam.structurize.generation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ldtteam.structurize.api.util.constant.Constants;

public class DataGeneratorConstants
{

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final String DATAPACK_DIR = "data/" + Constants.MOD_ID + "/";
    private static final String RESOURCEPACK_DIR = "assets/" + Constants.MOD_ID + "/";

    // DataPack Directories \\

    public static final String LOOT_TABLES_DIR = DATAPACK_DIR + "loot_tables/blocks";

    public static final String TAGS_DIR = DATAPACK_DIR + "tags/";

    public static final String RECIPES_DIR = DATAPACK_DIR + "/recipes/";

    // ResourcePack Directories \\

    public static final String SHINGLES_BLOCK_MODELS_DIR = RESOURCEPACK_DIR + "models/block/shingle/";
    public static final String SHINGLE_SLABS_BLOCK_MODELS_DIR = RESOURCEPACK_DIR + "models/block/shingle_slab/";

    public static final String EN_US_LANG = "assets/structurize/lang/en_us.json";

    public static final String ITEM_MODEL_DIR = "assets/structurize/models/item/";

    public static final String BLOCKSTATE_DIR = RESOURCEPACK_DIR + "blockstates/";
}
