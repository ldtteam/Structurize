package com.ldtteam.structurize.placement.handlers.entity;

import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Handler for entity placements.
 */
public interface IEntityHandler
{
    /**
     * Checks if this handler can deal with this entity.
     *
     * @param entity the entity to check.
     * @return       true if so; false if you need to keep looking.
     */
    boolean canHandle(final Entity entity);

    /**
     * Checks if it's permitted to place this entity.
     * By default, if a handler is defined it's assumed to be permitted, otherwise denied.
     *
     * @param entity     the entity being placed.
     * @param isCreative if placing in creative mode.
     * @param isFancy    if placing in fancy (non-placeholder) mode.
     * @return           true if permitted, false otherwise.
     */
    default boolean canPlace(final Entity entity, final boolean isCreative, final boolean isFancy)
    {
        return true;
    }

    /**
     * Gets the adjusted position in the world for the given entity.
     *
     * @param entity the entity.
     * @param pos    the anchor position in world.
     * @return       the adjusted entity position.
     */
    default Vec3 adjustPosition(final Entity entity, final BlockPos pos)
    {
        return entity.position().add(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Check if the entity should be placed.
     *
     * @param handler the actor placing the entity.
     * @param entity  the entity being placed.
     * @return SUCCESS, DENY, or PASS.
     */
    default ActionProcessingResult checkPlacement(final IStructureHandler handler, final Entity entity, final BlockPos pos)
    {
        if (!canHandle(entity)) return ActionProcessingResult.PASS;
        if (!canPlace(entity, handler.isCreative(), handler.fancyPlacement())) return ActionProcessingResult.DENY;

        Vec3 posInWorld = adjustPosition(entity, pos);
        entity.moveTo(posInWorld.x, posInWorld.y, posInWorld.z);

        final List<? extends Entity> list = entity.level().getEntitiesOfClass(entity.getClass(), AABB.unitCubeFromLowerCorner(posInWorld));
        for (Entity worldEntity : list)
        {
            if (worldEntity.position().equals(posInWorld))
            {
                return ActionProcessingResult.DENY;     // already in world, ignore it
            }
        }

        return ActionProcessingResult.SUCCESS;
    }

    /**
     * Method used to get the required items to place an entity.
     *
     * @param entity  the entity being placed.
     * @return        the list of items.
     */
    default List<ItemStack> getRequiredItems(final Entity entity)
    {
        return List.of(entity.getPickedResult(new HitResult(entity.position()) {
            @Override
            public Type getType()
            {
                return Type.ENTITY;
            }
        }));
    }

    /**
     * Gets an icon for this entity for the Scan Tool.
     *
     * @param entity the entity to query.
     * @return       the scan tool icon.
     */
    default ItemStack getIcon(final Entity entity)
    {
        return entity.getPickResult();
    }

    /**
     * Possible result of an IEntityHandler call.
     */
    enum ActionProcessingResult
    {
        PASS,
        DENY,
        SUCCESS
    }
}
