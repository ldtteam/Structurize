package com.ldtteam.structurize.management;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;
import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.TAG_UUID;

/**
 * The UUID storage class.
 */
public class UUIDStorage extends SavedData
{
    /**
     * The data description.
     */
    public static final String DATA_NAME = MOD_ID + "_UUID";

    /**
     * Required constructor.
     */
    public UUIDStorage()
    {
    }

    public UUIDStorage(CompoundTag s)
    {
        load(s);
    }

    public void load(final CompoundTag compound)
    {
        if (compound.hasUUID(TAG_UUID))
        {
            Manager.setServerUUID(compound.getUUID(TAG_UUID));
        }
    }

        @Override
    public CompoundTag save(final CompoundTag compound)
    {
        if (Manager.getServerUUID() != null)
        {
            compound.putUUID(TAG_UUID, Manager.getServerUUID());
        }
        return compound;
    }
}