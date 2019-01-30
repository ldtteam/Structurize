package com.ldtteam.structurize.management;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;
import org.jetbrains.annotations.NotNull;

import static com.structurize.api.util.constant.Constants.MOD_ID;
import static com.structurize.api.util.constant.NbtTagConstants.TAG_UUID;

/**
 * The UUID storage class.
 */
public class UUIDStorage extends WorldSavedData
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
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        if (compound.hasUniqueId(TAG_UUID))
        {
            Manager.setServerUUID(compound.getUniqueId(TAG_UUID));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(@NotNull final NBTTagCompound compound)
    {
        if (Manager.getServerUUID() != null)
        {
            compound.setUniqueId(TAG_UUID, Manager.getServerUUID());
        }
        return compound;
    }
}