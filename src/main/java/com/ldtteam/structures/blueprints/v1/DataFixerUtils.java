package com.ldtteam.structures.blueprints.v1;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.DSL.TypeReference;
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
    public static final List<Integer> VERSIONS_LIST =
        Arrays.asList(Versions.values()).stream().map(ver -> ver.getDataVersion()).sorted().collect(Collectors.toList());

    /**
     * MC to data versions
     */
    public enum Versions
    {
        v1_14_4(1976, "1.14.4", null),
        v1_14_3(1968, "1.14.3", v1_14_4),
        v1_14_2(1963, "1.14.2", v1_14_3),
        v1_14_1(1957, "1.14.1", v1_14_2),
        v1_14(1952, "1.14", v1_14_1),
        v1_13_2(1631, "1.13.2", v1_14),
        v1_13_1(1628, "1.13.1", v1_13_2),
        v1_13(1519, "1.13", v1_13_1),
        // 1466 does not like block entities
        post1466(1467, "post1466", v1_13),
        pre1466(1465, "pre1466", post1466),
        v1_12_2(1343, "1.12.2", pre1466),
        v1_12_1(1241, "1.12.1", v1_12_2),
        v1_12(1139, "1.12", v1_12_1),
        v1_11_2(922, "1.11.2", v1_12),
        v1_11_1(921, "1.11.1", v1_11_2),
        v1_11(819, "1.11", v1_11_1),
        v1_10_2(512, "1.10.2", v1_11),
        v1_10_1(511, "1.10.1", v1_10_2),
        v1_10(510, "1.10", v1_10_1),
        v1_9_4(184, "1.9.4", v1_10),
        v1_9_3(183, "1.9.3", v1_9_4),
        v1_9_2(176, "1.9.2", v1_9_3),
        v1_9_1(175, "1.9.1", v1_9_2),
        v1_9(169, "1.9", v1_9_1),
        DEFAULT(0, null, v1_9);

        private final int dataVersion;
        private final String mcVersion;
        private final Versions successor;

        private Versions(final int dataVersion, final String mcVersion, final Versions successor)
        {
            this.dataVersion = dataVersion;
            this.mcVersion = mcVersion;
            this.successor = successor;
        }

        /**
         * @return data version of specific mc version
         */
        public int getDataVersion()
        {
            return dataVersion;
        }

        /**
         * @return mc version as readable string
         */
        public String getMcVersion()
        {
            return mcVersion;
        }

        /**
         * @return mc version as readable string
         */
        public Versions getSuccessor()
        {
            return successor;
        }

        public static Versions findFromMcVersion(final String mcVersion)
        {
            for (final Versions ver : Versions.values())
            {
                if (ver.getMcVersion().equals(mcVersion))
                {
                    return ver;
                }
            }
            return Versions.DEFAULT;
        }

        public static Versions findFromDataVersion(final int dataVersion)
        {
            for (final Versions ver : Versions.values())
            {
                if (ver.getDataVersion() == dataVersion)
                {
                    return ver;
                }
            }
            return Versions.DEFAULT;
        }
    }

    /**
     * Private constructor to hide implicit one.
     */
    private DataFixerUtils()
    {
        // Intentionally left empty.
    }

    public static CompoundNBT runDataFixer(final CompoundNBT dataIn, final TypeReference dataType, final Versions startVersion)
    {
        return runDataFixer(dataIn, dataType, startVersion.getDataVersion(), SharedConstants.getVersion().getWorldVersion());
    }

    public static CompoundNBT runDataFixer(final CompoundNBT dataIn, final TypeReference dataType, final int startVersion)
    {
        return runDataFixer(dataIn, dataType, startVersion, SharedConstants.getVersion().getWorldVersion());
    }

    public static CompoundNBT runDataFixer(final CompoundNBT dataIn, final TypeReference dataType, final Versions startVersion, final Versions endVersion)
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
            startVersion <= Versions.pre1466.getDataVersion() && Versions.post1466.getDataVersion() <= endVersion && dataType == TypeReferences.BLOCK_ENTITY);
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
        Versions currentVersion = Versions.findFromDataVersion(startVersion);

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
            if (currentVersion == Versions.pre1466 && dataType == TypeReferences.BLOCK_ENTITY)
            {
                currentVersion = Versions.post1466;
            }
        }

        return fixedNbt;
    }
}
