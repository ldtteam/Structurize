package com.ldtteam.structurize.management;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.UUID;

/**
 * Server-scoped identifier used to keep operation history stable across restarts.
 */
public final class UUIDStorage extends SavedData
{
    public static final Codec<UUIDStorage> CODEC = UUIDUtil.CODEC.xmap(UUIDStorage::new, UUIDStorage::getUUID);
    public static final SavedDataType<UUIDStorage> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("structurize", "server_uuid"),
        level -> new UUIDStorage(),
        level -> CODEC
    );

    private final UUID uuid;

    public UUIDStorage()
    {
        this(UUID.randomUUID());
    }

    public UUIDStorage(final UUID uuid)
    {
        this.uuid = uuid;
    }

    public UUID getUUID()
    {
        return uuid;
    }
}
