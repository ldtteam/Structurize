package com.ldtteam.structurize.placement.structure;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.api.util.RotationMirror;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Abstract implementation of the handler holding information that is common for all handlers.
 */
public abstract class AbstractStructureHandler implements IStructureHandler
{
    /**
     * The blueprint future.
     */
    private Future<Blueprint> blueprintFuture = null;
    
    /**
     * blueprint of the structure.
     */
    private Blueprint               blueprint;

    /**
     * The MD5 value of the blueprint.
     */
    private String md5;

    /**
     * The used settings for the placement.
     */
    private RotationMirror rotMir;

    /**
     * The minecraft world this struture is displayed in.
     */
    private Level world;

    /**
     * The anchor position this structure will be
     * placed on in the minecraft world.
     */
    private BlockPos worldPos;

    /**
     * Abstract constructor of structure handler.
     * @param world the world it gets.
     * @param worldPos the position the anchor of the structure got placed.
     * @param blueprintFuture the name of the structure.
     * @param rotMir the placement settings.
     */
    public AbstractStructureHandler(final Level world, final BlockPos worldPos, final Future<Blueprint> blueprintFuture, final RotationMirror rotMir)
    {
        this.world = world;
        this.worldPos = worldPos;
        this.rotMir = rotMir;
        this.blueprintFuture = blueprintFuture;
    }

    /**
     * Load the handler with the blueprint already.
     * @param world the world.
     * @param pos the position.
     * @param blueprint the blueprint.
     * @param rotMir the placement settings.
     */
    public AbstractStructureHandler(final Level world, final BlockPos pos, final Blueprint blueprint, final RotationMirror rotMir)
    {
        this.world = world;
        this.worldPos = pos;
        this.rotMir = rotMir;
        this.blueprint = blueprint;
    }

    @Override
    public void triggerSuccess(final BlockPos pos, final List<ItemStack> requiredRes, final boolean placement)
    {
        final BlockEntity be = getWorld().getBlockEntity(getProgressPosInWorld(pos));
        if (be instanceof IBlueprintDataProviderBE)
        {
            if (getProgressPosInWorld(pos).equals(worldPos))
            {
                ((IBlueprintDataProviderBE) be).setBlueprintPath(StructurePacks.getStructurePack(getBluePrint().getPackName()).getSubPath(getBluePrint().getFilePath().resolve(getBluePrint().getFileName())));
            }
            ((IBlueprintDataProviderBE) be).setPackName(getBluePrint().getPackName());
        }
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
        if (blueprint == null && blueprintFuture != null && blueprintFuture.isDone())
        {
            try
            {
                blueprint = blueprintFuture.get();
                blueprint.setRotationMirror(rotMir, world);
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }
        return this.blueprint;
    }

    @Override
    public Level getWorld()
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
    public RotationMirror getRotationMirror()
    {
        return this.rotMir;
    }
    
    @Override
    public boolean isReady()
    {
        return blueprint != null || (blueprintFuture != null && blueprintFuture.isDone());
    }
}
