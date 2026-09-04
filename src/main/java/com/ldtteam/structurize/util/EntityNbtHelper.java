package com.ldtteam.structurize.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Adapters between legacy entity NBT workflows and the current value-tree API.
 */
public final class EntityNbtHelper
{
    private EntityNbtHelper()
    {
    }

    @Nullable
    public static CompoundTag save(final Entity entity, final HolderLookup.Provider registries)
    {
        final TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        return entity.save(output) ? output.buildResult() : null;
    }

    public static void load(final Entity entity, final CompoundTag data, final HolderLookup.Provider registries)
    {
        entity.load(TagValueInput.create(ProblemReporter.DISCARDING, registries, data));
    }
}
