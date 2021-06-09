package com.ldtteam.structurize.placement.structure;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Abstract implementation of the handler holding information that is common for all handlers.
 */
public abstract class AbstractStructureHandler implements IStructureHandler
{
    /**
     * blueprint of the structure.
     */
    private Blueprint blueprint;

    /**
     * The MD5 value of the blueprint.
     */
    private String md5;

    /**
     * The used settings for the placement.
     */
    private PlacementSettings settings;

    /**
     * The minecraft world this struture is displayed in.
     */
    private World world;

    /**
     * The anchor position this structure will be
     * placed on in the minecraft world.
     */
    private BlockPos worldPos;

    /**
     * Abstract constructor of structure handler.
     * @param world the world it gets.
     * @param worldPos the position the anchor of the structure got placed.
     * @param structureName the name of the structure.
     * @param settings the placement settings.
     */
    public AbstractStructureHandler(final World world, final BlockPos worldPos, final String structureName, final PlacementSettings settings)
    {
        this.world = world;
        this.worldPos = worldPos;
        this.settings = settings;
        this.loadBlueprint(structureName);
    }

    /**
     * Load the handler with the blueprint already.
     * @param world the world.
     * @param pos the position.
     * @param blueprint the blueprint.
     * @param settings the placement settings.
     */
    public AbstractStructureHandler(final World world, final BlockPos pos, final Blueprint blueprint, final PlacementSettings settings)
    {
        this.world = world;
        this.worldPos = pos;
        this.settings = settings;
        this.blueprint = blueprint;
    }

    @Override
    public boolean hasBluePrint()
    {
        return blueprint != null;
    }

    @Override
    public void setMd5(final String md5)
    {
        this.md5 = md5;
    }

    @Override
    public void setBlueprint(final Blueprint blueprint)
    {
        this.blueprint = blueprint;
    }

    @Override
    public Blueprint getBluePrint()
    {
        return this.blueprint;
    }

    @Override
    public World getWorld()
    {
        return this.world;
    }

    @Override
    public String getMd5()
    {
        return md5;
    }

    @Override
    public BlockPos getWorldPos()
    {
        return this.worldPos;
    }

    @Override
    public PlacementSettings getSettings()
    {
        return this.settings;
    }
}
