package com.copypaste.coremod.management;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;
import org.jetbrains.annotations.NotNull;

import static com.copypaste.api.util.constant.NbtTagConstants.TAG_UUID;
import static mod.chiselsandbits.core.ChiselsAndBits.MODID;

/**
 * The UUID storage class.
 */
public class UUIDStorage extends WorldSavedData
{
    /**
     * The data description.
     */
    public static final String DATA_NAME = MODID + "_UUID";

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