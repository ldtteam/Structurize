package com.ldtteam.structurize.blueprints.v1;

import net.minecraft.SharedConstants;

/**
 * Enum for data
 */
public enum DataVersion
{
    /*
     * if adding new values make sure:
     * - successors match
     * - upcoming has data version = (latest data version + 1)
     */
    UPCOMING(2731, null, null),
    v1_17_1(2730, "1.17.1", UPCOMING),
    v1_17(2724, "1.17", v1_17_1),
    v1_16_5(2586, "1.16.5", v1_17),
    v1_16_4(2584, "1.16.4", v1_16_5),
    v1_16_3(2580, "1.16.3", v1_16_4),
    v1_16_2(2578, "1.16.2", v1_16_3),
    v1_16_1(2567, "1.16.1", v1_16_2),
    v1_16(2566, "1.16", v1_16_1),
    v1_15_2(2230, "1.15.2", v1_16),
    v1_15_1(2227, "1.15.1", v1_15_2),
    v1_15(2225, "1.15", v1_15_1),
    v1_14_4(1976, "1.14.4", v1_15),
    v1_14_3(1968, "1.14.3", v1_14_4),
    v1_14_2(1963, "1.14.2", v1_14_3),
    v1_14_1(1957, "1.14.1", v1_14_2),
    v1_14(1952, "1.14", v1_14_1),
    v1_13_2(1631, "1.13.2", v1_14),
    v1_13_1(1628, "1.13.1", v1_13_2),
    v1_13(1519, "1.13", v1_13_1),
    // 1466 does not like block entities in it's standalone datafixer type
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

    public static final DataVersion CURRENT = findFromDataVersion(SharedConstants.getCurrentVersion().getWorldVersion());
    private final int dataVersion;
    private final String mcVersion;
    private final DataVersion successor;

    private DataVersion(final int dataVersion, final String mcVersion, final DataVersion successor)
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
    public DataVersion getSuccessor()
    {
        return successor;
    }

    /**
     * Find data version based on string in format "x.y.z" which should represent Minecraft release version.
     * Default is data version "0" which is pre 1.9
     *
     * @param mcVersion string in format "x.y.z" representing Minecraft release version
     * @return data version enum
     */
    public static DataVersion findFromMcVersion(final String mcVersion)
    {
        if (mcVersion == null)
        {
            return DataVersion.DEFAULT;
        }

        for (final DataVersion ver : DataVersion.values())
        {
            if (ver.mcVersion != null && ver.mcVersion.equals(mcVersion))
            {
                return ver;
            }
        }
        return DataVersion.DEFAULT;
    }

    /**
     * Find data version based on its interger value.
     *
     * @param dataVersion integer format of data version
     * @return dataVersion which is the nearest lower than supplied value
     */
    public static DataVersion findFromDataVersion(final int dataVersion)
    {
        DataVersion version = DEFAULT;
        while (version.successor != null && version.successor.dataVersion <= dataVersion)
        {
            version = version.successor;
        }
        return version;
    }
}
