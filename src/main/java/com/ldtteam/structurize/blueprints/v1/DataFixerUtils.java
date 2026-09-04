package com.ldtteam.structurize.blueprints.v1;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.serialization.Dynamic;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import com.mojang.datafixers.DataFixUtils;
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
     * Legacy flower-pot block-entity to block-state mappings retained for pre-1.13 blueprint migration.
     */
    public static final Map<String, Dynamic<?>> FLOWER_POT_MAP = DataFixUtils.make(Maps.newHashMap(), map -> {
        map.put("minecraft:air0", ExtraDataFixUtils.blockState("minecraft:flower_pot"));
        map.put("minecraft:red_flower0", ExtraDataFixUtils.blockState("minecraft:potted_poppy"));
        map.put("minecraft:red_flower1", ExtraDataFixUtils.blockState("minecraft:potted_blue_orchid"));
        map.put("minecraft:red_flower2", ExtraDataFixUtils.blockState("minecraft:potted_allium"));
        map.put("minecraft:red_flower3", ExtraDataFixUtils.blockState("minecraft:potted_azure_bluet"));
        map.put("minecraft:red_flower4", ExtraDataFixUtils.blockState("minecraft:potted_red_tulip"));
        map.put("minecraft:red_flower5", ExtraDataFixUtils.blockState("minecraft:potted_orange_tulip"));
        map.put("minecraft:red_flower6", ExtraDataFixUtils.blockState("minecraft:potted_white_tulip"));
        map.put("minecraft:red_flower7", ExtraDataFixUtils.blockState("minecraft:potted_pink_tulip"));
        map.put("minecraft:red_flower8", ExtraDataFixUtils.blockState("minecraft:potted_oxeye_daisy"));
        map.put("minecraft:yellow_flower0", ExtraDataFixUtils.blockState("minecraft:potted_dandelion"));
        map.put("minecraft:sapling0", ExtraDataFixUtils.blockState("minecraft:potted_oak_sapling"));
        map.put("minecraft:sapling1", ExtraDataFixUtils.blockState("minecraft:potted_spruce_sapling"));
        map.put("minecraft:sapling2", ExtraDataFixUtils.blockState("minecraft:potted_birch_sapling"));
        map.put("minecraft:sapling3", ExtraDataFixUtils.blockState("minecraft:potted_jungle_sapling"));
        map.put("minecraft:sapling4", ExtraDataFixUtils.blockState("minecraft:potted_acacia_sapling"));
        map.put("minecraft:sapling5", ExtraDataFixUtils.blockState("minecraft:potted_dark_oak_sapling"));
        map.put("minecraft:red_mushroom0", ExtraDataFixUtils.blockState("minecraft:potted_red_mushroom"));
        map.put("minecraft:brown_mushroom0", ExtraDataFixUtils.blockState("minecraft:potted_brown_mushroom"));
        map.put("minecraft:deadbush0", ExtraDataFixUtils.blockState("minecraft:potted_dead_bush"));
        map.put("minecraft:tallgrass2", ExtraDataFixUtils.blockState("minecraft:potted_fern"));
        map.put("minecraft:cactus0", ExtraDataFixUtils.blockState("minecraft:potted_cactus"));
    });

    /**
     * Legacy note-block block-entity to block-state mappings retained for pre-1.13 blueprint migration.
     */
    public static final Map<String, Dynamic<?>> NOTE_BLOCK_MAP = DataFixUtils.make(Maps.newHashMap(), map -> {
        for (int note = 0; note < 26; note++)
        {
            map.put("true" + note,
                ExtraDataFixUtils.blockState("minecraft:note_block", Map.of("powered", "true", "note", String.valueOf(note))));
            map.put("false" + note,
                ExtraDataFixUtils.blockState("minecraft:note_block", Map.of("powered", "false", "note", String.valueOf(note))));
        }
    });

    /**
     * Private constructor to hide implicit one.
     */
    private DataFixerUtils()
    {
        // Intentionally left empty.
    }

    public static CompoundTag runDataFixer(final CompoundTag dataIn, final TypeReference dataType, final DataVersion startVersion)
    {
        return runDataFixer(dataIn, dataType, startVersion.getDataVersion(), SharedConstants.getCurrentVersion().dataVersion().version());
    }

    public static CompoundTag runDataFixer(final CompoundTag dataIn, final TypeReference dataType, final int startVersion)
    {
        return runDataFixer(dataIn, dataType, startVersion, SharedConstants.getCurrentVersion().dataVersion().version());
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
