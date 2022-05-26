package com.ldtteam.structurize.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper that contains all the info of the structure pack.
 */
public class StructurePack
{
    /**
     * The name of the pack.
     */
    private final String packName;

    /**
     * The location of the icon.
     */
    private final String iconPath;

    /**
     * List of authors that participated in the creation.
     */
    private final List<String> authors;

    /**
     * Description of the pack.
     */
    private final String desc;

    /**
     * List of mods that were used in the pack.
     */
    private final List<String> mods;

    /**
     * This is the path of the structure pack, we need this to resolve it.
     */
    private final Path rootPath;

    /**
     * The pack format.
     * This describes for which range of versions of the mod this pack was created.
     */
    private final int packFormat;

    /**
     * The pack version.
     */
    private final int version;

    /**
     * This is a flag that tells us if we can update the pack from the remote server. Jar structure packs cannot be updated (for obvious reasons).
     */
    private boolean immutable = false;

    /**
     * Initialize the pack from json.
     *
     * @param json the json to use.
     */
    public StructurePack(final JsonObject json, final Path rootPath)
    {
        this.packName = json.get("name").getAsString();
        this.iconPath = json.get("icon").getAsString();

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
        this.version = json.get("version").getAsInt();
        this.packFormat  = json.get("pack-format").getAsInt();
    }

    public String getName()
    {
        return this.packName;
    }

    /**
     * Set the immutability param.
     *
     * @param immutable true if immutable, else false.
     */
    public void setImmutable(final boolean immutable)
    {
        this.immutable = immutable;
    }

    /**
     * Check if the structure pack is immutable.
     *
     * @return true if so.
     */
    public boolean isImmutable()
    {
        return this.immutable;
    }

    /**
     * Get the pack format.
     *
     * @return the format version.
     */
    public int getPackFormat()
    {
        return this.packFormat;
    }

    /**
     * Get the pack version.
     *
     * @return the pack version.
     */
    public int getVersion()
    {
        return this.version;
    }

    /**
     * Get the list of mods that were used in this schematic.
     * @return the list of mod ids.
     */
    public List<String> getModList()
    {
        return mods;
    }
}
