package com.ldtteam.structurize.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StructurePack
{
    private final String       packName;
    private final String       iconName;
    private final List<String> authors;
    private final String desc;
    private final List<String> mods;
    // If version doesn't fit, we skip.
    private final Path rootPath;

    public StructurePack(final String packName, final String iconName, final List<String> authors, final String desc, final List<String> mods, final Path rootPath)
    {
        this.packName = packName;
        this.iconName = iconName;
        this.authors = authors;
        this.desc = desc;
        this.mods = mods;
        this.rootPath = rootPath;
    }

    /**
     * Initialize the pack from json.
     * @param json the json to use.
     */
    public StructurePack(final JsonObject json, final Path rootPath)
    {
        this.packName = json.get("name").getAsString();
        this.iconName = json.get("icon").getAsString();

        this.authors = new ArrayList<>();
        final JsonArray authorArray = json.get("authors").getAsJsonArray();
        for (int i = 0; i < authorArray.size(); i++)
        {
            authors.add(authorArray.get(i).getAsString());
        }

        this.desc = json.get("desc").getAsString();

        this.mods = new ArrayList<>();
        final JsonArray modsArray = json.get("authors").getAsJsonArray();
        for (int i = 0; i < modsArray.size(); i++)
        {
            mods.add(modsArray.get(i).getAsString());
        }
        this.rootPath = rootPath;
    }

    public String getName()
    {
        return this.packName;
    }
}
