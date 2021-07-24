package com.ldtteam.structurize.management;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

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
        super(DATA_NAME);
    }

    /**
     * Required constructor.
     *
     * @param s name string.
     */
    public UUIDStorage(String s)
    {
        super(s);
    }

    @Override
    public void load(@NotNull final CompoundTag compound)
    {
        if (compound.hasUUID(TAG_UUID))
        {
            Manager.setServerUUID(compound.getUUID(TAG_UUID));
        }
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull final CompoundTag compound)
    {
        if (Manager.getServerUUID() != null)
        {
            compound.putUUID(TAG_UUID, Manager.getServerUUID());
        }
        return compound;
    }
}