package com.ldtteam.structures.blueprints.v1;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.TypeReferences;

/**
 * Utils for data fixer mechanism
 */
public class DataFixerUtils
{
    /**
     * If the used datafixer is the vanilla one.
     */
    public static boolean isVanillaDF = DataFixesManager.getDataFixer() instanceof com.mojang.datafixers.DataFixerUpper;

    /**
     * Private constructor to hide implicit one.
     */
    private DataFixerUtils()
    {
        // Intentionally left empty.
    }

    public static CompoundNBT runDataFixer(final CompoundNBT dataIn, final TypeReference dataType, final DataVersion startVersion)
    {
        return runDataFixer(dataIn, dataType, startVersion.getDataVersion(), SharedConstants.getCurrentVersion().getWorldVersion());
    }

    public static CompoundNBT runDataFixer(final CompoundNBT dataIn, final TypeReference dataType, final int startVersion)
    {
        return runDataFixer(dataIn, dataType, startVersion, SharedConstants.getCurrentVersion().getWorldVersion());
    }

    public static CompoundNBT runDataFixer(final CompoundNBT dataIn, final TypeReference dataType, final DataVersion startVersion, final DataVersion endVersion)
    {
        return runDataFixer(dataIn, dataType, startVersion.getDataVersion(), endVersion.getDataVersion());
    }

    public static CompoundNBT runDataFixer(final CompoundNBT dataIn, final TypeReference dataType, final int startVersion, final int endVersion)
    {
        return runDataFixer(
            dataIn,
            dataType,
            startVersion,
            endVersion,
            startVersion <= DataVersion.pre1466.getDataVersion() && DataVersion.post1466.getDataVersion() <= endVersion && dataType == TypeReferences.BLOCK_ENTITY);
    }

    public static CompoundNBT runDataFixer(
        final CompoundNBT dataIn,
        final TypeReference dataType,
        final int startVersion,
        final int endVersion,
        final boolean debugNonBlockstate)
    {
        return startVersion == endVersion
            ? dataIn
            : debugNonBlockstate && dataType != TypeReferences.BLOCK_STATE
                ? runDataFixerCascade(dataIn, dataType, startVersion, endVersion)
                : (CompoundNBT) DataFixesManager.getDataFixer()
                    .update(dataType, new Dynamic<>(NBTDynamicOps.INSTANCE, dataIn), startVersion, endVersion)
                    .getValue();
    }

    public static CompoundNBT runDataFixerCascade(final CompoundNBT dataIn, final TypeReference dataType, final int startVersion, final int endVersion)
    {
        CompoundNBT fixedNbt = dataIn;
        DataVersion currentVersion = DataVersion.findFromDataVersion(startVersion);

        while (currentVersion.getDataVersion() < endVersion)
        {
            fixedNbt = (CompoundNBT) DataFixesManager.getDataFixer()
                .update(
                    dataType,
                    new Dynamic<>(NBTDynamicOps.INSTANCE, fixedNbt),
                    currentVersion.getDataVersion(),
                    currentVersion.getSuccessor().getDataVersion())
                .getValue();
            currentVersion = currentVersion.getSuccessor();
            if (currentVersion == DataVersion.pre1466 && dataType == TypeReferences.BLOCK_ENTITY)
            {
                currentVersion = DataVersion.post1466;
            }
        }

        return fixedNbt;
    }
}
