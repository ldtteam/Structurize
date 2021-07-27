package com.ldtteam.structurize.blueprints.v1;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;

/**
 * Utils for data fixer mechanism
 */
public class DataFixerUtils
{
    /**
     * If the used datafixer is the vanilla one.
     */
    public static boolean isVanillaDF = DataFixers.getDataFixer() instanceof com.mojang.datafixers.DataFixerUpper;

    /**
     * Private constructor to hide implicit one.
     */
    private DataFixerUtils()
    {
        // Intentionally left empty.
    }

    public static CompoundTag runDataFixer(final CompoundTag dataIn, final TypeReference dataType, final DataVersion startVersion)
    {
        return runDataFixer(dataIn, dataType, startVersion.getDataVersion(), SharedConstants.getCurrentVersion().getWorldVersion());
    }

    public static CompoundTag runDataFixer(final CompoundTag dataIn, final TypeReference dataType, final int startVersion)
    {
        return runDataFixer(dataIn, dataType, startVersion, SharedConstants.getCurrentVersion().getWorldVersion());
    }

    public static CompoundTag runDataFixer(final CompoundTag dataIn, final TypeReference dataType, final DataVersion startVersion, final DataVersion endVersion)
    {
        return runDataFixer(dataIn, dataType, startVersion.getDataVersion(), endVersion.getDataVersion());
    }

    public static CompoundTag runDataFixer(final CompoundTag dataIn, final TypeReference dataType, final int startVersion, final int endVersion)
    {
        return runDataFixer(
            dataIn,
            dataType,
            startVersion,
            endVersion,
            startVersion <= DataVersion.pre1466.getDataVersion() && DataVersion.post1466.getDataVersion() <= endVersion && dataType == References.BLOCK_ENTITY);
    }

    public static CompoundTag runDataFixer(
        final CompoundTag dataIn,
        final TypeReference dataType,
        final int startVersion,
        final int endVersion,
        final boolean debugNonBlockstate)
    {
        return startVersion == endVersion
            ? dataIn
            : debugNonBlockstate && dataType != References.BLOCK_STATE
                ? runDataFixerCascade(dataIn, dataType, startVersion, endVersion)
                : (CompoundTag) DataFixers.getDataFixer()
                    .update(dataType, new Dynamic<>(NbtOps.INSTANCE, dataIn), startVersion, endVersion)
                    .getValue();
    }

    public static CompoundTag runDataFixerCascade(final CompoundTag dataIn, final TypeReference dataType, final int startVersion, final int endVersion)
    {
        CompoundTag fixedNbt = dataIn;
        DataVersion currentVersion = DataVersion.findFromDataVersion(startVersion);

        while (currentVersion.getDataVersion() < endVersion)
        {
            fixedNbt = (CompoundTag) DataFixers.getDataFixer()
                .update(
                    dataType,
                    new Dynamic<>(NbtOps.INSTANCE, fixedNbt),
                    currentVersion.getDataVersion(),
                    currentVersion.getSuccessor().getDataVersion())
                .getValue();
            currentVersion = currentVersion.getSuccessor();
            if (currentVersion == DataVersion.pre1466 && dataType == References.BLOCK_ENTITY)
            {
                currentVersion = DataVersion.post1466;
            }
        }

        return fixedNbt;
    }
}
